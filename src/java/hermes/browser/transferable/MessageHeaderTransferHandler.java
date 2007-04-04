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

import hermes.Domain;
import hermes.HermesException;
import hermes.HermesRuntimeException;
import hermes.browser.HermesBrowser;
import hermes.browser.actions.BrowserAction;
import hermes.browser.components.BrowserTree;
import hermes.browser.components.MessageHeaderTable;
import hermes.swing.actions.ActionRegistry;
import hermes.swing.actions.CopyMessagesToClipboardAction;
import hermes.swing.actions.CutMessagesToClipboardAction;
import hermes.swing.actions.PasteMessagesFromClipboardAction;

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

public class MessageHeaderTransferHandler extends TransferHandler
{
   private static final Logger log = Logger.getLogger(MessageHeaderTransferHandler.class);
   private BrowserAction action;

   public static Action getCutAction()
   {
      return ActionRegistry.getAction(CutMessagesToClipboardAction.class);
   }

   public static Action getCopyAction()
   {
      return ActionRegistry.getAction(CopyMessagesToClipboardAction.class);
   }

   public static Action getPasteAction()
   {
      return ActionRegistry.getAction(PasteMessagesFromClipboardAction.class);
   }

   public MessageHeaderTransferHandler(BrowserAction action)
   {
      this.action = action;
   }

   protected Transferable createTransferable(JComponent c)
   {
      return new JMSMessagesTransferable(new MessageGroup(action.getHermes(), action.getSelectedMessages()));
   }

   public void exportAsDrag(JComponent comp, InputEvent e, int action)
   {
      super.exportAsDrag(comp, e, action);
   }

   public int getSourceActions(JComponent c)
   {
      if (c instanceof MessageHeaderTable)
      {
         return COPY_OR_MOVE;
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
      try
      {
      if (HermesBrowser.getBrowser().getConfig().isCorrectDropSemantics() && action.getDomain() == Domain.QUEUE)
      {
         HermesBrowser.getBrowser().getBrowserTree().setLastDnDAction(dndAction) ;
      }
      else
      {
         HermesBrowser.getBrowser().getBrowserTree().setLastDnDAction(TransferHandler.COPY) ;
      }
      }
      catch (HermesException e)
      {
         throw new HermesRuntimeException(e.getLinkedException()) ;
      }
   }
}