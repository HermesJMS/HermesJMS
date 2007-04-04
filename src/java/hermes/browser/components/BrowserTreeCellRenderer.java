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

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Renderer for the icons in the main browser tree.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: BrowserTreeCellRenderer.java,v 1.1 2005/05/24 12:58:37 colincrist Exp $
 */

public class BrowserTreeCellRenderer extends DefaultTreeCellRenderer
{
   public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
   {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

      if (value instanceof AbstractTreeNode)
      {
         AbstractTreeNode node = (AbstractTreeNode) value;

         if (node.hasOpenIcon() && expanded)
         {
            setIcon(node.getOpenIcon());
         }
         else
         {
            setIcon(node.getIcon());
         }
      }
      else
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

      return this;
   }
}
