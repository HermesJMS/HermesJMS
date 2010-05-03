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
import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.model.tree.MessageStoreQueueTreeNode;
import hermes.browser.model.tree.MessageStoreTopicTreeNode;
import hermes.browser.model.tree.MessageStoreTreeNode;
import hermes.browser.model.tree.NamingConfigTreeNode;
import hermes.browser.model.tree.RepositoryTreeNode;
import hermes.util.TextUtils;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.NamingException;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * Action to browse a queue, topic or JNDI context.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: BrowseDestinationOrContextAction.java,v 1.1 2005/05/14 22:53:48
 *          colincrist Exp $
 */

public class BrowseDestinationOrContextAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -2261691717762103310L;
private static final Logger log = Logger.getLogger(BrowseDestinationOrContextAction.class);

   public BrowseDestinationOrContextAction()
   {
      putValue(Action.NAME, "Browse...");
      putValue(Action.SHORT_DESCRIPTION, "Browse the queue, topic or context");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.browse"));
      putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

      setEnabled(false);

      enableOnBrowserTreeSelection(new Class[] { MessageStoreQueueTreeNode.class, MessageStoreTopicTreeNode.class, MessageStoreTreeNode.class,
            DestinationConfigTreeNode.class, NamingConfigTreeNode.class }, this, true);
      enableOnBrowserTreeSelection(new Class[] { NamingConfigTreeNode.class }, this, new TreeSelectionListener()
      {
         public void valueChanged(TreeSelectionEvent e)
         {
            setEnabled(getBrowserTree().getLastSelectedHermesTreeNode() != null);
         }
      }, true);
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
            if (selectionPath.getLastPathComponent() instanceof NamingConfigTreeNode)
            {
               final NamingConfigTreeNode node = (NamingConfigTreeNode) selectionPath.getLastPathComponent();

               HermesBrowser.getBrowser().getActionFactory().createBrowseContextAction(node.getConfig());
            }
            else if (selectionPath.getLastPathComponent() instanceof DestinationConfigTreeNode)
            {
               final DestinationConfigTreeNode destinationNode = (DestinationConfigTreeNode) selectionPath.getLastPathComponent();
               final Hermes hermes = ((HermesTreeNode) destinationNode.getHermesTreeNode()).getHermes();

               log.info("browsing " + hermes.getId() + ": " + destinationNode.getDestinationName());

               HermesBrowser.getBrowser().getActionFactory().createQueueBrowseAction(hermes, destinationNode.getConfig());

            }
            else if (selectionPath.getLastPathComponent() instanceof RepositoryTreeNode)
            {
               final RepositoryTreeNode repNode = (RepositoryTreeNode) selectionPath.getLastPathComponent();

               if (getBrowserTree().getLastSelectedHermesTreeNode() == null)
               {
                  HermesBrowser.getBrowser().showErrorDialog(
                        "You must select a session first to use as the factory to\ncreate messages read from a repository file.");
               }
               else
               {
                  final Hermes hermes = HermesBrowser.getBrowser().getBrowserTree().getLastSelectedHermesTreeNode().getHermes();

                  log.info("reading " + repNode.getRepository().getId());

                  HermesBrowser.getBrowser().getActionFactory().createRepositoryBrowseAction(repNode.getRepository(), hermes);
               }
            }
            else if (selectionPath.getLastPathComponent() instanceof MessageStoreTreeNode)
            {
               final MessageStoreTreeNode node = (MessageStoreTreeNode) selectionPath.getLastPathComponent();
               final Hermes hermes = checkHermesForMessageStore();

               HermesBrowser.getBrowser().getActionFactory().createMessageStoreBrowseAction(node.getMessageStore(), hermes, null);

            }
            else if (selectionPath.getLastPathComponent() instanceof MessageStoreQueueTreeNode)
            {
               final MessageStoreQueueTreeNode queueNode = (MessageStoreQueueTreeNode) selectionPath.getLastPathComponent();

               if (queueNode.getParent() instanceof MessageStoreTreeNode)
               {
                  final MessageStoreTreeNode storeNode = (MessageStoreTreeNode) queueNode.getParent();
                  final Hermes hermes = checkHermesForMessageStore();

                  HermesBrowser.getBrowser().getActionFactory().createMessageStoreBrowseAction(storeNode.getMessageStore(), hermes,
                        (Queue) queueNode.getBean(), null);

               }
            }
            else if (selectionPath.getLastPathComponent() instanceof MessageStoreTopicTreeNode)
            {
               final MessageStoreTopicTreeNode topicNode = (MessageStoreTopicTreeNode) selectionPath.getLastPathComponent();

               if (topicNode.getParent() instanceof MessageStoreTreeNode)
               {
                  final MessageStoreTreeNode storeNode = (MessageStoreTreeNode) topicNode.getParent();
                  final Hermes hermes = checkHermesForMessageStore();

                  HermesBrowser.getBrowser().getActionFactory().createMessageStoreBrowseAction(storeNode.getMessageStore(), hermes,
                        (Topic) topicNode.getBean(), null);

               }
            }
         }
         catch (Exception ex)
         {
            log.error(ex.getMessage(), ex);

            JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), "During browse/read: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
      }
   }

   private Hermes checkHermesForMessageStore() throws HermesException
   {
      if (!TextUtils.isEmpty(HermesBrowser.getBrowser().getConfig().getMessageStoreMessageFactory()))
      {
         try
         {
            Hermes hermes = (Hermes) HermesBrowser.getBrowser().getContext().lookup(HermesBrowser.getBrowser().getConfig().getMessageStoreMessageFactory()) ;
            
            if (hermes != null)
            {
               return hermes ;
            }
         }
         catch (NamingException ex)
         {
            log.info("cannot find configured message store message factory, using one from the tree instead: " + ex.getMessage(), ex) ;
         }         
      }
      
      if (getBrowserTree().getLastSelectedHermesTreeNode() == null)
      {
         return getBrowserTree().getBrowserModel().getFirstHermesTreeNode().getHermes();
      }
      else
      {
         return getBrowserTree().getLastSelectedHermesTreeNode().getHermes();
      }
   }
}
