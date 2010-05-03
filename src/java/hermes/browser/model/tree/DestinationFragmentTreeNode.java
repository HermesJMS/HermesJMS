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

package hermes.browser.model.tree;

import hermes.Hermes;
import hermes.impl.DestinationConfigKeyWrapper;
import hermes.impl.DestinationConfigKeyWrapperComparator;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.tree.MutableTreeNode;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class DestinationFragmentTreeNode extends AbstractTreeNode
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 2063762264140498380L;
private Hermes hermes;

   public DestinationFragmentTreeNode(Hermes hermes, String id)
   {
      super(id, null);
      this.hermes = hermes;
   }

   public Hermes getHermes()
   {
      return hermes;
   }

   public void add(DestinationFragmentTreeNode node)
   {
      super.add(node);

      reorder();
   }

   public void add(DestinationConfigTreeNode node)
   {
      super.add(node);

      reorder();
   }

   private void reorder()
   {
      final TreeMap<DestinationConfigKeyWrapper, MutableTreeNode> map = new TreeMap<DestinationConfigKeyWrapper, MutableTreeNode>(
            new DestinationConfigKeyWrapperComparator());
      final TreeMap<String, MutableTreeNode> fragmentMap = new TreeMap<String, MutableTreeNode>();

      for (int i = 0; i < getChildCount(); i++)
      {
         if (getChildAt(i) instanceof DestinationConfigTreeNode)
         {
            DestinationConfigTreeNode child = (DestinationConfigTreeNode) getChildAt(i);
            map.put(new DestinationConfigKeyWrapper(getHermes(), child.getConfig()), child);
         }
         else
         {
            DestinationFragmentTreeNode child = (DestinationFragmentTreeNode) getChildAt(i);
            fragmentMap.put(child.getId(), child);
         }
      }

      removeAllChildren();

      for (Map.Entry<String, MutableTreeNode> entry : fragmentMap.entrySet())
      {
         super.add(entry.getValue());
      }

      for (Map.Entry<DestinationConfigKeyWrapper, MutableTreeNode> entry : map.entrySet())
      {
         super.add(entry.getValue());
      }
   }

}
