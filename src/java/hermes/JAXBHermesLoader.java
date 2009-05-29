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

package hermes;

import hermes.browser.HermesBrowser;
import hermes.browser.dialog.SelectorImpl;
import hermes.config.ClasspathGroupConfig;
import hermes.config.ConnectionConfig;
import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;
import hermes.config.HermesConfig;
import hermes.config.NamingConfig;
import hermes.config.QuickFIXConfig;
import hermes.config.SessionConfig;
import hermes.config.WatchConfig;
import hermes.fix.quickfix.QuickFIXMessageCache;
import hermes.impl.ClassLoaderManager;
import hermes.impl.ConnectionFactoryManager;
import hermes.impl.ConnectionFactoryManagerImpl;
import hermes.impl.ConnectionManager;
import hermes.impl.ConnectionManagerFactory;
import hermes.impl.DefaultHermesImpl;
import hermes.impl.DestinationManager;
import hermes.impl.JNDIDestinationManager;
import hermes.impl.NullClassLoaderManager;
import hermes.impl.SessionManager;
import hermes.impl.SimpleClassLoaderManager;
import hermes.impl.jms.SimpleDestinationManager;
import hermes.impl.jms.ThreadLocalSessionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JAXBHermesLoader.java,v 1.52 2007/02/18 19:01:42 colincrist Exp $
 */
public class JAXBHermesLoader implements HermesLoader
{
   private static final Logger log = Logger.getLogger(JAXBHermesLoader.class);
   private static final String FILE_NAME = "hermes-config.xml";
   private static final String DEFAULT_EXTENSION_LOADER = "hermes.ext.ExtensionFinderImpl";

   private URL url;
   private File file;
   private HermesConfig config;
   private JAXBElement<Object> element ;
   private ClassLoaderManager classLoaderManager;
   private Hashtable properties;
   private final Set listeners = new HashSet();
   private final Map<String, FactoryConfig> factoryConfigById = new HashMap<String, FactoryConfig>();
   private boolean ignoreClasspathGroups ;
   
   private Context context;
   private String extensionLoaderClass = DEFAULT_EXTENSION_LOADER;

   public JAXBHermesLoader()
   {

   }

   public void setContext(Context context)
   {
      this.context = context;
   }

   public void backup() throws HermesException
   {
      try
      {
         if (file != null)
         {
            copyFile(file.getAbsolutePath(), file.getAbsolutePath() + ".backup");
         }
      }
      catch (IOException e)
      {
         throw new HermesException(e);
      }
   }

   public void restore() throws HermesException
   {
      try
      {
         if (file != null)
         {
            copyFile(file.getAbsolutePath() + ".backup", file.getAbsolutePath());
         }
      }
      catch (IOException e)
      {
         throw new HermesException(e);
      }
   }

   public void save() throws HermesException
   {
      try
      {
         if (file != null)
         {
            OutputStream ostream = new FileOutputStream(file);
            JAXBContext jc = JAXBContext.newInstance("hermes.config");
            Marshaller m = jc.createMarshaller();

            config.setLastEditedByHermesVersion(Hermes.VERSION);
            config.setLastEditedByUser(System.getProperty("user.name"));
            config.setLookAndFeel(UIManager.getLookAndFeel().getClass().getName()) ;

            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            element.setValue(config) ;
            m.marshal(element, ostream);

            ostream.close();
         }
         else
         {
            throw new HermesException("No file to save configuration to (did you load from a URL?)");
         }
      }
      catch (FileNotFoundException e)
      {
         throw new HermesException(e);
      }
      catch (PropertyException e)
      {
         throw new HermesException(e);
      }
      catch (JAXBException e)
      {
         throw new HermesException(e);
      }
      catch (IOException e)
      {
         throw new HermesException(e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesLoader#doLoad()
    */

   public void addDestinationConfig(Hermes hermes, DestinationConfig config) throws JMSException
   {
      if (factoryConfigById.containsKey(hermes.getId()))
      {
         FactoryConfig fConfig = (FactoryConfig) factoryConfigById.get(hermes.getId());
         fConfig.getDestination().add(config);
         hermes.addDestinationConfig(config);
         notifyDestinationAdded(hermes, config);
      }
      else
      {
         throw new HermesException("No such session " + hermes.getId());
      }
   }

   public void replaceDestinationConfigs(Hermes hermes, Collection dConfigs) throws JMSException
   {
      boolean keepDurableSubscriptions = true, keepDurableSubscriptionsDialogShown = false;

      if (factoryConfigById.containsKey(hermes.getId()))
      {
         final FactoryConfig fConfig = factoryConfigById.get(hermes.getId());

         for (final Iterator iter = fConfig.getDestination().iterator(); iter.hasNext();)
         {
            final DestinationConfig dConfig = (DestinationConfig) iter.next();

            if (dConfig.getDomain() == Domain.TOPIC.getId() && dConfig.isDurable())
            {
               if (!keepDurableSubscriptionsDialogShown)
               {
                  if (HermesBrowser.getBrowser() != null)
                  {
                     if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), "Do you want to keep configured durable subscriptions?",
                           "Durable Subscriptions", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                     {
                        keepDurableSubscriptions = false;
                     }
                  }

                  keepDurableSubscriptionsDialogShown = true;
               }

               if (!keepDurableSubscriptions)
               {
                  iter.remove();
                  notifyDestinationRemoved(hermes, dConfig);
               }
            }
            else
            {
               iter.remove();
               notifyDestinationRemoved(hermes, dConfig);
            }
         }

         for (final Iterator iter = dConfigs.iterator(); iter.hasNext();)
         {
            final DestinationConfig dConfig = (DestinationConfig) iter.next();

            fConfig.getDestination().add(dConfig);
            hermes.addDestinationConfig(dConfig);
            notifyDestinationAdded(hermes, dConfig);
         }
      }
      else
      {
         throw new HermesException("No such session " + hermes.getId());
      }
   }

   /**
    * Everything is in here to load up all the Hermes instances from JAXB, it
    * kinda sux really. Would be nice to have proper framework for this.
    */
   public List<Hermes> load() throws HermesException
   {
      ArrayList<Hermes> rval = null;
      InputStream istream = null;
      String from;

      factoryConfigById.clear();

      if (properties == null)
      {
         throw new HermesException("No properties available");
      }

      try
      {
         if (file == null)
         {
            if (!properties.containsKey(Context.PROVIDER_URL))
            {
               from = FILE_NAME;
            }
            else
            {
               from = (String) properties.get(Context.PROVIDER_URL);
            }

            log.debug("attempting to load from URL: " + from);

            try
            {
               istream = new URL(from).openStream();
            }
            catch (IOException ex)
            {
               log.info("failed to load configuration from " + from + ", attempting to load as a file...");
            }

            if (istream == null)
            {
               file = new File(from);

               log.debug("trying to load from file: " + file.getName());

               if (!file.exists())
               {
                  throw new NoConfigurationException();
               }

               istream = new FileInputStream(file);
            }

         }
         else
         {
            istream = new FileInputStream(file);
         }

         rval = new ArrayList<Hermes>();

         JAXBContext jc = JAXBContext.newInstance("hermes.config");
         Unmarshaller u = jc.createUnmarshaller();
         
         element = (JAXBElement<Object>) u.unmarshal(istream) ;
         config = (HermesConfig) element.getValue() ;

         Hermes.ui.setConfig(config) ;
         
         if (config.getLastEditedByHermesVersion() == null)
         {
            //
            // First time Hermes1.7RC3 was run with this config

            config.setLastEditedByHermesVersion(Hermes.VERSION);
            config.setDisplayFactoryAdmin(true);
         }

         
         // This is for the 1.10 upgrade

         if (config.getQuickFIX() == null)
         {
            QuickFIXConfig quickFIXConfig = new QuickFIXConfig() ;
            
            quickFIXConfig.setCacheSize(1024) ;
            
            config.setQuickFIX(quickFIXConfig) ;
         }
         
         config.setSelectorImpl(SelectorImpl.JAMSEL.getClazz().getName());

         // This is for the 1.9 upgrdfe

         if (config.getAutoBrowseRefreshRate() == 0)
         {
            config.setAutoBrowseRefreshRate(10);
         }

         //
         // This is for the 1.8 upgrade

         if (config.getMaxColumnsInStatisticsTable() == 0)
         {
            config.setMaxColumnsInStatisticsTable(10);
         }

         //
         // This is for the 1.7 upgrade where we want to move the old global
         // classloader into a default ClassloaderGroup.

         if (config.getLoader() != null && config.getLoader().size() > 0)
         {
            ClasspathGroupConfig gConfig = new ClasspathGroupConfig();
            gConfig.setId(SimpleClassLoaderManager.DEFAULT_LOADER);
            gConfig.getLibrary().addAll(config.getLoader());

            config.getClasspathGroup().clear();
            config.getClasspathGroup().add(gConfig);
            config.getLoader().clear();
         }

         if (!ignoreClasspathGroups)
         {
            classLoaderManager = new SimpleClassLoaderManager(config.getClasspathGroup());
         }
         else
         {
            classLoaderManager = new NullClassLoaderManager();
         }
         
         SingletonManager.put(ClassLoaderManager.class, classLoaderManager);

         //
         // QuickFIX
         
         QuickFIXMessageCache cache = (QuickFIXMessageCache) SingletonManager.get(QuickFIXMessageCache.class) ;
         cache.setSize(config.getQuickFIX().getCacheSize()) ;
         
         //
         // Deal with any renderers

         HermesBrowser.getRendererManager().setConfig(classLoaderManager.getDefaultClassLoader(), config);
         Hermes.ui.getThreadPool().setThreads(config.getMaxThreadPoolSize());

         // HermesBrowser.getThreadPool().setClassLoader(classLoader);

         if (config.getWatch() == null)
         {
            //
            // Maybe an upgrade from a pre-1.6 version, fill in what should
            // be there in the config.

            WatchConfig wConfig = new WatchConfig();

            wConfig.setShowAge(true);
            config.getWatch().add(wConfig);
         }

         for (Iterator iter = config.getFactory().iterator(); iter.hasNext();)
         {
            final FactoryConfig factoryConfig = (FactoryConfig) iter.next();

            /*
             * If for some reason the XML has no connection or sesssion then
             * clean up the XML.
             */

            if (factoryConfig.getConnection().size() == 0)
            {
               log.debug("cleaning up FactoryConfig with no connections");
               iter.remove();
               continue;
            }

            ConnectionConfig firstConnection = (ConnectionConfig) factoryConfig.getConnection().get(0);

            if (firstConnection.getSession().size() == 0)
            {
               log.debug("cleaning up FactoryConfig with no sessions");
               iter.remove();
               continue;
            }

            SessionConfig firstSession = (SessionConfig) firstConnection.getSession().get(0);

            if (firstSession.getId() == null)
            {
               log.debug("cleaning up FactoryConfig with a null session");
               iter.remove();
               continue;
            }

            try
            {

               Hermes hermes = createHermes(factoryConfig);

               rval.add(hermes);

               notifyHermesAdded(hermes);

               for (Iterator diter = hermes.getDestinations(); diter.hasNext();)
               {
                  DestinationConfig destinationConfig = (DestinationConfig) diter.next();

                  boolean isQueue = hermes.getConnectionFactory() instanceof QueueConnectionFactory;
                  boolean isTopic = hermes.getConnectionFactory() instanceof TopicConnectionFactory;

                  if (destinationConfig.getDomain() == 0 && HermesBrowser.getBrowser() != null)
                  {
                     if (isQueue && isTopic)
                     {
                        Object options[] = { "Queue", "Topic" };

                        int n = JOptionPane.showOptionDialog(HermesBrowser.getBrowser(),
                              "This XML is from an older version of Hermes and it is unclear which domain the destination\n" + destinationConfig.getName()
                                    + " for session " + hermes.getId() + " is in. Please choose whether queue or topic domain", "Select domain",
                              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

                        if (n == JOptionPane.YES_OPTION)
                        {
                           isQueue = true;
                           isTopic = false;

                           log.info(destinationConfig.getName() + " is now in the queue domain");
                        }
                        else
                        {
                           isQueue = false;
                           isTopic = true;

                           log.info(destinationConfig.getName() + " is now in the topic domain");
                        }
                     }

                     if (isQueue)
                     {
                        destinationConfig.setDomain(Domain.QUEUE.getId());
                     }
                     else if (isTopic)
                     {
                        destinationConfig.setDomain(Domain.TOPIC.getId());
                     }
                  }

                  notifyDestinationAdded(hermes, destinationConfig);
               }
            }

            catch (Throwable t)
            {
               log.error("unable to create Hermes instance " + firstSession.getId() + ": " + t.getMessage(), t);
            }
         }

         return rval;
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);

         throw new HermesException(e);
      }
   }

   public Hermes createHermes(FactoryConfig factoryConfig) throws JAXBException, IOException, JMSException, NamingException, InstantiationException,
         ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
   {
      Hermes hermes = null;
      boolean isJNDI = false;
      ConnectionFactoryManager connectionFactoryManager = null;
      ConnectionFactory connectionFactory = null;

      if (factoryConfig.getExtension() == null)
      {
         factoryConfig.setExtension(HermesBrowser.getConfigDAO().createDefaultProviderExtConfig(factoryConfig.getProvider().getClassName()));
      }

      ClassLoader classLoader = getClass().getClassLoader();

      if (factoryConfig.getClasspathId() == null)
      {
         factoryConfig.setClasspathId(SimpleClassLoaderManager.DEFAULT_LOADER);
      }

      classLoader = classLoaderManager.createClassLoader(factoryConfig.getClasspathId(), factoryConfig.getExtension());
      Thread.currentThread().setContextClassLoader(classLoader);

      connectionFactoryManager = new ConnectionFactoryManagerImpl(classLoaderManager, factoryConfig);

      //
      // You can now get the factory from the manager.

      connectionFactory = connectionFactoryManager.getConnectionFactory();

      if (connectionFactory instanceof JNDIConnectionFactory)
      {
         isJNDI = true;
         JNDIConnectionFactory jndiFactory = (JNDIConnectionFactory) connectionFactory;
         jndiFactory._setDelegateClassLoader(classLoader);
      }

      for (Iterator iter2 = factoryConfig.getDestination().iterator(); iter2.hasNext();)
      {
         DestinationConfig destinationConfig = (DestinationConfig) iter2.next();
        
         if (destinationConfig.isDurable() && destinationConfig.getClientID() == null)
         {
            log.warn("removing durable subscription to " + destinationConfig.getName() + "with a null clientID");
            iter2.remove();
         }
         else
         {
            connectionFactoryManager.addDestinationConfig(destinationConfig);
         }
      }

      if (factoryConfig.getConnection().size() > 0)
      {
         ConnectionConfig connectionConfig = (ConnectionConfig) factoryConfig.getConnection().get(0);
         ConnectionManager connectionManager = null;
       
         if (connectionConfig.isConnectionPerThread())
         {
            connectionManager = ConnectionManagerFactory.create(ConnectionManager.Policy.CONNECTION_PER_THREAD);
         }
         else
         {
            connectionManager = ConnectionManagerFactory.create(ConnectionManager.Policy.SHARED_CONNECTION);
         }

         connectionManager.setClientID(connectionConfig.getClientID());
         connectionManager.setUsername(connectionConfig.getUsername());
         connectionManager.setPassword(connectionConfig.getPassword());

         if (connectionConfig.getSession().size() > 0)
         {
            SessionManager sessionManager;
            DestinationManager destinationManager;
            SessionConfig sessionConfig = (SessionConfig) connectionConfig.getSession().get(0);

            if (isJNDI)
            {
               JNDIConnectionFactory jndiCF = (JNDIConnectionFactory) connectionFactory;
               jndiCF._setDelegateClassLoader(classLoader);

               destinationManager = new JNDIDestinationManager(jndiCF._getProperties(), true);
               sessionManager = new ThreadLocalSessionManager(sessionConfig, destinationManager);
            }
            else
            {
               destinationManager = new SimpleDestinationManager();
               sessionManager = new ThreadLocalSessionManager(sessionConfig, destinationManager);
            }

            log.debug("SESSION IS " + sessionConfig.getId());

            if (sessionConfig.getReconnects() != null)
            {
               sessionManager.setReconnects(sessionConfig.getReconnects().intValue());
            }

            classLoaderManager.putClassLoaderByHermes(sessionConfig.getId(), classLoader);
            factoryConfigById.put(sessionConfig.getId(), factoryConfig);

            sessionManager.setTransacted(sessionConfig.isTransacted() );
            sessionManager.setId(sessionConfig.getId());
            sessionManager.setFactoryConfig(factoryConfig);
            sessionManager.setAudit(sessionConfig.isAudit());

            sessionManager.setParent(connectionManager);
            connectionManager.setParent(connectionFactoryManager);

            if (config.getAuditDirectory() != null)
            {
               sessionManager.setAuditDirectory(config.getAuditDirectory());
            }
            else if (sessionConfig.getAuditDirectory() != null)
            {
               sessionManager.setAuditDirectory(sessionConfig.getAuditDirectory());
            }

            connectionManager.addChild(sessionManager);

            hermes = new DefaultHermesImpl(factoryConfig.getExtension(), sessionManager, classLoader);
            
            connectionFactoryManager.addChild(connectionManager);
            connectionManager.setHermes(hermes) ;
         }

      }
      return hermes;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesLoader#getConfig()
    */

   public HermesConfig getConfig() throws HermesException
   {
      return config;
   }

   private static void copyFile(String src, String dest) throws IOException
   {
      File destFile = new File(dest);

      if (destFile.exists())
      {
         destFile.delete();
      }

      FileChannel srcChannel = new FileInputStream(src).getChannel();
      FileChannel dstChannel = new FileOutputStream(dest).getChannel();

      dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

      srcChannel.close();
      dstChannel.close();
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesLoader#setProperties(java.util.Hashtable)
    */
   public void setProperties(Hashtable map)
   {
      this.properties = map;

   }

   public Iterator getConfigurationListeners()
   {
      return listeners.iterator();
   }

   public void addConfigurationListener(HermesConfigurationListener listener)
   {

      listeners.add(listener);

      //
      // Give it its initial state..

      try
      {
         for (NamingEnumeration e = context.listBindings(""); e.hasMore();)
         {
            Binding binding = (Binding) e.next();

            try
            {
               if (context.lookup(binding.getName()) instanceof Hermes)
               {
                  Hermes hermes = (Hermes) context.lookup(binding.getName());

                  listener.onHermesAdded(hermes);

                  for (Iterator diter = hermes.getDestinations(); diter.hasNext();)
                  {
                     DestinationConfig destinationConfig = (DestinationConfig) diter.next();

                     listener.onDestinationAdded(hermes, destinationConfig);
                  }
               }
            }
            catch (NamingException ex)
            {
               // NOP
            }
         }

         for (Iterator namingIter = config.getNaming().iterator(); namingIter.hasNext();)
         {
            NamingConfig namingConfig = (NamingConfig) namingIter.next();

            listener.onNamingAdded(namingConfig);
         }
      }
      catch (NamingException e)
      {
         log.error(e.getMessage(), e);
      }
   }

   public void removeConfigurationListener(HermesConfigurationListener listener)
   {
      listeners.remove(listener);
   }

   public void notifyNamingAdded(NamingConfig namingConfig)
   {
      for (Iterator iter = listeners.iterator(); iter.hasNext();)
      {
         HermesConfigurationListener listener = (HermesConfigurationListener) iter.next();
         listener.onNamingAdded(namingConfig);
      }
   }

   public void notifyNamingRemoved(NamingConfig namingConfig)
   {
      for (Iterator iter = listeners.iterator(); iter.hasNext();)
      {
         HermesConfigurationListener listener = (HermesConfigurationListener) iter.next();
         listener.onNamingRemoved(namingConfig);
      }
   }

   public void notifyHermesAdded(Hermes hermes)
   {
      for (Iterator iter = listeners.iterator(); iter.hasNext();)
      {
         HermesConfigurationListener listener = (HermesConfigurationListener) iter.next();
         listener.onHermesAdded(hermes);
      }
   }

   public void notifyHermesRemoved(Hermes hermes)
   {
      for (Iterator iter = listeners.iterator(); iter.hasNext();)
      {
         HermesConfigurationListener listener = (HermesConfigurationListener) iter.next();
         listener.onHermesRemoved(hermes);
      }
   }

   public void notifyDestinationAdded(Hermes hermes, DestinationConfig destinationConfig)
   {
      for (Iterator iter = listeners.iterator(); iter.hasNext();)
      {
         HermesConfigurationListener listener = (HermesConfigurationListener) iter.next();
         listener.onDestinationAdded(hermes, destinationConfig);
      }
   }

   public void notifyDestinationRemoved(Hermes hermes, DestinationConfig destinationConfig)
   {
      for (Iterator iter = listeners.iterator(); iter.hasNext();)
      {
         HermesConfigurationListener listener = (HermesConfigurationListener) iter.next();
         listener.onDestinationRemoved(hermes, destinationConfig);
      }
   }

   /**
    * @return Returns the context.
    */
   public Context getContext()
   {
      return context;
   }

   /**
    * @param extensionLoaderClass
    *           The extensionLoaderClass to set.
    */
   public void setExtensionLoaderClass(String extensionLoader)
   {
      this.extensionLoaderClass = extensionLoader;
   }

   public ClassLoaderManager getClassLoaderManager()
   {
      return classLoaderManager;
   }

   public boolean isIgnoreClasspathGroups()
   {
      return ignoreClasspathGroups;
   }

   public void setIgnoreClasspathGroups(boolean ignoreClasspathGroups)
   {
      this.ignoreClasspathGroups = ignoreClasspathGroups;
   }
}