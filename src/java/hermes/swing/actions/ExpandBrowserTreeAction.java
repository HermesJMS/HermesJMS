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
import hermes.browser.components.BrowserTree;
import hermes.browser.model.TreeUtils;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class ExpandBrowserTreeAction extends ActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2848811547735769952L;
	private final BrowserTree browserTree;

	public ExpandBrowserTreeAction(BrowserTree browserTree) {
		this.browserTree = browserTree;
		putValue(Action.NAME, "Expand");
		putValue(Action.SHORT_DESCRIPTION, "Expand tree.");
		putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.expand.all"));

		setEnabled(false);

		browserTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				ExpandBrowserTreeAction.this.setEnabled(e.getPath() != null);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		TreeUtils.expandFully(browserTree, browserTree.getSelectionPath());

	}
}
