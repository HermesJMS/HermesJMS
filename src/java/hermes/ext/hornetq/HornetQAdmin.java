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

package hermes.ext.hornetq;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.hornetq.api.core.management.ObjectNameBuilder;
import org.hornetq.api.jms.management.JMSQueueControl;
import org.hornetq.api.jms.management.TopicControl;

/**
 * @author David Cole : dlcole@gmail.com
 */
public class HornetQAdmin extends HermesAdminSupport implements HermesAdmin {
	private static final Logger log = Logger.getLogger(HornetQAdmin.class);
	private final JNDIConnectionFactory jndiCF;
	private JMXConnector jmxConnector;
	private final HornetQAdminFactory factory;
	private MBeanServerConnection mBeanServer = null;

	/**
	 * Constructor
	 */
	public HornetQAdmin(HornetQAdminFactory factory, Hermes hermes, JNDIConnectionFactory jndiCF, ConnectionFactory cf) throws JMSException {
		super(hermes);

		this.jndiCF = jndiCF;
		this.factory = factory;

	}

	/**
	 * Creates an MBeanServerConnection by using either the jmxUrl to create a
	 * JMXConnector or will default to JNDI using the <br>
	 * The connection will be created using the jmxURL if configured on the
	 * HornetQAdminFactory. <br>
	 * An example jmxUrl for JBoss would look like:
	 * service:jmx:rmi:///jndi/rmi://localhost:1090/jmxconnector <br>
	 * If the jmxUrl is not set, the method will attempt to obtain access to the
	 * MBeanServerConnection via JNDI using the mBeanServerConnectionJndi
	 * property. An example for JBoss would be: jmx/invoker/RMIAdaptor<br>
	 * One of the two properties must be set to function properly: jmxURL or
	 * mBeanServerConnectionJndi
	 * 
	 * @return MBeanServerConnection
	 * @throws IOException
	 * @throws JMSException
	 * @throws HermesException
	 */
	private MBeanServerConnection getMBeanServerConnection() throws IOException, JMSException, HermesException {
		if (mBeanServer == null) {
			if (factory.getJmxUrl() != null) {
				if (jmxConnector == null) {
					log.info("Creating JMXConnector using jmxUrl: " + factory.getJmxUrl());
					// Create a JMX Connector to connect to the server's
					// MBeanServer
					try {
						jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(factory.getJmxUrl()), new HashMap());
						mBeanServer = jmxConnector.getMBeanServerConnection();
					} catch (Exception ex) {
						throw new HermesException("Error creating the JMXConnector. Check the jmxUrl in the Admin Factory. An example for JBoss would look like: "
								+ "service:jmx:rmi:///jndi/rmi://localhost:1090/jmxconnector", ex);
					}
				}
			} else if (factory.getMBeanServerConnectionJndi() != null) {
				try {
					mBeanServer = (MBeanServerConnection) jndiCF.createContext().lookup(factory.getMBeanServerConnectionJndi());
				} catch (NamingException e) {
					throw new HermesException("Error accessing the MBeanServerConnection via JNDI using: " + factory.getMBeanServerConnectionJndi() + ". "
							+ "Please make sure the mBeanServerConnectionJndi is configured properly. " + "An example for JBoss would be: jmx/invoker/RMIAdaptor", e);
				}

			} else {
				throw new HermesException("You must set either the jmxUrl or the jmxAdapterJndi. jmxUrl example: " + "service:jmx:rmi:///jndi/rmi://localhost:1090/jmxconnector. "
						+ "jmxAdapterJndi example: jmx/invoker/RMIAdaptor");
			}
		}
		return mBeanServer;
	}

	private JMSQueueControl getQueueControl(String queueName) throws Exception {
		log.debug("Creating JMSQueueControl for queue: " + queueName);
		MBeanServerConnection mbsc = getMBeanServerConnection();

		// Construct an ObjectName for JMX access to the Queue
		ObjectName on = ObjectNameBuilder.DEFAULT.getJMSQueueObjectName(queueName);

		// Create a JMSQueueControl proxy to manage the queue on the server
		JMSQueueControl queueControl = MBeanServerInvocationHandler.newProxyInstance(mbsc, on, JMSQueueControl.class, false);

		return queueControl;
	}

	private TopicControl getTopicControl(String topicName) throws Exception {
		log.debug("Creating TopicControl for topic: " + topicName);
		MBeanServerConnection mbsc = getMBeanServerConnection();

		// Construct an ObjectName for JMX access to the Queue
		ObjectName on = ObjectNameBuilder.DEFAULT.getJMSTopicObjectName(topicName);

		// Create a JMSQueueControl proxy to manage the queue on the server
		TopicControl topicControl = MBeanServerInvocationHandler.newProxyInstance(mbsc, on, TopicControl.class, false);

		return topicControl;
	}

	/**
	 * @see hermes.ProviderExtensionSession#size(javax.jms.Destination)
	 */
	@Override
	public int getDepth(DestinationConfig dConfig) throws JMSException {
		try {
			String dest = getRealDestinationName(dConfig);
			long depth = 0;

			if (dConfig.getDomain() == Domain.QUEUE.getId()) {

				log.debug("Looking for depth of queue: " + dest);
				JMSQueueControl queueControl = getQueueControl(dest);
				depth = queueControl.getMessageCount();
			} else {
				log.debug("Looking for depth of queue: " + dest);
				TopicControl topicControl = getTopicControl(dest);
				depth = topicControl.getMessageCount();
			}

			log.debug("Found the depth of (" + depth + ") for: " + dest);
			return (int) depth;
		} catch (HermesException ex) {
			throw ex;
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			close();
			throw new HermesException(e);
		}
	}

	/**
	 * @see hermes.ProviderExtensionSession#close()
	 */
	@Override
	public void close() throws JMSException {
		closeJMXConnector();
		mBeanServer = null;
	}

	private void closeJMXConnector() {
		if (jmxConnector != null) {
			try {
				jmxConnector.close();
			} catch (IOException e) {
				log.error("Error closing the JMXConnector: " + e.getMessage(), e);
			}
			jmxConnector = null;
		}
	}

	@Override
	public Map getStatistics(DestinationConfig dConfig) throws JMSException {
		final Map stats = new LinkedHashMap();
		try {
			String destination = getRealDestinationName(dConfig);
			log.debug("Generating statistics for : " + destination);

			if (dConfig.getDomain() == Domain.QUEUE.getId()) {
				JMSQueueControl queueControl = getQueueControl(destination);

				stats.put("QueueName", queueControl.getName());
				stats.put("ScheduledCount", queueControl.getScheduledCount());
				stats.put("MessagesAdded", queueControl.getMessagesAdded());
				stats.put("MessageCount", queueControl.getMessageCount());
				stats.put("DeliveringCount", queueControl.getDeliveringCount());
				stats.put("ConsumerCount", queueControl.getConsumerCount());
			} else {
				TopicControl topicControl = getTopicControl(destination);

				stats.put("TopicName", topicControl.getName());
				stats.put("MessageCount", topicControl.getMessageCount());
				stats.put("DurableMessageCount", topicControl.getDurableMessageCount());
				stats.put("NonDurableMessageCount", topicControl.getNonDurableMessageCount());
				stats.put("SubscriptionCount", topicControl.getSubscriptionCount());
				stats.put("DurableSubscriptionCount", topicControl.getDurableSubscriptionCount());
				stats.put("NonDurableSubscriptionCount", topicControl.getNonDurableSubscriptionCount());
			}
			log.debug("Statistics of destination: " + destination + " are:\n" + stats);
		} catch (Exception e) {
			throw new HermesException(e);
		}

		return stats;
	}

	@Override
	public int truncate(DestinationConfig dConfig) throws JMSException {
		try {
			String destination = getRealDestinationName(dConfig);

			if (dConfig.getDomain() == Domain.QUEUE.getId()) {
				JMSQueueControl queueControl = getQueueControl(destination);
				return queueControl.removeMessages(null);
			} else {
				throw new HermesException("JBoss does not support truncating a durable subscription");
			}
		} catch (HermesException ex) {
			throw ex;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			close();
			throw new HermesException(e);
		}
	}
}