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

package hermes.swing.actions;

import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.model.tree.MessageStoreTreeNode;
import hermes.browser.model.tree.MessageStoreURLTreeNode;
import hermes.store.MessageStore;
import hermes.store.MessageStoreManager;

import java.awt.event.ActionEvent;

import javax.jms.JMSException;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * Create a new JMS session.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: CreateNewContextAction.java,v 1.1 2005/05/14 22:53:48
 *          colincrist Exp $
 */

public class CreateNewMessageStoreAction extends ActionSupport
{
   private static final Logger log = Logger.getLogger(CreateNewMessageStoreAction.class);

   public CreateNewMessageStoreAction()
   {
      putValue(Action.NAME, "New Message Store...");
      putValue(Action.SHORT_DESCRIPTION, "Create new JMS message Store.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.store.new"));

      setEnabled(!HermesBrowser.getBrowser().isRestricted() && !HermesBrowser.getBrowser().isMessageStoresDisabled());
   }

   public void actionPerformed(ActionEvent arg0)
   {
      final String name = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Message store name:", null);

      if (name != null && !name.equals(""))
      {
         try
         {
            final TreePath treePath = HermesBrowser.getBrowser().getBrowserTree().getSelectionPath();
            MessageStore store = null;

            if (treePath != null)
            {
               if (treePath.getLastPathComponent() instanceof MessageStoreURLTreeNode)
               {
                  MessageStoreURLTreeNode node = (MessageStoreURLTreeNode) treePath.getLastPathComponent();
                  store = MessageStoreManager.create(node.getURL(), name);
               }
               else if (treePath.getLastPathComponent() instanceof MessageStoreTreeNode)
               {
                  MessageStoreTreeNode node = (MessageStoreTreeNode) treePath.getLastPathComponent();
                  MessageStoreURLTreeNode parent = (MessageStoreURLTreeNode) node.getParent();
                  store = MessageStoreManager.create(parent.getURL(), name);
               }
            }

            if (store == null)
            {
               store = MessageStoreManager.create(name);
            }

            HermesBrowser.getBrowser().getBrowserTree().getBrowserModel().onMessageStoreAdded(store);
         }
         catch (JMSException e)
         {
            HermesBrowser.getBrowser().showErrorDialog(e);
         }
      }

   }
}
