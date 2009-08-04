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

package hermes.ext.seebeyond;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;
import hermes.ext.seebeyond.model.SeeBeyondQueue;
import hermes.ext.seebeyond.model.SeeBeyondTopic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.apache.log4j.Logger;

/**
 * 
 * @author murali
 * 
 */
public abstract class SeeBeyondAdmin extends HermesAdminSupport implements HermesAdmin {

	public SeeBeyondAdmin(Hermes hermes) {
		super(hermes);
	}

	private static final Logger log = Logger.getLogger(SeeBeyondAdmin.class);

	protected ConnectionFactory connectionFactory;

	protected HermesAdminFactory factory;

	protected SunSeeBeyondAdminBase seeBeyondAdmin;

	protected DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm:ss");



	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.ProviderExtensionSession#size(javax.jms.Destination)
	 */
	public int getDepth(DestinationConfig dConfig) throws JMSException {
		String destinationName = getRealDestinationName(dConfig);
		Properties props = null;
		int depth = 0;
		if (dConfig.getDomain() == Domain.QUEUE.getId()) {
			props = seeBeyondAdmin.getQueueStatistics(destinationName);
		} else {
			props = seeBeyondAdmin.getTopicStatistics(destinationName);
		}
		Object count = props.get(SunSeeBeyondAdminBase.MESSAGE_COUNT_KEY);
		if (count != null) {
			depth = Integer.parseInt(count.toString());
		}
		return depth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.ProviderExtensionSession#close()
	 */
	public void close() throws JMSException {
		// NOP
	}

	public Collection<DestinationConfig> discoverDestinationConfigs() throws JMSException {
		final Collection<DestinationConfig> rval = new ArrayList<DestinationConfig>();
		List queues = seeBeyondAdmin.getQueues();
		log.info("Adding destination queues.");
		boolean addQueues = false;
		boolean addTopics = false;
		if (connectionFactory instanceof QueueConnectionFactory)
			addQueues = true;
		else if (connectionFactory instanceof TopicConnectionFactory)
			addTopics = true;
		else {
			addQueues = true;
			addTopics = true;
		}
		if (addQueues)
			for (Object queue : seeBeyondAdmin.getQueues()) {
				String queueName = queue.toString();
				if (queueName.startsWith("STCMS.") || queueName.startsWith("STCTemporary.")) {
					continue;
				}
				log.debug("Adding queue " + queueName);
				DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();
				dConfig.setDomain(Domain.QUEUE.getId());
				dConfig.setName(queueName);
				rval.add(dConfig);
			}
		log.info("Adding destination queues.");
		if (addTopics)
			for (Object topic : seeBeyondAdmin.getTopics()) {
				String topicName = topic.toString();
				if (topicName.startsWith("STCMS.") || topicName.startsWith("STCTemporary.")) {
					continue;
				}
				log.debug("Adding topic " + topicName);
				DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();
				dConfig.setDomain(Domain.TOPIC.getId());
				dConfig.setName(topicName);
				rval.add(dConfig);
				log.debug("Fetching durable subscriptions for topic " + topicName);
				rval.addAll(discoverDurableSubscriptions(topicName, null));
			}
		return rval;
	}

	public Map getStatistics(DestinationConfig dConfig) throws JMSException {
		final Map stats = new LinkedHashMap();
		String destinationName = getRealDestinationName(dConfig);
		Properties props = null;
		int depth = 0;
		if (dConfig.getDomain() == Domain.QUEUE.getId()) {
			props = seeBeyondAdmin.getQueueStatistics(destinationName);
			SeeBeyondQueue sbQueue = new SeeBeyondQueue(props);
			stats.put("Queue Name", sbQueue.getQueueName());
			stats.put("Min Sequence Number", sbQueue.getMinSequence());
			stats.put("Max Sequence Number", sbQueue.getMaxSequence());
			stats.put("Available Count", sbQueue.getMessageCount());
			stats.put("Number of Receivers", sbQueue.getCurrentReceivers());
			stats.put("Last Published Date/Time", sbQueue.getLastEnqueueTime());
		} else {
			props = seeBeyondAdmin.getTopicStatistics(destinationName);
			SeeBeyondTopic sbTopic = new SeeBeyondTopic(props);
			stats.put("Topic Name", sbTopic.getTopicName());
			stats.put("Min Sequence Number", sbTopic.getFirstSequence());
			stats.put("Max Sequence Number", sbTopic.getLastSequence());
			stats.put("Available Count", sbTopic.getMessageCount());
			stats.put("Number of Subscribers", sbTopic.getCurrentSubscribers());
			stats.put("Last Published Date/Time", sbTopic.getLastEnqueueTime());
		}

		return stats;
	}

	@Override
	protected Collection discoverDurableSubscriptions(String topicName, String jndiName) throws JMSException {
		final Collection<DestinationConfig> rval = new ArrayList<DestinationConfig>();
		List subscribers = seeBeyondAdmin.getSubscribers(topicName);
		log.debug("Durable subscriptions for " + topicName + " are " + subscribers);
		for (final Object subscriber : subscribers) {
			Properties subscriberProps = (Properties) subscriber;

			final DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();

			dConfig.setClientID(subscriberProps.getProperty("SUBSCRIBER_NAME"));
			dConfig.setName(jndiName == null ? "topics/" + topicName : jndiName);
			dConfig.setDomain(Domain.TOPIC.getId());
			dConfig.setSelector(null);// Not Available
			dConfig.setDurable(true);

			rval.add(dConfig);

		}
		return rval;
	}
	
	@Override
	public long getAge(DestinationConfig dConfig) throws JMSException {
		long rval = 0;
		String destName = getRealDestinationName(dConfig);
		Properties queueStats = null;
		try
		{
			if (dConfig.getDomain() == Domain.QUEUE.getId()) {
				queueStats = seeBeyondAdmin.getQueueStatistics(destName);
			} else if (dConfig.getDomain() == Domain.QUEUE.getId()){
				queueStats = seeBeyondAdmin.getTopicStatistics(destName);
			} else
			{
				log.error("Unsupported destination domain " + dConfig.getDomain());
			}
			rval = dateFormat.parse(queueStats.getProperty("LAST_ENQUEUE_TIME")).getTime();

		}catch(Exception e)
		{
			throw new HermesException("Could not fetch last published time.",e);
		}

		return rval;
	}


}