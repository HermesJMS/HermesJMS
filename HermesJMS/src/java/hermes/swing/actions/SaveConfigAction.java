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
import hermes.Hermes;
import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.apache.log4j.Logger;

/**
 * Save the current configuration.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: AddDestinationAction.java,v 1.1 2005/05/14 22:53:48 colincrist
 *          Exp $
 */

public class SaveConfigAction extends ActionSupport
{
   private static final Logger log = Logger.getLogger(SaveConfigAction.class);
   private Domain domain;

   public SaveConfigAction()
   {
      putValue(Action.NAME, "Save Settings");
      putValue(Action.SHORT_DESCRIPTION, "Save current configuration.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes/browser/icons/save.gif"));
      
   }
  
   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
         HermesBrowser.getBrowser().backupConfig() ;
      }
      catch (HermesException e)
      {
         log.error("Cannot backup configuration:" + e.getMessage(), e) ;
      }
      
      try
      {
         HermesBrowser.getBrowser().saveConfig() ;
         Hermes.ui.getDefaultMessageSink().add("Configuration saved.") ;
      }
      catch (HermesException e)
      {
        HermesBrowser.getBrowser().showErrorDialog("Unable to save configuration" , e);
      }
   }
}
