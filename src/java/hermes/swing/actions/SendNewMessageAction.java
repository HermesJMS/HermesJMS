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
import hermes.browser.actions.AbstractEditedMessageHandler;
import hermes.browser.actions.ActionFactory;
import hermes.browser.dialog.message.MessageEditorDialog;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.model.tree.MessageStoreQueueTreeNode;
import hermes.browser.model.tree.MessageStoreTopicTreeNode;
import hermes.browser.model.tree.MessageStoreTreeNode;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.Action;

/**
 * Send a file to the selected queue or topic.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SendTextFileAction.java,v 1.2 2005/05/14 22:53:48 colincrist
 *          Exp $
 */

public class SendNewMessageAction extends ActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5332422858213360837L;

	public SendNewMessageAction() {
		putValue(Action.NAME, "Send New Message");
		putValue(Action.SHORT_DESCRIPTION, "Create a new message...");
		putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.message.new"));
		// putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_S,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false)) ;

		setEnabled(false);
		enableOnBrowserTreeSelection(new Class[] { DestinationConfigTreeNode.class }, this, true);
	}

	public void actionPerformed(ActionEvent arg0) {
		final DestinationConfigTreeNode dNode = getBrowserTree().getSelectedDestinationNodes().get(0);
		final HermesTreeNode hNode = getBrowserTree().getSelectedHermesNode();

		try {
			MessageEditorDialog dialog = new MessageEditorDialog(null, dNode.getDestinationName(), dNode.getDomain(), new AbstractEditedMessageHandler(
					hNode.getHermes()) {
				@Override
				public void onMessage(Message message) {
					HermesBrowser
							.getBrowser()
							.getActionFactory()
							.createMessageCopyAction(hNode.getHermes(), dNode.getDestinationName(), dNode.getDomain(),
									new ArrayList<Message>(Arrays.asList(message)), false);
				}
			});
			dialog.setLocationRelativeTo(HermesBrowser.getBrowser());
			dialog.setVisible(true);
		} catch (JMSException e) {
			HermesBrowser.getBrowser().showErrorDialog(e);
		}

	}
}
