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

package hermes.browser.components;

import hermes.browser.IconCache;
import hermes.browser.model.tree.AbstractTreeNode;
import hermes.browser.model.tree.ContextTreeNode;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ContextTreeCellRenderer.java,v 1.1 2005/05/26 17:45:40 colincrist Exp $
 */

public class ContextTreeCellRenderer extends DefaultTreeCellRenderer
{
   public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
   {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

      if (value instanceof ContextTreeNode)
      {
         if (!leaf)
         {
            if (expanded)
            {
               setIcon(IconCache.getIcon("hermes.tree.folder.opened"));
            }
            else
            {
               setIcon(IconCache.getIcon("hermes.tree.folder.closed"));
            }
         }
      }
      else if (value instanceof AbstractTreeNode)
      {
         Icon icon = ((AbstractTreeNode) value).getIcon();

         if (icon != null)
         {
            setIcon(icon);
         }
      }
      else
      {
         setIcon(IconCache.getIcon("jms.queueOrTopic"));
      }

      return this;
   }

}
