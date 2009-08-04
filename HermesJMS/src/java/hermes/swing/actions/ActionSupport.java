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
import hermes.browser.components.BrowserTree;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.apache.log4j.Logger;

/**
 * Base class for all Hermes actions.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ActionSupport.java,v 1.5 2006/01/15 20:53:32 colincrist Exp $
 */
public abstract class ActionSupport extends AbstractAction
{
   private static final Logger log = Logger.getLogger(ActionSupport.class);

   public ActionSupport()
   {
      // NOP
   }

   public final BrowserTree getBrowserTree()
   {
      return HermesBrowser.getBrowser().getBrowserTree();
   }

   public void enableOnBrowserTreeSelection(final Class clazz, final Action action, boolean single)
   {
      enableOnBrowserTreeSelection(new Class[] { clazz }, action, null, single);
   }

   public void enableOnBrowserTreeSelection(final Class[] classes, final Action action, final TreeSelectionListener delegate, final boolean single)
   {
      getBrowserTree().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
      {
         public void valueChanged(final TreeSelectionEvent event)
         {
            for (int i = 0; i < classes.length; i++)
            {
               if (classes[i].isAssignableFrom(event.getPath().getLastPathComponent().getClass()))
               {
                  if (delegate != null)
                  {
                     delegate.valueChanged(event);
                  }
                  else
                  {
                     action.setEnabled(true);
                     
                     /*
                     if (single)
                     {
                        // log.debug("event.getPaths().length= " + event.getPaths().length) ;
                        
                        action.setEnabled(event.getPaths().length == 1) ;
                     }
                     else
                     {
                        action.setEnabled(true);
                     }
                     */
                  }

                  return;
               }
            }
            
            action.setEnabled(false);
         }
      });
   }

   public void enableOnBrowserTreeSelection(final Class[] classes, final Action action, boolean single)
   {
      enableOnBrowserTreeSelection(classes, action, null, single);
   }
}
