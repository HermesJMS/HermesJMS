/* 
 * Copyright 2003,2004 Colin Crist
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

package hermes.browser;

import hermes.browser.components.PopupMenuFactory;
import hermes.browser.dialog.AboutDialog;
import hermes.swing.actions.ActionRegistry;
import hermes.swing.actions.OpenConfigAction;
import hermes.swing.actions.OpenFIXFileAction;
import hermes.swing.actions.OpenFIXURLAction;
import hermes.swing.actions.OpenXMLFileAction;
import hermes.swing.actions.PreferencesAction;
import hermes.swing.actions.SaveConfigAction;
import hermes.swing.actions.SaveMessagesAsTextAction;
import hermes.swing.actions.SaveMessagesAsXMLAction;
import hermes.swing.actions.SaveMessagesIndividuallyAsXMLAction;
import hermes.swing.actions.SendTextFileAction;
import hermes.swing.actions.SendXMLFileAction;
import hermes.swing.actions.SetHTMLRendererXSLAction;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import com.jidesoft.action.CommandBarFactory;
import com.jidesoft.action.CommandMenuBar;
import com.jidesoft.swing.JideMenu;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: MenuBar.java,v 1.22 2007/01/14 15:32:24 colincrist Exp $
 */
public class MenuBar extends CommandMenuBar {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9141225979536359572L;
	private static final Logger log = Logger.getLogger(MenuBar.class);
	private HermesBrowser hermesBrowser;

	private JideMenu fileMenu = new JideMenu("File");
	private JideMenu messageMenu = new JideMenu("Messages");
	private JideMenu actionsMenu = new JideMenu("Actions");
	private JideMenu toolMenu = new JideMenu("Options");
	private JideMenu helpMenu = new JideMenu("Help");

	private JMenuItem sendAFile = new JMenuItem();
	private JMenuItem sendXMLFile = new JMenuItem();

	private JMenuItem saveConfig = new JMenuItem(new SaveConfigAction());
	private JMenuItem openXML = new JMenuItem(new OpenXMLFileAction());
	private JMenuItem exit = new JMenuItem("Exit");
	private JMenuItem prefs = new JMenuItem(new PreferencesAction());
	private JMenuItem openConfig = new JMenuItem(new OpenConfigAction());
	private JMenuItem loadLayout = new JMenuItem("Load layout");
	private JMenuItem saveLayout = new JMenuItem("Save layout");
	private JMenuItem resetLayout = new JMenuItem("Reset layout");
	private JMenuItem help = new JMenuItem("Help");
	private JMenuItem about = new JMenuItem("About Hermes");

	public MenuBar(HermesBrowser hermesBrowser) {
		this.hermesBrowser = hermesBrowser;

		setStretch(true);
		init();
	}

	private void init() {
		sendAFile.setAction(ActionRegistry.getAction(SendTextFileAction.class));
		sendXMLFile.setAction(ActionRegistry.getAction(SendXMLFileAction.class));

		exit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(1);
			}
		});

		messageMenu.add(sendXMLFile);
		messageMenu.add(sendAFile);

		fileMenu.add(openConfig);
		fileMenu.add(openXML);
		fileMenu.add(ActionRegistry.getAction(OpenFIXFileAction.class));
		fileMenu.add(ActionRegistry.getAction(OpenFIXURLAction.class));
		fileMenu.addSeparator();
		fileMenu.add(saveConfig);
		fileMenu.addSeparator();
		fileMenu.add(exit);

		toolMenu.add(saveLayout);
		toolMenu.add(loadLayout);
		toolMenu.add(resetLayout);
		toolMenu.add(prefs);

		toolMenu.add(createLnfMenu());

		helpMenu.add(help);
		helpMenu.addSeparator();
		helpMenu.add(about);

		messageMenu.add(new JMenuItem(ActionRegistry.getAction(SaveMessagesAsTextAction.class)));
		messageMenu.add(new JMenuItem(ActionRegistry.getAction(SaveMessagesAsXMLAction.class)));
		messageMenu.add(new JMenuItem(ActionRegistry.getAction(SaveMessagesIndividuallyAsXMLAction.class)));
		messageMenu.add(new JMenuItem(ActionRegistry.getAction(SetHTMLRendererXSLAction.class)));
		
		final Component[] components = PopupMenuFactory.createBrowserTreePopup(hermesBrowser.getBrowserTree()).getComponents();

		for (int i = 0; i < components.length; i++) {
			actionsMenu.add(components[i]);
		}

		loadLayout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				log.debug("loading layout profile as " + hermesBrowser.getUserProfileName());

				hermesBrowser.getLayoutPersistence().setProfileKey(hermesBrowser.getUserProfileName());
				hermesBrowser.getLayoutPersistence().loadLayoutData();
			}
		});

		saveLayout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				log.debug("saving layout profile as " + hermesBrowser.getUserProfileName());

				hermesBrowser.getLayoutPersistence().setProfileKey(hermesBrowser.getUserProfileName());
				hermesBrowser.getLayoutPersistence().saveLayoutData();
			}
		});

		resetLayout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				log.debug("resetting layout profile for " + hermesBrowser.getUserProfileName());

				hermesBrowser.getLayoutPersistence().setProfileKey(hermesBrowser.getUserProfileName());
				hermesBrowser.getLayoutPersistence().resetToDefault();
			}
		});

		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BrowserControl.displayURL(AboutDialog.HERMES_SOURCEFORGE);
			}
		});

		about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AboutDialog.showAboutDialog(hermesBrowser);
			}
		});

		add(fileMenu);
		add(messageMenu);
		add(actionsMenu);
		add(toolMenu);
		add(helpMenu);
	}

	private JMenu createLnfMenu() {
		return CommandBarFactory.createLookAndFeelMenu(hermesBrowser);
	}

}