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
import hermes.util.TextUtils;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Action;
import javax.swing.JOptionPane;

/**
 * Open and browse a file containing raw FIX messages
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: OpenFIXURLAction.java,v 1.3 2006/08/01 07:29:36 colincrist Exp $
 */

public class OpenFIXURLAction extends ActionSupport
{
   private String previousURL ;
   
   public OpenFIXURLAction()
   {
      putValue(Action.NAME, "Open FIX URL...");
      putValue(Action.SHORT_DESCRIPTION, "Open a URL referencing FIX messages");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.file.fix"));

      setEnabled(true);
   }

   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
         final String url = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "URL", previousURL);

         previousURL = url ;
         
         if (!TextUtils.isEmpty(url))
         { 
            int maxCachedMessages = HermesBrowser.getBrowser().getMaxMessagesInBrowserPane();
         
            new FIXFileBrowserAction(new URL(url), maxCachedMessages).start() ;
         }
         else
         {
            Hermes.ui.getDefaultMessageSink().add("URL open cancelled");
         }
      }
      catch (Exception ex)
      {
         HermesBrowser.getBrowser().showErrorDialog("Unable to open: ", ex);
      }
   }
}
