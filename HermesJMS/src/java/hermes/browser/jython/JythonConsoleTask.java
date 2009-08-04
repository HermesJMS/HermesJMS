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


import hermes.browser.IconCache;
import hermes.browser.tasks.TaskSupport;

import com.artenum.jyconsole.command.Command;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JythonConsoleTask.java,v 1.2 2006/09/16 15:49:24 colincrist Exp $
 */

public class JythonConsoleTask extends TaskSupport
{
   private Command command ;
   
   public JythonConsoleTask(Command command)
   {
      super(IconCache.getIcon("python")) ;
      
      this.command = command ;
   }

   @Override
   public String getTitle()
   {     
      return command.toString() ;
   }

   @Override
   public void stop()
   {
      command.stop() ;
   }

   @Override
   public void invoke() throws Exception
   {
      synchronized (command)
      {
         if (command.isRunning())
         {
            try
            {
               command.wait() ;
            }
            catch (InterruptedException e)
            {
               
            }
         }
      }
   }
}
