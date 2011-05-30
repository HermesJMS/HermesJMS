/* 
 * Copyright 2003,2004 Colin Crist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package hermes.browser.tasks;

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.BrowserAction;
import hermes.swing.SwingRunner;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: CopyOrMoveMessagesTask.java,v 1.2 2005/06/20 15:28:35
 *          colincrist Exp $
 */

public class CopyOrMoveMessagesTask extends TaskSupport
{
   private static final Logger cat = Logger.getLogger(CopyOrMoveMessagesTask.class);

   private Collection messages;
   private Hermes hermes;
   private String destination;
   private boolean keepRunning = true;
   private int optionRval;
   private ProgressMonitor monitor;
   private int action;
   private Domain target;

   public CopyOrMoveMessagesTask(Hermes hermes, String destination, Domain target, Collection messages, int action)
   {
      super(action == TransferHandler.COPY ? IconCache.getIcon("copy") : IconCache.getIcon("cut"));

      this.hermes = hermes;
      this.messages = messages;
      this.destination = destination;
      this.action = action;
      this.target = target;
   }

   public String getTitle()
   {
      if (action == TransferHandler.COPY)
      {
         return "Copy to " + destination;
      }
      else
      {
         return "Move to " + destination;
      }
   }

   private Message createMessage(Destination to, Object o, Collection ids) throws JMSException
   {
      if (o instanceof Message)
      {
      Message oldMessage = (Message) o ;
      Message newMessage = hermes.duplicate(to, oldMessage);

      ids.add(oldMessage.getJMSMessageID());
      return newMessage ;
      }
      else if (o instanceof byte[])
      {
         BytesMessage newMessage = hermes.createBytesMessage() ;
         newMessage.writeBytes((byte[]) o) ;
         return newMessage ;
      }
      else
      {
         return hermes.createTextMessage(o.toString()) ;
      }
   }
   public void invoke() throws Exception
   {
      action = HermesBrowser.getBrowser().getBrowserTree().getLastDndAction(); 

      
      synchronized (this)
      {
         //
         // See if the user still wants to continue...

         SwingRunner.invokeLater(new Runnable()
         {
            public void run()
            {
               final String copyOrMove = action == TransferHandler.COPY ? "copy" : "move";

               if (messages.size() > 1)
               {
                  optionRval = JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), "Do you wish to " + copyOrMove + " these " + messages.size()
                        + " messages to " + destination + "?", "Confirm.", JOptionPane.YES_NO_OPTION);
               }
               else
               {
                  optionRval = JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), "Do you wish to " + copyOrMove + " this message to " + destination
                        + "?", "Confirm.", JOptionPane.YES_NO_OPTION);
               }

               synchronized (CopyOrMoveMessagesTask.this)
               {
                  CopyOrMoveMessagesTask.this.notifyAll();
               }
            }
         });

         try
         {
            this.wait();
         }
         catch (InterruptedException ex)
         {
            // NOP
         }
      }

      if (optionRval == JOptionPane.YES_OPTION)
      {
         final StringBuffer finalStatus = new StringBuffer();
         final Collection ids = new ArrayList();
         
         try
         {
            //
            // Add a progress monitor as this may take some time with slow
            // transports

            final int startSize = messages.size();
            final Destination to = hermes.getDestination(destination, target);

            SwingRunner.invokeAndWait(new Runnable()
            {
               public void run()
               {
                  monitor = new ProgressMonitor(HermesBrowser.getBrowser(), "Copying " + messages.size() + ((messages.size() == 1) ? " message" : " messages")
                        + " to " + destination, "Connecting...", 0, messages.size());

                  monitor.setMillisToDecideToPopup(100);
                  monitor.setMillisToPopup(400);
                  monitor.setProgress(1);
               }
            });

            for (Iterator iter = messages.iterator(); iter.hasNext() && !monitor.isCanceled();)
            {
               Message newMessage = createMessage(to, iter.next(), ids) ;

               hermes.send(to, newMessage);  

               iter.remove();

               final int progress = startSize - messages.size();

               SwingRunner.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     monitor.setProgress(progress);
                     monitor.setNote(new Long(messages.size()) + " messages left to copy");
                  }
               });
            }

            //
            // Check to see if user cancelled

            if (monitor.isCanceled())
            {
               hermes.rollback();
               Toolkit.getDefaultToolkit().beep();

               finalStatus.append("Copy to ").append(destination).append(" cancelled");
            }
            else
            {
               hermes.commit();

               if (action == TransferHandler.COPY)
               {
                  if (startSize == 1)
                  {
                     finalStatus.append("Committed. Message copied to ").append(destination);
                  }
                  else
                  {
                     finalStatus.append("Committed. ").append(startSize).append(" messages copied to ").append(destination);
                  }
               }
               else if (action == TransferHandler.MOVE)
               {
                  BrowserAction activeAction = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();

                  HermesBrowser.getBrowser().getActionFactory().createTruncateAction(activeAction.getHermes(), activeAction.getConfig(), ids,
                        false, activeAction);
               }
            }

            notifyStatus(finalStatus.toString());
         }
         catch (Exception ex)
         {
            //
            // If anything went wrong, rollback and provide a popup.

            cat.error(ex.getMessage(), ex);

            notifyThrowable(ex);

            if (monitor != null)
            {
               monitor.close();
            }

            try
            {
               hermes.rollback();
            }
            catch (JMSException ex2)
            {

               cat.error("Rollback after failed copy: " + ex2.getMessage(), ex2);
            }
         }
         finally
         {
            hermes.close();
         }
      }
   }
}