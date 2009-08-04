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
import hermes.swing.FilterInputPanel;

import java.awt.event.ActionEvent;

import javax.swing.Action;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ToggleFilterInputPanelAction.java,v 1.1 2006/05/06 17:23:13 colincrist Exp $
 */

public class ToggleFilterInputPanelAction extends ActionSupport
{
   private FilterInputPanel filterInputPanel = new FilterInputPanel() ;
   
   public ToggleFilterInputPanelAction()
   {
      super();
      
      putValue(Action.NAME, "Toggle filter...");
      putValue(Action.SHORT_DESCRIPTION, "Show or hide the SQL filter window");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.filter.sql"));
     
      HermesBrowser.getBrowser().getDockingManager().addFrame(filterInputPanel) ;
      HermesBrowser.getBrowser().addDocumentComponentListener(filterInputPanel) ;
   }
  

   public void actionPerformed(ActionEvent e)
   {
     
      if (filterInputPanel.isHidden())
      {
         HermesBrowser.getBrowser().getDockingManager().showFrame(filterInputPanel.getTitle()) ;
      }
      else
      {
         HermesBrowser.getBrowser().getDockingManager().hideFrame(filterInputPanel.getTitle()) ;
      }

   }

}
