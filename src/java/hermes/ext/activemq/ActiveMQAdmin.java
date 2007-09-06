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

package hermes.ext.activemq;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ActiveMQAdmin.java,v 1.3 2006/07/13 07:35:32 colincrist Exp $
 */

public class ActiveMQAdmin extends HermesAdminSupport implements hermes.HermesAdmin
{
   private static final Logger log = Logger.getLogger(ActiveMQAdmin.class);
   private JMXServiceURL serviceURL;
   private JMXConnector connector;
   private MBeanServerConnection server;
 
   private String[] queueProperties = new String[] { "ConsumerCount", "EnqueueCount", "DequeueCount", "MemoryLimit", "MemoryPercentageUsed", "QueueSize" };
   private ActiveMQAdminFactory factory ;
   
   public ActiveMQAdmin(ActiveMQAdminFactory factory, Hermes hermes, String brokerName, JMXServiceURL serviceURL)
   {
      super(hermes);

      this.serviceURL = serviceURL;
      this.factory = factory ;
   }

   @Override
   public int getDepth(DestinationConfig destination) throws JMSException
   {
      try
      {
         if (destination.getDomain() == Domain.QUEUE.getId())
         {
            ObjectName objectName = getDestinationObjectName(destination);
            return ((Long) getConnection().getAttribute(objectName, "QueueSize")).intValue();
         }
         else if (destination.getDomain() == Domain.TOPIC.getId() && destination.isDurable())
         {
            ObjectName objectName = getDestinationObjectName(destination);
            return ((Integer) getConnection().getAttribute(objectName, "PendingQueueSize")).intValue();
         }
         else
         {
            return 0;
         }
      }
      catch (Exception ex)
      {
         close();

         log.error(ex.getMessage(), ex);
         throw new HermesException(ex);
      }
   }

   @Override
   public QueueBrowser createDurableSubscriptionBrowser(DestinationConfig dConfig) throws JMSException
   {
      try
      {
         /**
          * ObjectName objectName = getDestinationObjectName(dConfig);
          * TabularData data = (TabularData) getConnection().invoke(objectName,
          * "browseAsTable", new Object[] {}, new String[] {});
          * log.debug(data.size() + " messages");
          */

         return super.createDurableSubscriptionBrowser(dConfig);
      }
      catch (Exception ex)
      {
         throw new HermesException(ex);
      }
   }

   public Object getAttributeQuietly(ObjectName objectName, String attribute)
   {
      try
      {
         return getConnection().getAttribute(objectName, attribute);
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
         return e.getMessage() ;
      }

   }

   @Override
   public Collection<DestinationConfig> discoverDestinationConfigs() throws JMSException
   {
      Collection<DestinationConfig> rval = new HashSet<DestinationConfig>();

      
      try
      {
         ObjectName[] queues = (ObjectName[]) getConnection().getAttribute(getBrokerObjectName(), "Queues");

         for (ObjectName queue : queues)
         {
            String name = (String) getConnection().getAttribute(queue, "Name");

            rval.add(HermesBrowser.getConfigDAO().createDestinationConfig(name, Domain.QUEUE));
         }
      

     
         ObjectName[] topics = (ObjectName[]) getConnection().getAttribute(getBrokerObjectName(), "Topics");

         for (ObjectName topic : topics)
         {
            String name = (String) getConnection().getAttribute(topic, "Name");

            rval.add(HermesBrowser.getConfigDAO().createDestinationConfig(name, Domain.TOPIC));
         }
      }
      catch (Exception ex)
      {
         throw new HermesException(ex) ;
      }

      rval.addAll(_discoverDurableSubscriptions());
      return rval;
   }

   protected Collection<DestinationConfig> _discoverDurableSubscriptions() throws JMSException
   {
      Collection<DestinationConfig> rval = new HashSet<DestinationConfig>();

      try
      {
         ObjectName[] subs = (ObjectName[]) getConnection().getAttribute(getBrokerObjectName(), "DurableTopicSubscribers");

         for (ObjectName sub : subs)
         {
            String name = (String) getConnection().getAttribute(sub, "DestinationName");
            String subscriptionName = (String) getConnection().getAttribute(sub, "SubscriptionName");
            DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig(name, Domain.TOPIC);
            dConfig.setDurable(true);
            dConfig.setClientID(subscriptionName);

            rval.add(dConfig);
         }
      }
      catch (Exception e)
      {
         throw new HermesException(e) ;
      }

      try
      {
         ObjectName[] subs = (ObjectName[]) getConnection().getAttribute(getBrokerObjectName(), "InactiveDurableTopicSubscribers");

         for (ObjectName sub : subs)
         {
            String name = (String) getConnection().getAttribute(sub, "DestinationName");
            String subscriptionName = (String) getConnection().getAttribute(sub, "SubscriptionName");
            DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig(name, Domain.TOPIC);
            dConfig.setDurable(true);
            dConfig.setClientID(subscriptionName);

            rval.add(dConfig);
         }
      }
      catch (Exception e)
      {
         throw new HermesException(e) ;
      }

      return rval;
   }

   @Override
   public Map getStatistics(DestinationConfig destination) throws JMSException
   {
      Map<String, Object> rval = new HashMap<String, Object>();

      try
      {
         ObjectName objectName = getDestinationObjectName(destination);
         MBeanInfo objectInfo = getConnection().getMBeanInfo(objectName);

         for (MBeanAttributeInfo info : objectInfo.getAttributes())
         {
            Object o = getConnection().getAttribute(objectName, info.getName());
            rval.put(info.getName(), o);
         }
      }
      catch (Exception ex)
      {
         close();
         log.error(ex.getMessage(), ex);
         throw new HermesException(ex);
      }

      return rval;
   }

   /**
    * Convert a Hermes destination configuration object into an ActiveMQ JMX
    * ObjectName.
    * 
    * @param destination
    * @return
    * @throws MalformedObjectNameException
    * @throws NullPointerException
    * @throws JMSException
    */
   private ObjectName getDestinationObjectName(DestinationConfig destination) throws MalformedObjectNameException, NullPointerException, JMSException
   {
      if (destination.getDomain() == Domain.QUEUE.getId())
      {
         return new ObjectName("org.apache.activemq:BrokerName=" + factory.getBrokerName() + ",Type=Queue,Destination=" + destination.getName());
      }
      else
      {
         if (destination.isDurable())
         {
            try
            {
               return new ObjectName("org.apache.activemq:BrokerName=" + factory.getBrokerName() + ",Type=Subscription,active=true,name="
                     + getHermes().getConnection().getClientID() + "_" + destination.getClientID());
            }
            catch (MalformedObjectNameException e)
            {
               return new ObjectName("org.apache.activemq:BrokerName=" + factory.getBrokerName() + ",Type=Subscription,active=false,name="
                     + getHermes().getConnection().getClientID() + "_" + destination.getClientID());
            }
         }
         else
         {
            return new ObjectName("org.apache.activemq:BrokerName=" + factory.getBrokerName() + ",Type=Topic,Destination=" + destination.getName());
         }
      }
   }

   private ObjectName getBrokerObjectName() throws MalformedObjectNameException, NullPointerException
   {
      return new ObjectName("org.apache.activemq:BrokerName=" + factory.getBrokerName() + ",Type=Broker");
   }

   public synchronized void close() throws JMSException
   {
      try
      {
         if (connector != null)
         {
            connector.close();

            log.debug("closed ActiveMQ JMX connection to " + factory.getBrokerName());
         }
      }
      catch (IOException ex)
      {
         log.error(ex.getMessage(), ex);
      }
      finally
      {
         connector = null;
         server = null;
      }
   }

   private Map getMap()
   {
      if (factory.getUsername() == null)
      {
         return Collections.EMPTY_MAP;
      }
      else
      {
         final Map rval = new HashMap() ;
         rval.put("jmx.remote.credentials", new String[] { factory.getUsername(), factory.getPassword() }) ;
         return rval ;      
      }
   }

   private synchronized void connect() throws JMSException
   {
      close();

      try
      {
         connector = JMXConnectorFactory.connect(serviceURL, getMap());
         server = connector.getMBeanServerConnection();
      }
      catch (Exception ex)
      {
         throw new HermesException(ex);
      }
   }

   private synchronized MBeanServerConnection getConnection() throws JMSException
   {
      if (server == null)
      {
         connect();
      }

      return server;
   }
}
