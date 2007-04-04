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

package hermes.ext.jbossmq;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.log4j.Logger;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.mq.DurableSubscriptionID;
import org.jboss.mq.SpyConnectionFactory;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JBossMQAdmin.java,v 1.18 2006/07/13 07:35:32 colincrist Exp $
 */
public class JBossMQAdmin extends HermesAdminSupport implements HermesAdmin
{
   private static final Logger log = Logger.getLogger(JBossMQAdmin.class);
   private JNDIConnectionFactory jndiCF;
   private SpyConnectionFactory spyCF;
   private RMIAdaptor rmiAdaptor;
   private JBossMQAdminFactory factory;

   /**
    *  
    */
   public JBossMQAdmin(JBossMQAdminFactory factory, Hermes hermes, JNDIConnectionFactory jndiCF, ConnectionFactory spyCF) throws JMSException
   {
      super(hermes);

      this.jndiCF = jndiCF;
      this.spyCF = (SpyConnectionFactory) spyCF;
      this.factory = factory;

      if (factory.getRmiAdaptorBinding() == null)
      {
         throw new HermesException("You must set rmiAdaptorBinding in the Admin Factory ");
      }
   }

   private RMIAdaptor getRMIAdapter() throws NamingException, JMSException
   {
      if (rmiAdaptor == null)
      {
         Context ctx = jndiCF.createContext();

         rmiAdaptor = (RMIAdaptor) ctx.lookup(factory.getRmiAdaptorBinding());
      }

      return rmiAdaptor;

   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.ProviderExtensionSession#size(javax.jms.Destination)
    */
   public int getDepth(DestinationConfig dConfig) throws JMSException
   {
      ObjectName objectName = null;

      try
      {
         if (dConfig.getDomain() == Domain.QUEUE.getId())
         {
            objectName = new ObjectName("jboss.mq.destination:name=" + getRealDestinationName(dConfig) + ",service=Queue");

            return Integer.parseInt((getRMIAdapter().getAttribute(objectName, "QueueDepth")).toString());
         }
         else
         {
            if (dConfig.isDurable())
            {
               throw new HermesException("JBoss does not support depth of a durable subscription");
            }
            else
            {
               return 0 ;
            }
         }
      }
      catch (HermesException ex)
      {
         throw ex ;
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);

         rmiAdaptor = null;
         throw new HermesException(e);
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.ProviderExtensionSession#close()
    */
   public void close() throws JMSException
   {
      // NOP
   }

   public Map getStatistics(DestinationConfig dConfig) throws JMSException
   {
      final Map stats = new LinkedHashMap();
      ObjectName objectName = null;

      try
      {
         if (dConfig.getDomain() == Domain.QUEUE.getId())
         {

            objectName = new ObjectName("jboss.mq.destination:name=" + getRealDestinationName(dConfig) + ",service=Queue");

            try
            {
               stats.put("QueueName", getRMIAdapter().getAttribute(objectName, "QueueName"));
            }
            catch (JMSException ex)
            {
               log.error("Unable to get QueueName property", ex);
            }

            try
            {
               stats.put("ScheduledMessageCount", getRMIAdapter().getAttribute(objectName, "ScheduledMessageCount"));
            }
            catch (JMSException ex)
            {
               log.error("Unable to get ScheduledMessageCount property", ex);
            }

            try
            {
               stats.put("ReceiversCount", getRMIAdapter().getAttribute(objectName, "ReceiversCount"));
            }
            catch (JMSException ex)
            {
               log.error("Unable to get ReceiversCount property", ex);
            }
         }
         else
         {
            objectName = new ObjectName("jboss.mq.destination:name=" + getRealDestinationName(dConfig) + ",service=Topic");

            try
            {
               stats.put("TopicName", getRMIAdapter().getAttribute(objectName, "TopicName"));
            }
            catch (JMSException ex)
            {
               log.error("Unable to get TopicName property", ex);
            }
         }

         try
         {
            stats.put("StateString", getRMIAdapter().getAttribute(objectName, "StateString"));
         }
         catch (JMSException ex)
         {
            log.error("Unable to get StateString property", ex);
         }

         try
         {
            stats.put("State", getRMIAdapter().getAttribute(objectName, "State"));
         }
         catch (JMSException ex)
         {
            log.error("Unable to get State property", ex);
         }
      }
      catch (Exception e)
      {
         throw new HermesException(e);
      }

      return stats;
   }

   @Override
   protected Collection discoverDurableSubscriptions(String topicName, String jndiName) throws JMSException
   {
      final Collection<DestinationConfig> rval = new ArrayList<DestinationConfig>();

      try
      {
         final ObjectName objectName = new ObjectName("jboss.mq.destination:name=" + topicName + ",service=Topic");
         final Collection<DurableSubscriptionID> durableSubs = (Collection<DurableSubscriptionID>) getRMIAdapter().invoke(objectName,
               "listDurableSubscriptions", new String[0], new String[0]);

         for (final DurableSubscriptionID id : durableSubs)
         {
            if (getHermes().getConnection().getClientID() != null && getHermes().getConnection().getClientID().equals(id.getClientID()))
            {
               final DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();

               dConfig.setClientID(id.getSubscriptionName());
               dConfig.setName(jndiName == null ? topicName : jndiName);
               dConfig.setDomain(Domain.TOPIC.getId());
               dConfig.setSelector(id.getSelector());
               dConfig.setDurable(true);

               rval.add(dConfig);
            }
            else
            {
               log.debug("skipping subscription name=" + id.getSubscriptionName() + " as its not for this connection clientID");
            }
         }

      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
      }

      return rval;
   }

   @Override
   public int truncate(DestinationConfig dConfig) throws JMSException
   {
      int rval = getDepth(dConfig);

      try
      {
         if (dConfig.getDomain() == Domain.QUEUE.getId())
         {
            final ObjectName objectName = new ObjectName("jboss.mq.destination:name=" + getRealDestinationName(dConfig) + ",service=Queue");

            getRMIAdapter().invoke(objectName, "removeAllMessages", new String[0], new String[0]);
         }
         else
         {
            throw new HermesException("JBoss does not support truncating a durable subscription");
         }
      }
      catch (HermesException ex)
      {
         throw ex;
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);

         rmiAdaptor = null;
         throw new HermesException(e);

      }

      return rval;
   }

   public QueueBrowser createDurableSubscriptionBrowser(final DestinationConfig dConfig) throws JMSException
   {
      try
      {
         final ObjectName objectName = new ObjectName("jboss.mq.destination:name=" + getRealDestinationName(dConfig) + ",service=Topic");

         String[] params;
         String[] signature;

         if (dConfig.getSelector() == null)
         {
            params = new String[] { getHermes().getConnection().getClientID(), dConfig.getClientID() };
            signature = new String[] { String.class.getName(), String.class.getName() };
         }
         else
         {
            params = new String[] { getHermes().getConnection().getClientID(), dConfig.getClientID(), dConfig.getSelector() };
            signature = new String[] { String.class.getName(), String.class.getName(), String.class.getName() };
         }

         final Collection messages = (Collection) getRMIAdapter().invoke(objectName, "listDurableMessages", params, signature);

         return new QueueBrowser (){
         
            public void close() throws JMSException
            {
               // TODO Auto-generated method stub               
            }
         
            public Enumeration getEnumeration() throws JMSException
            {
               return new IteratorEnumeration(messages.iterator()) ;
            }
         
            public String getMessageSelector() throws JMSException
            {
               return dConfig.getSelector() ;
            }
         
            public Queue getQueue() throws JMSException
            {
               // TODO Auto-generated method stub
               return null;
            }
         }; 
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
         throw new HermesException(e);
      }
   }

}