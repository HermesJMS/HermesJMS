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
import hermes.browser.model.tree.ContextTreeNode;

import java.awt.event.ActionEvent;

import javax.naming.Context;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

/**
 * Create a new session from a ConnectionFactory bound in JNDI
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: CreateNewJNDIContextAction.java,v 1.2 2005/05/24 12:58:36
 *          colincrist Exp $
 */

public class CreateNewJNDIContextAction extends JNDIAction
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -7671492715045694328L;

public CreateNewJNDIContextAction()
   {
      putValue(Action.NAME, "Create new context...");
      putValue(Action.SHORT_DESCRIPTION, "Create a new context at this point in the tree.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("jndi.context.new"));

      setEnabled(false);

   }

   protected boolean checkEnabled(TreePath path)
   {
      return path != null && path.getLastPathComponent() instanceof ContextTreeNode ;
   }

   public void actionPerformed(ActionEvent e)
   {
      try
      {
         final BrowseContextAction browseContext = (BrowseContextAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
         final ContextTreeNode node = (ContextTreeNode) browseContext.getContextTree().getSelectionPath().getLastPathComponent();
         final String binding = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Enter Binding", "");

         if (binding != null && !binding.equals(""))
         {
            final Context context = node.getContextFactory().createContext();

            context.createSubcontext(binding);
            context.close();

            Hermes.ui.getDefaultMessageSink().add("Subcontext " + binding + " created.");
         }
         else
         {
            HermesBrowser.getBrowser().showErrorDialog("Invalid binding name.");
         }
      }
      catch (Throwable ex)
      {
         HermesBrowser.getBrowser().showErrorDialog("Cannot create context:", ex);
      }
   }
}
