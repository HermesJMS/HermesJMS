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

import hermes.browser.IconCache;
import hermes.browser.components.BrowserTree;
import hermes.browser.model.TreeUtils;
import hermes.browser.model.tree.HermesTreeNode;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class CascadeBrowserTreeAction extends ActionSupport
{
   private BrowserTree browserTree;

   public CascadeBrowserTreeAction(BrowserTree browserTree)
   {
      this.browserTree = browserTree;

      putValue(Action.NAME, "Cascade");
      putValue(Action.SHORT_DESCRIPTION, "Cascade tree.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.collapse.all"));
      
      

      browserTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
      {
         public void valueChanged(TreeSelectionEvent e)
         {
            CascadeBrowserTreeAction.this.setEnabled(e.getPath() != null && TreeUtils.pathContains(e.getPaths(), HermesTreeNode.class));
         }
      });
   }

   public void actionPerformed(ActionEvent e)
   {
      TreePath[] paths = browserTree.getSelectionPaths() ;
      
      for (int i = 0 ; i < paths.length ; i++)
      {
         for (int j = 0 ; j < paths[i].getPathCount() ; j++)
         {
            if (paths[i].getPath()[j] instanceof HermesTreeNode)
            {
               HermesTreeNode node = (HermesTreeNode) paths[i].getPath()[j] ;
               node.setCascadeNamespace(!node.isCascadeNamespace()) ;               
            }
         }
      }
     
   }
}
