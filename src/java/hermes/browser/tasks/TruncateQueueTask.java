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
import hermes.config.DestinationConfig;
import hermes.swing.SwingRunner;

import java.util.Collection;
import java.util.Iterator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.apache.log4j.Category;

/**
 * Truncate a queue.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: TruncateQueueAction.java,v 1.2 2004/07/30 17:25:14 colincrist
 *          Exp $
 */

public class TruncateQueueTask extends TaskSupport
{
   private static final Category cat = Category.getInstance(TruncateQueueTask.class);
   public static final int USE_SELECTOR = 1;
   public static final int USE_MESSAGE_ACKNOWLEDGE = 2;

   private DestinationConfig dConfig;
   private Hermes hermes;
   private Collection messageIds;
   private int mode = USE_SELECTOR;
   private boolean showWarning = true;

   public TruncateQueueTask(Hermes hermes, DestinationConfig dConfig, boolean showWarning)
   {
      super(IconCache.getIcon("hermes.messages.delete"));

      this.dConfig = dConfig;
      this.hermes = hermes;
      this.showWarning = showWarning;
   }

   public TruncateQueueTask(Hermes hermes, DestinationConfig dConfig, Collection messageIds, int mode, boolean showWarning)
   {
      super(IconCache.getIcon("hermes.messages.delete"));

      this.dConfig = dConfig;
      this.hermes = hermes;
      this.messageIds = messageIds;
      this.mode = mode;
      this.showWarning = showWarning;
   }

   public String getTitle()
   {
      return (messageIds == null ? "Truncate" : "Delete") + " from " + dConfig.getName() ;
   }

   public void invoke() throws Exception
   {
      if (messageIds == null)
      {
         doTruncate();
      }
      else
      {
         doDelete();
      }

      hermes.close();
   }

   private void doTruncate() throws Exception
   {
      synchronized (this)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               String message = null;

               if (dConfig.getDomain() == Domain.QUEUE.getId())
               {
                  message = "Are you sure you wish to truncate " + dConfig.getName() + " ?\nAll messages will be deleted from this queue.";
               }
               else
               {
                  if (dConfig.isDurable())
                  {
                     message = "Are you sure you wish to truncate all messages pending for the durable subscription " + dConfig.getClientID() + " on "
                           + dConfig.getName() + "?";
                  }
               }

               if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), message, "Warning", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
               {
                  stop();

                  notifyStatus("Truncate cancelled");
               }

               synchronized (TruncateQueueTask.this)
               {
                  TruncateQueueTask.this.notify();
               }
            }
         });

         try
         {
            this.wait();
         }
         catch (InterruptedException ex)
         {
            // Nah...
         }
      }

      if (isRunning())
      {
         int size = hermes.truncate(dConfig);

         if (size == 1)
         {
            Hermes.ui.getDefaultMessageSink().add("Message deleted from " + dConfig.getName() + (dConfig.isDurable() ?  " durableName=" + dConfig.getClientID() : ""));
         }
         else
         {
            Hermes.ui.getDefaultMessageSink().add("Deleted " + size + " messages from " + dConfig.getName() + (dConfig.isDurable() ?  " durableName=" + dConfig.getClientID() : ""));
         }
      }
   }

   private void doDelete() throws Exception
   {
      synchronized (this)
      {
         if (showWarning)
         {
            SwingRunner.invokeLater(new Runnable()
            {
               public void run()
               {
                  if (messageIds.size() > 0)
                  {
                     if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), "Are you sure you wish to delete " + messageIds.size()
                           + ((messageIds.size() > 1) ? " messages" : " message") + " from " + dConfig.getName() + "?", "Warning", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                     {
                        stop();

                        notifyStatus("Delete from " + dConfig.getName() + " cancelled");
                     }

                     synchronized (TruncateQueueTask.this)
                     {
                        TruncateQueueTask.this.notify();
                     }
                  }
                  else
                  {
                     JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), "No messages selected to delete.", "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                  }
               }
            });

            try
            {
               this.wait();
            }
            catch (InterruptedException ex)
            {
               // Nah...
            }
         }
      }

      if (isRunning())
      {
         final StringBuffer message = new StringBuffer();
         ProgressMonitor monitor = new ProgressMonitor(HermesBrowser.getBrowser(), "Deleting " + messageIds.size()
               + ((messageIds.size() == 1) ? " message" : " messages") + " from " + dConfig.getName(), "Connecting...", 0, messageIds.size());

         monitor.setMillisToDecideToPopup(50);
         monitor.setMillisToPopup(100);

         try
         {

            StringBuffer sqlBuffer = new StringBuffer();

            if (mode == USE_SELECTOR)
            {
               for (Iterator iter = messageIds.iterator(); iter.hasNext();)
               {
                  sqlBuffer.append("JMSMessageID = \'").append(iter.next()).append("\'");

                  if (iter.hasNext())
                  {
                     sqlBuffer.append(" or ");
                  }
               }
            }

            final Destination d = hermes.getDestination(dConfig.getName(), Domain.getDomain(dConfig.getDomain()));
            final String sql = sqlBuffer.toString();
            int deleted = 0;

            while (deleted < messageIds.size() && isRunning())
            {
               boolean wasDeleted = false;
               Message m = hermes.receive(d, 10000, sql);

               if (m != null)
               {
                  if (mode == USE_MESSAGE_ACKNOWLEDGE)
                  {
                     if (messageIds.contains(m.getJMSMessageID()))
                     {
                        m.acknowledge();
                        wasDeleted = true;
                        monitor.setNote(new Long(++deleted) + " messages deleted.");
                        monitor.setProgress(deleted);
                     }
                  }
                  else
                  {
                     wasDeleted = true;
                  }

                  if (wasDeleted)
                  {
                     monitor.setNote(new Long(++deleted) + " messages deleted.");
                     monitor.setProgress(deleted);
                  }

               }
               else
               {
                  stop();

                  message.append("Timeout reading from message consumer.");

                  HermesBrowser
                        .getBrowser()
                        .showErrorDialog(
                              message.toString()
                                    + "\nThis could be because the messages have already been deleted, the provider is slow or does not properly implement SQL92 selectors.");
               }
            }

            if (mode == USE_SELECTOR)
            {
               if (isRunning())
               {
                  hermes.commit();
                  message.append("Messages deleted and committed");
               }
               else
               {
                  hermes.rollback();
                  message.append("Messages rolledback");
               }
            }
            else
            {
               message.append("Messages deleted and committed");
            }

            hermes.close();

            Hermes.ui.getDefaultMessageSink().add(message.toString());

         }
         catch (Exception ex)
         {
            message.append("During delete from ").append(dConfig.getName()).append(": ").append(ex.getMessage());
            cat.error(ex.getMessage(), ex);

            try
            {
               hermes.rollback();
            }
            catch (JMSException ex2)
            {
               message.append("\nRollback also failed, probably transport failure.");
               cat.error(ex2);
            }

            SwingRunner.invokeLater(new Runnable()
            {
               public void run()
               {
                  JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), message.toString(), "Copy Failed.", JOptionPane.OK_OPTION);
               }
            });
         }
         finally
         {
            monitor.close();
         }
      }
   }
}