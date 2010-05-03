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

import hermes.browser.IconCache;
import hermes.browser.tasks.ThreadPool;
import hermes.browser.tasks.ThreadPoolActiveListener;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;


public class StopAllTasksAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -882986851096340308L;

public StopAllTasksAction()
   {
      putValue(Action.NAME, "Stop all") ;
      putValue(Action.SHORT_DESCRIPTION, "Stop all running tasks.") ;
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.stop.all")) ;
      putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false)) ; 
      
      
      setEnabled(false) ;
      
      ThreadPool.get().addActiveListener(new ThreadPoolActiveListener()
      {
         public void onInactive()
         {
            setEnabled(false) ;
         }
      
         public void onActive()
         {
            setEnabled(true) ;
         }
      }) ;
   }

   public void actionPerformed(ActionEvent e)
   {
      ThreadPool.get().stopAll() ;
   }
}
