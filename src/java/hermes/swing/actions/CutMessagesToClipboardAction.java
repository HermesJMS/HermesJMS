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
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.BrowserAction;
import hermes.browser.actions.MessageStoreBrowserAction;
import hermes.impl.DefaultXMLHelper;
import hermes.impl.XMLHelper;
import hermes.util.TextUtils;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

/**
 * Copy any selected messages to the clipboard as XML and remove them from the
 * queue.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: CutMessagesToClipboardAction.java,v 1.5 2005/05/23 15:13:41
 *          colincrist Exp $
 */

public class CutMessagesToClipboardAction extends BrowseActionListenerAdapter
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 8018363610821830799L;
private static final Logger log = Logger.getLogger(CutMessagesToClipboardAction.class);
   private XMLHelper xml = new DefaultXMLHelper() ;
   
   public CutMessagesToClipboardAction()
   {
      putValue(Action.NAME, "Cut");
      putValue(Action.SHORT_DESCRIPTION, "Cut selected messages to clipboard, delete them from the queue.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("cut"));
      putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_CUT, 0, false)) ; 

      setEnabled(false);
   }

   public void actionPerformed(ActionEvent arg0)
   {
      if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof BrowserAction)
      {

         try
         {
            BrowserAction browserAction = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();

            final String message = browserAction.getSelectedMessages().size() == 1 ? "You are about to copy this message to the clipboard and delete it from "
                  : "You are about to copy " + browserAction.getSelectedMessages().size() + " messages to the clipboard and delete them from ";

            if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), message + browserAction.getDestination() + " - are you sure ?", "Warning",
                  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
               Collection<Message> messages = browserAction.getSelectedMessages();
               Clipboard systemcClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
               Transferable t = new StringSelection(xml.toXML(messages));

               systemcClipboard.setContents(t, null);

               Hermes.ui.getDefaultMessageSink().add(messages.size() + TextUtils.plural(messages.size()) + " messages copied to clipboard, now deleting.");

               if (browserAction instanceof MessageStoreBrowserAction)
               {
                  final MessageStoreBrowserAction storeAction = (MessageStoreBrowserAction) browserAction;

                  HermesBrowser.getBrowser().getActionFactory().createDeleteFromMessageStoreAction(storeAction.getMessageStore(),
                        browserAction.getSelectedMessages(), false);
               }
               else
               {
                  HermesBrowser.getBrowser().getActionFactory().createTruncateAction(browserAction.getHermes(), browserAction.getConfig(),
                        browserAction.getSelectedMessageIDs(),  false);
               }
            }
            else
            {
               Hermes.ui.getDefaultMessageSink().add("Cut from " + browserAction.getDestination() + " cancelled");
            }
         }
         catch (HeadlessException e)
         {
            log.error("doCopy(): " + e.getMessage(), e);
         }
         catch (JMSException e)
         {
            HermesBrowser.getBrowser().showErrorDialog("During copy: ", e);
         }
      }

   }

}
