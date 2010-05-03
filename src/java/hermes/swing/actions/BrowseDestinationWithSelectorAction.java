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

import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.model.tree.MessageStoreQueueTreeNode;
import hermes.browser.model.tree.MessageStoreTopicTreeNode;
import hermes.browser.model.tree.MessageStoreTreeNode;
import hermes.config.DestinationConfig;
import hermes.util.TextUtils;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.jms.Queue;
import javax.jms.Topic;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * Action to browse a queue or topic, adding a selector interactively.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: BrowseDestinationWithSelectorAction.java,v 1.2 2005/08/15
 *          20:37:27 colincrist Exp $
 */

public class BrowseDestinationWithSelectorAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 3361291057724640916L;
private static final Logger log = Logger.getLogger(BrowseDestinationWithSelectorAction.class);
   public BrowseDestinationWithSelectorAction()
   {
      putValue(Action.NAME, "Browse with selector...");
      putValue(Action.SHORT_DESCRIPTION, "Browse the queue, topic or message store with a selector");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.browseWithSelector"));
      putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false)) ; 
      
      setEnabled(false);

      enableOnBrowserTreeSelection(new Class[] { MessageStoreQueueTreeNode.class, MessageStoreTopicTreeNode.class, MessageStoreTreeNode.class,
            DestinationConfigTreeNode.class }, this, true);

   }

   public void actionPerformed(ActionEvent e)
   {
      actionPerformed(getBrowserTree().getSelectionModel().getSelectionPath());
   }

   public void actionPerformed(TreePath selectionPath)
   {
      if (selectionPath != null)
      {
         try
         {
            final String selector = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Enter Selector", "");

            if (!TextUtils.isEmpty(selector))
            {
               if (selectionPath.getLastPathComponent() instanceof DestinationConfigTreeNode)
               {
                  final DestinationConfigTreeNode destinationNode = (DestinationConfigTreeNode) selectionPath.getLastPathComponent();
                  final Hermes hermes = ((HermesTreeNode) destinationNode.getHermesTreeNode()).getHermes();

                  DestinationConfig newConfig = HermesBrowser.getConfigDAO().duplicate(destinationNode.getConfig());
                  newConfig.setSelector(selector);

                  log.info("browsing " + hermes.getId() + ": " + destinationNode.getDestinationName() + " with user selector " + selector);

                  HermesBrowser.getBrowser().getActionFactory().createQueueBrowseAction(hermes, newConfig);
               }
               else if (selectionPath.getLastPathComponent() instanceof MessageStoreTreeNode)
               {
                  final MessageStoreTreeNode node = (MessageStoreTreeNode) selectionPath.getLastPathComponent();
                  final Hermes hermes = checkHermesForMessageStore();

                  HermesBrowser.getBrowser().getActionFactory().createMessageStoreBrowseAction(node.getMessageStore(), hermes, selector);

               }
               else if (selectionPath.getLastPathComponent() instanceof MessageStoreQueueTreeNode)
               {
                  final MessageStoreQueueTreeNode queueNode = (MessageStoreQueueTreeNode) selectionPath.getLastPathComponent();

                  if (queueNode.getParent() instanceof MessageStoreTreeNode)
                  {
                     final MessageStoreTreeNode storeNode = (MessageStoreTreeNode) queueNode.getParent();
                     final Hermes hermes = checkHermesForMessageStore();

                     HermesBrowser.getBrowser().getActionFactory()
                           .createMessageStoreBrowseAction(storeNode.getMessageStore(), hermes, (Queue) queueNode.getBean(), selector);

                  }
               }
               else if (selectionPath.getLastPathComponent() instanceof MessageStoreTopicTreeNode)
               {
                  final MessageStoreTopicTreeNode topicNode = (MessageStoreTopicTreeNode) selectionPath.getLastPathComponent();

                  if (topicNode.getParent() instanceof MessageStoreTreeNode)
                  {
                     final MessageStoreTreeNode storeNode = (MessageStoreTreeNode) topicNode.getParent();
                     final Hermes hermes = checkHermesForMessageStore();

                     HermesBrowser.getBrowser().getActionFactory()
                           .createMessageStoreBrowseAction(storeNode.getMessageStore(), hermes, (Topic) topicNode.getBean(), selector);
                  }
               }
            }
         }
         catch (Exception ex)
         {
            log.error(ex.getMessage(), ex);

            HermesBrowser.getBrowser().showErrorDialog(ex);
         }
      }
   }

   private Hermes checkHermesForMessageStore()
   {
      return getBrowserTree().getLastSelectedHermesTreeNode() == null ? null : getBrowserTree().getLastSelectedHermesTreeNode().getHermes();
   }
}
