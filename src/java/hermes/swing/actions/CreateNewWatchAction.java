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
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

/**
 * Create a new watch dockable frame...
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: CreateNewWatchAction.java,v 1.5 2005/12/14 08:11:24 colincrist
 *          Exp $
 */

public class CreateNewWatchAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -5393454892723919058L;
private static final Logger log = Logger.getLogger(CreateNewWatchAction.class);

   public CreateNewWatchAction()
   {
      putValue(Action.NAME, "New Watch...");
      putValue(Action.SHORT_DESCRIPTION, "Create a new watch frame.");

      setEnabled(false);

      enableOnBrowserTreeSelection(new Class[] { DestinationConfigTreeNode.class, HermesTreeNode.class }, this, true);
   }

   public void actionPerformed(ActionEvent event)
   {
      List<DestinationConfigTreeNode> nodes = getBrowserTree().getSelectedDestinationNodes();

      if (nodes.size() > 0)
      {
         String namne = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Enter name for this watch tab:", null);

         if (namne != null && !namne.equals(""))
         {
            for (DestinationConfigTreeNode node : nodes)
            {
               HermesBrowser.getBrowser().addOrCreateWatch(namne, HermesBrowser.getBrowser().getBrowserTree().getSelectedHermesNode().getHermes(),
                     node.getConfig());
            }
         }
      }
      else if (getBrowserTree().getSelectedHermesNode() != null)
      {
         final Hermes hermes = getBrowserTree().getSelectedHermesNode().getHermes();

         String namne = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Enter name for this watch tab:", hermes.getId());

         if (namne != null && !namne.equals(""))
         {
            HermesBrowser.getBrowser().addOrCreateWatch(namne, hermes);
         }
      }
   }
}
