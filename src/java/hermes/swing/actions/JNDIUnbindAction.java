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
import hermes.browser.actions.BrowseContextAction;
import hermes.browser.model.tree.AbstractTreeNode;
import hermes.browser.model.tree.ContextTreeNode;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Context;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

/**
 * Unbind an object in JNDI
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: JNDIUnbindAction.java,v 1.4 2007/01/13 14:12:42 colincrist Exp $
 */

public class JNDIUnbindAction extends JNDIAction
{

   public JNDIUnbindAction()
   {
      putValue(Action.NAME, "Unbind...");
      putValue(Action.SHORT_DESCRIPTION, "Unbind object.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.jndi.unbind"));

      setEnabled(false);
   }

   @Override
   protected boolean checkEnabled(TreePath path)
   {
      return path != null && path.getLastPathComponent() instanceof AbstractTreeNode;
   }

   public void actionPerformed(ActionEvent e)
   {
      try
      {
         final BrowseContextAction browseContext = (BrowseContextAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
         final Set<AbstractTreeNode> treeNodes = new HashSet<AbstractTreeNode>();

         if (browseContext.getContextTree().getSelectionPaths() != null)
         {
            for (final TreePath treePath : browseContext.getContextTree().getSelectionPaths())
            {
               if (treePath.getLastPathComponent() instanceof AbstractTreeNode)
               {
                  treeNodes.add((AbstractTreeNode) treePath.getLastPathComponent());
               }
            }
         }

         if (treeNodes.size() > 0)
         {
            String confirmMessage = null;
            String doneMessage = null;

            if (treeNodes.size() == 1)
            {
               confirmMessage = "Are you sure you wish to unbind this object?";
               doneMessage = "Object unbound";
            }
            else
            {
               confirmMessage = "Are you sure you wish to unbind these " + treeNodes.size() + " objects?";
               doneMessage = treeNodes.size() + " objects unbound";
            }

            if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), confirmMessage, "Please confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
               for (final AbstractTreeNode node : treeNodes)
               {
                  ContextTreeNode contextNode = (ContextTreeNode) node.getParent();
                  Context context = contextNode.getContextFactory().createContext();
                  context.unbind(node.getId());
                  context.close();
               }

               Hermes.ui.getDefaultMessageSink().add(doneMessage);
            }
         }

      }
      catch (Throwable ex)
      {
         HermesBrowser.getBrowser().showErrorDialog("Cannot unbind:", ex);
      }
   }
}
