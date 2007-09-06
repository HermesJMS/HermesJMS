/* 
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
package hermes.ext.ems;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.tibco.tibjms.admin.DestinationInfo;
import com.tibco.tibjms.admin.DurableInfo;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.TibjmsAdmin;
import com.tibco.tibjms.admin.TibjmsAdminException;
import com.tibco.tibjms.admin.TopicInfo;

/**
 * Administration plugin for Tibco EMS.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: TibcoEMSAdmin.java,v 1.10 2006/02/08 09:17:08 colincrist Exp $
 */
public class TibcoEMSAdmin extends HermesAdminSupport implements HermesAdmin
{
   private static final Logger log = Logger.getLogger(TibcoEMSAdmin.class);

   private TibcoEMSAdminFactory factory;
   private TibjmsAdmin admin;

   /** 
    * 
    */
   public TibcoEMSAdmin(Hermes hermes, TibcoEMSAdminFactory factory)
   {
      super(hermes);

      this.factory = factory;
   }

   private synchronized TibjmsAdmin getAdmin() throws JMSException
   {
      if (admin == null)
      {
         admin = factory.createAdmin(getHermes().getConnectionFactory());
      }

      return admin;
   }

   public synchronized void close() throws JMSException
   {
      try
      {
         if (admin != null)
         {
            admin.close();
            admin = null;
         }
      }
      catch (TibjmsAdminException e)
      {
         throw new HermesException(e);
      }
   }

   public int getDepth(final DestinationConfig destination) throws JMSException
   {
      if (destination.isDurable())
      {
         try
         {
            DurableInfo info = getAdmin().getDurable(destination.getClientID(), getHermes().getConnection().getClientID());

            if (info != null)
            {
               return (int) info.getPendingMessageCount();
            }
            else
            {
               throw new HermesException("No durable information availble for clientID=" + getHermes().getConnection().getClientID() + ", durableName="
                     + destination.getClientID());
            }
         }
         catch (TibjmsAdminException ex)
         {
            throw new HermesException(ex);
         }
      }
      else
      {
         DestinationInfo info = getDestinationInfo(destination);

         if (info != null)
         {
            return (int) info.getPendingMessageCount();
         }
         else
         {
            return 0;
         }
      }
   }

   public Map getStatistics(final DestinationConfig destination) throws JMSException
   {
      try
      {
         final DestinationInfo info = getDestinationInfo(destination);
         final TreeMap rval = new TreeMap();

         rval.putAll(PropertyUtils.describe(info));

         rval.remove("inboundStatistics");
         rval.remove("outboundStatistics");

         rval.put("inboundByteRate", new Long(info.getInboundStatistics().getByteRate()));
         rval.put("inboundMessageRate", new Long(info.getInboundStatistics().getMessageRate()));
         rval.put("inboundTotalBytes", new Long(info.getInboundStatistics().getTotalBytes()));
         rval.put("inboundTotalMessages", new Long(info.getInboundStatistics().getTotalMessages()));

         rval.put("outboundByteRate", new Long(info.getOutboundStatistics().getByteRate()));
         rval.put("outboundMessageRate", new Long(info.getOutboundStatistics().getMessageRate()));
         rval.put("outboundTotalBytes", new Long(info.getOutboundStatistics().getTotalBytes()));
         rval.put("outboundTotalMessages", new Long(info.getOutboundStatistics().getTotalMessages()));

         return rval;
      }
      catch (IllegalAccessException e)
      {
         throw new HermesException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new HermesException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new HermesException(e);
      }
   }

   private DestinationInfo getDestinationInfo(final DestinationConfig dConfig) throws JMSException
   {
      try
      {
         if (dConfig.getDomain() == Domain.QUEUE.getId())
         {
            return getAdmin().getQueue(getRealDestinationName(dConfig));
         }
         else
         {

            return getAdmin().getTopic(getRealDestinationName(dConfig));
         }
      }
      catch (TibjmsAdminException e)
      {
         throw new HermesException(e);
      }
   }

   private DestinationInfo getDestinationInfo(final Destination destination) throws JMSException
   {
      final String name = getHermes().getDestinationName(destination);

      try
      {
         if (destination instanceof Queue)
         {
            return getAdmin().getQueue(name);
         }
         else
         {
            return getAdmin().getTopic(name);
         }
      }
      catch (TibjmsAdminException e)
      {
         throw new HermesException(e);
      }
   }

   public int truncate(final DestinationConfig dConfig) throws JMSException
   {
      try
      {
         final int rval = getDepth(dConfig);

         if (dConfig.getDomain() == Domain.QUEUE.getId())
         {
            getAdmin().purgeQueue(dConfig.getName());
         }
         else
         {
            if (dConfig.isDurable())
            {
               getAdmin().purgeDurable(dConfig.getClientID(), getHermes().getConnection().getClientID());
            }
            else
            {
               getAdmin().purgeTopic(dConfig.getName());
            }
         }

         return rval;
      }
      catch (TibjmsAdminException e)
      {
         throw new HermesException(e);
      }
   }

   public Collection discoverDestinationConfigs() throws JMSException
   {
      /*
       * As Tibco do not implement the browsing of their JNDI contexts, when the
       * connection factory is JNDI we use their admin API instead to get the
       * jndi names of queues/topics. If there is no JNDI name then it's not
       * bound anywhere.
       */

      try
      {
         final Collection<DestinationConfig> rval = new ArrayList<DestinationConfig>();
         
         if (!(getHermes().getConnectionFactory() instanceof TopicConnectionFactory))
         {
            final QueueInfo[] qinfos = getAdmin().getQueues();

            for (int i = 0; i < qinfos.length; i++)
            {
               if (getHermes().getConnectionFactory() instanceof JNDIConnectionFactory)
               {
                  if (qinfos[i].getJNDINames() != null)
                  {
                     for (int j = 0; j < qinfos[i].getJNDINames().length; j++)
                     {
                        final DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();

                        dConfig.setName(qinfos[i].getJNDINames()[j]);
                        dConfig.setDomain(Domain.QUEUE.getId());
                        rval.add(dConfig);

                     }
                  }
               }
               else
               {
                  final DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();

                  dConfig.setName(qinfos[i].getName());
                  dConfig.setDomain(Domain.QUEUE.getId());
                  rval.add(dConfig);
               }
            }
         }

         if (!(getHermes().getConnectionFactory() instanceof QueueConnectionFactory))
         {
            final TopicInfo[] tinfos = getAdmin().getTopics();

            for (int i = 0; i < tinfos.length; i++)
            {
               if (getHermes().getConnectionFactory() instanceof JNDIConnectionFactory)
               {
                  if (tinfos[i].getJNDINames() != null)
                  {
                     for (int j = 0; j < tinfos[i].getJNDINames().length; j++)
                     {
                        final DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();

                        dConfig.setName(tinfos[i].getJNDINames()[j]);
                        dConfig.setDomain(Domain.TOPIC.getId());
                        rval.add(dConfig);
                        rval.addAll(discoverDurableSubscriptions(tinfos[i].getName(), tinfos[i].getJNDINames()[j]));
                     }
                  }
               }
               else
               {
                  final DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();
                  dConfig.setName(tinfos[i].getName());

                  dConfig.setDomain(Domain.TOPIC.getId());
                  rval.add(dConfig);

                  rval.addAll(discoverDurableSubscriptions(tinfos[i].getName(), null));
               }
            }
         }

         return rval;
      }
      catch (TibjmsAdminException e)
      {
         log.error(e.getMessage(), e);

         throw new HermesException(e);
      }

   }

   @Override
   protected Collection<DestinationConfig> discoverDurableSubscriptions(String topicName, String jndiName) throws JMSException
   {
      try
      {
         final Collection<DestinationConfig> rval = new ArrayList<DestinationConfig>();
         final DurableInfo[] dInfos = getAdmin().getDurables(topicName);

         for (int j = 0; j < dInfos.length; j++)
         {
            DurableInfo dInfo = dInfos[j];

            final DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();

            dConfig.setDurable(true);
            dConfig.setName(jndiName == null ? dInfo.getTopicName() : jndiName);
            dConfig.setClientID(dInfo.getDurableName());
            dConfig.setSelector(dInfo.getSelector());
            dConfig.setDomain(Domain.TOPIC.getId());

            rval.add(dConfig);
         }

         return rval;
      }
      catch (TibjmsAdminException e)
      {
         log.error(e.getMessage(), e);

         throw new HermesException(e);
      }
   }

}