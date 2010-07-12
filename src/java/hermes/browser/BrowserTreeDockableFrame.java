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

import hermes.HermesException;
import hermes.HermesLoader;
import hermes.browser.components.BrowserTree;
import hermes.browser.model.BrowserTreeModel;

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.swing.JideScrollPane;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: BrowserTreeDockableFrame.java,v 1.3 2004/07/21 19:46:14
 *          colincrist Exp $
 */
public class BrowserTreeDockableFrame extends DockableFrame
{
   private static final long serialVersionUID = 1L;

   private BrowserTree browserTree ;

   /**
    * @param arg0
    */
   public BrowserTreeDockableFrame() throws HermesException
   {
      super("Sessions", IconCache.getIcon("hermes.browser.tree"));

      browserTree = new BrowserTree(); 
      
      getContext().setInitSide(DockContext.DOCK_SIDE_WEST);
      getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);      
      
      setAvailableButtons(DockableFrame.BUTTON_AUTOHIDE | DockableFrame.BUTTON_FLOATING);
 
      getContentPane().add(new JideScrollPane(browserTree));      
   }

   public void setLoader(HermesLoader loader)
   {
      browserTree.init();
      loader.addConfigurationListener((BrowserTreeModel) browserTree.getModel());
   }

   public BrowserTree getBrowserTree()
   {
      return browserTree;
   }

}