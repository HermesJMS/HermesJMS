/* 
 * Copyright 2003,2004 Colin Crist
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

package hermes.browser.transferable;

import hermes.browser.components.BrowserTree;
import hermes.fix.FIXMessageTable;
import hermes.swing.actions.ActionRegistry;
import hermes.swing.actions.CopyMessagesToClipboardAction;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: MessageHeaderTransferHandler.java,v 1.1 2005/05/26 17:45:41
 *          colincrist Exp $
 */

public class MessagesTransferHandler extends TransferHandler
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 5563525230406256117L;
private static final Logger log = Logger.getLogger(MessagesTransferHandler.class);
   private FIXMessageTable table;

  
   public static Action getCopyAction()
   {
      return ActionRegistry.getAction(CopyMessagesToClipboardAction.class);
   }

   public MessagesTransferHandler(FIXMessageTable table)
   {
      this.table = table;
   }

   protected Transferable createTransferable(JComponent c)
   {
      return new MessagesTransferable(table.getSelectedMessages());
   }

   public void exportAsDrag(JComponent comp, InputEvent e, int action)
   {
      super.exportAsDrag(comp, e, action);
   }

   public int getSourceActions(JComponent c)
   {
      if (c instanceof FIXMessageTable)
      {
         return COPY;
      }
      else
      {
         return NONE;
      }
   }

   public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
   {
      if (comp instanceof BrowserTree)
      {
         return ((BrowserTree) comp).isCurrentSelectionADestination();
      }
      else
      {
         return false;
      }

   }

   public boolean importData(JComponent comp, Transferable t)
   {
      if (comp instanceof BrowserTree)
      {
         BrowserTree browserTree = (BrowserTree) comp;

         return browserTree.doTransfer(t, TransferHandler.COPY);
      }
      else
      {
         return false;
      }
   }

   protected void exportDone(JComponent comp, Transferable data, int dndAction)
   {
     
   }
}