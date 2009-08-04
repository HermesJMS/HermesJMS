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

package hermes.impl;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesException;
import hermes.config.ClasspathConfig;
import hermes.config.ClasspathGroupConfig;
import hermes.config.ConnectionConfig;
import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;
import hermes.config.HermesConfig;
import hermes.config.JDBCStore;
import hermes.config.ObjectFactory;
import hermes.config.PropertySetConfig;
import hermes.config.ProviderExtConfig;
import hermes.config.RendererConfig;
import hermes.config.SessionConfig;
import hermes.config.WatchConfig;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public interface ConfigDAO
{

   public static final String DEFAULT_PLUGIN = "Default";

   public abstract ObjectFactory getFactory();

   public abstract String getAdminClassForPlugIn(String plugin);

   public abstract String getPlugInName(String adminFactoryClass);

   public abstract URL[] getAdminFactoryURLs();

   public abstract URL getURLForAdminFactory(String adminFactoryClass);

   public abstract void removeJDBC(HermesConfig config, JDBCStore store);

   public abstract JDBCStore addJDBCStore(HermesConfig config, String alias, String driver, String url) throws JAXBException;

   public abstract void removeNamingConfig(HermesConfig config, String id);

   public abstract ClasspathGroupConfig getClasspathGroupConfig(HermesConfig config, String id);

   public abstract ClasspathConfig duplicate(ClasspathConfig cConfig) throws JAXBException;

   public abstract List<ClasspathGroupConfig> duplicateClasspathGroups(List<?> classPathGroups) throws JAXBException;

   public abstract void removeHermes(HermesConfig config, String hermesId) throws HermesException;

   public abstract void removeDestination(HermesConfig config, String hermesId, String destinationName) throws HermesException;

   public abstract Collection<SessionConfig> getAllSessions(HermesConfig config) throws HermesException;

   public abstract Collection getAllDestinations(HermesConfig config, String hermesId) throws HermesException;

   public abstract Properties getRendererProperties(RendererConfig rConfig);

   public abstract Properties getRendererProperties(HermesConfig config, String className) throws HermesException;

   public abstract RendererConfig createRendererConfig(String className, Map<?, ?> map) throws HermesException;

   public abstract void updatePropertySet(PropertySetConfig properties, Map<?, ?> map) throws JAXBException;

   public abstract void populatePropertySet(Map<?, ?> map, PropertySetConfig properties) throws JAXBException;

   public abstract PropertySetConfig createPropertySet() throws HermesException;

   public abstract void setRendererProperties(HermesConfig config, String className, Map<?, ?> props) throws HermesException, JAXBException;

   public abstract WatchConfig createWatchConfig();

   public abstract DestinationConfig createDestinationConfig();

   public abstract DestinationConfig createDestinationConfig(String name, Domain domain);

   public abstract ProviderExtConfig createDefaultProviderExtConfig(String connectionFactoryName) throws JAXBException;

   public abstract DestinationConfig duplicateForWatch(DestinationConfig dConfig, Hermes hermes);

   public abstract DestinationConfig duplicate(DestinationConfig dConfig);

   public abstract Collection<String> getAdminFactories();

   public abstract FactoryConfig createJNDIFactoryConfig(String classpathId, String sessionId, String binding, PropertySetConfig properties, String className)
         throws JAXBException;

   public abstract FactoryConfig createDefaultFactoryConfig(String sessionId) throws JAXBException;

   public abstract void replaceDestinationConfigs(HermesConfig config, String hermesId, Collection<DestinationConfig> destinationConfigs);

   public abstract void renameSession(FactoryConfig factoryConfig, String newSessionId);

   public abstract FactoryConfig getFactoryConfig(HermesConfig config, String hermesId) throws HermesException;

   public abstract SessionConfig duplicate(SessionConfig sourceSession, String newId) throws JAXBException;

   public abstract ConnectionConfig duplicate(ConnectionConfig sourceConnection, String newSessionId) throws JAXBException;

   public abstract FactoryConfig duplicate(FactoryConfig sourceFactory, String newSessionId) throws JAXBException;

   public abstract void duplicateSession(HermesConfig config, String hermesId, String newHermesId) throws JAXBException, HermesException;

}