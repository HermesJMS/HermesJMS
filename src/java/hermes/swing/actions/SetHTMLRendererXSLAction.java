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

import java.awt.event.ActionEvent;

import hermes.Hermes;
import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.tasks.SendMessageTask;

import javax.swing.Action;
import javax.swing.JFileChooser;

/**
 * Helper for actions that send files to queues or topics.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: AbstractSendFileAction.java,v 1.2 2005/05/24 12:58:36
 *          colincrist Exp $
 */

public class SetHTMLRendererXSLAction extends ActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7964360849799569657L;

	public SetHTMLRendererXSLAction() {
		putValue(Action.NAME, "Set XSL");
		putValue(Action.SHORT_DESCRIPTION, "Set the XSL to use for HTML rendering of XML");
		putValue(Action.SMALL_ICON, IconCache.getIcon("xsl"));
		setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = null;

		if (DirectoryCache.lastUploadDirectory == null) {
			chooser = new JFileChooser(System.getProperty("user.dir"));
		} else {
			chooser = new JFileChooser(DirectoryCache.lastUploadDirectory);
		}

		if (chooser.showDialog(HermesBrowser.getBrowser(), "Select XSL...") == JFileChooser.APPROVE_OPTION) {
			DirectoryCache.lastUploadDirectory = chooser.getSelectedFile().getParentFile();
			try {
				HermesBrowser.getBrowser().getConfig().setHTMLRendererXSL(chooser.getSelectedFile().getAbsolutePath());
			} catch (HermesException e1) {
				HermesBrowser.getBrowser().showErrorDialog(e1);
			}
		}
	}

}
