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

package hermes.browser;

import hermes.browser.dialog.AboutDialog;
import hermes.swing.SwingUtils;
import hermes.swing.actions.ActionRegistry;
import hermes.swing.actions.AddDurableTopicAction;
import hermes.swing.actions.AddQueueAction;
import hermes.swing.actions.AddTopicAction;
import hermes.swing.actions.CreateNewContextAction;
import hermes.swing.actions.CreateNewMessageStoreAction;
import hermes.swing.actions.CreateNewSessionAction;
import hermes.swing.actions.DeleteBrowserTreeNodeAction;
import hermes.swing.actions.DiscoverDestinationsAction;
import hermes.swing.actions.EditObjectAction;
import hermes.swing.actions.NewPreferencesAction;
import hermes.swing.actions.PreferencesAction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DockableBarContext;
import com.jidesoft.swing.JideButton;

public class ConfigurationToolBar extends CommandBar
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 7106014299869433789L;

public ConfigurationToolBar()
   {
      super("Configuration");
      
      setHidable(false) ;

      getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
      getContext().setInitMode(DockableBarContext.STATE_HORI_DOCKED);

      final JideButton aboutButton = SwingUtils.createToolBarButton("toolbarButtonGraphics/general/About16.gif", "About");

      // No actions for this...

      aboutButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            AboutDialog.showAboutDialog(HermesBrowser.getBrowser());
         }
      });

      
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(CreateNewSessionAction.class))) ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(CreateNewContextAction.class))) ;
      // add(SwingUtils.createToolBarButton(ActionRegistry.getAction(CreateNewJDBCAction.class))) ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(CreateNewMessageStoreAction.class))) ;
      addSeparator() ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(EditObjectAction.class)));
      addSeparator();
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(DiscoverDestinationsAction.class)) ) ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(AddQueueAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(AddTopicAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(AddDurableTopicAction.class))) ;
      addSeparator();
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(DeleteBrowserTreeNodeAction.class))) ;
      addSeparator() ;    
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(PreferencesAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(NewPreferencesAction.class)));

      add(aboutButton);
   }
}
