/* 
 * Copyright 2003,2004,2005,2006 Colin Crist
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
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.Action;


public class ExportSpringXMLAction extends ActionSupport
{
   public ExportSpringXMLAction()
   {
      putValue(Action.NAME, "Export as Spring") ;
      putValue(Action.SHORT_DESCRIPTION, "Export the selected sessions and destinations as Spring") ;
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.export.spring")) ;
      
      setEnabled(false) ;
      
      enableOnBrowserTreeSelection(new Class[] { DestinationConfigTreeNode.class, HermesTreeNode.class }, this, false);

   }

   protected void onHermesConfig(Writer writer, HermesTreeNode node)
   {
      
   }
   
   protected void onDestinationConfig(Writer writer, DestinationConfigTreeNode node)
   {
      
   }
   public void actionPerformed(ActionEvent e)
   {
      
      Writer writer = null ;
      
      Collection<DestinationConfigTreeNode> destinations = HermesBrowser.getBrowser().getBrowserTree().getSelectedDestinationNodes() ;
      Collection<HermesTreeNode> visitedHermes = new HashSet<HermesTreeNode> () ;
      
      for (DestinationConfigTreeNode destination : destinations)
      {
         if (!visitedHermes.contains(destination.getHermesTreeNode()))
         {
            HermesTreeNode hermesNode = (HermesTreeNode) destination.getHermesTreeNode() ;
            onHermesConfig(writer, hermesNode) ;
         }
      }
   }
}
