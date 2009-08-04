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

package hermes.impl;

import hermes.HermesDispatcher;
import hermes.HermesException;
import hermes.HermesMessageListener;
import hermes.util.JMSUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Category;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultHermesDispatcherImpl.java,v 1.1 2004/05/01 15:52:35
 *          colincrist Exp $
 */

public class DefaultHermesDispatcherImpl implements HermesDispatcher, Runnable
{
   private static final Category cat = Category.getInstance(DefaultHermesDispatcherImpl.class);
   private static int numDispatchers = 0;
   private Map<DestinationKeyWrapper, MessageListener> destinations = new HashMap<DestinationKeyWrapper, MessageListener>();
   private Set<DestinationKeyWrapper> removedDestinations = new HashSet<DestinationKeyWrapper>();
   private List queue = new ArrayList();
   private DefaultHermesImpl hermes;
   private boolean keepRunning = true;
   private long sleepPeriod = 50;
   private Thread dispatchThread;
   private boolean synchronizeThreadStart = false;

   /**
    * Constructor for Dispatcher.
    */
   public DefaultHermesDispatcherImpl(DefaultHermesImpl hermes)
   {
      super();

      this.hermes = hermes;
   }

   /**
    * Constructor for Dispatcher.
    */
   public DefaultHermesDispatcherImpl(DefaultHermesImpl hermes, boolean synchronizeThreadStart)
   {
      super();

      this.hermes = hermes;
      this.synchronizeThreadStart = synchronizeThreadStart;
   }

   private final String getName(Destination d) throws JMSException
   {
      return hermes.getDestinationName(d);
   }

   /**
    * Add a desination and message listener for messages to be dispatched to.
    */
   public void addDestination(Destination d, MessageListener ml) throws JMSException
   {
      synchronized (destinations)
      {
         if (dispatchThread == null)
         {
            start();
         }

         destinations.put(new DestinationKeyWrapper(d), ml);
      }

      cat.debug("new destination: " + getName(d));
   }

   /**
    * Remove a destination from dispatching
    */
   public void removeDestination(final Destination d) throws JMSException
   {
      synchronized (destinations)
      {
         DestinationKeyWrapper key = new DestinationKeyWrapper(d) ;
         if (destinations.remove(key) == null)
         {
            //
            // If the thread is running give some extra error information

            if (dispatchThread != null)
            {
               throw new JMSException("destination " + getName(d) + " not being dispatched on " + dispatchThread.getName());
            }
            else
            {
               throw new JMSException("destination " + getName(d) + " not registered");
            }
         }
         else
         {
            removedDestinations.add(key);
         }
      }

      cat.debug("removed destination: " + JMSUtils.getDestinationName(d));
   }

   /**
    * Dispatch a runnable on this thread at the next opportunity, you can add
    * events to be dispatched even though the object is not running on a thread,
    * they will be queued.
    */
   public void invoke(Runnable runnable) throws JMSException
   {
      synchronized (queue)
      {
         queue.add(runnable);
         queue.notifyAll();
      }
   }

   /**
    * Thread mainline, alternate between pulling events off the dispatch queue
    * as well as from the thread local session from the Hermes impl.
    */
   public void run()
   {
      //
      // Cache the thread and notify anyone who's waiting to be informed that
      // the dispatcher is up and running

      dispatchThread = Thread.currentThread();

      synchronized (dispatchThread)
      {
         dispatchThread.notifyAll();
      }

      cat.debug("dispatcher starting");

      while (keepRunning)
      {
         int messagesRead = 0;

         //
         // Drain any internal events, if message dispatching took a long
         // time you may get several
         // timer triggered runnables of the same type bunched up together.

         synchronized (queue)
         {
            while (queue.size() > 0)
            {
               Runnable r = (Runnable) queue.remove(0);

               r.run();
            }
         }

         //
         // Drain messages, just do a single pass accross all the
         // destinations so that we don't
         // starve any Runnable's on the queue.

         synchronized (destinations)
         {
            if (removedDestinations.size() > 0)
            {
               for (DestinationKeyWrapper key : removedDestinations)
               {
                 
                  destinations.remove(key);

                  try
                  {
                     hermes.closeConsumer(key.getDestination());
                  }
                  catch (JMSException e)
                  {
                     cat.error("closing async consumer: " + e.getMessage(), e);
                  }
               }

               removedDestinations.clear();
            }

            if (destinations.size() == 0 && hermes.isOpen())
            {
               try
               {
                  cat.debug("nothing to dispatch, closing Hermes " + hermes.getId());

                  hermes.close();
               }
               catch (JMSException e)
               {
                  cat.error(e.getMessage(), e);
               }
            }
            else
            {
               for (Map.Entry<DestinationKeyWrapper, MessageListener> entry : destinations.entrySet())
               {
                  MessageListener ml = null;

                  try
                  {
                     ml = entry.getValue();

                     final Destination d = (Destination) entry.getKey().getDestination() ;
                     final Message m = hermes.receiveNoWait(d);

                     if (m != null)
                     {
                        if (ml != null)
                        {
                           ml.onMessage(m);
                        }
                        else
                        {
                           cat.fatal("no message listener available for destination " + hermes.getDestinationName(d) + " message discarded");
                        }

                        messagesRead++;
                     }
                  }
                  catch (JMSException ex)
                  {
                     cat.error(ex.getMessage(), ex);
                     removedDestinations.add(entry.getKey()) ;

                     if (ml instanceof HermesMessageListener)
                     {
                        ((HermesMessageListener) ml).onException(ex);
                     }
                     
                     break ;
                  }
               }
            }
         }

         // 
         // Only sleep if nothing happened last time round otherwise you'll
         // soak the CPU.

         if (messagesRead == 0)
         {
            try
            {
               Thread.sleep(sleepPeriod);
            }
            catch (InterruptedException e)
            {
               // NOP
            }
         }
      }

      dispatchThread = null;
      cat.debug("dispatcher stopping");
   }

   /**
    * Returns the sleepPeriod which is the time for the thread to sleep if no
    * messages where dispatched on the last poll of all the destinations
    * registered.
    * 
    * @return long
    */
   public long getSleepPeriod()
   {
      return sleepPeriod;
   }

   /**
    * Sets the sleepPeriod which is the time for the thread to sleep if no
    * messages where dispatched on the last poll of all the destinations
    * registered.
    * 
    * @param sleepPeriod
    *           The sleepPeriod to set
    */
   public void setSleepPeriod(long sleepPeriod)
   {
      this.sleepPeriod = sleepPeriod;
   }

   /**
    * @see hermes.HermesDispatcher#invokeAndWait(Runnable)
    */
   public void invokeAndWait(final Runnable runnable) throws JMSException
   {
      if (dispatchThread == null)
      {
         throw new HermesException("dispatcher thread not running so cannot invokeAndWait");
      }

      Runnable myRunnable = new Runnable()
      {
         public void run()
         {
            runnable.run();

            synchronized (this)
            {
               notifyAll();
            }
         }
      };

      synchronized (myRunnable)
      {
         invoke(myRunnable);

         try
         {
            myRunnable.wait();
         }
         catch (Exception ex)
         {
            cat.error(ex.getMessage(), ex);
         }
      }
   }

   /**
    * Helper to start the dispatcher on a thread. If synchronizeThreadStart is
    * set then this will not return until this Runnable is active on the thread -
    * this ensures that any subsequent dispatchAndWait() will not throw a
    * DispatcherNotRunningException
    */
   public Thread start() throws JMSException
   {
      synchronized (DefaultHermesDispatcherImpl.class)
      {
         return start("dispatcher-" + numDispatchers++);
      }
   }

   /**
    * Helper to start the dispatcher on a thread. If synchronizeThreadStart is
    * set then this will not return until this Runnable is active on the thread -
    * this ensures that any subsequent dispatchAndWait() will not throw a
    * DispatcherNotRunningException
    * 
    * @param threadName
    *           The name to assign the thread.
    */
   public Thread start(String threadName) throws JMSException
   {
      if (dispatchThread != null)
      {
         throw new HermesException("Dispatcher thread not running");
      }

      Thread thread = new Thread(this, threadName);

      synchronized (thread)
      {
         thread.start();

         if (synchronizeThreadStart)
         {
            try
            {
               thread.wait();
            }
            catch (InterruptedException ex)
            {
               cat.error(ex.getMessage(), ex);
            }
         }
      }

      return thread;
   }

   /**
    * @see hermes.HermesDispatcher#close()
    */
   public void close() throws JMSException
   {
      keepRunning = false;

      hermes.removeDispatcher(this);
   }

   /**
    * @see hermes.HermesDispatcher#setMessageListener(Destination,
    *      MessageListener)
    */
   public void setMessageListener(Destination from, MessageListener ml) throws JMSException
   {
      if (ml != null)
      {
         addDestination(from, ml);
      }
      else
      {
         removeDestination(from);
      }
   }

}
