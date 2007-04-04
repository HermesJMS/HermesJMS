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

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;

import javax.swing.Action;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ClipboardFlavourListener.java,v 1.1 2006/08/08 18:01:19 colincrist Exp $
 */

public class ClipboardFlavourListener implements FlavorListener
{
   private Action action ;
   private DataFlavor flavor ;
   
   public ClipboardFlavourListener(Action action,DataFlavor flavor)
   {
      this.action = action ;
      this.flavor = flavor ;
   }
   
   public void flavorsChanged(FlavorEvent arg0)
   {
      try
      {
         if (Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(flavor))
         {
            final Transferable clipboardContent = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

            action.setEnabled(clipboardContent != null && clipboardContent.isDataFlavorSupported(flavor));
         }
         else
         {
            action.setEnabled(false);
         }
      }
      catch (IllegalStateException ex)
      {
         action.setEnabled(false);
      }
   }
}
