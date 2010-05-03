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

import hermes.Domain;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.config.DestinationConfig;

import javax.swing.Action;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: AddDurableTopicAction.java,v 1.3 2006/02/08 09:17:08 colincrist Exp $
 */

public class AddDurableTopicAction extends AddDestinationAction
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -1595361787706939902L;

public AddDurableTopicAction()
   {
      super(Domain.TOPIC);

      putValue(Action.NAME, "Add durable...");
      putValue(Action.SHORT_DESCRIPTION, "Add a new durable subscripton.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("jms.durableTopic.new"));

   }

   protected void registerTreeSelection()
   {
      enableOnBrowserTreeSelection(new Class[] { HermesTreeNode.class, DestinationConfigTreeNode.class }, this, new TreeSelectionListener()
      {
         public void valueChanged(TreeSelectionEvent e)
         {
            if (e.getPath() != null)
            {
               if (e.getPath().getLastPathComponent() instanceof DestinationConfigTreeNode)
               {
                  final DestinationConfigTreeNode dNode = (DestinationConfigTreeNode) e.getPath().getLastPathComponent();

                  setEnabled(dNode.getDomain() == Domain.TOPIC);
               }
               else
               {
                  setEnabled(e.getPath().getLastPathComponent() instanceof HermesTreeNode) ;
               }
            }
            else
            {
               setEnabled(false) ;
            }
         }
      }, true);
   }

   @Override
   protected DestinationConfig createDestinationConfig()
   {
      final DestinationConfig rval = super.createDestinationConfig();
      final DestinationConfigTreeNode node = HermesBrowser.getBrowser().getBrowserTree().getFirstSelectedDestinationNode() ;
      
      rval.setDomain(Domain.TOPIC.getId());
      rval.setDurable(true);
      
      if (node != null)
      {
         DestinationConfig selectedConfig = node.getConfig();

         if (selectedConfig.getDomain() == Domain.TOPIC.getId())
         {
            rval.setName(selectedConfig.getName());
         }
      }

      return rval;
   }
}
