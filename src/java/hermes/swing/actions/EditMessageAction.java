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
import hermes.browser.actions.BrowserAction;
import hermes.browser.actions.MessageStoreBrowserAction;
import hermes.browser.actions.RepositoryFileBrowserAction;

import java.awt.event.ActionEvent;

import javax.swing.Action;

/**
 * Send a file to the selected queue or topic.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SendTextFileAction.java,v 1.2 2005/05/14 22:53:48 colincrist
 *          Exp $
 */

public class EditMessageAction extends BrowseActionListenerAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5332422858213360837L;

	public EditMessageAction() {
		putValue(Action.NAME, "Edit Message");
		putValue(Action.SHORT_DESCRIPTION, "Edit message...");
		putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.message.edit"));
		// putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_S,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false)) ;

		setEnabled(false);
	}

	@Override
	protected void checkEnabled(Object object) {
		setEnabled(object instanceof MessageStoreBrowserAction || object instanceof RepositoryFileBrowserAction);
	}

	public void actionPerformed(ActionEvent arg0) {

		if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof BrowserAction) {
			BrowserAction action = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
			action.getMessageHeaderTable().onDoubleClick();
		}

	}
}
