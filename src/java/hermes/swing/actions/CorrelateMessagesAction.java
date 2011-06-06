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
import hermes.browser.actions.QueueBrowseAction;
import hermes.browser.components.BrowserTree;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.config.DestinationConfig;

import java.awt.event.ActionEvent;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;

import javax.jms.Message;
import javax.swing.Action;

/**
 * Action to search a queue, topic or all destinations on a session.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class CorrelateMessagesAction extends ActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3691177566297585905L;
	private boolean searchUserHeader = true;

	public CorrelateMessagesAction() {
		putValue(Action.NAME, "Correlate...");
		putValue(Action.SHORT_DESCRIPTION, "Correlate JMSMessageID and JMSCorrelationID");
//		putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.browse.filter"));
		setEnabled(true) ;	
	}

	

	public void actionPerformed(ActionEvent arg0) {
		final BrowserTree browserTree = HermesBrowser.getBrowser().getBrowserTree();
		try {
			if (browserTree.getSelectionPath().getLastPathComponent() instanceof DestinationConfigTreeNode) {
				final DestinationConfigTreeNode destinationNode = (DestinationConfigTreeNode) browserTree.getSelectionPath().getLastPathComponent();
				final HermesTreeNode hermesNode = (HermesTreeNode) destinationNode.getHermesTreeNode();
				final BrowserAction browserAction = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();
				if (browserAction != null) {
					final Collection<Message> messages = browserAction.getSelectedMessages();
					if (messages.size() > 0) {
						final StringWriter selector = new StringWriter();
						for (Iterator<Message> iter = messages.iterator(); iter.hasNext();) {
							Message message = iter.next();
							selector.append("JMSMessageID = '" + message.getJMSMessageID() + "' ");
							selector.append("OR JMSCorrelationID = '" + message.getJMSMessageID() + "'");
							if (message.getJMSCorrelationID() != null) {
								selector.append("OR JMSMessageID = '" + message.getJMSMessageID() + "'");
								selector.append("OR JMSCorrelationID = '" + message.getJMSCorrelationID() + "'");

							}

							if (iter.hasNext()) {
								selector.append(" OR ");
							}
						}

						final DestinationConfig dConfig = HermesBrowser.getConfigDAO().duplicate(destinationNode.getConfig());
						dConfig.setSelector(selector.toString());

						HermesBrowser.getBrowser().getActionFactory().createQueueBrowseAction(hermesNode.getHermes(), dConfig);
					}
				}
			}
		} catch (Exception ex) {
			HermesBrowser.getBrowser().showErrorDialog(ex);
		}
	}
}
