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

import hermes.HermesConstants;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;

import java.util.HashSet;
import java.util.Set;

import javax.jms.Message;
import javax.swing.Icon;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public abstract class TaskSupport implements Task, Runnable
{
   private static final Logger log = Logger.getLogger(TaskSupport.class);
   private static final Set<TaskListener> globalListeners = new HashSet<TaskListener>();
   private Set<TaskListener> listeners = new HashSet<TaskListener>();
   private boolean isRunning = true;
   private Icon icon = IconCache.getIcon("jms.unknown");
   private Thread myThread;

   public static void addGlobalListener(TaskListener listener)
   {
      globalListeners.add(listener);
   }

   public static void removeGlobalListener(TaskListener listener)
   {
      globalListeners.remove(listener);
   }

   protected TaskSupport(Icon icon)
   {
      this.icon = icon;

      listeners.addAll(globalListeners);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.Task#getTitle()
    */
   public String getTitle()
   {
      return HermesConstants.EMPTY_STRING;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.Task#getIcon()
    */
   public Icon getIcon()
   {
      return icon;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.Task#addTaskListener(hermes.browser.tasks.TaskListener)
    */
   public void addTaskListener(TaskListener taskListener)
   {
      synchronized (listeners)
      {
         listeners.add(taskListener);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.Task#removeTaskListener(hermes.browser.tasks.TaskListener)
    */
   public void removeTaskListener(TaskListener taskListener)
   {
      synchronized (listeners)
      {
         listeners.remove(taskListener);
      }
   }

   protected void notifyThrowable(Throwable t)
   {
      for (TaskListener listener : listeners)
      {
         listener.onThrowable(this, t);
      }
   }

   protected void notifyMessage(Message message)
   {
      for (TaskListener listener : listeners)
      {
         if (listener instanceof MessageTaskListener)
         {
            ((MessageTaskListener) listener).onMessage(this, message);
         }
      }
   }

   protected void notifyStatus(String status)
   {
      for (TaskListener listener : listeners)
      {
         listener.onStatus(this, status);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.Task#stop()
    */
   public synchronized void stop()
   {
      isRunning = false;

      if (myThread != null)
      {
         myThread.interrupt();
         myThread = null ;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.Task#isRunning()
    */
   public boolean isRunning()
   {
      return isRunning;
   }

   public abstract void invoke() throws Exception;

   public void run()
   {
      try
      {
         myThread = Thread.currentThread();
         
         try
         {
            for (TaskListener listener : listeners)
            {
               listener.onStarted(this);
            }

            TaskSupport.this.invoke();
         }
         catch (Throwable t)
         {
            // log.error(t.getMessage(), t) ;

            for (TaskListener listener : listeners)
            {
               listener.onThrowable(this, t);
            }
         }
      }
      finally
      {
         for (TaskListener listener : listeners)
         {
            listener.onStopped(this);              
         }
         
         listeners.clear() ;
         isRunning = false;
         myThread = null;
      }
   }

   public void start()
   {
      HermesBrowser.getBrowser().getThreadPool().invokeLater(this);
   }
}
