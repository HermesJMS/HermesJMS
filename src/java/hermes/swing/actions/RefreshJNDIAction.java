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

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: RefreshJNDIAction.java,v 1.2 2006/10/09 19:58:39 colincrist Exp $
 */

public class RefreshJNDIAction extends JNDIAction
{
   public RefreshJNDIAction()
   {
      putValue(Action.NAME, "Refresh.");
      putValue(Action.SHORT_DESCRIPTION, "Refresh tree from the context.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.jndi.refresh"));
      
      setEnabled(false) ;
   }

   
   @Override
   protected boolean checkEnabled(TreePath path)
   {
     return true ;
   }


   public void valueChanged(TreeSelectionEvent e)
   {
     // NOP
   }

   public void actionPerformed(ActionEvent e)
   {
      final BrowseContextAction browseContext = (BrowseContextAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
      browseContext.update();
   }
}
