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
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

/**
 * Duplicate a sessions configuration
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: CopyBrowserNodeAction.java,v 1.2 2005/12/14 08:11:24 colincrist Exp $
 */

public class CopyBrowserNodeAction extends ActionSupport
{
   private static final Logger log = Logger.getLogger(CopyBrowserNodeAction.class);

   public CopyBrowserNodeAction()
   {
      putValue(Action.NAME, "Duplicate...");
      putValue(Action.SHORT_DESCRIPTION, "Duplicate the sessions/destination configuration.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("copy"));

      setEnabled(false);

      if (!HermesBrowser.getBrowser().isRestricted())
      {
         enableOnBrowserTreeSelection(new Class[] { HermesTreeNode.class, DestinationConfigTreeNode.class }, this, true);
      }
   }

   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
         if (getBrowserTree().getSelectionPath() != null)
         {
            final TreeNode node = (TreeNode) getBrowserTree().getSelectionPath().getLastPathComponent();

            if (node instanceof HermesTreeNode)
            {
               final HermesTreeNode hermesNode = (HermesTreeNode) node;
               final String newName = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Enter new name for this session:", "Copy of "
                     + hermesNode.getHermes().getId());

               if (newName != null && !newName.equals("") && !newName.equals(hermesNode.getHermes().getId()))
               {
                  HermesBrowser.getConfigDAO().duplicateSession(HermesBrowser.getBrowser().getConfig(), hermesNode.getHermes().getId(), newName);

                  HermesBrowser.getBrowser().saveConfig();
                  HermesBrowser.getBrowser().loadConfig();
               }
            }
            else if (node instanceof DestinationConfigTreeNode)
            {
               final DestinationConfigTreeNode destinationNode = (DestinationConfigTreeNode) node;
               final HermesTreeNode hermesNode = (HermesTreeNode) destinationNode.getHermesTreeNode();
               final String newName = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Enter new name for this destination:", "Copy of "
                     + destinationNode.getConfig().getName());

               if (newName != null && !newName.equals("") && !newName.equals(destinationNode.getConfig().getName()))
               {
                  hermesNode.getHermes().addDestinationConfig(HermesBrowser.getConfigDAO().duplicate(destinationNode.getConfig()));
                  
                  HermesBrowser.getBrowser().saveConfig();
                  HermesBrowser.getBrowser().loadConfig();
               }
            }
         }
      }
      catch (Exception ex)
      {
         HermesBrowser.getBrowser().showErrorDialog("Copying object: " + ex.getMessage(), ex);
      }
   }
}
