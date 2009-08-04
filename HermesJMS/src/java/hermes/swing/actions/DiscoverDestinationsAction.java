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
import hermes.browser.model.BrowserTreeModel;
import hermes.browser.model.tree.HermesTreeNode;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.tree.TreeNode;

/**
 * Discover destinations from the provider.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DiscoverDestinationsAction.java,v 1.6 2005/12/14 08:11:24
 *          colincrist Exp $
 */

public class DiscoverDestinationsAction extends ActionSupport
{

   public DiscoverDestinationsAction()
   {
      putValue(Action.NAME, "Discover...");
      putValue(Action.SHORT_DESCRIPTION, "Discover queues and topics from the provider");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.discover"));

      setEnabled(false);

      if (!HermesBrowser.getBrowser().isRestricted())
      {
         enableOnBrowserTreeSelection(new Class[] { HermesTreeNode.class }, this, true);
      }
   }

   public void actionPerformed(ActionEvent e)
   {
      try
      {
         if (HermesBrowser.getBrowser().getBrowserTree().getSelectionPath() != null)
         {
            final TreeNode node = (TreeNode) HermesBrowser.getBrowser().getBrowserTree().getSelectionPath().getLastPathComponent();

            if (node instanceof HermesTreeNode)
            {
               final HermesTreeNode hermesNode = (HermesTreeNode) node;

               HermesBrowser.getBrowser().getActionFactory().createDiscoverDestinationAction(
                     (BrowserTreeModel) HermesBrowser.getBrowser().getBrowserTree().getModel(), hermesNode);
            }
         }
      }
      catch (Exception ex)
      {
         HermesBrowser.getBrowser().showErrorDialog("Discovering: " + ex.getMessage(), ex);
      }
   }
}
