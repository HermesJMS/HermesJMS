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

import hermes.browser.HermesBrowser;
import hermes.browser.model.BrowserTreeModel;
import hermes.browser.model.tree.MessageStoreTreeNode;
import hermes.browser.model.tree.MessageStoreURLTreeNode;
import hermes.store.MessageStore;
import hermes.store.MessageStoreManager;

import java.util.Collection;

import javax.jms.JMSException;
import javax.swing.SwingUtilities;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: UpdateMessageStoresTask.java,v 1.3 2005/08/15 20:37:31
 *          colincrist Exp $
 */

public class UpdateMessageStoresTask implements Runnable
{
   private MessageStoreURLTreeNode parent;
   private BrowserTreeModel model;

   public UpdateMessageStoresTask(BrowserTreeModel model, MessageStoreURLTreeNode parent)
   {
      this.model = model;
      this.parent = parent;
   }

   public void run()
   {
      try
      {
         final Collection<MessageStore> stores = MessageStoreManager.find(parent.getURL());

         //
         // Remove old ones along the way...

         for (final MessageStore store : stores)
         {
            for (int i = 0; i < parent.getChildCount(); i++)
            {
               if (parent.getChildAt(i) instanceof MessageStoreTreeNode)
               {
                  MessageStoreTreeNode existing = (MessageStoreTreeNode) parent.getChildAt(i);

                  if (existing.getMessageStore().getId().equals(store.getId()))
                  {
                     parent.remove(i);
                     existing.close();

                     break;
                  }
               }
            }

            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  parent.add(new MessageStoreTreeNode(model, store.getId(), store));
                  model.nodesWereInserted(parent, new int[] { parent.getChildCount() -1 }) ;
               }
            });

         }

         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               // model.nodeStructureChanged(parent);
            }
         });
      }
      catch (JMSException e)
      {
         HermesBrowser.getBrowser().showErrorDialog("Unable to find any message stores", e);
      }

   }
}