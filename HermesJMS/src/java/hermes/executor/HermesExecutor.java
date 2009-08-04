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

package hermes.executor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * Not used (yet).
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HermesExecutor.java,v 1.4 2006/08/01 07:29:36 colincrist Exp $
 */

public class HermesExecutor extends ThreadPoolExecutor
{
   private static final Logger log = Logger.getLogger(HermesExecutor.class);

   public static final int CORE_POOL_SIZE = 6;
   public static final int MINIMUM_POOL_SIZE = 6;
   public static final long KEEP_ALIVE_TIME = 30 * 1000;
   public static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;
   
   private List<HermesExecutorListener> listeners = new CopyOnWriteArrayList<HermesExecutorListener>();
   private Set<Thread> running = new HashSet<Thread>() ;
   public HermesExecutor()
   {
      super(CORE_POOL_SIZE, MINIMUM_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, new LinkedBlockingQueue<Runnable>());
   }

   public void addListener(HermesExecutorListener l)
   {
      listeners.add(l);
      
     
   }

   public void removeListener(HermesExecutorListener l)
   {
      listeners.remove(l);
   }

   @Override
   protected void afterExecute(Runnable r, Throwable t)
   {
      super.afterExecute(r, t);

      for (HermesExecutorListener l : listeners)
      {
         if (getActiveCount() == 0)
         {
            l.onInactive();
         }
      }
   }

   @Override
   protected void beforeExecute(Thread t, Runnable r)
   {
      for (HermesExecutorListener l : listeners)
      {
         l.onActive();
      }

      super.beforeExecute(t, r);
      
      running.add(t) ;
   }
}
