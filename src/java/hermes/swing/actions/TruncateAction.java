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
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.model.tree.RepositoryTreeNode;

import java.awt.event.ActionEvent;

import javax.jms.JMSException;
import javax.swing.Action;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * Truncate a queue or file repository.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id $
 */

public class TruncateAction extends ActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 139535445442209172L;

	public TruncateAction() {
		putValue(Action.NAME, "Truncate");
		putValue(Action.SHORT_DESCRIPTION, "Truncate a queue, durable subscription or file.");
		putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.queue.truncate"));

		setEnabled(false);

		if (!HermesBrowser.getBrowser().isRestricted()) {
			enableOnBrowserTreeSelection(new Class[] { DestinationConfigTreeNode.class, RepositoryTreeNode.class }, this, new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					if (getBrowserTree().hasSelection() && getBrowserTree().getLastSelectedPathComponent() instanceof DestinationConfigTreeNode) {
						final DestinationConfigTreeNode treeNode = (DestinationConfigTreeNode) getBrowserTree().getSelectionPath().getLastPathComponent();

						setEnabled(treeNode.isQueue() || (treeNode.getConfig().isDurable()));
					}
				}
			}, true);
		}
	}

	public void actionPerformed(ActionEvent event) {
		if (getBrowserTree().getSelectionPath().getLastPathComponent() instanceof DestinationConfigTreeNode) {
			final DestinationConfigTreeNode destinationNode = (DestinationConfigTreeNode) getBrowserTree().getSelectionPath().getLastPathComponent();

			if (destinationNode != null) {
				HermesTreeNode hermesNode = (HermesTreeNode) destinationNode.getHermesTreeNode();

				try {
					BrowserAction browserAction = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
					HermesBrowser.getBrowser().getActionFactory().createTruncateAction(hermesNode.getHermes(), destinationNode.getConfig(), browserAction);
				} catch (JMSException ex) {
					HermesBrowser.getBrowser().showErrorDialog("Cannot truncate: ", ex);
				}
			}
		} else if (HermesBrowser.getBrowser().getBrowserTree().getSelectionPath().getLastPathComponent() instanceof RepositoryTreeNode) {
			final RepositoryTreeNode repositoryNode = (RepositoryTreeNode) getBrowserTree().getSelectionPath().getLastPathComponent();

			repositoryNode.getRepository().delete();

			Hermes.ui.getDefaultMessageSink().add("Repository " + repositoryNode.getRepository().getId() + " truncated");
		}
	}
}
