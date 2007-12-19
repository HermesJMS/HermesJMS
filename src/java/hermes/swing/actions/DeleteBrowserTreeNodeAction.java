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
import hermes.browser.model.tree.AbstractTreeNode;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.DestinationFragmentTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.model.tree.MessageStoreQueueTreeNode;
import hermes.browser.model.tree.MessageStoreTopicTreeNode;
import hermes.browser.model.tree.MessageStoreTreeNode;
import hermes.browser.model.tree.MessageStoreURLTreeNode;
import hermes.browser.model.tree.NamingConfigTreeNode;
import hermes.config.HermesConfig;
import hermes.store.MessageStore;
import hermes.util.JMSUtils;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * Delete a destination, session or context from the browser tree,
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DeleteBrowserTreeNodeAction,v 1.1 2005/05/14 22:53:48
 *          colincrist Exp $
 */

public class DeleteBrowserTreeNodeAction extends ActionSupport
{
   private static final Logger log = Logger.getLogger(DeleteBrowserTreeNodeAction.class);

   public DeleteBrowserTreeNodeAction()
   {
      putValue(Action.NAME, "Delete");
      putValue(Action.SHORT_DESCRIPTION, "Delete the queue, topic, session, context or message store.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.objects.delete"));
      putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

      setEnabled(false);

      if (!HermesBrowser.getBrowser().isRestricted())
      {
         enableOnBrowserTreeSelection(new Class[] { MessageStoreURLTreeNode.class, DestinationConfigTreeNode.class, NamingConfigTreeNode.class,
               HermesTreeNode.class, MessageStoreTreeNode.class, MessageStoreQueueTreeNode.class, MessageStoreTopicTreeNode.class }, this, false);
      }

   }

   private void doDelete(NamingConfigTreeNode namingNode) throws HermesException
   {
      final HermesConfig config = HermesBrowser.getBrowser().getConfig();
      final DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) namingNode.getParent();

      HermesBrowser.getConfigDAO().removeNamingConfig(config, namingNode.getConfig().getId());
      HermesBrowser.getBrowser().saveConfig();
      Hermes.ui.getDefaultMessageSink().add(namingNode.getId() + " removed");

      final int[] index = { namingNode.getParent().getIndex(namingNode) };
      final Object[] objects = { namingNode };

      parentNode.remove(namingNode);
      HermesBrowser.getBrowser().getBrowserTree().getBrowserModel().nodesWereRemoved(parentNode, index, objects);
   }

   private void doDelete(MessageStoreURLTreeNode node) throws JMSException
   {
      final HermesConfig config = HermesBrowser.getBrowser().getConfig();
      final MutableTreeNode parent = (MutableTreeNode) node.getParent();
      final int index = parent.getIndex(node);
      HermesBrowser.getConfigDAO().removeJDBC(config, node.getConfig());

      parent.remove(node);
      HermesBrowser.getBrowser().getBrowserTree().getBrowserModel().nodesWereRemoved(parent, new int[] { index }, new Object[] { node });
      HermesBrowser.getBrowser().saveConfig();
   }

   private void doDelete(DestinationConfigTreeNode destinationNode) throws JMSException
   {
      final HermesConfig config = HermesBrowser.getBrowser().getConfig();
      final HermesTreeNode hermesNode = (HermesTreeNode) destinationNode.getHermesTreeNode();
      final String type = destinationNode.isQueue() ? "queue" : "topic";

      HermesBrowser.getConfigDAO().removeDestination(config, hermesNode.getHermes().getId(), destinationNode.getDestinationName());
      hermesNode.getHermes().removeDestinationConfig(destinationNode.getConfig());

      HermesBrowser.getBrowser().saveConfig();
      Hermes.ui.getDefaultMessageSink().add(destinationNode.getDestinationName() + " removed");

      if (destinationNode.getParent() == hermesNode)
      {
         final int[] index = { destinationNode.getHermesTreeNode().getIndex(destinationNode) };
         final Object[] objects = { destinationNode };
         hermesNode.remove(destinationNode);

         HermesBrowser.getBrowser().getBrowserTree().getBrowserModel().nodesWereRemoved(hermesNode, index, objects);
      }
      else
      {
         AbstractTreeNode cleanup = destinationNode;
         do
         {
            AbstractTreeNode cleanupParent = (AbstractTreeNode) cleanup.getParent();
            int index = cleanupParent.getIndex(cleanup);
            cleanupParent.remove(cleanup);

            HermesBrowser.getBrowser().getBrowserTree().getBrowserModel().nodesWereRemoved(cleanupParent, new int[] { index }, new Object[] { cleanup });
            cleanup = cleanupParent;
         }
         while (cleanup.getChildCount() == 0 && cleanup instanceof DestinationFragmentTreeNode);
      }
   }

   private void doDelete(final MessageStoreTreeNode node) throws JMSException
   {
      final MessageStore store = node.getMessageStore();

      HermesBrowser.getBrowser().getThreadPool().invokeLater(new Runnable()
      {
         public void run()
         {
            try
            {
               store.delete();
               store.checkpoint();

               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     HermesBrowser.getBrowser().getBrowserTree().remove(node);
                  }
               });
            }
            catch (Throwable e)
            {
               String message = "Cannot delete from store " + store.getId();

               try
               {
                  store.rollback();
               }
               catch (Throwable e1)
               {
                  log.error(e1);
               }

               HermesBrowser.getBrowser().showErrorDialog(message, e);
            }
         }
      });
   }

   private void doDelete(final MessageStore store, final Destination destination) throws JMSException
   {
      HermesBrowser.getBrowser().getThreadPool().invokeLater(new Runnable()
      {
         public void run()
         {
            try
            {
               Hermes.ui.getDefaultMessageSink().add("Deleting all messages from " + JMSUtils.getDestinationName(destination) + " in " + store.getId() + "...");

               store.delete(destination);
               store.checkpoint();

               Hermes.ui.getDefaultMessageSink().add("All messages from " + JMSUtils.getDestinationName(destination) + " removed from " + store.getId() + ".");
            }
            catch (Throwable e)
            {
               String message = "Cannot delete from store " + store.getId();

               try
               {
                  store.rollback();
               }
               catch (Throwable e1)
               {
                  log.error(e1);
               }
               HermesBrowser.getBrowser().showErrorDialog(message, e);
            }
         }
      });
   }

   private void doDelete(MessageStoreQueueTreeNode node) throws JMSException
   {
      if (node.getParent() instanceof MessageStoreTreeNode)
      {
         MessageStoreTreeNode parent = (MessageStoreTreeNode) node.getParent();
         doDelete(parent.getMessageStore(), (Destination) node.getBean());
      }
   }

   private void doDelete(MessageStoreTopicTreeNode node) throws JMSException
   {
      if (node.getParent() instanceof MessageStoreTreeNode)
      {
         MessageStoreTreeNode parent = (MessageStoreTreeNode) node.getParent();
         doDelete(parent.getMessageStore(), (Destination) node.getBean());
      }
   }

   private void doDelete(HermesTreeNode hermesNode) throws HermesException
   {
      final HermesConfig config = HermesBrowser.getBrowser().getConfig();
      final MutableTreeNode parentNode = (MutableTreeNode) hermesNode.getParent();

      HermesBrowser.getConfigDAO().removeHermes(config, hermesNode.getHermes().getId());

      HermesBrowser.getBrowser().saveConfig();
      Hermes.ui.getDefaultMessageSink().add(hermesNode.getHermes().getId() + " removed");

      final int[] index = { parentNode.getIndex(hermesNode) };
      final Object[] objects = { hermesNode };

      parentNode.remove(hermesNode);
      HermesBrowser.getBrowser().getBrowserTree().getBrowserModel().nodesWereRemoved(parentNode, index, objects);

   }

   public void actionPerformed(ActionEvent event)
   {
      try
      {
         final TreePath[] paths = HermesBrowser.getBrowser().getBrowserTree().getSelectionPaths();

         if (paths != null && paths.length > 0)
         {
            String msg = paths.length == 1 ? "Are you sure you want to delete this object?" : "Are you sure you want to delete these objects?";

            if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), msg, "Please confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
               for (final TreePath path : paths)
               {
                  final Object object = path.getLastPathComponent();

                  if (object instanceof DestinationConfigTreeNode)
                  {
                     doDelete((DestinationConfigTreeNode) object);
                  }
                  else if (object instanceof NamingConfigTreeNode)
                  {
                     doDelete((NamingConfigTreeNode) object);
                  }
                  else if (object instanceof HermesTreeNode)
                  {
                     doDelete((HermesTreeNode) object);
                  }
                  else if (object instanceof MessageStoreTreeNode)
                  {
                     doDelete((MessageStoreTreeNode) object);
                  }
                  else if (object instanceof MessageStoreQueueTreeNode)
                  {
                     doDelete((MessageStoreQueueTreeNode) object);
                  }
                  else if (object instanceof MessageStoreTopicTreeNode)
                  {
                     doDelete((MessageStoreTopicTreeNode) object);
                  }
                  else if (object instanceof MessageStoreURLTreeNode)
                  {
                     doDelete((MessageStoreURLTreeNode) object);
                  }
               }
            }

            HermesBrowser.getBrowser().saveConfig();
         }
      }
      catch (Exception ex)
      {
         log.error(ex.getMessage(), ex);
         HermesBrowser.getBrowser().showErrorDialog("Cannot delete: ", ex);
      }
   }
}
