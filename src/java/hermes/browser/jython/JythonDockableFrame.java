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

package hermes.browser.jython;

import hermes.SingletonManager;
import hermes.browser.IconCache;

import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JythonDockableFrame.java,v 1.2 2006/06/25 19:48:14 colincrist Exp $
 */

public class JythonDockableFrame extends DockableFrame
{
   private JPanel topPanel;
  
   public JythonDockableFrame()
   {
      super("Jython", IconCache.getIcon("python"));

      getContext().setInitMode(DockContext.STATE_AUTOHIDE);
      getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
      getContext().setDockedHeight(200);
      setDockedHeight(200);
      setAvailableButtons(DockableFrame.BUTTON_AUTOHIDE | DockableFrame.BUTTON_FLOATING);

      JythonManager jython = (JythonManager) SingletonManager.get(JythonManager.class) ;
      
      
      getContentPane().add(jython.getConsole());
      
      jython.getConsole().setCommandRunner(new JythonCommandRunner(jython.getConsole().getInterpreterThreadName())) ;
   }

   public boolean isNavigableForward()
   {
      return false;
   }

   public boolean isNavigableBackward()
   {
      return false;
   }

   public void navigateForward()
   {

   }

   public void navigateBackward()
   {

   }

   public ListSelectionModel getListSelectionModel()
   {
      // TODO Auto-generated method stub
      return null;
   }

}
