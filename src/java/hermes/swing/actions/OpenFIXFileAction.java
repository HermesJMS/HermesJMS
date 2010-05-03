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
import hermes.browser.IconCache;
import hermes.browser.actions.FIXFileBrowserAction;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

/**
 * Open and browse a file containing raw FIX messages
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: OpenFIXFileAction.java,v 1.5 2006/10/09 19:58:39 colincrist Exp $
 */

public class OpenFIXFileAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 1265030833566439864L;

public OpenFIXFileAction()
   {
      putValue(Action.NAME, "Open FIX File...");
      putValue(Action.SHORT_DESCRIPTION, "Open a file containing FIX messages");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.file.fix"));
      putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false)) ; 
      
      setEnabled(true);
   }

   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
         JFileChooser chooser = null;
         
         if (DirectoryCache.lastUploadDirectory == null)
         {
            chooser = new JFileChooser(System.getProperty("user.dir"));
         }
         else
         {
            chooser = new JFileChooser(DirectoryCache.lastUploadDirectory);
         }
         
         if (chooser.showDialog(HermesBrowser.getBrowser(), "Open") == JFileChooser.APPROVE_OPTION)
         {
            DirectoryCache.lastUploadDirectory = chooser.getSelectedFile().getParentFile();
            int maxCachedMessages = HermesBrowser.getBrowser().getMaxMessagesInBrowserPane();
            
            new FIXFileBrowserAction(chooser.getSelectedFile(), maxCachedMessages).start() ;
           
         }
         else
         {
            Hermes.ui.getDefaultMessageSink().add("File open cancelled");
         }
      }
      catch (Exception ex)
      {
         HermesBrowser.getBrowser().showErrorDialog("Unable to open: ", ex);
      }

   }
}
