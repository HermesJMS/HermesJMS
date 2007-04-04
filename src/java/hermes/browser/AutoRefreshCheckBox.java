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

package hermes.browser;

import hermes.browser.actions.BrowserAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import com.jidesoft.document.DocumentComponentEvent;
import com.jidesoft.document.DocumentComponentListener;

public class AutoRefreshCheckBox extends JCheckBox implements DocumentComponentListener
{
   private static AutoRefreshCheckBox singleton = new AutoRefreshCheckBox();

   private AutoRefreshCheckBox()
   {
      setSelectedIcon(IconCache.getIcon("hermes.messages.autorefresh.off"));
      setIcon(IconCache.getIcon("hermes.messages.autorefresh.on"));
      setEnabled(false);
      setSelected(false);
      setOpaque(false);

      setToolTipText("Enable auto refresh.");

      addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof BrowserAction)
            {
               BrowserAction action = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
               action.setAutoBrowse(isSelected());

               if (isSelected())
               {
                  setToolTipText("Disable auto refresh.");
               }
               else
               {
                  setToolTipText("Enable auto refresh.");
               }
            }
         }
      });
   }

   public static AutoRefreshCheckBox getInstance()
   {
      return singleton;
   }

   protected void checkEnabled(Object object)
   {
      if (object instanceof BrowserAction)
      {
         final BrowserAction browseAction = (BrowserAction) object;

         if (browseAction.isRefreshable())
         {
            setEnabled(true);
            setSelected(browseAction.isAutoBrowse());
         }
         else
         {
            setEnabled(false);
         }
      }
   }

   public void documentComponentMoved(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentMoving(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentActivated(DocumentComponentEvent event)
   {
      checkEnabled(event.getSource());
   }

   public void documentComponentClosed(DocumentComponentEvent event)
   {
      setEnabled(false);
   }

   public void documentComponentClosing(DocumentComponentEvent event)
   {
      setEnabled(false);
   }

   public void documentComponentDeactivated(DocumentComponentEvent event)
   {
      setEnabled(false);
   }

   public void documentComponentOpened(DocumentComponentEvent event)
   {
      checkEnabled(event.getSource());
   }
}
