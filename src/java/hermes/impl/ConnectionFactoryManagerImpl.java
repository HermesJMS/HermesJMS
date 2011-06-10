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
import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;
import hermes.config.ProviderConfig;
import hermes.config.ProviderExtConfig;
import hermes.ext.ExtensionFinder;
import hermes.ext.ExtensionFinderImpl;
import hermes.util.JMSUtils;
import hermes.util.ReflectUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

/**
 * Manager for a ConnectionFactory
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ConnectionFactoryManager.java,v 1.5 2004/07/30 17:25:14
 *          colincrist Exp $
 */

public class ConnectionFactoryManagerImpl extends JMSManagerImpl implements ConnectionFactoryManager {
	private static final Logger log = Logger.getLogger(ConnectionFactoryManagerImpl.class);
	private static final Logger traceLogger = Logger.getLogger("javax.jms.trace");
	private static Class[] interfaces = { ConnectionFactory.class, QueueConnectionFactory.class, TopicConnectionFactory.class };

	private ExtensionFinder extensionFinder;
	private ConnectionFactory connectionFactory;
	private ConnectionFactory proxyConnectionFactory;
	private String id;
	private String shortString;
	private HermesAdminFactory extension = null;
	private Map<DestinationConfigKeyWrapper, DestinationConfig> destinationConfigs = new HashMap<DestinationConfigKeyWrapper, DestinationConfig>();
	// private Map<String, DestinationConfig> topics = new HashMap<String,
	// DestinationConfig>();
	private List<DestinationConfig> destinationConfigsAsList = new ArrayList<DestinationConfig>();
	private boolean trace = false;
	private FactoryConfig factoryConfig;
	private ClassLoaderManager classLoaderManager;

	/**
	 * CFNode constructor comment.
	 * 
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IOException
	 */
	public ConnectionFactoryManagerImpl(ClassLoaderManager classLoaderManager, FactoryConfig factoryConfig) throws InstantiationException,
			ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
		super();

		this.factoryConfig = factoryConfig;
		this.classLoaderManager = classLoaderManager;
		this.extensionFinder = new ExtensionFinderImpl(classLoaderManager);

		setProvider(factoryConfig.getProvider());
	}

	public void close() throws JMSException {

	}

	/**
	 * Add a statically configured destination reachable via this
	 * ConnectionFactory
	 */
	public void addDestinationConfig(DestinationConfig destConfig) {
		DestinationConfigKeyWrapper key = new DestinationConfigKeyWrapper(destConfig);

		// cat.debug("new destination, domain=" +
		// Domain.getDomain(destConfig.getDomain()).toString() + ", name=" +
		// destConfig.getName());

		if (destinationConfigs.containsKey(key)) {
			DestinationConfig old = destinationConfigs.remove(key);
			destinationConfigsAsList.remove(old);

			log.debug("removed duplicate reference, reference domain=" + Domain.getDomain(destConfig.getDomain()).toString());
		}

		destinationConfigs.put(key, destConfig);
		destinationConfigsAsList.add(destConfig);
	}

	public Context createContext() throws NamingException, JMSException {
		if (connectionFactory instanceof JNDIConnectionFactory) {
			JNDIConnectionFactory jndiCF = (JNDIConnectionFactory) connectionFactory;
			return jndiCF.createContext();
		} else {
			return null;
		}
	}

	public void removeDestinationConfig(DestinationConfig destConfig) {

		DestinationConfigKeyWrapper key = new DestinationConfigKeyWrapper(destConfig);
		destinationConfigs.remove(key);
		destinationConfigsAsList.remove(destConfig);
	}

	/**
	 * Connect. No implementation for a connection factory.
	 */
	public void connect() throws javax.jms.JMSException {

	}

	/**
	 * Get some short description of this connection factory
	 */

	public String getConnectionFactoryType() {
		return null;
	}

	public ConnectionFactory getConnectionFactory() throws JMSException {
		if (trace) {
			return proxyConnectionFactory;
		} else {
			return connectionFactory;
		}
	}

	public DestinationConfig getDestinationConfig(String id, Domain domain) {
		DestinationConfigKeyWrapper key = new DestinationConfigKeyWrapper(id, domain);
		DestinationConfig rval = destinationConfigs.get(key);

		if (rval == null) {
			rval = HermesBrowser.getConfigDAO().createDestinationConfig();
			rval.setName(id);
			rval.setDomain(domain.getId());
		}

		return rval;
	}

	public DestinationConfig getDestinationConfig(Destination d) throws JMSException {
		return getDestinationConfig(JMSUtils.getDestinationName(d), Domain.getDomain(d));
	}

	public Collection getDestinationConfigs() {
		return destinationConfigsAsList;
	}

	public Object getObject() throws JMSException {
		return getConnectionFactory();
	}

	public String toString() {
		try {
			return "connectionFactory class=" + connectionFactory.getClass().getName() + ", properties=" + BeanUtils.describe(connectionFactory);
		} catch (Throwable e) {
			return e.getMessage();
		}
	}

	public void setProvider(ProviderConfig pConfig) throws InstantiationException, ClassNotFoundException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, IOException {
		ClassLoader classLoader = classLoaderManager.getClassLoader(factoryConfig.getClasspathId());
		connectionFactory = ReflectUtils.createConnectionFactory(classLoader.loadClass(pConfig.getClassName()));

		LoaderSupport.populateBean(connectionFactory, pConfig.getProperties());
		setConnectionFactory(connectionFactory);
	}

	private void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	/**
	 * @return Returns the trace.
	 */
	public boolean isTrace() {
		return trace;
	}

	/**
	 * @param trace
	 *            The trace to set.
	 */
	public void setTrace(boolean trace) {
		this.trace = trace;
	}

	/**
	 * @return Returns the extension.
	 */
	public HermesAdminFactory getExtension(ProviderExtConfig extConfig) throws HermesException {
		if (extension == null) {
			try {
				extension = extensionFinder.createExtension(factoryConfig.getClasspathId(), extConfig, connectionFactory);

				return extension;
			} catch (Exception e) {
				throw new HermesException(e);
			}
		} else {
			return extension;
		}
	}

	/**
	 * @return Returns the extensionFinder.
	 */
	public ExtensionFinder getExtensionFinder() {
		return extensionFinder;
	}

	/**
	 * @param extensionFinder
	 *            The extensionFinder to set.
	 */
	public void setExtensionFinder(ExtensionFinder extensionFinder) {
		this.extensionFinder = extensionFinder;
	}
}