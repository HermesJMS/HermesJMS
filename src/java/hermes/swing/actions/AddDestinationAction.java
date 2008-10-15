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
import hermes.browser.dialog.DestinationConfigDialog;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.config.DestinationConfig;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.jidesoft.swing.JideSwingUtilities;

/**
 * Add a new queue or topic.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: AddDestinationAction.java,v 1.1 2005/05/14 22:53:48 colincrist
 *          Exp $
 */

public abstract class AddDestinationAction extends ActionSupport
{
   private static final Logger log = Logger.getLogger(AddDestinationAction.class);
   private Domain domain;

   public AddDestinationAction(Domain domain)
   {
      this.domain = domain;

      setEnabled(false);

      if (!HermesBrowser.getBrowser().isRestricted())
      {
         registerTreeSelection() ;
      }
   }
   
   protected void registerTreeSelection()
   {
      enableOnBrowserTreeSelection(new Class[] { HermesTreeNode.class }, this, true);
   }

   protected DestinationConfig createDestinationConfig()
   {
      return new DestinationConfig();
   }
   
   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
         final HermesTreeNode hermesNode = HermesBrowser.getBrowser().getBrowserTree().getSelectedHermesNode();
         final DestinationConfig config = createDestinationConfig() ;

         config.setDomain(domain.getId());

         final DestinationConfigDialog dialog = new DestinationConfigDialog(HermesBrowser.getBrowser(), hermesNode.getHermes().getId(), null, config);

         dialog.addOKAction(new Runnable()
         {
            public void run()
            {
               try
               {
                  if (config.getName() != null)
                  {
                     HermesBrowser.getBrowser().addDestinationConfig(hermesNode.getHermes(), config);
                     HermesBrowser.getBrowser().saveConfig();                    
                  }
               }
               catch (Exception e)
               {
                  log.error(e.getMessage(), e);

                  JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
               }
            }
         });

         dialog.pack();
         JideSwingUtilities.centerWindow(dialog);
         dialog.show();

      }
      catch (Exception ex)
      {
         log.error(ex.getMessage(), ex);

         JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }

   }
}
