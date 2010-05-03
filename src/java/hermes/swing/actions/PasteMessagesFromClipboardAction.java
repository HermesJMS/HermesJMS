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
import hermes.MessageFactory;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.BrowserAction;
import hermes.browser.actions.MessageStoreBrowserAction;
import hermes.browser.tasks.AddToMessageStoreTask;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;

import org.apache.log4j.Logger;

/**
 * Paste XML messages from the clipboard to the currently selected queue.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: PasteMessagesFromClipboardAction.java,v 1.1 2005/05/13 15:31:20
 *          colincrist Exp $
 */

public class PasteMessagesFromClipboardAction extends BrowseActionListenerAdapter
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 315578006254064372L;
private static final Logger log = Logger.getLogger(PasteMessagesFromClipboardAction.class);

   public PasteMessagesFromClipboardAction()
   {
      super(false, false, false);

      putValue(Action.NAME, "Paste");
      putValue(Action.SHORT_DESCRIPTION, "Paste any messages from the clipboard to this queue/topic.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("paste"));
      putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_PASTE, 0, false)) ; 

      setEnabled(true);

      try
      {
         //Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(new ClipboardFlavourListener(this, DataFlavor.stringFlavor)) ;
      }
      catch (NoClassDefFoundError ex)
      {
         setEnabled(true) ;
      }
   }

   public void actionPerformed(ActionEvent arg0)
   {
      if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof BrowserAction)
      {
         try
         {
            final BrowserAction browserAction = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
            final Clipboard systemcClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            final Transferable clipboardContent = systemcClipboard.getContents(null);

            if (clipboardContent == null)
            {
               Hermes.ui.getDefaultMessageSink().add("Clipboard is empty");
            }
            else
            {
               if (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))
               {
                  try
                  {
                     final String xml = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);

                     if (browserAction instanceof MessageStoreBrowserAction)
                     {
                        if (HermesBrowser.getBrowser().getBrowserTree().getLastSelectedHermesTreeNode() != null)
                        {
                           final MessageStoreBrowserAction storeAction = (MessageStoreBrowserAction) browserAction;
                           final MessageFactory messageFactory = HermesBrowser.getBrowser().getBrowserTree().getLastSelectedHermesTreeNode().getHermes();
                           HermesBrowser.getBrowser().getThreadPool()
                                 .invokeLater(new AddToMessageStoreTask(messageFactory, storeAction.getMessageStore(), xml));
                        }
                        else
                        {
                           HermesBrowser.getBrowser().showErrorDialog("A session must be selected to use as a factory for the messages") ;                                                    
                        }
                     }
                     else
                     {
                        HermesBrowser.getBrowser().getActionFactory().createSimpleSendMessageAction(browserAction.getHermes(), browserAction.getDestination(),
                              browserAction.getDomain(), xml, false);
                     }
                  }
                  catch (Exception e)
                  {
                     HermesBrowser.getBrowser().showErrorDialog("During paste: ", e);
                  }
               }
               else
               {
                  HermesBrowser.getBrowser().showErrorDialog("The data on the clipboard cannot be converted to a String") ;
               }
            }

         }
         catch (HeadlessException e)
         {
            log.error("Cannot paste: " + e.getMessage(), e);
         }
         catch (Throwable e)
         {
            HermesBrowser.getBrowser().showErrorDialog("During paste: ", e);
         }
      }
   }

   public void valueChanged(ListSelectionEvent event)
   {
      // Override this as we don't care if anything is selected or not.
   }
}
