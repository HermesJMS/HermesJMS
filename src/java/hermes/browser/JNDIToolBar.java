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

import hermes.swing.SwingUtils;
import hermes.swing.actions.ActionRegistry;
import hermes.swing.actions.CreateNewJNDIContextAction;
import hermes.swing.actions.CreateNewSessionFromJNDIAction;
import hermes.swing.actions.JNDIUnbindAction;
import hermes.swing.actions.RefreshJNDIAction;
import hermes.swing.actions.RenameJNDIBindingAction;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DockableBarContext;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JNDIToolBar.java,v 1.3 2005/07/07 10:26:13 colincrist Exp $
 */

public class JNDIToolBar extends CommandBar
{
   public JNDIToolBar()
   {
      super("JNDI");

      setHidable(false);

      getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
      getContext().setInitMode(DockableBarContext.STATE_HORI_DOCKED);

      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(CreateNewSessionFromJNDIAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(CreateNewJNDIContextAction.class)));
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(RenameJNDIBindingAction.class)));
      addSeparator();
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(JNDIUnbindAction.class)));
      addSeparator();
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(RefreshJNDIAction.class)));
   }
}
