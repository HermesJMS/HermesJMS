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
import hermes.Hermes;
import hermes.HermesException;
import hermes.NullConnectionFactory;
import hermes.SystemProperties;
import hermes.config.ClasspathConfig;
import hermes.config.ClasspathGroupConfig;
import hermes.config.ConnectionConfig;
import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;
import hermes.config.HermesConfig;
import hermes.config.JDBCStore;
import hermes.config.NamingConfig;
import hermes.config.ObjectFactory;
import hermes.config.PropertyConfig;
import hermes.config.PropertySetConfig;
import hermes.config.ProviderConfig;
import hermes.config.ProviderExtConfig;
import hermes.config.RendererConfig;
import hermes.config.SessionConfig;
import hermes.config.WatchConfig;
import hermes.ext.DefaultHermesAdminFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ConfigHelper.java,v 1.36 2005/10/21 08:37:22 colincrist Exp $
 */
public class ConfigDAOImpl implements ConfigDAO
{
   private static final Logger log = Logger.getLogger(ConfigDAOImpl.class);
   private final ObjectFactory factory = new ObjectFactory();
   private final Properties adminProperties = new Properties();
   private final Collection<String> adminFactories = new ArrayList<String>();
   private final String ADMIN_FACTORIES = "admin.factories";
   private final Map<String, URL> adminFactoryToJARMap = new HashMap<String, URL>();
   private final Map<String, String> adminFactoryToPlugIn = new HashMap<String, String>();
   private final Map<String, String> plugInToAdminFactory = new HashMap<String, String>();
   private URL[] adminFactoryURLs = null;

   public ConfigDAOImpl()
   {
      List<URL> adminFactoryList = new ArrayList<URL>();

      try
      {
         adminProperties.load(ConfigDAOImpl.class.getClassLoader().getResourceAsStream("hermes/impl/confighelper.properties"));

         log.debug(adminProperties);

         if (adminProperties.containsKey(ADMIN_FACTORIES))
         {
            for (StringTokenizer tokens = new StringTokenizer(adminProperties.getProperty(ADMIN_FACTORIES), ","); tokens.hasMoreTokens();)
            {
               String adminFactoryClass = tokens.nextToken();

               adminFactories.add(adminFactoryClass);

               log.debug("factory=" + adminFactoryClass);

               if (adminProperties.containsKey(adminFactoryClass))
               {
                  String libraryPair = (String) adminProperties.get(adminFactoryClass);
                  String[] split = libraryPair.split(",");

                  String library = SystemProperties.EXT_LIBRARY_PATH + "/" + split[0];
                  String libraryLongName = split[1];

                  adminFactoryToPlugIn.put(adminFactoryClass, libraryLongName);
                  
                  if (library.startsWith("http"))
                  {
                     log.debug(libraryLongName + "(" + adminFactoryClass + ") lives in " + library);
                     adminFactoryToJARMap.put(adminFactoryClass, new URL(library));                    
                     plugInToAdminFactory.put(libraryLongName, adminFactoryClass);
                  }
                  else
                  {
                     File libraryFile = new File(library);

                     if (libraryFile.exists())
                     {
                        log.debug(libraryLongName + "(" + adminFactoryClass + ") lives in " + library);
                        adminFactoryToJARMap.put(adminFactoryClass, libraryFile.toURL());                      

                        adminFactoryList.add(libraryFile.toURL());
                     }
                     else
                     {
                        log.error("cannot find " + library + " for " + adminFactoryClass);
                     }
                     plugInToAdminFactory.put(libraryLongName, adminFactoryClass);
                  }
               }
            }
         }
      }
      catch (IOException e)
      {
         log.error("Cannot load confighelper.properties from the ClassLoader: " + e.getMessage(), e);
      }

      adminFactoryURLs = new URL[adminFactoryList.size()];
      int i = 0;

      for (Iterator<URL> iter = adminFactoryList.iterator(); iter.hasNext();)
      {
         URL url = iter.next();
         adminFactoryURLs[i++] = url;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getFactory()
    */
   public ObjectFactory getFactory()
   {
      return factory;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getAdminClassForPlugIn(java.lang.String)
    */
   public String getAdminClassForPlugIn(String plugin)
   {
      if (plugInToAdminFactory.containsKey(plugin))
      {
         return plugInToAdminFactory.get(plugin);
      }
      else
      {
         return DefaultHermesAdminFactory.class.getName();
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getPlugInName(java.lang.String)
    */
   public String getPlugInName(String adminFactoryClass)
   {
      if (adminFactoryToPlugIn.containsKey(adminFactoryClass))
      {
         return adminFactoryToPlugIn.get(adminFactoryClass);
      }
      else
      {
         return DEFAULT_PLUGIN;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getAdminFactoryURLs()
    */
   public URL[] getAdminFactoryURLs()
   {
      return adminFactoryURLs;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getURLForAdminFactory(java.lang.String)
    */
   public URL getURLForAdminFactory(String adminFactoryClass)
   {
      return adminFactoryToJARMap.get(adminFactoryClass);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#removeJDBC(hermes.config.HermesConfig,
    *      hermes.config.JDBCStore)
    */
   public void removeJDBC(HermesConfig config, JDBCStore store)
   {
      config.getJdbcStore().remove(store);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#addJDBCStore(hermes.config.HermesConfig,
    *      java.lang.String, java.lang.String, java.lang.String)
    */
   public JDBCStore addJDBCStore(HermesConfig config, String alias, String driver, String url) throws JAXBException
   {
      JDBCStore storeConfig = factory.createJDBCStore();
      storeConfig.setAlias(alias);
      storeConfig.setDriver(driver);
      storeConfig.setUrl(url);

      for (Iterator iter = config.getJdbcStore().iterator(); iter.hasNext();)
      {
         JDBCStore existing = (JDBCStore) iter.next();

         if (existing.getAlias().equals(alias))
         {
            iter.remove();
         }
      }

      config.getJdbcStore().add(storeConfig);

      return storeConfig;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#removeNamingConfig(hermes.config.HermesConfig,
    *      java.lang.String)
    */
   public void removeNamingConfig(HermesConfig config, String id)
   {
      for (Iterator<?> iter = config.getNaming().iterator(); iter.hasNext();)
      {
         NamingConfig namingConfig = (NamingConfig) iter.next();

         if (namingConfig.getId().equals(id))
         {
            iter.remove();
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getClasspathGroupConfig(hermes.config.HermesConfig,
    *      java.lang.String)
    */
   public ClasspathGroupConfig getClasspathGroupConfig(HermesConfig config, String id)
   {
      for (Iterator<?> iter = config.getClasspathGroup().iterator(); iter.hasNext();)
      {
         ClasspathGroupConfig gConfig = (ClasspathGroupConfig) iter.next();

         if (gConfig.getId().equals(id))
         {
            return gConfig;
         }
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#duplicate(hermes.config.ClasspathConfig)
    */
   public ClasspathConfig duplicate(ClasspathConfig cConfig) throws JAXBException
   {
      ClasspathConfig newCConfig = factory.createClasspathConfig();

      newCConfig.setFactories(cConfig.getFactories());
      newCConfig.setJar(cConfig.getJar());
      newCConfig.setNoFactories(cConfig.isNoFactories());

      return newCConfig;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#duplicateClasspathGroups(java.util.List)
    */
   public List<ClasspathGroupConfig> duplicateClasspathGroups(List<?> classPathGroups) throws JAXBException
   {
      List<ClasspathGroupConfig> rval = new ArrayList<ClasspathGroupConfig>();

      for (Iterator<?> iter = classPathGroups.iterator(); iter.hasNext();)
      {
         ClasspathGroupConfig gConfig = (ClasspathGroupConfig) iter.next();
         ClasspathGroupConfig newGConfig = factory.createClasspathGroupConfig();

         newGConfig.setId(gConfig.getId());
         rval.add(newGConfig);

         for (Iterator<ClasspathConfig> iter2 = gConfig.getLibrary().iterator(); iter2.hasNext();)
         {
            ClasspathConfig cConfig = iter2.next();
            ClasspathConfig newCConfig = duplicate(cConfig);

            newGConfig.getLibrary().add(newCConfig);
         }
      }

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#removeHermes(hermes.config.HermesConfig,
    *      java.lang.String)
    */
   public void removeHermes(HermesConfig config, String hermesId) throws HermesException
   {
      for (Iterator<FactoryConfig> iter = config.getFactory().iterator(); iter.hasNext();)
      {
         FactoryConfig factoryConfig = iter.next();

         for (Iterator<?> iter2 = factoryConfig.getDestination().iterator(); iter2.hasNext();)
         {
            DestinationConfig destinationConfig = (DestinationConfig) iter2.next();
         }

         for (Iterator<ConnectionConfig> iter3 = factoryConfig.getConnection().iterator(); iter3.hasNext();)
         {
            ConnectionConfig connectionConfig = iter3.next();

            for (Iterator<SessionConfig> iter4 = connectionConfig.getSession().iterator(); iter4.hasNext();)
            {

               SessionConfig sessionConfig = iter4.next();

               if (sessionConfig.getId() == null)
               {
                  iter4.remove();
               }
               else if (sessionConfig.getId().equals(hermesId))
               {
                  iter4.remove();

                  log.debug(hermesId + " removed");

                  return;
               }
            }
         }
      }

      throw new HermesException("no session " + hermesId);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#removeDestination(hermes.config.HermesConfig,
    *      java.lang.String, java.lang.String)
    */
   public void removeDestination(HermesConfig config, String hermesId, String destinationName) throws HermesException
   {
      for (Iterator<FactoryConfig> iter = config.getFactory().iterator(); iter.hasNext();)
      {
         FactoryConfig factoryConfig = iter.next();

         for (Iterator<ConnectionConfig> iter3 = factoryConfig.getConnection().iterator(); iter3.hasNext();)
         {

            ConnectionConfig connectionConfig = iter3.next();

            for (Iterator<SessionConfig> iter4 = connectionConfig.getSession().iterator(); iter4.hasNext();)
            {

               SessionConfig sessionConfig = iter4.next();

               if (sessionConfig.getId().equals(hermesId))
               {
                  for (Iterator<?> iter2 = factoryConfig.getDestination().iterator(); iter2.hasNext();)
                  {
                     DestinationConfig destinationConfig = (DestinationConfig) iter2.next();

                     if (destinationConfig.getName().equals(destinationName))
                     {
                        log.debug(destinationName + " removed.");

                        iter2.remove();

                        return;
                     }
                  }

               }
            }
         }
      }

      throw new HermesException("no destination " + destinationName + " configured for session " + hermesId);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getAllSessions(hermes.config.HermesConfig)
    */
   public Collection<SessionConfig> getAllSessions(HermesConfig config) throws HermesException
   {
      Collection<SessionConfig> rval = new ArrayList<SessionConfig>();

      for (Iterator<FactoryConfig> iter1 = config.getFactory().iterator(); iter1.hasNext();)
      {
         FactoryConfig factoryConfig = iter1.next();

         for (Iterator<ConnectionConfig> iter2 = factoryConfig.getConnection().iterator(); iter2.hasNext();)
         {
            ConnectionConfig connectionConfig = iter2.next();

            for (Iterator<SessionConfig> iter3 = connectionConfig.getSession().iterator(); iter3.hasNext();)
            {
               SessionConfig sessionConfig = iter3.next();

               rval.add(sessionConfig);
            }
         }
      }

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getAllDestinations(hermes.config.HermesConfig,
    *      java.lang.String)
    */
   public Collection getAllDestinations(HermesConfig config, String hermesId) throws HermesException
   {
      Collection<Object> rval = new ArrayList<Object>();

      for (Iterator<FactoryConfig> iter1 = config.getFactory().iterator(); iter1.hasNext();)
      {
         FactoryConfig factoryConfig = iter1.next();

         for (Iterator<ConnectionConfig> iter2 = factoryConfig.getConnection().iterator(); iter2.hasNext();)
         {
            ConnectionConfig connectionConfig = iter2.next();

            for (Iterator<SessionConfig> iter3 = connectionConfig.getSession().iterator(); iter3.hasNext();)
            {

               SessionConfig sessionConfig = iter3.next();

               if (sessionConfig.getId() == null)
               {
                  log.debug("session with a null id removed");

                  iter3.remove();
               }
               else
               {
                  if (hermesId == null || sessionConfig.getId().equals(hermesId))
                  {
                     for (Iterator iter4 = factoryConfig.getDestination().iterator(); iter4.hasNext();)
                     {
                        rval.add(iter4.next());
                     }
                  }
               }
            }
         }
      }

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getRendererProperties(hermes.config.RendererConfig)
    */
   public Properties getRendererProperties(RendererConfig rConfig)
   {
      Properties props = new Properties();

      if (rConfig.getProperties() != null)
      {
         for (Iterator<PropertyConfig> iter2 = rConfig.getProperties().getProperty().iterator(); iter2.hasNext();)
         {
            PropertyConfig pConfig = iter2.next();

            props.put(pConfig.getName(), pConfig.getValue());
         }
      }

      return props;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getRendererProperties(hermes.config.HermesConfig,
    *      java.lang.String)
    */
   public Properties getRendererProperties(HermesConfig config, String className) throws HermesException
   {
      Properties props = new Properties();

      for (Iterator<?> iter = config.getRenderer().iterator(); iter.hasNext();)
      {
         RendererConfig rConfig = (RendererConfig) iter.next();

         if (rConfig.getClassName().equals(className))
         {
            return getRendererProperties(rConfig);
         }
      }

      throw new HermesException("no such renderer " + className);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#createRendererConfig(java.lang.String,
    *      java.util.Map)
    */
   public RendererConfig createRendererConfig(String className, Map<?, ?> map) throws HermesException
   {
      try
      {
         RendererConfig rConfig = factory.createRendererConfig();
         rConfig.setClassName(className);
         PropertySetConfig properties = rConfig.getProperties();

         if (properties == null)
         {
            properties = new PropertySetConfig();
            rConfig.setProperties(properties);
         }

         populatePropertySet(map, properties);

         return rConfig;
      }
      catch (JAXBException e)
      {
         throw new HermesException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#updatePropertySet(hermes.config.PropertySetConfig,
    *      java.util.Map)
    */
   public void updatePropertySet(PropertySetConfig properties, Map<?, ?> map) throws JAXBException
   {
      for (Iterator<PropertyConfig> iter = properties.getProperty().iterator(); iter.hasNext();)
      {
         final PropertyConfig pConfig = iter.next();

         if (map.containsKey(pConfig.getName()))
         {
            iter.remove();
         }
      }

      for (Iterator<?> iter2 = map.entrySet().iterator(); iter2.hasNext();)
      {
         final Map.Entry entry = (Map.Entry) iter2.next();
         final PropertyConfig pConfig = factory.createPropertyConfig();

         pConfig.setName((String) entry.getKey());

         if (entry.getValue() != null)
         {
            pConfig.setValue(entry.getValue().toString());
            properties.getProperty().add(pConfig);
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#populatePropertySet(java.util.Map,
    *      hermes.config.PropertySetConfig)
    */
   public void populatePropertySet(Map<?, ?> map, PropertySetConfig properties) throws JAXBException
   {
      for (Iterator<?> iter2 = map.keySet().iterator(); iter2.hasNext();)
      {
         String key = (String) iter2.next();

         if (map.get(key) != null)
         {
            String value = map.get(key).toString();

            if (!value.equals("") && !key.equals("class") && !key.equals("name"))
            {
               PropertyConfig pConfig = factory.createPropertyConfig();

               pConfig.setName(key);
               pConfig.setValue(value);

               properties.getProperty().add(pConfig);
            }
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#createPropertySet()
    */
   public PropertySetConfig createPropertySet() throws HermesException
   {
      return factory.createPropertySetConfig();
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#setRendererProperties(hermes.config.HermesConfig,
    *      java.lang.String, java.util.Map)
    */
   public void setRendererProperties(HermesConfig config, String className, Map<?, ?> props) throws HermesException, JAXBException
   {
      for (Iterator<?> iter = config.getRenderer().iterator(); iter.hasNext();)
      {
         RendererConfig rConfig = (RendererConfig) iter.next();

         if (rConfig.getClassName().equals(className))
         {
            if (rConfig.getProperties() == null)
            {
               rConfig.setProperties(new PropertySetConfig());
            }

            rConfig.getProperties().getProperty().clear();

            for (Iterator<?> iter2 = props.keySet().iterator(); iter2.hasNext();)
            {
               final String key = (String) iter2.next();

               if (props.get(key) != null)
               {
                  final String value = props.get(key).toString();

                  if (!key.equals("class") && !key.equals("name"))
                  {
                     PropertyConfig pConfig = factory.createPropertyConfig();

                     pConfig.setName(key);
                     pConfig.setValue(value);

                     rConfig.getProperties().getProperty().add(pConfig);
                  }
               }
            }

            return;
         }
      }

      throw new HermesException("no such renderer " + className);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#createWatchConfig()
    */
   public WatchConfig createWatchConfig()
   {
      WatchConfig rval = new WatchConfig();

      rval.setShowAge(true);
      rval.setUpdateFrequency(30 * 1000L);

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#createDestinationConfig()
    */
   public DestinationConfig createDestinationConfig()
   {
      return new DestinationConfig();
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#createDestinationConfig(java.lang.String,
    *      hermes.Domain)
    */
   public DestinationConfig createDestinationConfig(String name, Domain domain)
   {
      DestinationConfig rval = new DestinationConfig();
      rval.setName(name);
      rval.setDomain(domain.getId());

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#createDefaultProviderExtConfig(java.lang.String)
    */
   public ProviderExtConfig createDefaultProviderExtConfig(String connectionFactoryName) throws JAXBException
   {
      final ProviderExtConfig config = factory.createProviderExtConfig();

      if (adminProperties.containsKey(connectionFactoryName))
      {
         config.setClassName((String) adminProperties.get(connectionFactoryName));
      }
      else
      {
         config.setClassName(DefaultHermesAdminFactory.class.getName());
      }

      config.setProperties(factory.createPropertySetConfig());

      return config;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#duplicateForWatch(hermes.config.DestinationConfig,
    *      hermes.Hermes)
    */
   public DestinationConfig duplicateForWatch(DestinationConfig dConfig, Hermes hermes)
   {
      DestinationConfig rval = duplicate(dConfig);
      rval.setSelector(null);
      rval.setMyHermes(hermes.getId());
      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#duplicate(hermes.config.DestinationConfig)
    */
   public DestinationConfig duplicate(DestinationConfig dConfig)
   {
      DestinationConfig rval = new DestinationConfig();

      rval.setName(dConfig.getName());
      rval.setShortName(dConfig.getShortName());
      rval.setSelector(dConfig.getSelector());
      rval.setDomain(dConfig.getDomain());
      rval.setDurable(dConfig.isDurable());
      rval.setClientID(dConfig.getClientID());

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getAdminFactories()
    */
   public Collection<String> getAdminFactories()
   {
      return adminFactories;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#createJNDIFactoryConfig(java.lang.String,
    *      java.lang.String, java.lang.String, hermes.config.PropertySetConfig,
    *      java.lang.String)
    */
   public FactoryConfig createJNDIFactoryConfig(String classpathId, String sessionId, String binding, PropertySetConfig properties, String className)
         throws JAXBException
   {
      FactoryConfig factoryConfig = new FactoryConfig();
      factoryConfig.setClasspathId(classpathId);
      SessionConfig sessionConfig = new SessionConfig();
      ConnectionConfig connectConfig = new ConnectionConfig();
      ProviderConfig providerConfig = new ProviderConfig();

      sessionConfig.setId(sessionId);
      sessionConfig.setTransacted(true);
      sessionConfig.setReconnects(BigInteger.ZERO);

      connectConfig.getSession().add(sessionConfig);

      factoryConfig.getConnection().add(connectConfig);

      factoryConfig.setExtension(createDefaultProviderExtConfig(NullConnectionFactory.class.getName()));

      PropertySetConfig propertySet = factory.createPropertySetConfig();

      // Copy over the other ones...

      for (Iterator<PropertyConfig> iter = properties.getProperty().iterator(); iter.hasNext();)
      {
         PropertyConfig pConfig = iter.next();

         if (!pConfig.getName().equals("binding"))
         {
            propertySet.getProperty().add(pConfig);
         }
      }

      PropertyConfig bProperty = factory.createPropertyConfig();

      // Update with the binding.

      bProperty.setName("binding");
      bProperty.setValue(binding);

      propertySet.getProperty().add(bProperty);
      providerConfig.setProperties(propertySet);

      providerConfig.setClassName(className);
      factoryConfig.setProvider(providerConfig);

      return factoryConfig;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#createDefaultFactoryConfig(java.lang.String)
    */
   public FactoryConfig createDefaultFactoryConfig(String sessionId) throws JAXBException
   {
      FactoryConfig factoryConfig = new FactoryConfig();
      factoryConfig.setClasspathId(SimpleClassLoaderManager.SYSTEM_LOADER);
      SessionConfig sessionConfig = new SessionConfig();
      ConnectionConfig connectConfig = new ConnectionConfig();
      ProviderConfig providerConfig = new ProviderConfig();

      sessionConfig.setId(sessionId);
      connectConfig.getSession().add(sessionConfig);
      factoryConfig.getConnection().add(connectConfig);
      factoryConfig.setExtension(createDefaultProviderExtConfig(NullConnectionFactory.class.getName()));
      sessionConfig.setTransacted(true);

      providerConfig.setProperties(new PropertySetConfig());
      factoryConfig.setProvider(providerConfig);

      return factoryConfig;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#replaceDestinationConfigs(hermes.config.HermesConfig,
    *      java.lang.String, java.util.Collection)
    */
   public void replaceDestinationConfigs(HermesConfig config, String hermesId, Collection<DestinationConfig> destinationConfigs)
   {
      for (Iterator<FactoryConfig> fIter = config.getFactory().iterator(); fIter.hasNext();)
      {
         final FactoryConfig factoryConfig = fIter.next();

         for (Iterator<ConnectionConfig> cIter = factoryConfig.getConnection().iterator(); cIter.hasNext();)
         {
            final ConnectionConfig connConfig = cIter.next();

            for (Iterator<SessionConfig> sIter = connConfig.getSession().iterator(); sIter.hasNext();)
            {
               final SessionConfig sConfig = sIter.next();

               if (sConfig.getId().equals(hermesId))
               {
                  for (Iterator iter = factoryConfig.getDestination().iterator(); iter.hasNext();)
                  {
                     DestinationConfig dConfig = (DestinationConfig) iter.next();

                     if (dConfig.getDomain() == Domain.TOPIC.getId() && dConfig.isDurable())
                     {
                        // Don't replace the durable topics....
                     }
                     else
                     {
                        iter.remove();
                     }
                  }

                  factoryConfig.getDestination().addAll(destinationConfigs);

                  return;
               }
            }
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#renameSession(hermes.config.FactoryConfig,
    *      java.lang.String)
    */
   public void renameSession(FactoryConfig factoryConfig, String newSessionId)
   {
      ConnectionConfig connectionConfig = (ConnectionConfig) factoryConfig.getConnection().get(0);
      SessionConfig sessionConfig = (SessionConfig) connectionConfig.getSession().get(0);

      sessionConfig.setId(newSessionId);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#getFactoryConfig(hermes.config.HermesConfig,
    *      java.lang.String)
    */
   public FactoryConfig getFactoryConfig(HermesConfig config, String hermesId) throws HermesException
   {
      for (Iterator<FactoryConfig> factoryIter = config.getFactory().iterator(); factoryIter.hasNext();)
      {
         FactoryConfig factoryConfig = factoryIter.next();
         ConnectionConfig connectionConfig = (ConnectionConfig) factoryConfig.getConnection().get(0);

         if (connectionConfig.getSession().size() > 0)
         {
            SessionConfig sessionConfig = (SessionConfig) connectionConfig.getSession().get(0);

            if (sessionConfig.getId().equals(hermesId))
            {
               return factoryConfig;
            }
         }
      }

      throw new HermesException("no such session " + hermesId);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#duplicate(hermes.config.SessionConfig,
    *      java.lang.String)
    */
   public SessionConfig duplicate(SessionConfig sourceSession, String newId) throws JAXBException
   {
      SessionConfig rval = factory.createSessionConfig();

      rval.setAudit(sourceSession.isAudit());
      rval.setAuditDirectory(sourceSession.getAuditDirectory());
      rval.setCheckSize(sourceSession.isCheckSize());
      rval.setCheckSizePeriod(sourceSession.getCheckSizePeriod());
      rval.setId(newId);
      rval.setReconnects(sourceSession.getReconnects());
      rval.setTransacted(sourceSession.isTransacted());

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#duplicate(hermes.config.ConnectionConfig,
    *      java.lang.String)
    */
   public ConnectionConfig duplicate(ConnectionConfig sourceConnection, String newSessionId) throws JAXBException
   {
      ConnectionConfig rval = factory.createConnectionConfig();
      SessionConfig sessionConfig = (SessionConfig) sourceConnection.getSession().get(0);

      rval.setPassword(sourceConnection.getPassword());
      rval.setUsername(sourceConnection.getUsername());

      rval.getSession().add(duplicate(sessionConfig, newSessionId));

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#duplicate(hermes.config.FactoryConfig,
    *      java.lang.String)
    */
   public FactoryConfig duplicate(FactoryConfig sourceFactory, String newSessionId) throws JAXBException
   {
      FactoryConfig rval = factory.createFactoryConfig();
      ConnectionConfig connectionConfig = (ConnectionConfig) sourceFactory.getConnection().get(0);

      rval.setClasspathId(sourceFactory.getClasspathId());
      rval.setExtension(sourceFactory.getExtension());
      rval.setProvider(sourceFactory.getProvider());

      rval.getConnection().add(duplicate(connectionConfig, newSessionId));

      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.ConfigDAO#duplicateSession(hermes.config.HermesConfig,
    *      java.lang.String, java.lang.String)
    */
   public void duplicateSession(HermesConfig config, String hermesId, String newHermesId) throws JAXBException, HermesException
   {
      FactoryConfig sourceFactory = getFactoryConfig(config, hermesId);
      FactoryConfig newFactory = duplicate(sourceFactory, newHermesId);

      newFactory.getDestination().addAll(sourceFactory.getDestination());

      config.getFactory().add(newFactory);
   }
}