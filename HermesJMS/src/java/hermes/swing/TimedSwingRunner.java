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

package hermes.swing;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: TimedSwingRunner.java,v 1.1 2006/05/13 14:06:55 colincrist Exp $
 */

public class TimedSwingRunner
{
   private static Timer timer = new Timer();

   private long timeout;
   private Map<Object, Runnable> runnables = new HashMap<Object, Runnable>();

   private boolean stopped = false;

   public TimedSwingRunner(long timeout)
   {
      super();

      this.timeout = timeout;

      TimerTask task = new TimerTask()
      {

         @Override
         public void run()
         {
            drain();
         }
      };

      timer.schedule(task, timeout);
   }

   public synchronized void invokeLater(Object context, Runnable runnable)
   {
      runnables.put(context, runnable);
   }

   public void stop()
   {
      stopped = true ;
   }
   
   private synchronized void drain()
   {
      for (Runnable runnable : runnables.values())
      {
         if (runnable != null)
         {
            SwingRunner.invokeLater(runnable);
         }
      }
      
      runnables.clear();

      if (!stopped)
      {
         TimerTask task = new TimerTask()
         {

            @Override
            public void run()
            {
               drain();
            }
         };

         timer.schedule(task, timeout);
      }
   }
}
