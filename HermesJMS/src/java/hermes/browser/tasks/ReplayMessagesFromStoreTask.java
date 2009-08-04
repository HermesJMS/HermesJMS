/* 
 * Copyright 2003,2004,2005 Colin Crist
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
import hermes.store.MessageStore;
import hermes.util.JMSUtils;
import hermes.util.TextUtils;

import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ReplayMessagesFromStoreTask.java,v 1.2 2005/07/22 17:02:23
 *          colincrist Exp $
 */

public class ReplayMessagesFromStoreTask extends TaskSupport
{
   private MessageStore messageStore;
   private Destination sourceDestination;
   private Hermes hermes;
   private DestinationConfig targetDestinationConfig;
   private int messagesSent = 0 ;

   public ReplayMessagesFromStoreTask(MessageStore messageStore, Destination sourceDestination, Hermes hermes, DestinationConfig targetDestinationConfig)
   {
      super(IconCache.getIcon("hermes.replay"));

      this.messageStore = messageStore;
      this.sourceDestination = sourceDestination;
      this.hermes = hermes;
      this.targetDestinationConfig = targetDestinationConfig;

   }

   public ReplayMessagesFromStoreTask(MessageStore messageStore, Hermes hermes, DestinationConfig targetDestinationConfig)
   {
      this(messageStore, null, hermes, targetDestinationConfig);
   }

   public ReplayMessagesFromStoreTask(MessageStore messageStore, Hermes hermes)
   {
      this(messageStore, null, hermes, null);
   }

   @Override
   public String getTitle()
   {
      return "Replaying from " + messageStore.getId() + " to " + hermes.getId() ;
   }

   @Override
   public void invoke() throws Exception
   {

      synchronized (this)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), "Are you sure you wish to replay message(s) to " + hermes.getId() + "?",
                     "Warning", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
               {
                  stop();

                  notifyStatus("Replay cancelled");
               }

               synchronized (ReplayMessagesFromStoreTask.this)
               {
                  ReplayMessagesFromStoreTask.this.notify();
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

      QueueBrowser browser = null;

      try
      {
         if (isRunning())
         {
            Hermes.ui.getDefaultMessageSink().add(getTitle() + "...") ;
            
            Destination targetDestination = null;

            if (targetDestinationConfig != null)
            {
               targetDestination = hermes.getDestination(targetDestinationConfig.getName(), Domain.getDomain(targetDestinationConfig.getDomain()));
            }

            final MessageStore.HeaderPolicy headerPolicy = targetDestination == null ? MessageStore.HeaderPolicy.DESTINATION_ONLY
                  : MessageStore.HeaderPolicy.NO_HEADER;
            browser = (sourceDestination == null) ? messageStore.visit(hermes, headerPolicy) : messageStore.visit(hermes, sourceDestination, headerPolicy);

            for (Enumeration iter = browser.getEnumeration(); iter.hasMoreElements() && isRunning();)
            {
               final Message message = (Message) iter.nextElement();

               if (message != null)
               {
                  if (targetDestination != null)
                  {
                     message.setJMSDestination(targetDestination);
                  }

                  hermes.send(message.getJMSDestination(), message);

                  if (hermes.getTransacted())
                  {
                     hermes.commit();
                  }
               }

               messagesSent++;
            }

            Hermes.ui.getDefaultMessageSink().add("Replayed " + messagesSent + " message" + TextUtils.plural(messagesSent) + " to " + hermes.getId());
         }
      }
      finally
      {
         JMSUtils.closeQuietly(browser);
         hermes.close();
      }
   }

}
