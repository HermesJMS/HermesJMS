/* 
 * Copyright 2009 Laurent Bovet, Swiss Post IT
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

package hermes.ext.imq ;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Support for Sun MQ (aka Java MQ or even Open MQ, technically named imq).
 *
 * @author bovetl
 * @version $Revision$
 * @since 01.00.00.00 
 */
public class ImqAdmin extends HermesAdminSupport implements HermesAdmin, MessageListener
{
    static final String DEST_LIST_TOPIC_NAME = "mq.metrics.destination_list";
    static final String QUEUE_METRICS_TOPIC_PREFIX = "mq.metrics.destination.queue.";
    static final String TOPIC_METRICS_TOPIC_PREFIX = "mq.metrics.destination.topic.";
    
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;        
    private MessageConsumer destListTopicSubscriber;

    private Map<String,MessageConsumer> destMetricTopicSubscribers = new HashMap<String, MessageConsumer>();
    private Map<String, Long> messageCounts = new HashMap<String, Long>();
    private Map<String, Map<String,Long>> stats = new HashMap<String, Map<String,Long>>();
    
    private List<DestinationConfig> destinations = new ArrayList<DestinationConfig>();
    
    private static Log LOG = LogFactory.getLog(ImqAdminFactory.class);
    
    private Object destListGuard = new Object();
    private Object destMetricGuard = new Object();
    
    public ImqAdmin(Hermes hermes, ConnectionFactory connectionFactory)
    {
        super(hermes);
        this.connectionFactory = connectionFactory;
              
        LOG.debug("Building ImqAdmin");
               
        try
        {
            connection = this.connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);            
            Topic destListTopic = session.createTopic(DEST_LIST_TOPIC_NAME);
            destListTopicSubscriber = session.createConsumer(destListTopic);
            destListTopicSubscriber.setMessageListener(this);
        }
        catch (JMSException e)
        {
            throw new RuntimeException(e);
        }        
    }

    @Override
    public Collection<DestinationConfig> discoverDestinationConfigs() throws JMSException
    {                 
        
        LOG.debug("Discovering destination(s)");
        
        try
        {
            synchronized(destListGuard) {
                destListGuard.wait();
            }
        }
        catch (InterruptedException e)
        {
        }
        
        LOG.debug("Discovered "+destinations.size()+" destination(s)");
        
        return destinations;
    }

    @Override
    public int getDepth(DestinationConfig destinationConfig) throws JMSException
    {
        LOG.debug("Getting depth");
        
        subscribeToDestMetricTopic(destinationConfig.getShortName(), destinationConfig.getDomain());

        try
        {
            synchronized(destMetricGuard) {
                destMetricGuard.wait(5000);
            }
        }
        catch (InterruptedException e)
        {
        }
        
        Long result = messageCounts.get(destinationConfig.getShortName());
        
        LOG.debug("Got depth for "+destinationConfig.getShortName()+": "+result);
        
        if(result==null) {
            throw new RuntimeException("Timeout: Got no data from metric topic.");
        }
        
        clearDestMetricTopicSubscribers();
        
        return result.intValue();
    }

    @Override
    public Map<String,Long> getStatistics(DestinationConfig destination) throws JMSException
    {
        LOG.debug("Getting statistics for "+destination);
        
        subscribeToDestMetricTopic(destination.getShortName(), destination.getDomain());

        try
        {
            synchronized(destMetricGuard) {
                destMetricGuard.wait(5000);
            }
        }
        catch (InterruptedException e)
        {
        }
        
        Map<String,Long> result = stats.get(destination.getShortName());               
              
        clearDestMetricTopicSubscribers();
        
        if(result==null) {
            throw new RuntimeException("Timeout: Got no data from metric topic.");
        }
        
        return result;
    }
    
    public void close() throws JMSException
    {   
        LOG.debug("Closing IMQ Session");
        clearDestMetricTopicSubscribers();
        destListTopicSubscriber.close();
        session.close();
        connection.close();
    }
    
    public void onMessage(Message msg)
    {
       
        try {
        
            MapMessage mapMsg = (MapMessage) msg;
            String type = mapMsg.getStringProperty("type");          

            LOG.debug("Got admin message from broker of type: "+type);    
            
            if (type.equals(DEST_LIST_TOPIC_NAME))
            {                
                List<DestinationConfig> result = new ArrayList<DestinationConfig>();                               
                
                for (@SuppressWarnings("unchecked")Enumeration e = mapMsg.getMapNames(); e.hasMoreElements();)
                {                    
                    String name = (String) e.nextElement();  
                    
                    @SuppressWarnings("unchecked")
                    Map<String,String> object = (Map<String,String>) mapMsg.getObject(name);
                    
                    DestinationConfig dest = HermesBrowser.getConfigDAO().createDestinationConfig();
                    dest.setName(object.get("name"));
                    dest.setShortName(object.get("name"));                  
                    dest.setDomain("queue".equals(object.get("type")) ? Domain.QUEUE.getId() :
                        ( "queue".equals(object.get("topic")) ? Domain.TOPIC.getId() : 
                            Domain.UNKNOWN.getId()));     
                    
                    result.add(dest);                    
               }
                
               Collections.sort(result, new Comparator<DestinationConfig>() {
                    public int compare(DestinationConfig o1, DestinationConfig o2)
                    {
                        return o1.getShortName().compareTo(o2.getShortName());
                    }
               });
               
               destinations = result;
               
               synchronized(destListGuard) {
                   destListGuard.notifyAll();
               }
               
            } else if (type.startsWith(QUEUE_METRICS_TOPIC_PREFIX)) {
                LOG.debug("Got queue metrics: "+type);
                
                String queueName = type.substring(QUEUE_METRICS_TOPIC_PREFIX.length());
                messageCounts.put(queueName, mapMsg.getLong("numMsgs"));
                HashMap<String,Long> map = new HashMap<String, Long>();
                
                @SuppressWarnings("unchecked")
                Enumeration<String> e = mapMsg.getMapNames();
                while(e.hasMoreElements()) {
                    String name = (String)e.nextElement();
                    map.put(name, mapMsg.getLong(name));                    
                }
                stats.put(queueName, map);
                LOG.debug("Stored stats for: "+queueName);
                
                synchronized(destMetricGuard) {
                    destMetricGuard.notifyAll();
                }
                
            }  else if (type.startsWith(TOPIC_METRICS_TOPIC_PREFIX)) {
                LOG.debug("Got topic metrics: "+type);
                
                String topicName = type.substring(TOPIC_METRICS_TOPIC_PREFIX.length());
                messageCounts.put(topicName, mapMsg.getLong("numMsgs"));
                
                HashMap<String,Long> map = new HashMap<String, Long>();
                
                @SuppressWarnings("unchecked")
                Enumeration<String> e = mapMsg.getMapNames();
                while(e.hasMoreElements()) {
                    String name = (String)e.nextElement();
                    map.put(name, mapMsg.getLong(name));                    
                }
                stats.put(topicName, map);
                LOG.debug("Stored stats for: "+topicName);
                
                synchronized(destMetricGuard) {
                    destMetricGuard.notifyAll();
                }                
            }
                

            
        } catch(JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private MessageConsumer subscribeToDestMetricTopic(String destName, int domain) throws JMSException
    {
        LOG.debug("Subscribing to "+destName);
        
        String topicName=null;
        
        if (domain == Domain.QUEUE.getId())
        {
            topicName = QUEUE_METRICS_TOPIC_PREFIX + destName;
        }
        else if (domain == Domain.TOPIC.getId())
        {
            topicName = TOPIC_METRICS_TOPIC_PREFIX + destName;
        }
        
        Topic destListTopic = session.createTopic(topicName);
        MessageConsumer subscriber = session.createConsumer(destListTopic);
        subscriber.setMessageListener(this);
        
        LOG.debug("Created subscriber "+subscriber+" listening to "+topicName);
        return subscriber;
    }

    private void clearDestMetricTopicSubscribers() throws JMSException
    {
        for(MessageConsumer subscriber : destMetricTopicSubscribers.values() ) {
            LOG.debug("Closing subscriber: "+subscriber);
            subscriber.close();
        }
        destMetricTopicSubscribers.clear();
    }
    
}
