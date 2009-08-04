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

package hermes.browser.model.tree;

import hermes.Hermes;
import hermes.JNDIConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.model.BrowserTreeModel;
import hermes.browser.model.TreeUtils;
import hermes.impl.DestinationConfigKeyWrapper;
import hermes.impl.DestinationConfigKeyWrapperComparator;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.jms.JMSException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

/**
 * A Hermes tree node.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HermesTreeNode.java,v 1.13 2006/11/06 07:16:32 colincrist Exp $
 */

public class HermesTreeNode extends AbstractTreeNode
{
   private static final Logger log = Logger.getLogger(HermesTreeNode.class);

   private BrowserTreeModel model;
   private boolean cascadeNamespace = false;

   /**
    * HermesTreeNode constructor comment.
    * 
    * @param userObject
    *           java.lang.Object
    */
   public HermesTreeNode(String id, Hermes hermes, BrowserTreeModel model) throws javax.jms.JMSException
   {
      super(id, hermes);

      this.model = model;

      setIcon(IconCache.getIcon("jms.connectionOpen"));
   }

   public boolean isCascadeNamespace()
   {
      return cascadeNamespace;
   }

   private void getDestinationTreeNodes(TreeNode root, HashMap<DestinationConfigKeyWrapper, DestinationConfigTreeNode> map)
   {
      for (int i = 0; i < root.getChildCount(); i++)
      {
         TreeNode node = root.getChildAt(i);

         if (node instanceof DestinationConfigTreeNode)
         {
            DestinationConfigTreeNode dNode = (DestinationConfigTreeNode) node;

            map.put(new DestinationConfigKeyWrapper(getHermes(), dNode.getConfig()), dNode);
         }
         else
         {
            getDestinationTreeNodes(node, map);
         }
      }
   }

   public void setCascadeNamespace(boolean cascadeNamespace)
   {
      this.cascadeNamespace = cascadeNamespace;

      HashMap<DestinationConfigKeyWrapper, DestinationConfigTreeNode> map = new HashMap<DestinationConfigKeyWrapper, DestinationConfigTreeNode>();
      getDestinationTreeNodes(this, map);
      removeAllChildren();
      model.nodeStructureChanged(this);

      for (Map.Entry<DestinationConfigKeyWrapper, DestinationConfigTreeNode> entry : map.entrySet())
      {       
         DestinationConfigTreeNode destinationNode = entry.getValue() ;
         
         if (isCascadeNamespace())
         {
            try
            {
               if (getHermes().getConnectionFactory() instanceof JNDIConnectionFactory)
               {
                  TreeUtils.add(model, entry.getValue().getHermesTreeNode().getHermes(), entry.getValue().getDestinationName(), "/", this, new DestinationConfigTreeNode(this, destinationNode.getConfig(), true));
               }
               else
               {
                  TreeUtils.add(model, entry.getValue().getHermesTreeNode().getHermes(), entry.getValue().getDestinationName(), ".", this, new DestinationConfigTreeNode(this, destinationNode.getConfig(), true));
               }
            }
            catch (JMSException ex)
            {
               HermesBrowser.getBrowser().showErrorDialog(ex);
            }
         }
         else
         {            
            add(new DestinationConfigTreeNode(this, destinationNode.getConfig(), false));
         }
      }
   }

   public Hermes getHermes()
   {
      return (Hermes) getBean();
   }

   public void add(DestinationConfigTreeNode node)
   {
      TreeMap<DestinationConfigKeyWrapper, MutableTreeNode> map = new TreeMap<DestinationConfigKeyWrapper, MutableTreeNode>(
            new DestinationConfigKeyWrapperComparator());

      for (int i = 0; i < getChildCount(); i++)
      {
         DestinationConfigTreeNode child = (DestinationConfigTreeNode) getChildAt(i);
         map.put(new DestinationConfigKeyWrapper(getHermes(), child.getConfig()), child);
      }

      map.put(new DestinationConfigKeyWrapper(getHermes(), node.getConfig()), node);
      removeAllChildren();

      for (Map.Entry<DestinationConfigKeyWrapper, MutableTreeNode> entry : map.entrySet())
      {
         super.add(entry.getValue());
      }
   }

   public void setConnectionOpen(boolean connectionOpen)
   {
      // NOP
   }

}