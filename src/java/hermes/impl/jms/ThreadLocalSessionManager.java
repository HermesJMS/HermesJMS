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

package hermes.impl.jms;

import hermes.Domain;
import hermes.HermesException;
import hermes.config.DestinationConfig;
import hermes.config.SessionConfig;
import hermes.impl.ConnectionManager;
import hermes.impl.DestinationManager;
import hermes.util.JMSUtils;

import java.io.EOFException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.jms.TransactionRolledBackException;

import org.apache.log4j.Logger;

/**
 * Manager for sessions, holds the thread local producers, consumers, sessions
 * and (if configured), destinations. Implements reconnect via the connect()
 * method but does not in itself dictate the reconect policy, that is left to
 * the implementor of Hermes that calls this. Asynchronous message listeners are
 * <b>not</b> supported as this functionality is managed via the dispatchers at
 * the DefaultHermesImpl layer.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ThreadLocalSessionManager.java,v 1.1 2004/07/21 20:25:40
 *          colincrist Exp $
 */

public class ThreadLocalSessionManager extends AbstractSessionManager
{
   private static final Logger log = Logger.getLogger(ThreadLocalSessionManager.class);

   private int sessions = 0;
   private long asyncSessionCloseTimeout = 30 * 1000;
   private ThreadLocal sessionTL = new ThreadLocal();
   // private ThreadLocal queueProducerTL = new ThreadLocal();
   private ThreadLocal producerTL = new ThreadLocal();
   private ThreadLocal consumersTL = new ThreadLocal();
   private ThreadLocal consumersWithSelectorTL = new ThreadLocal();
   private Thread dispatchThread;
   private boolean doReconnectConsumers = false;
 
   /**
    * SessionManager constructor
    */
   public ThreadLocalSessionManager(SessionConfig config, DestinationManager destinationManager)
   {
      super(destinationManager, config);
   }

   public boolean isOpen()
   {
      return sessionTL.get() != null;
   }
   
  
   /**
    * Close the session, the session will be recreated on the next use of this
    * hermes in this thread
    */
   public synchronized void close() throws JMSException
   {
      if (sessionTL.get() != null)
      {
         closeConsumers((Map) consumersTL.get());
         closeConsumers((Map) consumersWithSelectorTL.get());
        
         
         try
         {
            getSession().close();
         }
         catch(JMSException ex)
         {
            log.info("closing session:" + ex.getMessage(), ex) ;
         }
         
         sessionTL.set(null);

         consumersTL.set(null);
         consumersWithSelectorTL.set(null);
         producerTL.set(null);

         sessions--;

         if (getConnectionManager().getType() == ConnectionManager.Policy.SHARED_CONNECTION)
         {
            if (sessions == 0)
            {
               log.debug("all sessions closed, closing Connection");

               getConnectionManager().close();
               getConnectionFactoryManager().close();
            }
         }
         else
         {
            log.debug("session closed, closing its Connection");

            getConnectionManager().close();
            getConnectionFactoryManager().close();
         }
      }
   }

   public void reconnect(String username, String password) throws JMSException
   {
      try
      {
         close() ;
      }
      catch (JMSException ex) 
      {
         log.warn("when closing session: " + ex.getMessage(), ex) ;
      }
      
      getConnectionManager().reconnect(username, password) ;     
   }

   /**
    * Reconect the consumers in the map, keyed on destination
    */
   private void reconnect(Map consumers) throws JMSException
   {
      /*
       * for (Iterator iter = consumers.keySet().iterator(); iter.hasNext();) {
       * Destination dest = (Destination) iter.next(); String destName =
       * (isQueue(getConnection())) ? ((Queue) dest).getQueueName() : ((Topic)
       * dest).getTopicName(); DestinationConfig dConfig =
       * getDestinationConfig(destName); Domain domain; if ( dConfig != null) {
       * domain = Domain.getDomain(dConfig.getDomain()); } else { domain =
       * Domain.getDomain(dest); } try { MessageConsumer oldConsumer =
       * (MessageConsumer) consumers.get(dest); MessageConsumer newConsumer =
       * getConsumer(getDestination(destName, domain),
       * oldConsumer.getMessageSelector()); if (
       * oldConsumer.getMessageListener() != null) {
       * newConsumer.setMessageListener(oldConsumer.getMessageListener()); } }
       * catch (NamingException ex) { cat.error("unable to locate destination " +
       * destName + " in JNDI"); } }
       */
   }

   /**
    * Create the session for this thread. Not that if consumers exist (during a
    * reconnect for example) then they will be recreated and the
    * MessageListener's reconnected. Possibly a bit dodgy.
    */
   public synchronized void connect() throws JMSException
   {
      boolean connected = false;
      boolean firstConnect = (sessionTL.get() == null) ? true : false;
      int attempts = 0;
      JMSException throwThis = null; // this goes against my better
      // judgements...
      while (!connected)
      {
         try
         {
            Connection conn = (Connection) parent.getObject();
            Session session = createSession();

            sessionTL.set(session);
            connected = true;

            if (doReconnectConsumers)
            {
               //
               // Tidy up and reconnect any MessageConsumers.

               Map consumers = (Map) consumersTL.get();
               Map consumersWithSelector = (Map) consumersWithSelectorTL.get();

               consumersTL.set(new HashMap());

               if (consumers != null)
               {
                  reconnect(consumers);
               }

               if (consumersWithSelector != null)
               {
                  reconnect(consumersWithSelector);
               }

               if (!firstConnect && session.getTransacted())
               {
                  throwThis = new TransactionRolledBackException("reconnect has forced transaction rollback");
               }
            }
         }
         catch (JMSException ex)
         {
            synchronized (this)
            {
               if (getReconnects() == -1 || attempts < getReconnects())
               {
                  attempts++;

                  log.error("connect failed (" + attempts + "): " + ex.getMessage());

                  try
                  {
                     Thread.sleep(getReconnectTimeout());
                  }
                  catch (InterruptedException ex2)
                  {
                     log.error("unexpected: " + ex2.getMessage(), ex);
                  }
               }
               else
               {
                  //
                  // If the linked exception is an EOFException we've probably lost connection to the broker 
                  // so force a reconnect.
                  
                  if (ex.getLinkedException() != null && ex.getLinkedException() instanceof EOFException)
                  {
                     try
                     {
                        getParent().close() ;
                     }
                     catch (JMSException ex2)
                     {
                        // Ignore.
                     }
                  }
                  else
                  {
                     throw ex;
                  }
               }
            }
         }
      }

      if (throwThis != null)
      {
         throw throwThis;
      }
   }

   public void closeConsumers(final Map consumers)
   {
      if (consumers != null)
      {
         for (final Iterator iter = consumers.entrySet().iterator(); iter.hasNext();)
         {
            final Map.Entry entry = (Map.Entry) iter.next();
            final Destination d = (Destination) entry.getKey();
            final MessageConsumer consumer = (MessageConsumer) entry.getValue();

            try
            {
               log.debug("closing consumer for " + JMSUtils.getDestinationName(d));

               consumer.close();
            }
            catch (JMSException e)
            {
               log.error("closing consumer: " + e.getMessage(), e);
            }
         }
      }
   }

   public void closeConsumer(final Destination d, String selector) throws JMSException
   {
      final Map map = selector == null ? (Map) consumersTL.get() : (Map) consumersWithSelectorTL.get() ;

      if (map != null)
      {
         final MessageConsumer consumer = (MessageConsumer) map.remove(d);

         if (consumer != null)
         {
            log.debug("closing consumer for " + JMSUtils.getDestinationName(d));

            consumer.close();
         }
      }
      else
      {
         log.debug("no consumer found to close for " + JMSUtils.getDestinationName(d));
      }
   }

   /**
    * Get a consumer for a destination. The consumer is cached thread local.
    */
   public MessageConsumer getConsumer(final Destination d) throws JMSException
   {
      return getConsumer(d, null);
   }

   /**
    * Get a consumer for a destination and a selector. The consumer is cached
    * thread local.
    */
   public MessageConsumer getConsumer(final Destination d, final String selector) throws JMSException
   {
      MessageConsumer consumer = null;
      Map<Destination, MessageConsumer> map;

      if (selector != null)
      {
         map = (Map) consumersWithSelectorTL.get();
         
         if (map != null) {
        	 if (map.get(d) != null) {
        		 if (!map.get(d).getMessageSelector().equals(selector)) {
        			 map.remove(d).close() ; ;
        		 }
        	 }
         }

      }
      else
      {
         map = (Map) consumersTL.get();

      }

      if (map == null)
      {
         map = new HashMap();

         if (selector != null)
         {
            consumersWithSelectorTL.set(map);

         }
         else
         {
            consumersTL.set(map);
         }
      }

      if (map.containsKey(d) )
      {
         consumer = (MessageConsumer) map.get(d);
       
      }
      else
      {
         final DestinationConfig dConfig = getDestinationConfig(d);
         Domain domain;

         if (dConfig != null)
         {
            domain = Domain.getDomain(dConfig.getDomain());
         }
         else
         {
            domain = Domain.getDomain(d);
         }

         if (domain == Domain.QUEUE)
         {
            try
            {
               if (selector == null)
               {
                  consumer = getSession().createConsumer(d);
               }
               else
               {
                  consumer = getSession().createConsumer(d, selector, true);
               }
            }
            catch (NoSuchMethodError ex)
            {
               log.debug("JMS 1.1 interface failed, trying 1.0.2b");
            }
            catch (AbstractMethodError ex)
            {
               log.debug("JMS 1.1 interface failed, trying 1.0.2b");
            }
            catch (JMSException t)
            {
               //
               // WebSphereMQ hack, it does not correctly support JMS 1.1

               if (d.getClass().getName().equals("com.ibm.mq.jms.MQQueue"))
               {
                  log.debug("createConsumer() failed with WMQ via JMS 1.1 call, falling back to 1.0.2b call") ;
               }
               else
               {
                  throw t;
               }
            }

            if (consumer == null)
            {
               if (selector == null)
               {
                  consumer = ((QueueSession) getSession()).createReceiver((Queue) d);
               }
               else
               {
                  consumer = ((QueueSession) getSession()).createReceiver((Queue) d, selector);
               }
            }
         }
         else
         {
            if (dConfig != null && dConfig.isDurable())
            {
               try
               {
                  if (selector == null)
                  {
                     consumer = getSession().createDurableSubscriber((Topic) d, dConfig.getClientID());
                  }
                  else
                  {
                     consumer = getSession().createDurableSubscriber((Topic) d, dConfig.getClientID(), selector, true);
                  }
               }
               catch (NoSuchMethodError ex)
               {
                  log.debug("JMS 1.1 interface failed, trying 1.0.2b");
               }
               catch (AbstractMethodError ex)
               {
                  log.debug("JMS 1.1 interface failed, trying 1.0.2b");
               }

               if (consumer == null)
               {
                  if (selector == null)
                  {
                     consumer = ((TopicSession) getSession()).createDurableSubscriber((Topic) d, dConfig.getClientID());
                  }
                  else
                  {
                     consumer = ((TopicSession) getSession()).createDurableSubscriber((Topic) d, dConfig.getClientID(), selector, true);
                  }
               }
            }
            else
            {
               try
               {
                  if (selector == null)
                  {
                     consumer = getSession().createConsumer(d);
                  }
                  else
                  {
                     consumer = getSession().createConsumer(d, selector, true);
                  }
               }
               catch (NoSuchMethodError ex)
               {
                  log.debug("JMS 1.1 interface failed, trying 1.0.2b");
               }
               catch (AbstractMethodError ex)
               {
                  log.debug("JMS 1.1 interface failed, trying 1.0.2b");
               }

               if (consumer == null)
               {
                  if (selector == null)
                  {
                     consumer = ((TopicSession) getSession()).createSubscriber((Topic) d);
                  }
                  else
                  {
                     consumer = ((TopicSession) getSession()).createSubscriber((Topic) d, selector, true);
                  }
               }
            }
         }

         map.put(d, consumer);
      }

      return consumer;
   }

   public MessageProducer getProducer() throws JMSException
   {
      MessageProducer producer = (MessageProducer) producerTL.get();

      if (producer == null)
      {
         try
         {
            /**
             * Begin WebMethods Enterprise Hack. When using this provider with a
             * JMS 1.1 interface some of the 1.1 methods match and some do not.
             */

            if (getSession().getClass().getName().equals("com.wm.broker.jms.QueueSession"))
            {
               log.debug("WebMethods session creation hack is active");
            }
            else
            {
               producer = getSession().createProducer(null);
               log.debug("producer created using JMS 1.1 interface");
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
         finally
         {
            try
            {
               if (producer == null)
               {
                  producer = createQueueProducer();
               }
            }
            catch (Throwable t)
            {
               log.debug("cannot create a QueueSender: " + t.getMessage(), t);
               log.debug("trying a TopicPublisher");

               producer = createTopicProducer();
            }

            producerTL.set(producer);
         }
      }

      return producer;
   }

   /**
    * Get the session itself, note that if no session is available then
    * connect() is invoked to create one.
    */
   public synchronized Session getSession() throws JMSException
   {
      Session rval = (Session) sessionTL.get();

      if (rval == null)
      {
         connect();

         synchronized (this)
         {
            sessions++;
         }

         rval = (Session) sessionTL.get();
      }

      return rval;
   }

   public void unsubscribe(String name) throws JMSException
   {
      try
      {
         try
         {
            getSession().unsubscribe(name);
         }
         catch (NoSuchMethodError ex)
         {
            ((TopicSession) getSession()).unsubscribe(name);
         }
         catch (AbstractMethodError ex)
         {
            ((TopicSession) getSession()).unsubscribe(name);
         }
      }
      catch (Throwable ex)
      {
         log.error(ex.getMessage(), ex);

         throw new HermesException("Session " + getId() + " cannot unsubscribe: " + ex.getMessage());
      }
   }
}