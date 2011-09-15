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
import hermes.Hermes;
import hermes.HermesException;
import hermes.JNDIQueueConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;
import hermes.config.SessionConfig;
import hermes.impl.ConnectionFactoryManagerImpl;
import hermes.impl.ConnectionManager;
import hermes.impl.DestinationManager;
import hermes.impl.JMSManagerImpl;
import hermes.impl.LoaderSupport;
import hermes.impl.SessionManager;
import hermes.impl.TopicBrowser;
import hermes.util.JMSUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * Generic session management helper.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: AbstractSessionManager.java,v 1.1 2004/07/21 20:25:40
 *          colincrist Exp $
 */

public abstract class AbstractSessionManager extends JMSManagerImpl implements SessionManager {
	private static final Logger log = Logger.getLogger(AbstractSessionManager.class);
	private FactoryConfig factoryConfig;
	private boolean transacted = true;
	private String id;
	private int reconnects = -1;
	private long reconnectTimeout = 30 * 1000;
	private boolean audit = false;
	private String auditDirectory = ".";
	private ConnectionFactoryManagerImpl myCFManager;
	private int acknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
	private DestinationManager destinationManager;
	private Map destinationToConfigMap = new HashMap();
	private SessionConfig config;

	public AbstractSessionManager(DestinationManager destinationManager, SessionConfig config) {
		this.destinationManager = destinationManager;
		this.config = config;
	}

	public SessionConfig getConfig() {
		return config;
	}

	/**
	 * Get the session, creating is as needed.
	 * 
	 * @return
	 * @throws JMSException
	 */
	public abstract Session getSession() throws JMSException;

	/**
	 * Get a consumer for a destination. The consumer is cached thread local.
	 */
	public abstract MessageConsumer getConsumer(Destination d) throws JMSException;

	/**
	 * Get a consumer for a destination and a selector. The consumer is cached
	 * thread local.
	 */
	public abstract MessageConsumer getConsumer(Destination d, String selector) throws JMSException;

	protected Session createSession() throws JMSException {
		try {
			return doCreateSession();
		} catch (IllegalStateException ex) {
			try {
				close();
			} catch (JMSException e) {
				log.error("closing session: " + e.getMessage(), e);
			}

			try {
				getConnectionManager().close();
			} catch (JMSException e) {
				log.error("closing connection: " + e.getMessage(), e);
			}

			return doCreateSession();
		}
	}

	/**
	 * Create a session based on configured options.
	 */
	private Session doCreateSession() throws JMSException {
		Session session = null;
		Connection conn = (Connection) parent.getObject();

		try {
			ConnectionFactory connectionFactory = getConnectionFactoryManager().getConnectionFactory();

			/**
			 * Hack so that although the interface seems to be JMS 1.1, if its
			 * WebMethods only use the JMS 1.0.2b methods.
			 */
			boolean isWebMethodsHack = false;

			if (connectionFactory instanceof JNDIQueueConnectionFactory) {
				JNDIQueueConnectionFactory jndiCF = (JNDIQueueConnectionFactory) connectionFactory;

				if (jndiCF.getInitialContextFactory() != null && jndiCF.getInitialContextFactory().equals("hermes.ext.wme.WMEInitialContextFactory")) {
					isWebMethodsHack = true;
				}
			}

			if (!isWebMethodsHack) {
				session = conn.createSession(isTransacted(), getAcknowledgeMode());

				log.debug("session created using JMS 1.1 interface");
			}
		} catch (NoSuchMethodError ex) {
			// NOP
		} catch (AbstractMethodError ex) {
			// NOP
		}

		if (session == null) {
			if (JMSUtils.isQueue(conn)) {
				session = ((QueueConnection) conn).createQueueSession(isTransacted(), getAcknowledgeMode());
			} else {
				session = ((TopicConnection) conn).createTopicSession(isTransacted(), getAcknowledgeMode());
			}

			log.debug("session created using JMS 1.0.2b interface");
		}

		return session;
	}

	public int getAcknowledgeMode() {
		log.debug("acknowledgeMode = " + (isTransacted() ? "Session.CLIENT_ACKNOWLEDGE" : "Session.AUTO_ACKNOWLEDGE"));

		return isTransacted() ? Session.CLIENT_ACKNOWLEDGE : Session.AUTO_ACKNOWLEDGE;
	}

	/**
	 * Get some identifier for this session manager
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returned the managed object, in this case the session.
	 */
	public Object getObject() throws JMSException {
		return getSession();
	}

	/**
	 * Set the transaction policy
	 */
	public void setTransacted(boolean transacted) {
		this.transacted = transacted;
	}

	public boolean isTransacted() {
		return transacted;
	}

	/**
	 * Set the ID.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Set the parent
	 */
	public void setParent(JMSManagerImpl parent) {
		super.setParent(parent);
	}

	/**
	 * Set the number of times we want to try and reconnect
	 */
	public void setReconnects(int reconnects) {
		this.reconnects = reconnects;
	}

	/**
	 * Set the period to try reconnects
	 */
	public void setReconnectTimeout(Long reconnectTimeout) {
		this.reconnectTimeout = reconnectTimeout.longValue();
	}

	/**
	 * Get the connection factory manager, that is the parent.
	 */
	public ConnectionFactoryManagerImpl getConnectionFactoryManager() {
		if (myCFManager == null) {
			synchronized (this) {
				if (myCFManager == null && parent != null) {
					myCFManager = (ConnectionFactoryManagerImpl) parent.getParent();
				}
			}
		}

		return myCFManager;
	}

	/**
	 * @return
	 */
	public boolean isAudit() {
		return audit;
	}

	/**
	 * @return
	 */
	public String getAuditDirectory() {
		return auditDirectory;
	}

	/**
	 * @param b
	 */
	public void setAudit(boolean b) {
		audit = b;
	}

	/**
	 * @param string
	 */
	public void setAuditDirectory(String string) {
		auditDirectory = string;
	}

	/**
	 * When reconnects is set > 1 this timeout is used between reconnet attempts
	 */
	public long getReconnectTimeout() {
		return reconnectTimeout;
	}

	/**
	 * When reconnects is set > 1 this timeout is used between reconnet attempts
	 */
	public void setReconnectTimeout(long reconnectTimeout) {
		this.reconnectTimeout = reconnectTimeout;
	}

	public int getReconnects() {
		return reconnects;
	}

	public void setFactoryConfig(FactoryConfig factoryConfig) {
		this.factoryConfig = factoryConfig;
	}

	public FactoryConfig getFactoryConfig() {
		return factoryConfig;
	}

	/**
	 * Get the connection factory.
	 */
	public ConnectionFactory getConnectionFactory() throws JMSException {
		return getConnectionFactoryManager().getConnectionFactory();
	}

	/**
	 * Get the connection.
	 */
	public Connection getConnection() throws JMSException {
		return (Connection) parent.getObject();
	}

	public QueueBrowser createBrowser(Hermes hermes, DestinationConfig config) throws JMSException {
		if (config.getDomain() == Domain.QUEUE.getId()) {
			final Queue queue = (Queue) destinationManager.getDestination(getSession(), config.getName(), Domain.QUEUE);

			try {

				if (config.getSelector() != null) {
					return getSession().createBrowser(queue, config.getSelector());
				} else {
					return getSession().createBrowser(queue);
				}
			} catch (NoSuchMethodError ex) {
				// NOP
			} catch (AbstractMethodError ex) {
				// NOP
			}

			if (!(getSession() instanceof QueueSession)) {
				throw new HermesException("Session is 1.0.2 and not in the queue domain");
			}

			if (config.getSelector() != null) {
				return ((QueueSession) getSession()).createBrowser(queue, config.getSelector());
			} else {
				return ((QueueSession) getSession()).createBrowser(queue);
			}
		} else if (config.getDomain() == Domain.TOPIC.getId()) {
			return new TopicBrowser(hermes.getSession(), destinationManager, config);
		} else {
			throw new HermesException("The domain for " + config.getName() + " is not defined, configure it as a queue or a topic");
		}

	}

	/**
	 * Create browser, can be used with topics too.
	 */
	public QueueBrowser createBrowser(Hermes hermes, Destination destination, String selector) throws JMSException {
		final DestinationConfig dConfig = HermesBrowser.getConfigDAO().duplicate(getDestinationConfig(destination));

		if (dConfig.getSelector() == null) {
			dConfig.setSelector(selector);
		}

		return createBrowser(hermes, dConfig);
	}

	/**
	 * Get an iterator over all the <i>statically configured </i> destinations.
	 */
	public Collection getDestinations() {
		return getConnectionFactoryManager().getDestinationConfigs();
	}

	/**
	 * Get the destination specific configuration
	 */
	public DestinationConfig getDestinationConfig(String d, Domain domain) throws JMSException {
		return getConnectionFactoryManager().getDestinationConfig(d, domain);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.impl.SessionManager#getDestination(java.lang.String)
	 */
	public Destination getDestination(String named, Domain domain) throws JMSException, NamingException {
		Destination rval = destinationManager.getDestination(getSession(), named, domain);
		DestinationConfig config = getDestinationConfig(named, domain);

		if (config != null) {
			destinationToConfigMap.put(rval, getDestinationConfig(named, domain));

			if (config.getProperties() != null) {
				try {
					LoaderSupport.populateBean(rval, config.getProperties());
				} catch (Exception ex) {
					throw new HermesException(ex);
				}

			}
		}

		return rval;
	}

	public DestinationConfig getDestinationConfig(Destination d) {
		return (DestinationConfig) destinationToConfigMap.get(d);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.impl.SessionManager#getConnectionManager()
	 */
	public ConnectionManager getConnectionManager() throws JMSException {
		return (ConnectionManager) parent;
	}

	/**
	 * Get a producer, producers are cached on thread local.
	 */
	protected MessageProducer createQueueProducer() throws JMSException {
		return ((QueueSession) getSession()).createSender(null);

	}

	/**
	 * Get a producer, producers are cached on thread local.
	 */
	protected MessageProducer createTopicProducer() throws JMSException {
		return ((TopicSession) getSession()).createPublisher(null);
	}

	public DestinationManager getDestinationManager() {
		return destinationManager;
	}

	/**
	 * Return a verbose description of this session.
	 */
	public String toString() {
		return (getConnectionFactoryManager() == null) ? super.toString() : getConnectionFactoryManager().toString();
	}
}