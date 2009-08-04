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

import hermes.Hermes;
import hermes.SingletonManager;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.tasks.TaskSupport;
import hermes.config.DestinationConfig;

import org.python.core.PyException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JythonTask.java,v 1.3 2006/07/13 07:35:36 colincrist Exp $
 */

public class JythonTask extends TaskSupport
{
   private String command;
   private Hermes[] hermes;
   private DestinationConfig[] configs;
   private Thread myThread;

   public JythonTask(String command)
   {
      super(IconCache.getIcon("python"));
      this.command = command;
   }

   @Override
   public String getTitle()
   {
      return null;
   }

   @Override
   public void stop()
   {
      synchronized (this)
      {
         if (myThread != null && myThread.isAlive())
         {
            myThread.interrupt();
         }
      }
   }

   @Override
   public void invoke() throws Exception
   {
      try
      {
         synchronized (this)
         {
            myThread = Thread.currentThread();
         }
         
         JythonManager jython = (JythonManager) SingletonManager.get(JythonManager.class) ;
         
         jython.getInterpreter().exec(command);
      }
      catch (PyException ex)
      {
         HermesBrowser.getBrowser().showErrorDialog(ex);
      }
      finally
      {
         synchronized (this)
         {
            myThread = null;
         }
      }
   }
}
