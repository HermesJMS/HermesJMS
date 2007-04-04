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
import hermes.browser.actions.BrowseContextAction;
import hermes.browser.actions.BrowserAction;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * Stop and restart the currently selected browse action.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: RefreshBrowseAction.java,v 1.5 2006/10/09 19:58:39 colincrist Exp $
 */

public class RefreshBrowseAction extends BrowseActionListenerAdapter
{
   public RefreshBrowseAction()
   {
      super(false, false, true);
      putValue(Action.NAME, "Refresh");
      putValue(Action.SHORT_DESCRIPTION, "Refresh now.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.messages.refresh"));
      putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0 , false)) ; 

      setEnabled(false);
   }

   public void actionPerformed(ActionEvent event)
   {
     
      if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof BrowserAction)
      {
         final BrowserAction action = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();

         action.refresh();
      }
      else if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof BrowseContextAction)
      {
         final BrowseContextAction action = (BrowseContextAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() ;
                 
         action.update() ;
      }
   }
}
