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

import hermes.Domain;
import hermes.browser.IconCache;
import hermes.config.DestinationConfig;

import java.util.Comparator;

import javax.swing.tree.TreeNode;

/**
 * This is a destination node for the browser tree, it contains the
 * configuration object
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationTreeNode.java,v 1.3 2004/07/21 19:46:14 colincrist
 *          Exp $
 */

public class DestinationConfigTreeNode extends AbstractTreeNode
{
   public static class NodeComparator implements Comparator
   {
      public int compare(Object arg0, Object arg1)
      {
         DestinationConfigTreeNode lval = (DestinationConfigTreeNode) arg0;
         DestinationConfigTreeNode rval = (DestinationConfigTreeNode) arg1;

         if (lval.getDomain() == rval.getDomain())
         {
            return lval.getId().compareTo(rval.getId());
         }
         else
         {
            return -1;
         }
      }
   }

   private int depth = -1;
   private Domain domain;
   private HermesTreeNode hermesTreeNode ;

   private static String getLabel(DestinationConfig config, boolean cascadeNamespace)
   {
      String name = (config.getShortName() == null || config.getShortName().equals("")) ? config.getName() : config.getShortName();

      if (config.getShortName() == null && cascadeNamespace)
      {
         name = name.substring(name.lastIndexOf(".") + 1) ;
      }
      
      if (config.getDomain() == Domain.TOPIC.getId() && config.isDurable() && config.getClientID() != null)
      {
         name = name.concat(" name=").concat(config.getClientID());

      }
      if (config.getSelector() != null)
      {
         return name.concat(" (").concat(config.getSelector()).concat(" )");
      }
      else
      {
         return name;
      }
   }
   
   
   public String getToolTipText()
   {
      return getDestinationName() ;
   }

   public DestinationConfigTreeNode(HermesTreeNode hermesTreeNode, DestinationConfig destinationConfig, boolean cascadeNamespace)
   {
      super(getLabel(destinationConfig, cascadeNamespace), destinationConfig);

      this.domain = Domain.getDomain(destinationConfig.getDomain());
      this.hermesTreeNode = hermesTreeNode ;
      
      if (destinationConfig.getName() == null)
      {
         throw new RuntimeException("name is null");
      }

      if (destinationConfig.getDomain() == Domain.QUEUE.getId())
      {
         setIcon(IconCache.getIcon(IconCache.QUEUE));
      }
      else if (destinationConfig.getDomain() == Domain.TOPIC.getId())
      {
         if (destinationConfig.isDurable())
         {
            setIcon(IconCache.getIcon("jms.durableTopic"));
         }
         else
         {
            setIcon(IconCache.getIcon(IconCache.TOPIC));
         }
      }
      else
      {
         setIcon(IconCache.getIcon(IconCache.QUEUE_OR_TOPIC));
      }      
   }

   public HermesTreeNode getHermesTreeNode()
   {
      return hermesTreeNode ;
   }
   
   
   @Override
   public TreeNode getParent()
   {
      // TODO Auto-generated method stub
      return super.getParent();
   }

   public String getSelector()
   {
      return getConfig().getSelector();
   }

   public Domain getDomain()
   {
      return domain;
   }

   public DestinationConfig getConfig()
   {
      return (DestinationConfig) getBean();

   }

   public String getDestinationName()
   {
      return getConfig().getName();

   }

   public boolean isQueue()
   {
      return getConfig().getDomain() == Domain.QUEUE.getId();
   }

   public void setDepth(int depth)
   {
      this.depth = depth;
   }
}