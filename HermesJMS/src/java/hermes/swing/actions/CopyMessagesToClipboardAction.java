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
import hermes.browser.actions.AbstractFIXBrowserDocumentComponent;
import hermes.browser.actions.BrowserAction;
import hermes.fix.FIXMessage;
import hermes.fix.FIXUtils;
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
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

/**
 * Copy any selected messages to the clipboard as XML.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: CopyMessagesToClipboardAction.java,v 1.11 2006/10/09 19:58:39 colincrist Exp $
 */
public class CopyMessagesToClipboardAction extends BrowseActionListenerAdapter
{
   private static final Logger log = Logger.getLogger(CopyMessagesToClipboardAction.class);
   private XMLHelper xmlHelper = new DefaultXMLHelper() ;
   public CopyMessagesToClipboardAction()
   {
      putValue(Action.NAME, "Copy");
      putValue(Action.SHORT_DESCRIPTION, "Copy selected messages to clipboard");
      putValue(Action.SMALL_ICON, IconCache.getIcon("copy"));
      putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_COPY, 0, false)) ; 
      
      
      setEnabled(false) ;
      addDocumentType(AbstractFIXBrowserDocumentComponent.class) ;
   }

   public void actionPerformed(ActionEvent arg0)
   {
      if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof BrowserAction)
      {

         try
         {
            BrowserAction browserAction = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
            Collection<Message> messages = browserAction.getSelectedMessages();
            Clipboard systemcClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable t = new StringSelection(xmlHelper.toXML(messages));

            systemcClipboard.setContents(t, null);

            Hermes.ui.getDefaultMessageSink().add(messages.size() +  " message" + TextUtils.plural(messages.size()) + " copied to clipboard");
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
      
      if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof AbstractFIXBrowserDocumentComponent)
      {
         AbstractFIXBrowserDocumentComponent browserAction = (AbstractFIXBrowserDocumentComponent) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() ;
         StringBuffer b = new StringBuffer() ; 
         
         for (Object o : browserAction.getSelectedMessages())
         {
            FIXMessage message = (FIXMessage) o ;
            b.append(FIXUtils.prettyPrint(message)) ;
            b.append("\n") ;
            
         }
         
         Clipboard systemcClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         Transferable t = new StringSelection(b.toString());

         systemcClipboard.setContents(t, null);

         Hermes.ui.getDefaultMessageSink().add(browserAction.getSelectedMessages().size() +  " message" + TextUtils.plural(browserAction.getSelectedMessages().size()) + " copied to clipboard");
         
         
      }

   }

}
