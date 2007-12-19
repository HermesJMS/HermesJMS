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

import java.awt.event.ActionEvent;

import javax.swing.Action;

public class SelectPersistentSendAction extends ActionSupport
{
   public SelectPersistentSendAction()
   {
      super();

      putValue(Action.NAME, "Persistent");
      putValue(Action.SHORT_DESCRIPTION, "Toggle persistent message send.");

      updateIcon();

   }

   private void updateIcon()
   {
      try
      {
         if (HermesBrowser.getBrowser().getConfig().isDeliveryModePersistent())
         {
            putValue(Action.SMALL_ICON, IconCache.getIcon("send.persistent"));
         }
         else
         {
            putValue(Action.SMALL_ICON, IconCache.getIcon("send.non.persistent"));
         }
      }
      catch (HermesException ex)
      {
         HermesBrowser.getBrowser().showErrorDialog(ex);
      }
   }

   public void actionPerformed(ActionEvent e)
   {
      try
      {
         HermesBrowser.getBrowser().getConfig().setDeliveryModePersistent(!HermesBrowser.getBrowser().getConfig().isDeliveryModePersistent());
         updateIcon();
      }
      catch (HermesException ex)
      {
         HermesBrowser.getBrowser().showErrorDialog(ex);
      }
   }
}
