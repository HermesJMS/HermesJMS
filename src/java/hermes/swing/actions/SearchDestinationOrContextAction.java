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
import hermes.browser.components.BrowserTree;
import hermes.browser.dialog.QueueSearchDialog;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.config.DestinationConfig;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import com.jidesoft.swing.JideSwingUtilities;

/**
 * Action to search a queue, topic or all destinations on a session.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SearchDestinationOrContextAction,v 1.1 2005/05/13 15:31:20
 *          colincrist Exp $
 */

public class SearchDestinationOrContextAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 3691177566297585905L;
private boolean searchUserHeader = true ;
   
   public SearchDestinationOrContextAction()
   {
      putValue(Action.NAME, "Search...");
      putValue(Action.SHORT_DESCRIPTION, "Search the queues/topics.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.browse.filter"));

      setEnabled(false);

      enableOnBrowserTreeSelection(new Class[] { DestinationConfigTreeNode.class, HermesTreeNode.class } , this, true) ;    
   }

   public void actionPerformed(ActionEvent arg0)
   {
      final BrowserTree browserTree = HermesBrowser.getBrowser().getBrowserTree();

      if (browserTree.getSelectionPath().getLastPathComponent() instanceof DestinationConfigTreeNode)
      {
         final DestinationConfigTreeNode destinationNode = (DestinationConfigTreeNode) browserTree.getSelectionPath().getLastPathComponent();
         final HermesTreeNode hermesNode = (HermesTreeNode) destinationNode.getHermesTreeNode();
         final QueueSearchDialog dialog = new QueueSearchDialog(HermesBrowser.getBrowser(), hermesNode.getHermes(), (DestinationConfig)  destinationNode.getBean(), searchUserHeader);

         dialog.setLocationRelativeTo(null) ;
         dialog.show();
      }
      else if (browserTree.getSelectionPath().getLastPathComponent() instanceof HermesTreeNode)
      {
         final HermesTreeNode hermesNode = (HermesTreeNode) browserTree.getSelectionPath().getLastPathComponent();

         final QueueSearchDialog dialog = new QueueSearchDialog(HermesBrowser.getBrowser(), hermesNode.getHermes());

         JideSwingUtilities.centerWindow(dialog) ;
         dialog.show();
      }

   }
}
