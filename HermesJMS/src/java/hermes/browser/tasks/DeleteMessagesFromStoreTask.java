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

import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.store.MessageStore;
import hermes.util.TextUtils;

import java.util.Collection;

import javax.jms.Message;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DeleteMessagesFromStoreTask.java,v 1.1 2005/06/28 15:36:21
 *          colincrist Exp $
 */

public class DeleteMessagesFromStoreTask extends TaskSupport
{
   private MessageStore messageStore;
   private Collection<Message> messages;
   private boolean warning;

   public DeleteMessagesFromStoreTask(MessageStore messageStore, Collection<Message> messages, boolean warning)
   {
      super(IconCache.getIcon("hermes.messages.delete"));

      this.messageStore = messageStore;
      this.messages = messages;
      this.warning = warning;
   }

   public String getTitle()
   {
      return "Deleting " + messages.size() + " message" + TextUtils.plural(messages.size()) + " from " + messageStore.getId();
   }

   @Override
   public void invoke() throws Exception
   {
      if (warning)
      {
         synchronized (this)
         {
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {

                  if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), "Are you sure you wish to delete " + messages.size() + " message"
                        + TextUtils.plural(messages.size()) + " from " + messageStore.getId(), "Warning", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                  {
                     stop();

                     notifyStatus("Delete from " + messageStore.getId() + " cancelled");
                  }

                  synchronized (DeleteMessagesFromStoreTask.this)
                  {
                     DeleteMessagesFromStoreTask.this.notify();
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
         int numDeleted = 0;

         final ProgressMonitor monitor = new ProgressMonitor(HermesBrowser.getBrowser(), "Deleting " + messages.size()
               + ((messages.size() == 1) ? " message" : " messages") + " from " + messageStore.getId(), "Connecting...", 0, messages.size() + 1);

         monitor.setMillisToDecideToPopup(50);
         monitor.setMillisToPopup(50);

         for (Message m : messages)
         {
            messageStore.delete(m);

            monitor.setNote(++numDeleted + " messages deleted.");
            monitor.setProgress(numDeleted);
            
            if (monitor.isCanceled() || !isRunning())
            {
               Hermes.ui.getDefaultMessageSink().add("Delete from " + messageStore.getId() + " cancelled") ;
               break ;
            }
         }

         if (isRunning() && !monitor.isCanceled())
         {
            monitor.setNote("Checkpointing...");
            monitor.setProgress(numDeleted + 1);

            messageStore.checkpoint();

            Hermes.ui.getDefaultMessageSink().add(
                  messages.size() + " message" + TextUtils.plural(messages.size()) + " deleted from store " + messageStore.getId());
         }
         else
         {
            messageStore.rollback();
         }
      }
   }
}
