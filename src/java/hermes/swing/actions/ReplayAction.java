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
import hermes.browser.dialog.replay.ReplayMessagesDialog;
import hermes.browser.model.tree.MessageStoreQueueTreeNode;
import hermes.browser.model.tree.MessageStoreTopicTreeNode;
import hermes.browser.model.tree.MessageStoreTreeNode;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * Replay messages from a store.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class ReplayAction extends ActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2261691717762103310L;
	private static final Logger log = Logger.getLogger(ReplayAction.class);
	private String lastHermesId;
	private String lastDestination;

	public ReplayAction() {
		putValue(Action.NAME, "Replay...");
		putValue(Action.SHORT_DESCRIPTION, "Replay messages....");
		putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.replay"));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

		setEnabled(false);

		enableOnBrowserTreeSelection(new Class[] { MessageStoreQueueTreeNode.class, MessageStoreTopicTreeNode.class, MessageStoreTreeNode.class, }, this, true);
	}

	public void actionPerformed(ActionEvent e) {
		actionPerformed(getBrowserTree().getSelectionModel().getSelectionPath());
	}

	public void actionPerformed(TreePath selectionPath) {

		if (selectionPath != null) {
			try {
				if (selectionPath.getLastPathComponent() instanceof MessageStoreTreeNode) {
					final MessageStoreTreeNode node = (MessageStoreTreeNode) selectionPath.getLastPathComponent();
					ReplayMessagesDialog dialog = new ReplayMessagesDialog(node.getMessageStore(), lastHermesId, lastDestination);

					dialog.setLocationRelativeTo(HermesBrowser.getBrowser());
					dialog.setModal(true);
					dialog.setVisible(true);

					lastHermesId = dialog.getHermesId();
					lastDestination = dialog.getDestination();
				}
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);

				JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), "During replay: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
