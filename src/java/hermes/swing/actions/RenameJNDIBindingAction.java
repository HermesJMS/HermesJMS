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

import javax.naming.Context;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

/**
 * Rename a binding in JNDI
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: RenameJNDIBindingAction.java,v 1.6 2006/02/08 09:17:08 colincrist Exp $
 */

public class RenameJNDIBindingAction extends JNDIAction
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -6794595496519263679L;

public RenameJNDIBindingAction()
   {
      putValue(Action.NAME, "Rename...");
      putValue(Action.SHORT_DESCRIPTION, "Rename the binding.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.object.rename")) ;

      setEnabled(false);

   }

   @Override
   protected boolean checkEnabled(TreePath path)
   {
    return path != null && path.getLastPathComponent() instanceof AbstractTreeNode ;
   }

   public void actionPerformed(ActionEvent e)
   {
      try
      {
         final BrowseContextAction browseContext = (BrowseContextAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
         final AbstractTreeNode node = (AbstractTreeNode) browseContext.getContextTree().getSelectionPath().getLastPathComponent();
         final String binding = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Enter Binding", "");

         if (binding != null && !binding.equals(""))
         {
            if (node.getParent() instanceof ContextTreeNode)
            {
               ContextTreeNode contextNode = (ContextTreeNode) node.getParent();
               Context context = (Context) contextNode.getContextFactory().createContext().lookup(node.getPathFromRoot());
               context.rename(node.getId(), binding);
               context.close();

               Hermes.ui.getDefaultMessageSink().add(node.getId() + " renamed to " + binding);
            }
         }
      }
      catch (Throwable ex)
      {
         HermesBrowser.getBrowser().showErrorDialog("Cannot create context:", ex);
      }
   }
}
