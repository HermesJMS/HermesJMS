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
import hermes.Hermes;
import hermes.HermesAuditLog;
import hermes.HermesDispatcher;
import hermes.HermesException;
import hermes.HermesRuntimeException;
import hermes.ProviderMetaData;
import hermes.browser.HermesBrowser;
import hermes.browser.MessageRenderer;
import hermes.config.DestinationConfig;
import hermes.config.HermesConfig;
import hermes.config.ProviderExtConfig;
import hermes.config.SessionConfig;
import hermes.util.JMSUtils;
import hermes.util.MessageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.naming.NamingException;

import org.apache.log4j.Category;

/**
 * Default implementation for the Hermes interface. Its role is to delegate to
 * the SessionManager and implement the reconnection policy.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultHermesImpl.java,v 1.18 2004/10/28 21:34:01 colincrist
 *          Exp $
 */

public class DefaultHermesImpl implements Hermes
{

   private static final Category cat = Category.getInstance(DefaultHermesImpl.class);
   private static final String DEFAULT_DISPATCHER = "defaultHermesDispatcher";
   private static final Timer timer = new Timer();

   private XMLHelper xmlSupport = new DefaultXMLHelper();
   private SessionManager sessionManager;
   private ProviderMetaData metaData;
   private Map dispatchers = new HashMap();
   private Map timerMap = new HashMap();
   private DefaultHermesDispatcherImpl defaultDespatcher;
   private ThreadLocal auditLogTL = new ThreadLocal();
   private boolean readOnly = false;
   private final boolean reconnecting = false;
   private ProviderExtConfig extConfig;
   private HermesAdminAdapter adminAdapter;
   private ClassLoader contextClassLoader;

   /**
    * DefaultHermesImpl constructor with a SessionManager.
    */
   public DefaultHermesImpl(ProviderExtConfig extConfig, SessionManager sessionManager, ClassLoader contextClassLoader) throws JMSException, IOException
   {
      super();

      this.extConfig = extConfig;
      this.sessionManager = sessionManager;
      this.defaultDespatcher = _getDispatcher(DEFAULT_DISPATCHER);
      this.contextClassLoader = contextClassLoader;

      try
      {
         getAdminAdapter();
      }
      catch (JMSException ex)
      {
         cat.error("cannot bootstrap AdminAdapter: " + ex.getMessage(), ex);
      }
   }

   public SessionConfig getSessionConfig()
   {
      return sessionManager.getConfig();
   }

   private synchronized HermesAdminAdapter getAdminAdapter() throws JMSException
   {
      if (adminAdapter == null)
      {
         adminAdapter = new HermesAdminAdapter(this, sessionManager.getConnectionFactoryManager().getExtension(extConfig));
      }

      return adminAdapter;
   }

   /**
    * Is this hermes transacted?
    */
   public boolean getTransacted() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getSession().getTransacted();
   }

   /**
    * Close any consumer open...
    */
   public void close(Destination destination, String selector) throws JMSException
   {
      sessionManager.closeConsumer(destination, selector);
   }

   public void closeConsumer(Destination d) throws JMSException
   {
      sessionManager.closeConsumer(d, null);
   }

   public Session getSession() throws JMSException
   {
      return sessionManager.getSession();
   }

   /**
    * close resources. any reuse of this object will reacquie the resoures.
    */
   public synchronized void close() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      sessionManager.close();

      if (adminAdapter != null)
      {
         adminAdapter.close();
      }

      getAuditLog().rollback();
   }

   public synchronized boolean isOpen()
   {
      return sessionManager.isOpen();
   }

   /**
    * commit the current transaction.
    */
   public void commit() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (sessionManager.getSession().getTransacted())
      {
         sessionManager.getSession().commit();
      }
      else
      {
         cat.warn("session " + getId() + " is not transacted");
      }

      try
      {
         if (isAudit())
         {
            getAuditLog().commit();
         }
      }
      catch (JMSException ex)
      {
         cat.error("cannot log audit during commit: " + ex.getMessage(), ex);
      }
   }

   public QueueBrowser createBrowser(DestinationConfig config) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (config.getDomain() == Domain.TOPIC.getId() && config.isDurable())
      {
         return getAdminAdapter().createDurableSubscriptionBrowser(config);
      }
      else
      {
         return sessionManager.createBrowser(this, config);
      }
   }

   /**
    * Create a browser.
    */
   public QueueBrowser createBrowser(Destination destination) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.createBrowser(this, destination, null);
   }

   /**
    * Create a browser.
    */
   public QueueBrowser createBrowser(Destination destination, String selector) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.createBrowser(this, destination, selector);
   }

   /**
    * createBytesMessage.
    */
   public final BytesMessage createBytesMessage() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getSession().createBytesMessage();
   }

   /**
    * createMapMessage.
    */
   public final MapMessage createMapMessage() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getSession().createMapMessage();
   }

   /**
    * createObjectMessage.
    */
   public final ObjectMessage createObjectMessage() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getSession().createObjectMessage();
   }

   /**
    * createStreamMessage.
    */
   public final StreamMessage createStreamMessage() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getSession().createStreamMessage();
   }

   /**
    * createTextMessage.
    */
   public final TextMessage createTextMessage() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getSession().createTextMessage();
   }

   /**
    * Create a javax.jms.Message
    */
   public Message createMessage() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getSession().createMessage();
   }

   /**
    * createTextMessage.
    */
   public final TextMessage createTextMessage(String s) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getSession().createTextMessage(s);
   }

   /**
    * Get the ConnectionFactory associted with this instance.
    */
   public final javax.jms.ConnectionFactory getConnectionFactory() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getConnectionFactory();
   }

   /**
    * Get the Connection associated with this Hermes
    */
   public Connection getConnection() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getConnection();
   }

   /**
    * Get a JMS destination.
    */
   public final Destination getDestination(String name, Domain domain) throws JMSException, NamingException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      final DestinationConfig dConfig = getDestinationConfig(name, domain);

      if (dConfig != null)
      {
         return sessionManager.getDestination(name, Domain.getDomain(dConfig.getDomain()));
      }
      else
      {
         if (getConnectionFactory() instanceof QueueConnectionFactory)
         {
            return sessionManager.getDestination(name, Domain.QUEUE);
         }
         else
         {
            return sessionManager.getDestination(name, Domain.TOPIC);
         }
      }
   }

   /**
    * Get the domain from any static configuration, falling back to instanceof
    * if that does not exist.
    */
   public Domain getDomain(Destination destination) throws JMSException
   {
      final DestinationConfig dConfig = getDestinationConfig(getDestinationName(destination), Domain.UNKNOWN);

      if (dConfig != null)
      {
         if (dConfig.getDomain() == Domain.QUEUE.getId())
         {
            return Domain.QUEUE;
         }
         else
         {
            return Domain.TOPIC;
         }
      }

      if (destination instanceof Queue)
      {
         return Domain.QUEUE;
      }
      else
      {
         return Domain.TOPIC;
      }
   }

   /**
    * List all destinations that have been pre-configured
    */
   public final Iterator getDestinations()
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getDestinations().iterator();
   }

   /**
    * Get my identifier
    */
   public final String getId()
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getId();
   }

   /**
    * receive.
    */
   public Message receive(Destination d) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (isReadOnly())
      {
         throw new JMSException("Hermes is in read-only mode");
      }

      final Message m = sessionManager.getConsumer(d).receive();

      if (m != null && isAudit())
      {
         getAuditLog().onRead(d, m);
      }

      return m;
   }

   public Message receive(Destination d, String selector) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (isReadOnly())
      {
         throw new JMSException("Hermes is in read-only mode");
      }

      final Message m = sessionManager.getConsumer(d, selector).receive();

      if (m != null && isAudit())
      {
         getAuditLog().onRead(d, m);
      }

      return m;
   }

   public Message receive(Destination d, long ms, String selector) throws javax.jms.JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (isReadOnly())
      {
         throw new JMSException("Hermes is in read-only mode");
      }

      final Message m = sessionManager.getConsumer(d, selector).receive(ms);

      if (m != null && isAudit())
      {
         getAuditLog().onRead(d, m);
      }

      return m;
   }

   /**
    * Receive a message with a timeout.
    */
   public Message receive(Destination d, long ms) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (isReadOnly())
      {
         throw new JMSException("Hermes is in read-only mode");
      }

      final Message m = sessionManager.getConsumer(d).receive(ms);

      if (m != null && isAudit())
      {
         getAuditLog().onRead(d, m);
      }

      return m;
   }

   /**
    * Receive a message if one is waiting otherwise returns null of no message
    * available.
    */
   public Message receiveNoWait(Destination d) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (isReadOnly())
      {
         throw new JMSException("Hermes is in read-only mode");
      }

      final Message m = sessionManager.getConsumer(d).receiveNoWait();

      if (m != null && isAudit())
      {
         getAuditLog().onRead(d, m);
      }

      return m;
   }

   /**
    * Receive a message with a selector if one is waiting otherwise returns null
    * of no message available.
    */
   public Message receiveNoWait(Destination d, String selector) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (isReadOnly())
      {
         throw new JMSException("Hermes is in read-only mode");
      }

      final Message m = sessionManager.getConsumer(d, selector).receiveNoWait();

      if (m != null && isAudit())
      {
         getAuditLog().onRead(d, m);
      }

      return m;

   }

   /**
    * Rollback everything since last commit or rollback.
    */
   public void rollback() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (sessionManager.getSession().getTransacted())
      {
         sessionManager.getSession().rollback();
      }
      else
      {
         cat.warn("session " + getId() + " is not transacted");
      }

      if (isAudit())
      {
         getAuditLog().rollback();
      }
   }

   /**
    * Send messge to destination
    */
   public void send(Destination d, Message m) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
      boolean try102 = false;

      if (isReadOnly())
      {
         throw new JMSException("Hermes is in read-only mode");
      }

      MessageProducer producer = sessionManager.getProducer();

      // http://hermesjms.com/jira/browse/HJMS-11
      
      if (m.getJMSPriority() < 0)
      {
         m.setJMSPriority(0) ;
      }
      
      if (m.getJMSExpiration() < 0)
      {
         m.setJMSExpiration(0) ;
      }
      
      try
      {
         producer.send(d, m, m.getJMSDeliveryMode(), m.getJMSPriority(), m.getJMSExpiration());
      }
      catch (NoSuchMethodError ex)
      {
         try102 = true;
      }
      catch (AbstractMethodError ex)
      {
         try102 = true;
      }

      /*
       * Try JMS 1.0.2b interface
       */
      if (try102)
      {
         if (JMSUtils.isQueue(d))
         {
            final QueueSender sender = (QueueSender) producer;

            if (d != null)
            {
               sender.send((Queue) d, m, m.getJMSDeliveryMode(), m.getJMSPriority(), m.getJMSExpiration());
            }
            else
            {
               sender.send(m, m.getJMSDeliveryMode(), m.getJMSPriority(), m.getJMSExpiration());
            }
         }
         else
         {
            final TopicPublisher publisher = (TopicPublisher) producer;

            if (d != null)
            {
               publisher.publish((Topic) d, m, m.getJMSDeliveryMode(), m.getJMSPriority(), m.getJMSExpiration());
            }
            else
            {
               publisher.publish(m, m.getJMSDeliveryMode(), m.getJMSPriority(), m.getJMSExpiration());
            }
         }
      }

      if (isAudit())
      {
         getAuditLog().onWrite(d, m);
      }
   }

   /**
    * Set the callback listener for a destination.
    */
   public void setMessageListener(final javax.jms.Destination d, final javax.jms.MessageListener l) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (isReadOnly())
      {
         throw new JMSException("Hermes is in read-only mode");
      }

      if (isAudit())
      {
         MessageListener auditListener = new MessageListener()
         {
            public void onMessage(Message arg0)
            {
               try
               {
                  getAuditLog().onRead(d, arg0);
               }
               catch (JMSException e)
               {
                  cat.error(e.getMessage(), e);
               }

               l.onMessage(arg0);
            }
         };

         defaultDespatcher.setMessageListener(d, auditListener);
      }
      else
      {
         defaultDespatcher.setMessageListener(d, l);
      }
   }

   /**
    * Get a long description of this Hermes, suitable for use in debugging
    */
   public String toString()
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.toString();
   }

   /**
    * Is the javax.jms.ConnectionFactory in the Queue domain?
    */
   public final boolean isQueue() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return JMSUtils.isQueue(getConnectionFactory());
   }

   /**
    * Is the javax.jms.ConnectionFactory in the Topic domain?
    */
   public final boolean isTopic() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return !JMSUtils.isQueue(getConnectionFactory());
   }

   /**
    * Get the meta data
    */
   public ProviderMetaData getMetaData() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      if (metaData == null)
      {
         metaData = new DefaultProviderMetaData(this, sessionManager.getFactoryConfig(), true);
      }

      return metaData;
   }

   /**
    * Create a copy of a message
    */
   public Message duplicate(Destination to, Message in) throws JMSException
   {
      HermesConfig config = HermesBrowser.getBrowser().getConfig();

      Thread.currentThread().setContextClassLoader(contextClassLoader);

      Message out = null;

      if (in instanceof TextMessage)
      {
         out = createTextMessage(((TextMessage) in).getText());
      }
      else if (in instanceof ObjectMessage)
      {
         out = createObjectMessage();
         ((ObjectMessage) out).setObject(((ObjectMessage) in).getObject());

      }
      else if (in instanceof MapMessage)
      {
         out = createMapMessage();
         final MapMessage inMap = (MapMessage) in;
         final MapMessage outMap = (MapMessage) out;

         for (Enumeration iter = inMap.getMapNames(); iter.hasMoreElements();)
         {
            String key = (String) iter.nextElement();

            outMap.setObject(key, inMap.getObject(key));
         }
      }
      else if (in instanceof StreamMessage)
      {
         out = createStreamMessage();
         final StreamMessage inS = (StreamMessage) in;
         final StreamMessage outS = (StreamMessage) out;

         try
         {
            for (;;)
            {
               outS.writeObject(inS.readObject());
            }
         }
         catch (MessageEOFException ex2)
         {
            // NOP
         }
      }
      else if (in instanceof BytesMessage)
      {
         out = createBytesMessage();
         final BytesMessage inBytes = (BytesMessage) in;
         final BytesMessage outBytes = (BytesMessage) out;

         try
         {
            outBytes.writeBytes(MessageUtils.asBytes(inBytes));
         }
         catch (MessageEOFException ex2)
         {
            // NOP
         }
      }
      else
      {
         throw new HermesException("Cannot handle " + in.getClass().getName());
      }

      try
      {
         out.setJMSDestination(to);
      }
      catch (Exception ex)
      {
         cat.error(ex.getMessage(), ex);
      }
      //
      // Header properties

      if (config.isCopyJMSCorrelationID())
      {
         try
         {
            out.setJMSCorrelationID(in.getJMSCorrelationID());
         }
         catch (JMSException ex)
         {
            cat.error(ex.getMessage(), ex);
         }
      }

      if (config.isCopyJMSExpiration())
      {
         try
         {
            out.setJMSExpiration(in.getJMSExpiration());
         }
         catch (JMSException ex)
         {
            cat.error(ex.getMessage(), ex);
         }
      }

      if (config.isCopyJMSPriority())
      {
         try
         {
            out.setJMSPriority(in.getJMSPriority());
         }
         catch (JMSException ex)
         {
            cat.error(ex.getMessage(), ex);
         }
      }

      if (config.isCopyJMSReplyTo())
      {
         try
         {
            if (in.getJMSReplyTo() != null)
            {
               out.setJMSReplyTo(getDestination(getDestinationName(in.getJMSReplyTo()), Domain.getDomain(in.getJMSReplyTo())));
            }
         }

         catch (JMSException ex)
         {
            cat.error(ex.getMessage(), ex);
         }
         catch (NamingException ex)
         {
            throw new HermesRuntimeException(ex);
         }
      }

      if (config.isCopyJMSType())
      {
         try
         {
            out.setJMSType(in.getJMSType());
         }
         catch (JMSException ex)
         {
            cat.error(ex.getMessage(), ex);
         }
      }

      if (in.getPropertyNames() != null)
      {
         for (final Enumeration iter = in.getPropertyNames(); iter.hasMoreElements();)
         {
            final String key = (String) iter.nextElement();

            //
            // Dont copy over provider properties unless
            // copyJMSProviderProperties
            // is set.

            if (!key.startsWith("JMS") || config.isCopyJMSProviderProperties())
            {
               out.setObjectProperty(key, in.getObjectProperty(key));
            }
         }
      }

      return out;
   }

   /**
    * Create a copy of a message
    */
   public Message duplicate(Message in) throws JMSException
   {
      return duplicate(null, in);
   }

   /**
    * Get my session manager
    */
   protected SessionManager getSessionManager()
   {
      return sessionManager;
   }

   /**
    * @see hermes.Hermes#schedule(TimerTask, long, boolean)
    */
   public void schedule(final TimerTask task, final long delay, final boolean repeating) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      TimerTask timerTask = new TimerTask()
      {
         public void run()
         {
            try
            {
               invoke(task);
            }
            catch (JMSException ex)
            {
               cat.error(ex.getMessage(), ex);
            }
         }
      };

      timerMap.put(task, timerTask);

      if (repeating)
      {
         timer.schedule(task, delay, delay);
      }
      else
      {
         timer.schedule(task, delay);
      }
   }

   /**
    * @see hermes.Hermes#cancel(TimerTask)
    */
   public void cancel(TimerTask task) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      TimerTask timerTask;

      if ((timerTask = (TimerTask) timerMap.remove(task)) == null)
      {
         throw new JMSException("Unknown timer");
      }

      timerTask.cancel();

   }

   public String getDestinationName(Destination to) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return JMSUtils.getDestinationName(to);
   }

   /**
    * @see hermes.Hermes#getDestinationConfig(Destination)
    */
   public DestinationConfig getDestinationConfig(String d, Domain domain) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return sessionManager.getDestinationConfig(d, domain);
   }

   /**
    * @see hermes.Hermes#getDispatcher(String)
    */
   public HermesDispatcher getDispatcher(String name) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return _getDispatcher(name);
   }

   private final DefaultHermesDispatcherImpl _getDispatcher(String name) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      DefaultHermesDispatcherImpl dispatcher;

      synchronized (dispatchers)
      {
         if (dispatchers.containsKey(name))
         {
            dispatcher = (DefaultHermesDispatcherImpl) dispatchers.get(name);
         }
         else
         {
            dispatcher = new DefaultHermesDispatcherImpl(this);
            dispatchers.put(name, dispatcher);
         }
      }

      return dispatcher;
   }

   /**
    * Clean up the dispatcher, called from HermesDispatcher.close() ;
    */
   final void removeDispatcher(HermesDispatcher dispatcher)
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      synchronized (dispatchers)
      {
         dispatchers.remove(dispatcher);
      }
   }

   /**
    * @see hermes.Hermes#setMessageListener(HermesDispatcher, Destination,
    *      MessageListener)
    */
   public void setMessageListener(HermesDispatcher dispatcher, Destination d, MessageListener ml) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      dispatcher.setMessageListener(d, ml);
   }

   /**
    * @see hermes.Hermes#invoke(Runnable)
    */
   public void invoke(Runnable runnable) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      defaultDespatcher.invoke(runnable);
   }

   /**
    * @see hermes.Hermes#invokeAll(Runnable)
    */
   public void invokeAll(Runnable runnable) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      synchronized (dispatchers)
      {
         for (Iterator iter = dispatchers.entrySet().iterator(); iter.hasNext();)
         {
            Map.Entry entry = (Map.Entry) iter.next();
            HermesDispatcher dispatcher = (HermesDispatcher) entry.getValue();

            dispatcher.invoke(runnable);
         }
      }
   }

   /**
    * @see hermes.Hermes#invokeAllAndWait(Runnable)
    */
   public void invokeAllAndWait(Runnable runnable) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      synchronized (dispatchers)
      {
         for (Iterator iter = dispatchers.entrySet().iterator(); iter.hasNext();)
         {
            Map.Entry entry = (Map.Entry) iter.next();
            HermesDispatcher dispatcher = (HermesDispatcher) entry.getValue();

            dispatcher.invokeAndWait(runnable);
         }
      }
   }

   /**
    * @see hermes.Hermes#invokeAndWait(Runnable)
    */
   public void invokeAndWait(Runnable runnable) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      defaultDespatcher.invokeAndWait(runnable);
   }

   /**
    * @return
    */
   public boolean isReadOnly()
   {
      return readOnly;
   }

   /**
    * @param b
    */
   public void setReadOnly(boolean b)
   {
      readOnly = b;
   }

   public Queue createQueue(String queueName) throws JMSException, NamingException
   {
      return (Queue) getDestination(queueName, Domain.QUEUE);
   }

   public Topic createTopic(String topicName) throws JMSException, NamingException
   {
      return (Topic) getDestination(topicName, Domain.TOPIC);
   }

   public boolean isAudit() throws JMSException
   {
      return sessionManager.isAudit() && getTransacted();
   }

   public HermesAuditLog getAuditLog() throws JMSException
   {
      try
      {
         HermesAuditLog rval = (HermesAuditLog) auditLogTL.get();

         if (rval == null)
         {
            rval = new DefaultHermesLogImpl(this, sessionManager.getAuditDirectory(), true, true);
            auditLogTL.set(rval);
         }

         return rval;
      }
      catch (IOException e)
      {
         throw new HermesException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.Hermes#fromXML(java.io.InputStream)
    */
   public Collection fromXML(InputStream istream) throws JMSException, IOException
   {
      return xmlSupport.fromXML(this, istream);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.Hermes#fromXML(java.lang.String)
    */
   public Collection fromXML(String document) throws JMSException
   {
      return xmlSupport.fromXML(this, document);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.Hermes#toXML(java.util.Collection, java.io.OutputStream)
    */
   public void toXML(Collection messages, OutputStream ostream) throws JMSException, IOException
   {
      xmlSupport.toXML(messages, ostream);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.Hermes#toXML(java.util.Collection)
    */
   public String toXML(Collection messages) throws JMSException
   {
      return xmlSupport.toXML(messages);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.MessageFactory#createObjectMessage(java.io.Serializable)
    */
   public ObjectMessage createObjectMessage(Serializable object) throws JMSException
   {
      return sessionManager.getSession().createObjectMessage(object);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.Hermes#isConnectable()
    */
   public boolean isConnectable() throws JMSException
   {
      sessionManager.getSession();

      sessionManager.close();

      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.Hermes#toXML(javax.jms.Message, java.io.OutputStream)
    */
   public void toXML(Message message, OutputStream ostream) throws JMSException, IOException
   {
      Collection c = new ArrayList();
      c.add(message);

      toXML(c, ostream);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.Hermes#toXML(javax.jms.Message)
    */
   public String toXML(Message message) throws JMSException
   {
      Collection c = new ArrayList();
      c.add(message);

      return toXML(c);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.Hermes#addDestinationConfig(hermes.config.DestinationConfig)
    */
   public void addDestinationConfig(DestinationConfig dConfig) throws JMSException
   {
      sessionManager.getConnectionFactoryManager().addDestinationConfig(dConfig);
   }

   /**
    * Remove a destination configuration
    */
   public void removeDestinationConfig(DestinationConfig dConfig) throws JMSException
   {
      sessionManager.getConnectionFactoryManager().removeDestinationConfig(dConfig);
   }

   public long getAge(DestinationConfig dest) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return getAdminAdapter().getAge(dest);
   }

   public MessageRenderer getMessageRenderer() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return getAdminAdapter().getMessageRenderer();
   }

   public int getDepth(DestinationConfig dest) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return getAdminAdapter().getDepth(dest);
   }

   public Collection getStatistics(Collection destinations) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return getAdminAdapter().getStatistics(destinations);
   }

   public Map getStatistics(DestinationConfig dConfig) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return getAdminAdapter().getStatistics(dConfig);
   }

   public QueueBrowser createDurableSubscriptionBrowser(DestinationConfig dConfig) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return getAdminAdapter().createDurableSubscriptionBrowser(dConfig);
   }

   public Collection discoverDestinationConfigs() throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return getAdminAdapter().discoverDestinationConfigs();
   }

   public int truncate(DestinationConfig dest) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return getAdminAdapter().truncate(dest);
   }

   public Enumeration createBrowserProxy(Enumeration iter) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return getAdminAdapter().createBrowserProxy(iter);
   }

   public QueueBrowser createRegexBrowser(Destination d, String selector, String regex) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return new RegexQueueBrowser(createBrowser(d, selector), regex, false, true);
   }

   public QueueBrowser createRegexBrowser(Destination d, String regex) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      return new RegexQueueBrowser(createBrowser(d), regex, false, true);
   }

   public void unsubscribe(String name) throws JMSException
   {
      Thread.currentThread().setContextClassLoader(contextClassLoader);

      getSessionManager().unsubscribe(name);

   }

   public DestinationManager getDestinationManager()
   {
      return getSessionManager().getDestinationManager();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof Hermes)
      {
         Hermes other = (Hermes) obj;

         return getId().equals(other.getId());
      }
      else
      {
         return false;
      }
   }
}