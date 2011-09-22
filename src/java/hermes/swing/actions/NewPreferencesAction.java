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
import hermes.browser.dialog.general.PreferencesDialog;

import java.awt.event.ActionEvent;

import javax.swing.Action;

/**
 * Preferences Action.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: PreferencesAction.java,v 1.5 2005/06/17 14:35:03 colincrist Exp
 *          $
 */

public class NewPreferencesAction extends ActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5628725407722664279L;

	public NewPreferencesAction() {
		putValue(Action.NAME, "Preferences...");
		putValue(Action.SHORT_DESCRIPTION, "Preferences.");
		putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.config"));

		setEnabled(!HermesBrowser.getBrowser().isRestricted());
	}

	public void actionPerformed(ActionEvent arg0) {
		try {
			PreferencesDialog dialog = new PreferencesDialog();
			dialog.setVisible(true);
		} catch (Exception ex) {
			HermesBrowser.getBrowser().showErrorDialog(ex);
		}
	}
}
