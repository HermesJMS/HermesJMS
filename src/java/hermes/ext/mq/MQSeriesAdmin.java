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

package hermes.ext.mq;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.browser.MessageRenderer;
import hermes.config.DestinationConfig;
import hermes.config.impl.DestinationConfigImpl;
import hermes.ext.HermesAdminSupport;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Logger;

import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueueEnumeration;
import com.ibm.mq.pcf.CMQC;
import com.ibm.mq.pcf.CMQCFC;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MQSeriesAdmin.java,v 1.17 2006/07/13 07:35:35 colincrist Exp $
 */
public class MQSeriesAdmin extends HermesAdminSupport implements HermesAdmin
{
   private static final Logger log = Logger.getLogger(MQSeriesAdmin.class);
   private static Field baseMessageField;
   private MQQueueManager queueManager;
   private MQConnectionFactory mqCF;
   private MQSeriesMessageRenderer messageRenderer = new MQSeriesMessageRenderer(this);
   private WeakHashMap jmsToNativeMap = new WeakHashMap();
   
   
   static
   {
      try
      {
         baseMessageField = MQQueueEnumeration.class.getDeclaredField("baseMessage");
         baseMessageField.setAccessible(true);
      }
      catch (Throwable t)
      {
         log.error("cannot location baseMessage field in MQEnumeration, access to native messags unavailable");
      }
   }

   /**
    *  
    */
   public MQSeriesAdmin(Hermes hermes, MQConnectionFactory mqCF)
   {
      super(hermes);

      this.mqCF = mqCF;

      // log.debug("session to " + hermes.getId()) ;
   }

 
   
   private synchronized MQQueueManager getQueueManager() throws MQException
   {
      if (queueManager == null)
      {
         MQEnvironment.channel = mqCF.getChannel();
         MQEnvironment.port = mqCF.getPort();
         MQEnvironment.hostname = mqCF.getHostName();
         MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES);

         queueManager = new MQQueueManager(mqCF.getQueueManager());
      }

      return queueManager;
   }

   public Enumeration createBrowserProxy(final Enumeration iter) throws JMSException
   {
      if (false)
      {
         final MQQueueEnumeration mqEnum = (MQQueueEnumeration) iter;

         return new Enumeration()
         {
            public boolean hasMoreElements()
            {
               return iter.hasMoreElements();
            }

            public Object nextElement()
            {
               final Message m = (Message) iter.nextElement();

               try
               {
                  if (baseMessageField != null)
                  {
                     final Object o = baseMessageField.get(iter);

                     if (o instanceof MQMessage)
                     {
                        synchronized (jmsToNativeMap)
                        {
                           jmsToNativeMap.put(m, new WeakReference(o));
                        }
                     }
                  }
               }
               catch (Throwable e)
               {
                  log.error(e.getMessage(), e);
               }

               return m;
            }
         };
      }
      else
      {
         return iter;
      }
   }

   MQMessage getMQMessage(Message m) throws JMSException
   {
      synchronized (jmsToNativeMap)
      {
         if (jmsToNativeMap.containsKey(m))
         {
            WeakReference ref = (WeakReference) jmsToNativeMap.get(m);
            return (MQMessage) ref.get();
         }
         else
         {
            throw new JMSException("No reference found to native message");
         }
      }
   }

   @Override
   public String getRealDestinationName(DestinationConfig dConfig) throws JMSException
   {
      String queueName = super.getRealDestinationName(dConfig);

      if (queueName.startsWith("queue:///"))
      {
         queueName = queueName.substring(9);
      }

      if (queueName.indexOf("?") != -1)
      {
         queueName = queueName.substring(0, queueName.indexOf("?"));
      }

      log.debug("real name=" + queueName);

      return queueName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.ProviderExtensionSession#size(javax.jms.Destination)
    */
   public int getDepth(DestinationConfig dConfig) throws JMSException
   {
      try
      {
         final String queueName = getRealDestinationName(dConfig);
         final MQQueue queue = getQueueManager().accessQueue(queueName, MQC.MQOO_INQUIRE | MQC.MQOO_INPUT_AS_Q_DEF, null, null, null);
         final int depth = queue.getCurrentDepth();

         queue.close();

         return depth;
      }
      catch (MQException e)
      {
         close();

         throw new HermesException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.ProviderExtensionSession#close()
    */
   public synchronized void close() throws JMSException
   {
      try
      {
         if (queueManager != null)
         {
            try
            {
               //
               // Do I need both?

               queueManager.disconnect();
               queueManager.close();
            }
            finally
            {
               queueManager = null;
            }
         }
      }
      catch (MQException e)
      {
         throw new HermesException(e);
      }
   }

   public synchronized Collection discoverDestinationConfigs() throws JMSException
   {
      final Collection rval = new ArrayList();
      PCFMessageAgent agent = null;

      try
      {
         agent = new PCFMessageAgent(getQueueManager());
         final PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_NAMES);

         request.addParameter(CMQC.MQCA_Q_NAME, "*");
         request.addParameter(CMQC.MQIA_Q_TYPE, MQC.MQQT_LOCAL);

         final PCFMessage[] responses = agent.send(request);
         final String[] names = (String[]) responses[0].getParameterValue(CMQCFC.MQCACF_Q_NAMES);

         for (int i = 0; i < names.length; i++)
         {
            final DestinationConfig dConfig = new DestinationConfigImpl();

            dConfig.setName(names[i].trim());
            dConfig.setDomain(Domain.QUEUE.getId());

            rval.add(dConfig);
         }
      }
      catch (MQException ex)
      {
         if (ex.reasonCode != 2033)
         {
            throw new HermesException(ex) ;
         }
         else
         {
            log.debug("PCF calls gave a 2033 reason code, ignoring") ;
         }
      }
      catch (IOException ex)
      {
         throw new HermesException(ex);
      }
      finally
      {
         if (agent != null)
         {
            try
            {
               agent.disconnect();
            }
            catch (MQException e)
            {
               log.error(e.getMessage(), e);
            }
         }
      }

      return rval;
   }

   public synchronized Map getStatistics(DestinationConfig dConfig) throws JMSException
   {
      final Map stats = new LinkedHashMap();
      MQQueue queue = null ;
      
      try
      {
         final String queueName = getRealDestinationName(dConfig);
         queue = getQueueManager().accessQueue(queueName, MQC.MQOO_INQUIRE | MQC.MQOO_INPUT_AS_Q_DEF, null, null, null);

         stats.put("Description", queue.getDescription().trim());
         stats.put("CurrentDepth", new Integer(queue.getCurrentDepth()));
         stats.put("OpenOutputCount", new Integer(queue.getOpenOutputCount()));
         stats.put("OpenInputCount", new Integer(queue.getOpenInputCount()));

         if (queue.getInhibitGet() == MQC.MQQA_GET_INHIBITED)
         {
            stats.put("InhibitGet", Boolean.TRUE);
         }
         else
         {
            stats.put("InhibitGet", Boolean.FALSE);
         }

         if (queue.getInhibitPut() == MQC.MQQA_PUT_INHIBITED)
         {
            stats.put("InhibitPut", Boolean.TRUE);
         }
         else
         {
            stats.put("InhibitPut", Boolean.FALSE);
         }

         if (queue.getShareability() == MQC.MQQA_SHAREABLE)
         {
            stats.put("Sharable", Boolean.TRUE);
         }
         else
         {
            stats.put("Sharable", Boolean.FALSE);
         }

         if (queue.getTriggerControl() == MQC.MQTC_ON)
         {
            stats.put("TriggerControl", Boolean.TRUE);
            stats.put("TriggerData", queue.getTriggerData());
            stats.put("TriggerDepth", new Integer(queue.getTriggerDepth()));
            stats.put("TriggerMessagePriority", new Integer(queue.getTriggerMessagePriority()));

            switch (queue.getTriggerType())
            {
               case MQC.MQTT_NONE:
                  stats.put("TriggerType", "None");
                  break;

               case MQC.MQTT_DEPTH:
                  stats.put("TriggerType", "Depth");
                  break;

               case MQC.MQTT_EVERY:
                  stats.put("TriggerType", "Every");
                  break;

               case MQC.MQTT_FIRST:
                  stats.put("TriggerType", "First");
                  break;

               default:
                  stats.put("TriggerType", "Unknown");
            }
         }
         else
         {
            stats.put("TriggerControl", Boolean.FALSE);
         }

         stats.put("MaximumDepth", new Integer(queue.getMaximumDepth()));
         stats.put("MaximumMessageLength", new Integer(queue.getMaximumMessageLength()));
      }
      catch (MQException ex)
      {
         if (ex.reasonCode != 2033)
         {
            throw new HermesException(ex) ;
         }
         else
         {
            log.debug("PCF calls gave a 2033 reason code, ignoring") ;
         }
      }
      finally
      {
         if (queue != null)
         {
            try
            {
               queue.close() ;
            }
            catch (MQException ex)
            {
               log.error("ignoring error closing queue: " + ex.getMessage(), ex) ;
            }
         }
      }
         
      return stats ;
   }

   public MessageRenderer getMessageRenderer() throws JMSException
   {
      return null;
   }
}