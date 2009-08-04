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

import hermes.swing.SwingUtils;
import hermes.swing.actions.ActionRegistry;
import hermes.swing.actions.BrowseDestinationOrContextAction;
import hermes.swing.actions.BrowseDestinationWithSelectorAction;
import hermes.swing.actions.GetDestinationStatisticsAction;
import hermes.swing.actions.OpenConfigAction;
import hermes.swing.actions.OpenFIXFileAction;
import hermes.swing.actions.OpenXMLFileAction;
import hermes.swing.actions.SearchDestinationOrContextAction;
import hermes.swing.actions.SendTextFileAction;
import hermes.swing.actions.SendXMLFileAction;
import hermes.swing.actions.StopAllTasksAction;

import org.apache.log4j.Category;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DockableBarContext;

/**
 * The main toolbar
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: MainToolBar.java,v 1.28 2006/07/13 07:35:32 colincrist Exp $
 */

public class MainToolBar extends CommandBar
{
   private static final Category cat = Category.getInstance(MainToolBar.class);

   /**
    * BrowserToolBar constructor.
    */
   public MainToolBar()
   {
      super("Main");

      setHidable(false);

      getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
      getContext().setInitMode(DockableBarContext.STATE_HORI_DOCKED);

      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(OpenConfigAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(OpenXMLFileAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(OpenFIXFileAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(StopAllTasksAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(BrowseDestinationOrContextAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(BrowseDestinationWithSelectorAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(SearchDestinationOrContextAction.class)));
      //add(SwingUtils.createToolBarButton(ActionRegistry.getAction(ToggleFilterInputPanelAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(GetDestinationStatisticsAction.class)));
      addSeparator();
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(SendTextFileAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(SendXMLFileAction.class)));
   }
}