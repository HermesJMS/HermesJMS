/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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

package hermes.ext.sonicmq;

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
import java.util.Hashtable;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

import com.sonicsw.mf.jmx.client.JMSConnectorAddress;
import com.sonicsw.mf.jmx.client.JMSConnectorClient;
import com.sonicsw.mq.common.runtime.IDurableSubscriptionData;
import com.sonicsw.mq.common.runtime.IQueueData;
import com.sonicsw.mq.mgmtapi.runtime.IBrokerProxy;
import com.sonicsw.mq.mgmtapi.runtime.MQProxyFactory;

/**
 * Plugin for SonicMQ. This plugin does not support providing any destination
 * level statistics.
 * 
 * @author colincrist@hermesjms.com
 */
public class SonicMQAdmin extends HermesAdminSupport implements HermesAdmin
{
   private static final Logger log = Logger.getLogger(SonicMQAdmin.class);
   private SonicMQAdminFactory adminFactory;

   private JMSConnectorClient connector;
   private IBrokerProxy brokerProxy;

   /**
    * @param hermes
    */
   public SonicMQAdmin(Hermes hermes, SonicMQAdminFactory adminFactory)
   {
      super(hermes);

      this.adminFactory = adminFactory;
   }

   private synchronized IBrokerProxy getBrokerProxy() throws JMSException
   {
      if (brokerProxy == null)
      {
         Hashtable env = new Hashtable();

         env.put("ConnectionURLs", adminFactory.getConnectionURL());
         env.put("DefaultUser", adminFactory.getDefaultUser());
         env.put("DefaultPassword", adminFactory.getDefaultPassword());

         JMSConnectorAddress address = new JMSConnectorAddress(env);

         connector = new JMSConnectorClient();
         connector.connect(address, adminFactory.getTimeout());

         // Now get the broker proxy...

         try
         {
            ObjectName jmxName = new ObjectName(adminFactory.getDomain() + "." + adminFactory.getContainer() + ":ID=" + adminFactory.getBrokerName());

            return MQProxyFactory.createBrokerProxy(connector, jmxName);
         }
         catch (MalformedObjectNameException e)
         {
            throw new HermesException(e.getMessage(), e);
         }
      }
      else
      {
         return brokerProxy;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.ext.HermesAdminSupport#getDepth(javax.jms.Destination)
    */
   public synchronized int getDepth(DestinationConfig dConfig) throws JMSException
   {
      if (dConfig.getDomain() == Domain.QUEUE.getId())
      {
         final ArrayList<IQueueData> queues = getBrokerProxy().getQueues(getRealDestinationName(dConfig));

         if (queues.size() > 1)
         {
            log.warn("getDepth() for " + dConfig.getName() + " matches " + queues.size() + " queues");
         }

         if (queues.size() == 0)
         {
            throw new HermesException("getQueues() for " + dConfig.getName() + " has returned no data");
         }

         final IQueueData queueData = (IQueueData) queues.get(0);

         return queueData.getMessageCount();
      }
      else
      {
         if (dConfig.isDurable())
         {
            final ArrayList<IDurableSubscriptionData> durables = getBrokerProxy().getDurableSubscriptions(getRealDestinationName(dConfig));

            for (IDurableSubscriptionData data : durables)
            {
               if (data.getClientID() == null && getHermes().getConnection().getClientID() == null
                     || data.getClientID().equals(getHermes().getConnection().getClientID()))
               {
                  if (data.getSubscriptionName().equals(dConfig.getClientID()))
                  {
                     return (int) data.getMessageCount();
                  }
               }
            }

            throw new HermesException("Durable subscription " + dConfig.getClientID() + " on topic " + dConfig.getName() + " cannot be found");
         }
         else
         {
            throw new HermesException("SonicMQ cannot give the depth of a non-durable topic");
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesAdmin#close()
    */
   public synchronized void close() throws JMSException
   {
      if (brokerProxy != null)
      {
         brokerProxy.stop();
         brokerProxy = null;
      }

      if (connector != null)
      {
         connector.disconnect();
         connector = null;
      }
   }

   public synchronized Collection discoverDestinationConfigs() throws JMSException
   {
      if (getHermes().getConnectionFactory() instanceof JNDIConnectionFactory)
      {
         return super.discoverDestinationConfigs();
      }
      else
      {
         try
         {
            final ArrayList rval = new ArrayList();

            for (Iterator queues = getBrokerProxy().getQueues(null).iterator(); queues.hasNext();)
            {
               final IQueueData queueData = (IQueueData) queues.next();
               final DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();

               dConfig.setName(queueData.getQueueName());
               dConfig.setDomain(Domain.QUEUE.getId());

               rval.add(dConfig);
            }

            return rval;
         }
         catch (JMSException e)
         {
            brokerProxy = null;
            throw e;
         }
      }
   }

   public int truncate(DestinationConfig dConfig) throws JMSException
   {
      
         if (dConfig.getDomain() == Domain.QUEUE.getId())
         {
            try
            {
            final int rval = getDepth(dConfig);
            final ArrayList<String> queues = new ArrayList<String>();

            queues.add(getRealDestinationName(dConfig));
            getBrokerProxy().deleteQueueMessages(queues);

            return rval;
            }
            catch (JMSException e)
            {
               brokerProxy = null;
               throw e;
            }
         }
         else
         {
           throw new HermesException("SonicMQ does not support truncating topic subscriptions") ;
         }
      
   }
}