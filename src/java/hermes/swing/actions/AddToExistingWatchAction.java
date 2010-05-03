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
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.config.WatchConfig;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

/**
 * Add a destination or all the destinations on a session to an existing watch.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: AddToExistingWatchAction.java,v 1.5 2005/12/14 08:11:24
 *          colincrist Exp $
 */

public class AddToExistingWatchAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -5349110632967857914L;
private static final Logger log = Logger.getLogger(AddToExistingWatchAction.class);
   private WatchConfig watchConfig;

   public AddToExistingWatchAction(WatchConfig watchConfig)
   {
      this.watchConfig = watchConfig;

      putValue(Action.NAME, watchConfig.getId());
      putValue(Action.SHORT_DESCRIPTION, "Add selection to " + watchConfig.getId());

      setEnabled(false);

      enableOnBrowserTreeSelection(new Class[] { DestinationConfigTreeNode.class, HermesTreeNode.class }, this, false);
   }

   public void actionPerformed(ActionEvent event)
   {
      List<DestinationConfigTreeNode> nodes = HermesBrowser.getBrowser().getBrowserTree().getSelectedDestinationNodes();

      if (nodes.size() > 0)
      {
         for (DestinationConfigTreeNode node : nodes)
         {
            HermesBrowser.getBrowser().addOrCreateWatch(watchConfig.getId(), HermesBrowser.getBrowser().getBrowserTree().getSelectedHermesNode().getHermes(),
                  node.getConfig());
         }
      }
      else if (HermesBrowser.getBrowser().getBrowserTree().getSelectedHermesNode() != null)
      {

         if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), "Do you want to add all the destinations for this session to the " + watchConfig.getId()
               + " watch window?", "Please confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
         {

            HermesBrowser.getBrowser().addOrCreateWatch(watchConfig.getId(), HermesBrowser.getBrowser().getBrowserTree().getSelectedHermesNode().getHermes());
         }
      }
   }
}
