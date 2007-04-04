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
import hermes.swing.actions.CopyMessagesToClipboardAction;
import hermes.swing.actions.CutMessagesToClipboardAction;
import hermes.swing.actions.DeleteMessagesAction;
import hermes.swing.actions.NavigateBackwardMessageAction;
import hermes.swing.actions.NavigateForwardMessageAction;
import hermes.swing.actions.PasteMessagesFromClipboardAction;
import hermes.swing.actions.RefreshBrowseAction;
import hermes.swing.actions.SaveMessagesAsTextAction;
import hermes.swing.actions.SaveMessagesAsXMLAction;
import hermes.swing.actions.SaveMessagesIndividuallyAsXMLAction;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DockableBarContext;

public class MessageToolBar extends CommandBar
{
   public MessageToolBar()
   {
      super("Messages") ;
      
      setHidable(false) ;
      
      getContext().setInitSide(DockableBarContext.DOCK_SIDE_NORTH);
      getContext().setInitMode(DockableBarContext.STATE_HORI_DOCKED);
           
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(NavigateBackwardMessageAction.class)))  ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(NavigateForwardMessageAction.class))) ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(RefreshBrowseAction.class))) ;
      add(AutoRefreshCheckBox.getInstance()) ;
      addSeparator() ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(DeleteMessagesAction.class))) ;
      addSeparator() ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(CutMessagesToClipboardAction.class)) ) ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(CopyMessagesToClipboardAction.class))) ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(PasteMessagesFromClipboardAction.class))) ;
      addSeparator() ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(SaveMessagesAsTextAction.class))) ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(SaveMessagesAsXMLAction.class)) ) ;
      add(SwingUtils.createToolBarButton(ActionRegistry.getAction(SaveMessagesIndividuallyAsXMLAction.class))) ;
   }
}
