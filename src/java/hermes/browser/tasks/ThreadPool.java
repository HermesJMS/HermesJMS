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

package hermes.browser.tasks;

import hermes.SingletonManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Absolutely no JMS work can occur on the main, GUI thread. So all the work
 * gets delegated to this thread pool to do...
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ThreadPool.java,v 1.8 2006/08/01 07:29:36 colincrist Exp $
 */

public class ThreadPool implements Runnable
{
   private static final Logger log = Logger.getLogger(ThreadPool.class);
   private static final ThreadPool threadPool = new ThreadPool(1);
   private ArrayList<Runnable> tasks = new ArrayList<Runnable>();
   private List<Thread> threads = new ArrayList<Thread>();
   private int nthreads;
   private Set<Runnable> running = new HashSet<Runnable>();
   private List<ThreadPoolActiveListener> listeners = new ArrayList<ThreadPoolActiveListener>();

   static
   {
      SingletonManager.put(ThreadPool.class, new ThreadPool(1));
   }

   public static ThreadPool get()
   {
      return (ThreadPool) SingletonManager.get(ThreadPool.class);
   }

   public ThreadPool(int nthreads)
   {
      this.nthreads = nthreads;

      for (int i = 0; i < nthreads; i++)
      {
         Thread t = new Thread(this, "Hermes ThreadPool-" + i);

         threads.add(t);
         t.setDaemon(true);
         t.start();
      }
   }

   public void setClassLoader(ClassLoader classLoader)
   {
      for (final Thread t : threads)
      {

         log.debug("updating ContextClassLoader on " + t.getName());

         t.setContextClassLoader(classLoader);
      }
   }

   public void invokeLater(Runnable run)
   {
      synchronized (tasks)
      {
         tasks.add(run);
         tasks.notify();
      }
   }

   public void addActiveListener(ThreadPoolActiveListener listener)
   {
      listeners.add(listener);
   }

   public void removeActiveListener(ThreadPoolActiveListener listener)
   {
      listeners.remove(listener);
   }

   public void stopAll()
   {
      synchronized (tasks)
      {
         synchronized (running)
         {
            for (final Runnable r : running)
            {
               if (r instanceof Task)
               {
                  Task task = (Task) r;
                  task.stop();
               }
            }
         }
         tasks.clear();
      }

   }

   public void run()
   {
      //
      // Minimum priority means the GUI will always get priority.
      
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY) ;
      
      boolean keepRunning = true;

      while (keepRunning)
      {
         Runnable task = null;

         synchronized (tasks)
         {
            while (tasks.size() == 0)
            {
               try
               {
                  for (final ThreadPoolActiveListener l : listeners)
                  {
                     l.onInactive();
                  }

                  tasks.wait();
               }
               catch (InterruptedException ex)
               {
                  log.error(ex.getMessage(), ex);
               }
            }

            task = (Runnable) tasks.remove(0);
         }

         for (final ThreadPoolActiveListener l : listeners)
         {
            l.onActive();
         }

         log.debug("task " + task + " starting");

         try
         {
            synchronized (running)
            {
               running.add(task);
            }

            task.run();

         }
         catch (Throwable ex)
         {
            log.error("during run(): " + ex.getMessage(), ex);
         }
         finally
         {
            synchronized (running)
            {
               running.remove(task);
            }
         }

         log.debug("task " + task + "  stopped");

         synchronized (threads)
         {
            if (threads.size() > nthreads)
            {
               threads.remove(Thread.currentThread());

               keepRunning = false;

               log.debug("threadpool shrinking to " + threads.size());
            }
         }
      }
   }

   public void setThreads(int n)
   {
      if (n > nthreads)
      {
         log.debug("growing threadpool to " + n);

         for (int i = nthreads; i < n; i++)
         {
            Thread t = new Thread(this, "Hermes ThreadPool-" + i);

            synchronized (threads)
            {
               threads.add(t);
            }

            t.setDaemon(true);
            t.start();
         }
      }

      nthreads = n;
   }

   public int getSize()
   {
      return nthreads ;
   }
}