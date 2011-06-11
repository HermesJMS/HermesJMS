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

package hermes.ext;

import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.config.ProviderExtConfig;
import hermes.impl.ClassLoaderManager;
import hermes.impl.LoaderSupport;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * I've avoided any reference to the actualy interfaces that are provider
 * specific so this class will compile even if they provider libraries are not
 * present. Makes the code look scrappy and its clearly un-typesafe but there u
 * go.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ExtensionFinderImpl.java,v 1.6 2004/07/30 17:25:15 colincrist
 *          Exp $
 */
public class ExtensionFinderImpl implements ExtensionFinder {
	private static final Logger log = Logger.getLogger(ExtensionFinderImpl.class);

	private ClassLoaderManager classLoaderManager;

	public ExtensionFinderImpl(ClassLoaderManager classLoaderManager) {
		this.classLoaderManager = classLoaderManager;
	}

	public synchronized HermesAdminFactory createExtension(String classPathId, ProviderExtConfig extConfig, ConnectionFactory cf)
			throws InstantiationException, ClassNotFoundException, NamingException, JMSException {
		if (extConfig != null && extConfig.getClassName() != null && !extConfig.getClassName().equals("")) {
			log.debug("creating extension " + extConfig.getClassName() + " for " + cf.getClass().getName());

			HermesAdminFactory providerExtension = null;

			try {

				ClassLoader classLoader = classLoaderManager.getClassLoader(classPathId);

				log.debug("loading from " + classLoader);

				Thread.currentThread().setContextClassLoader(classLoader);

				providerExtension = (HermesAdminFactory) classLoader.loadClass(extConfig.getClassName()).newInstance();

				LoaderSupport.populateBean(providerExtension, extConfig.getProperties());

			} catch (Exception e) {
				log.debug(e.getMessage(), e);

				throw new HermesException(e);
			}

			return providerExtension;
		}

		return new DefaultHermesAdminFactory();
	}
}