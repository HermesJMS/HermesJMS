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

import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.dialog.EditNamingConfigDialog;
import hermes.config.NamingConfig;
import hermes.config.PropertySetConfig;
import hermes.impl.SimpleClassLoaderManager;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.jidesoft.swing.JideSwingUtilities;

/**
 * Create a new JNDI context
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: CreateNewContextAction.java,v 1.6 2005/06/17 14:35:03 colincrist Exp $
 */

public class CreateNewContextAction extends ActionSupport
{
   private static final Logger log = Logger.getLogger(CreateNewContextAction.class) ;
   
   public CreateNewContextAction()
   {
      putValue(Action.NAME, "New Context...") ;
      putValue(Action.SHORT_DESCRIPTION, "Create new JNDI InitialContext") ;
      putValue(Action.SMALL_ICON, IconCache.getIcon("jndi.context.new")) ;
      
      setEnabled(!HermesBrowser.getBrowser().isRestricted()) ;  
   }
   
   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
          final NamingConfig newConfig = new NamingConfig();
          
          newConfig.setClasspathId(SimpleClassLoaderManager.SYSTEM_LOADER);
          newConfig.setProperties(new PropertySetConfig());

          final String newName = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Name:", "Enter InitialContext Name", JOptionPane.PLAIN_MESSAGE);

          if ( newName != null && !newName.equals(""))
          {
              newConfig.setId(newName);

              EditNamingConfigDialog dialog = new EditNamingConfigDialog(HermesBrowser.getBrowser(), newConfig, HermesBrowser.getBrowser().getConfig()
                      .getNaming());

              dialog.pack();
              JideSwingUtilities.centerWindow(dialog);
              dialog.show();
          }
      }
      catch (HermesException e)
      {
          log.error(e.getMessage(), e);

          HermesBrowser.getBrowser().showErrorDialog(e);
      }
   }
}
