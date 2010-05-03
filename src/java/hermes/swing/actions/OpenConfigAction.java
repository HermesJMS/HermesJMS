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

import java.awt.event.ActionEvent;
import java.io.FileInputStream;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 * Action to open a new Hermes configuration.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: OpenConfigAction.java,v 1.4 2005/05/24 12:58:36 colincrist Exp $
 */

public class OpenConfigAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -1923859101231554781L;

public OpenConfigAction()
   {
      putValue(Action.NAME, "Open Configuration...");
      putValue(Action.SHORT_DESCRIPTION, "Open another Hermes configuration.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.open")) ;
   }

   public void actionPerformed(ActionEvent arg0)
   {
      JFileChooser chooser;

      if (DirectoryCache.lastOpenConfigDirectory == null)
      {
         chooser = new JFileChooser(System.getProperty("user.dir"));
      }
      else
      {
         chooser = new JFileChooser(DirectoryCache.lastOpenConfigDirectory);
      }

      if (chooser.showDialog(HermesBrowser.getBrowser(), "Open") == JFileChooser.APPROVE_OPTION)
      {
         DirectoryCache.lastOpenConfigDirectory = chooser.getSelectedFile().getParentFile();

         try
         {
            /*
             * Check to see we can parse it first.
             */

            JAXBContext jc = JAXBContext.newInstance("hermes.config");
            Unmarshaller u = jc.createUnmarshaller();

            u.unmarshal(new FileInputStream(chooser.getSelectedFile()));

            HermesBrowser.getBrowser().setCurrentConfig(chooser.getSelectedFile().getAbsolutePath());
            HermesBrowser.getBrowser().loadConfig();
         }
         catch (Exception e)
         {
            HermesBrowser.getBrowser().showErrorDialog("Cannot load configuration: ", e);
         }
      }
   }
}
