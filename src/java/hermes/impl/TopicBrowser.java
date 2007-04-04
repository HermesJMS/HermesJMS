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

import hermes.Domain;
import hermes.HermesException;
import hermes.HermesMessageListener;
import hermes.HermesRuntimeException;
import hermes.config.DestinationConfig;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSession;

import org.apache.log4j.Logger;

/**
 * A queue browser that actually works on a topic, the browse will never stop.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: TopicBrowser.java,v 1.15 2007/01/10 15:59:54 colincrist Exp $
 */

public class TopicBrowser implements javax.jms.QueueBrowser
{
   private class Enumeration implements java.util.Enumeration
   {
      private List messages = new ArrayList();
      private int maxSize = 1000;
      private int receiveTimeout = 1000;
      private boolean keepRunning = true;
      private JMSException ex;

      private Enumeration() throws JMSException
      {
         init();
      }

      private void stop()
      {
         log.debug("stopping iteration for " + dConfig.getName());

         synchronized (messages)
         {
            keepRunning = false;
            messages.notify();
            messages.clear();
         }
      }

      public Object nextElement()
      {
         synchronized (messages)
         {
            if (keepRunning)
            {
               while (keepRunning && messages.size() == 0)
               {
                  try
                  {
                     messages.wait(1000);
                  }
                  catch (InterruptedException ex)
                  {

                  }
               }

               if (keepRunning && messages.size() > 0)
               {
                  return messages.remove(0);
               }
            }

            if (ex != null)
            {
               throw new RuntimeException(ex);
            }
            else
            {
               return null;
            }
         }
      }

      private void init() throws JMSException
      {
         log.debug("itereration running for " + dConfig.getName());

         MessageListener listener = new HermesMessageListener()
         {
            public void onMessage(Message m)
            {
               synchronized (messages)
               {
                  if (m != null)
                  {
                     messages.add(m);

                     if (messages.size() == 1)
                     {
                        messages.notify();
                     }
                  }
               }
            }

            public void onException(JMSException ex)
            {
               keepRunning = false;

               Enumeration.this.ex = ex;

            }
         };

         try
         {
            if (keepRunning)
            {
               consumer.setMessageListener(listener);
            }
         }
         catch (JMSException ex)
         {
            log.error("in browse thread: " + ex.getMessage());
            stop();
         }

         // cat.debug("itereration stopped for " + subscriber) ;

      }

      /**
       * Returns true of the itereration is still running, else returns false
       */

      public boolean hasMoreElements()
      {
         synchronized (messages)
         {
            return keepRunning;
         }
      }
   }

   private static final Logger log = Logger.getLogger(TopicBrowser.class);
   private Session session;
   private Enumeration iter;
   private DestinationConfig dConfig;
   private DestinationManager destinationManager;
   private MessageConsumer consumer = null;

   /**
    * TopicBrowser constructor comment.
    */
   public TopicBrowser(Session session, DestinationManager destinationManager, DestinationConfig dConfig)
   {
      this.session = session;
      this.dConfig = dConfig;
      this.destinationManager = destinationManager;
   }

   /**
    * Stop the browser, this will stop any itereration running and unsubscribe.
    */
   public void close() throws JMSException
   {
      if (iter != null)
      {
         iter.stop();
         iter = null;
      }

      if (consumer != null)
      {
         consumer.close();
         consumer = null;
      }
   }

   /**
    * Get the itereraiton for this browser.
    */
   public synchronized java.util.Enumeration getEnumeration() throws JMSException
   {
      if (iter == null)
      {
         final Topic topic = (Topic) destinationManager.getDestination(session, dConfig.getName(), Domain.TOPIC);

         if (dConfig.isDurable())
         {
            log.debug("creating DuableSubscriber for topic=" + dConfig.getName() + ", ClientID=" + dConfig.getClientID() + ", selector="
                  + dConfig.getSelector());
            try
            {
               consumer = session.createDurableSubscriber(topic, dConfig.getClientID(), dConfig.getSelector(), false);
            }
            catch (NoSuchMethodError ex)
            {
               // NOP
            }
            catch (AbstractMethodError ex)
            {
               // NOP
            }

            if (consumer == null)
            {
               if (session instanceof TopicSession)
               {
                  final TopicSession topicSession = (TopicSession) session;

                  consumer = session.createDurableSubscriber(topic, dConfig.getClientID(), dConfig.getSelector(), false);
               }
               else
               {
                  throw new HermesException("Session is 1.0.2 and not in the topic domain");
               }
            }
         }
         else
         {
            try
            {
               if (dConfig.getSelector() == null)
               {
                  consumer = session.createConsumer(topic);
               }
               else
               {
                  consumer = session.createConsumer(topic, dConfig.getSelector(), false);
               }
            }
            catch (NoSuchMethodError ex)
            {
               // NOP
            }
            catch (AbstractMethodError ex)
            {
               // NOP
            }

            if (consumer == null)
            {
               if (session instanceof TopicSession)
               {
                  final TopicSession topicSession = (TopicSession) session;

                  if (dConfig.getSelector() == null)
                  {
                     consumer = topicSession.createSubscriber(topic);
                  }
                  else
                  {
                     consumer = topicSession.createSubscriber(topic, dConfig.getSelector(), false);
                  }
               }
               else
               {
                  throw new HermesRuntimeException("The session is JMS 1.0.2b and not in the topic domain.");
               }
            }
         }

         iter = new Enumeration();
      }

      return iter;
   }

   /**
    * Get the message selector
    */
   public String getMessageSelector() throws JMSException
   {
      return dConfig.getSelector();
   }

   /**
    * Get the queue. There isn't one so this will return null
    */
   public Queue getQueue() throws JMSException
   {
      return null;
   }
}