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
import hermes.browser.components.NavigableComponent;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.apache.log4j.Logger;

/**
 * Simple action to move the selection forward in a queue/topic browser
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: NavigateForwardMessageAction.java,v 1.3 2005/05/23 15:13:41
 *          colincrist Exp $
 */

public class NavigateForwardMessageAction extends BrowseActionListenerAdapter
{
   private static final Logger log = Logger.getLogger(NavigateForwardMessageAction.class);

   public NavigateForwardMessageAction()
   {
      super(true, false, false);
      putValue(Action.NAME, "Next");
      putValue(Action.SHORT_DESCRIPTION, "Next message...");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.messages.navigate.forward"));

      setEnabled(false);
   }

   @Override
   protected void checkEnabled(Object object)
   {
      if (object instanceof NavigableComponent)
      {
         final NavigableComponent component = (NavigableComponent) object;

         setEnabled(component != null && component.isNavigableForward());
      }
      else
      {
         setEnabled(false);
      }
   }

   public void actionPerformed(ActionEvent e)
   {
      if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof NavigableComponent)
      {
         final NavigableComponent component = (NavigableComponent) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
         component.navigateForward() ;
      }
   }
}
