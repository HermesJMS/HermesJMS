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

import hermes.browser.IconCache;
import hermes.browser.tasks.SendMessageTask;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * Send jms2xml.xsd format file to currently selected queue or topic.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SendXMLFileAction.java,v 1.4 2007/01/14 15:32:22 colincrist Exp $
 */

public class SendXMLFileAction extends AbstractSendFileAction
{
   public SendXMLFileAction()
   {
      putValue(Action.NAME, "Send XML Encoded Messages");
      putValue(Action.SHORT_DESCRIPTION, "Send a group of messages encoded as XML.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.send.xml")) ;
      putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false)) ; 
      
   }

   public void actionPerformed(ActionEvent arg0)
   {
      doSendAFile(SendMessageTask.IS_XML, false) ;
   }
}
