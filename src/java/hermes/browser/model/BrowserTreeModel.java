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

package hermes.browser.model;

import hermes.ConnectionListener;
import hermes.Hermes;
import hermes.HermesConfigurationListener;
import hermes.HermesException;
import hermes.HermesRepository;
import hermes.HermesRepositoryListener;
import hermes.HermesRuntimeException;
import hermes.JNDIConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.browser.model.tree.AbstractTreeNode;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.DestinationFragmentTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.model.tree.HermesTreeNodeComparator;
import hermes.browser.model.tree.MessageStoreTreeNode;
import hermes.browser.model.tree.MessageStoreURLTreeNode;
import hermes.browser.model.tree.NamingConfigTreeNode;
import hermes.browser.model.tree.RepositoryTreeNode;
import hermes.browser.tasks.UpdateMessageStoresTask;
import hermes.config.DestinationConfig;
import hermes.config.JDBCStore;
import hermes.config.NamingConfig;
import hermes.config.WatchConfig;
import hermes.impl.DestinationConfigKeyWrapper;
import hermes.store.MessageStore;
import hermes.store.StoreUtils;
import hermes.swing.SwingRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;

import javax.jms.JMSException;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Category;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: BrowserTreeModel.java,v 1.26 2006/10/30 17:44:07 colincrist Exp $
 */

public class BrowserTreeModel extends DefaultTreeModel implements HermesConfigurationListener, HermesRepositoryListener, ConnectionListener
{
   private static final Category cat = Category.getInstance(BrowserTreeModel.class);
   private static final Timer timer = new Timer();
   private DefaultMutableTreeNode ctxRootNode = new DefaultMutableTreeNode("contexts");
   private Map<DestinationConfigKeyWrapper, DestinationConfigTreeNode> destinationMap = new HashMap<DestinationConfigKeyWrapper, DestinationConfigTreeNode>();

   // private Map<String, Map<DestinationConfigKeyWrapper,
   // DestinationConfigTreeNode>> topicMap = new HashMap<String, Map<String,
   // DestinationConfigKeyWrapper>>();

   private Map<String, MessageStoreURLTreeNode> stores = new HashMap<String, MessageStoreURLTreeNode>();
   private Map<String, HermesTreeNode> hermesMap = new TreeMap<String, HermesTreeNode>();
   private DefaultMutableTreeNode jmsRootNode = new DefaultMutableTreeNode("sessions");
   private Map<String, NamingConfigTreeNode> namingNodeById = new HashMap<String, NamingConfigTreeNode>();
   private Map<HermesRepository, RepositoryTreeNode> rep2Node = new HashMap<HermesRepository, RepositoryTreeNode>();
   private DefaultMutableTreeNode repositoryNode = new DefaultMutableTreeNode("files");
   private DefaultMutableTreeNode storesNode = new DefaultMutableTreeNode("stores");
   private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("jms");

   public BrowserTreeModel() throws HermesException
   {
      super(new DefaultMutableTreeNode());
      setRoot(rootNode);

      rootNode.removeAllChildren();
      jmsRootNode.removeAllChildren();
      ctxRootNode.removeAllChildren();

      rootNode.add(jmsRootNode);
      rootNode.add(ctxRootNode);
      rootNode.add(storesNode);

      if (!HermesBrowser.getBrowser().isRestricted())
      {
         rootNode.add(repositoryNode);
      }

      //
      // Update any MessageStores....

      if (!HermesBrowser.getBrowser().isMessageStoresDisabled())
      {
         MessageStoreURLTreeNode localURLNode = new MessageStoreURLTreeNode("local", StoreUtils.getDefaultConnectionURL());
         addMessageStoreURLTreeNode(localURLNode);
      }

      for (Iterator iter = HermesBrowser.getBrowser().getConfig().getJdbcStore().iterator(); iter.hasNext();)
      {
         JDBCStore storeConfig = (JDBCStore) iter.next();

         try
         {
            addMessageStoreURLTreeNode(new MessageStoreURLTreeNode(storeConfig));
         }
         catch (ClassNotFoundException ex)
         {
            HermesBrowser.getBrowser().showErrorDialog("Cannot load JDBC driver " + storeConfig.getDriver());
         }

      }

      Hermes.events.addConnectionListener(this);
   }

   public void addMessageStoreURLTreeNode(MessageStoreURLTreeNode node)
   {
      stores.put(node.getURL(), node);

      storesNode.add(node);
      nodesWereInserted(storesNode, new int[] { storesNode.getChildCount() - 1 });

      HermesBrowser.getBrowser().getThreadPool().invokeLater(new UpdateMessageStoresTask(this, node));
   }

   public void onConnectionClosed(Hermes hermes)
   {
      if (hermesMap.containsKey(hermes.getId()))
      {
         final HermesTreeNode node = hermesMap.get(hermes.getId());
         node.setConnectionOpen(false);
      }
      else
      {
         cat.error("onConnectionClosed(), no such Hermes id=" + hermes.getId());
      }
   }

   public void onConnectionOpen(Hermes hermes)
   {
      if (hermesMap.containsKey(hermes.getId()))
      {
         final HermesTreeNode node = hermesMap.get(hermes.getId());
         node.setConnectionOpen(true);
      }
      else
      {
         cat.error("onConnectionOpen(), no such Hermes id=" + hermes.getId());
      }
   }

   public Collection<MessageStore> getMessageStores()
   {
      final Collection<MessageStore> stores = new ArrayList<MessageStore>();

      for (int i = 0; i < storesNode.getChildCount(); i++)
      {
         if (storesNode.getChildAt(i) instanceof MessageStoreURLTreeNode)
         {
            final MessageStoreURLTreeNode urlNode = (MessageStoreURLTreeNode) storesNode.getChildAt(i);

            for (int j = 0; j < urlNode.getChildCount(); j++)
            {
               final MessageStoreTreeNode node = (MessageStoreTreeNode) urlNode.getChildAt(j);

               stores.add(node.getMessageStore());
            }
         }
      }

      return stores;
   }

   public HermesTreeNode getFirstHermesTreeNode()
   {
      final Iterator<Map.Entry<String, HermesTreeNode>> iter = hermesMap.entrySet().iterator();

      return iter.hasNext() ? iter.next().getValue() : null;
   }

   public void onMessageStoreAdded(final MessageStore store)
   {
      SwingRunner.invokeLater(new Runnable()
      {
         public void run()
         {
            final MessageStoreURLTreeNode parent = stores.get(store.getURL());

            if (parent != null)
            {
               parent.add(new MessageStoreTreeNode(BrowserTreeModel.this, store.getId(), store));
               nodeStructureChanged(parent);
            }
            else
            {
               throw new HermesRuntimeException("Cannot find JDBC URL " + store.getURL());
            }
         }
      });
   }

   public void onMessageStoreRemoved(final MessageStore store)
   {
      SwingRunner.invokeLater(new Runnable()
      {
         public void run()
         {
            final MessageStoreURLTreeNode parent = stores.get(store.getURL());

            if (parent != null)
            {
               for (int i = 0; i < parent.getChildCount(); i++)
               {
                  if (parent.getChildAt(i) instanceof MessageStoreTreeNode)
                  {
                     final MessageStoreTreeNode node = (MessageStoreTreeNode) parent.getChildAt(i);

                     if (node.getId().equals(store.getId()))
                     {
                        parent.remove(i);
                        nodeStructureChanged(parent);
                        break;
                     }
                  }
               }
            }
         }
      });
   }

   public Collection<String> getAllHermesIds()
   {
      return hermesMap.keySet();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.metastuff.hermes.HermesConfigurationListener#onDestinationAdded(org.metastuff.hermes.Hermes,
    *      org.metastuff.hermes.config.DestinationConfig)
    */
   public void onDestinationAdded(final Hermes hermes, final DestinationConfig destinationConfig)
   {
      // cat.debug("onDestinationAdded, hermes=" + hermes.getId() + " " +
      // Domain.getDomain(destinationConfig.getDomain()).toString() + "=" +
      // destinationConfig.getName());

      SwingRunner.invokeLater(new Runnable()
      {
         public void run()
         {
            if (hermesMap.containsKey(hermes.getId()))
            {
               final HermesTreeNode cfNode = (HermesTreeNode) hermesMap.get(hermes.getId());
               final DestinationConfigTreeNode dNode = new DestinationConfigTreeNode(cfNode, destinationConfig, cfNode.isCascadeNamespace());
               final DestinationConfigKeyWrapper key = new DestinationConfigKeyWrapper(hermes, destinationConfig);

               if (destinationMap.containsKey(key))
               {
                  cat.error("duplicate destination key=" + key);
               }
               else
               {
                  destinationMap.put(key, dNode);

                  if (cfNode.isCascadeNamespace())
                  {
                     try
                     {
                        if (cfNode.getHermes().getConnectionFactory() instanceof JNDIConnectionFactory)
                        {
                           TreeUtils.add(BrowserTreeModel.this, cfNode.getHermes(), destinationConfig.getName(), "/", cfNode, dNode);
                        }
                        else
                        {
                           TreeUtils.add(BrowserTreeModel.this, cfNode.getHermes(), destinationConfig.getName(), ".", cfNode, dNode);
                        }
                     }
                     catch (JMSException ex)
                     {
                        HermesBrowser.getBrowser().showErrorDialog(ex);
                     }
                  }
                  else
                  {
                     cfNode.add(dNode);
                     nodesWereInserted(cfNode, new int[] { cfNode.getIndex(dNode) });
                  }
               }
            }
         }
      });
   }

   public void onDestinationRemoved(final Hermes hermes, final DestinationConfig destinationConfig)
   {
      SwingRunner.invokeLater(new Runnable()
      {
         public void run()
         {
            if (hermesMap.containsKey(hermes.getId()))
            {
               final HermesTreeNode cfNode = (HermesTreeNode) hermesMap.get(hermes.getId());
               final DestinationConfigKeyWrapper key = new DestinationConfigKeyWrapper(hermes, destinationConfig);

               if (destinationMap.containsKey(key))
               {
                  final DestinationConfigTreeNode node = destinationMap.remove(key);

                  if (node != null)
                  {
                     if (node.getParent() == cfNode)
                     {

                        int index = cfNode.getIndex(node);
                        cfNode.remove(node);
                        nodesWereRemoved(cfNode, new int[] { index }, new Object[] { node });
                     }
                     else if (node.getParent() instanceof DestinationFragmentTreeNode)
                     {
                        AbstractTreeNode cleanup = node;
                        do
                        {
                           AbstractTreeNode cleanupParent = (AbstractTreeNode) cleanup.getParent();
                           int index = cleanupParent.getIndex(cleanup);
                           cleanupParent.remove(cleanup);

                           nodesWereRemoved(cleanupParent, new int[] { index }, new Object[] { cleanup });
                           cleanup = cleanupParent;
                        }
                        while (cleanup.getChildCount() == 0 && cleanup instanceof DestinationFragmentTreeNode);
                     }
                  }
               }
            }
         }
      });
   }

   public void onHermesAdded(Hermes hermes)
   {
      // cat.debug("onHermesAdded, hermes=" + hermes);

      try
      {
         if (hermesMap.containsKey(hermes.getId()))
         {
            onHermesRemoved(hermes);
         }

         HermesTreeNode cfNode = new HermesTreeNode(hermes.getId(), hermes, this);
         HermesTreeNodeComparator comparator = new HermesTreeNodeComparator();

         hermesMap.put(hermes.getId(), cfNode);
         int i = 0;

         for (; i < jmsRootNode.getChildCount(); i++)
         {
            HermesTreeNode node = (HermesTreeNode) jmsRootNode.getChildAt(i);

            if (comparator.compare(cfNode, node) < 0)
            {
               break;
            }
         }
         jmsRootNode.insert(cfNode, i);

         nodesWereInserted(jmsRootNode, new int[] { i });

      }
      catch (JMSException ex)
      {
         cat.error(ex.getMessage(), ex);
      }

   }

   public void onHermesRemoved(Hermes hermes)
   {
      // cat.debug("onHermesRemoved, hermes=" + hermes);

      if (hermesMap.containsKey(hermes.getId()))
      {
         HermesTreeNode cfNode = (HermesTreeNode) hermesMap.get(hermes.getId());
         int index = jmsRootNode.getIndex(cfNode);

         if (jmsRootNode.isNodeChild(cfNode))
         {
            jmsRootNode.remove(cfNode);
            nodesWereRemoved(jmsRootNode, new int[] { index }, new Object[] { cfNode });
         }

         hermesMap.remove(hermes.getId());

         for (Iterator<DestinationConfigKeyWrapper> iter = destinationMap.keySet().iterator(); iter.hasNext();)
         {
            DestinationConfigKeyWrapper key = iter.next();

            if (key.getHermes().equals(hermes))
            {
               iter.remove();
            }
         }
      }
   }

   public void onNamingAdded(NamingConfig namingConfig)
   {
      final NamingConfigTreeNode node = new NamingConfigTreeNode(namingConfig);

      namingNodeById.put(namingConfig.getId(), node);
      ctxRootNode.add(node);

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            if (ctxRootNode.getChildCount() == 0)
            {
               nodeStructureChanged(ctxRootNode);
            }
            else
            {
               nodesWereInserted(ctxRootNode, new int[] { ctxRootNode.getIndex(node) });
            }
         }
      });
   }

   public void onNamingRemoved(NamingConfig namingConfig)
   {
      final NamingConfigTreeNode node = (NamingConfigTreeNode) namingNodeById.get(namingConfig.getId());

      if (node != null)
      {
         final int index = ctxRootNode.getIndex(node);

         if (node != null)
         {
            // HJMS-7 Check that the node is a child before removing it.

            if (ctxRootNode.isNodeChild(node))
            {
               ctxRootNode.remove(node);

               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     nodesWereRemoved(ctxRootNode, new int[] { index }, new Object[] { node });
                  }
               });
            }

            namingNodeById.remove(namingConfig.getId());
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesRepositoryListener#onRepositoryAdded(hermes.HermesRepository)
    */
   public void onRepositoryAdded(HermesRepository repository)
   {
      final RepositoryTreeNode node = new RepositoryTreeNode(repository);

      rep2Node.put(repository, node);
      repositoryNode.add(node);

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            nodesWereInserted(repositoryNode, new int[] { repositoryNode.getIndex(node) });
         }
      });

   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesRepositoryListener#onRepositoryRemoved(hermes.HermesRepository)
    */
   public void onRepositoryRemoved(HermesRepository repository)
   {
      final RepositoryTreeNode node = (RepositoryTreeNode) rep2Node.remove(repository);
      final int index = repositoryNode.getIndex(node);

      rep2Node.remove(repository);
      repositoryNode.remove(node);

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            nodeStructureChanged(repositoryNode);
         }
      });
   }

   public void onWatchAdded(WatchConfig watchConfig)
   {
      // NOP
   }

   public void onWatchRemoved(WatchConfig watchConfig)
   {
      // NOP
   }

}