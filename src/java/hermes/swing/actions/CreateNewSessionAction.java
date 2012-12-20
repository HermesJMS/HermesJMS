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
import hermes.browser.dialog.PreferencesDialog;
import hermes.browser.dialog.SessionConfigPanel;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.apache.log4j.Logger;

import com.jidesoft.swing.JideSwingUtilities;

/**
 * Create a new JMS session.
 *
 * @author colincrist@hermesjms.com
 * @version $Id: CreateNewContextAction.java,v 1.1 2005/05/14 22:53:48
 *          colincrist Exp $
 */

public class CreateNewSessionAction extends ActionSupport
{
   /**
	 *
	 */
	private static final long serialVersionUID = 5744748734635515190L;
private static final Logger log = Logger.getLogger(CreateNewSessionAction.class);

   public CreateNewSessionAction()
   {
      putValue(Action.NAME, "New session...");
      putValue(Action.SHORT_DESCRIPTION, "Create new JMS session.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("jms.connectionFactory.new"));

      setEnabled(!HermesBrowser.getBrowser().isRestricted()) ;
   }

   public void actionPerformed(ActionEvent arg0)
   {
      final PreferencesDialog dialog = new PreferencesDialog(HermesBrowser.getBrowser());

      dialog.init();
      dialog.getDestinationConfigPanel().reset();
      dialog.refocus(SessionConfigPanel.NEWSESSION);
      JideSwingUtilities.centerWindow(dialog);
      dialog.show();

   }
}
