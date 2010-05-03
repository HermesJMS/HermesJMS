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

import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.BrowseContextAction;
import hermes.browser.model.tree.ConnectionFactoryTreeNode;
import hermes.browser.transferable.HermesConfigGroup;
import hermes.browser.transferable.JMSAdministeredObjectTransferable;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

/**
 * Create a new session from a ConnectionFactory bound in JNDI
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: CreateNewSessionFromJNDIAction.java,v 1.5 2005/06/17 14:35:04 colincrist Exp $
 */

public class CreateNewSessionFromJNDIAction extends JNDIAction
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 1012282319352405353L;

public CreateNewSessionFromJNDIAction()
   {
     
      putValue(Action.NAME, "Create new session...");
      putValue(Action.SHORT_DESCRIPTION, "Create a session using this connection factory.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("jms.connectionFactory.new"));

      setEnabled(false);
   }

   @Override
   protected boolean checkEnabled(TreePath path)
   {
     return path != null && path.getLastPathComponent() instanceof ConnectionFactoryTreeNode ;
   }

   public void actionPerformed(ActionEvent e)
   {
      final BrowseContextAction browseContext = (BrowseContextAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
      HermesBrowser.getBrowser().getBrowserTree().doTransfer(
            new JMSAdministeredObjectTransferable(new HermesConfigGroup(null, browseContext.getContextTree().getSelectedDestinations(), browseContext.getContextTree().getSelectedConnectionFactories())), TransferHandler.COPY);
   }
}
