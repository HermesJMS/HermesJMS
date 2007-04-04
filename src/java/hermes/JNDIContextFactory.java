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

import hermes.config.NamingConfig;
import hermes.impl.ClassLoaderManager;
import hermes.impl.LoaderSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * A factory with typed get/set methods for populating the properties needed to
 * create a JNDI context, means you can get at the properties using reflection
 * (e.g. with Jakarta BeanUtils).
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: JNDIContextFactory.java,v 1.4 2005/05/03 21:36:16 colincrist
 *          Exp $
 */
public class JNDIContextFactory
{
   private static final Logger log = Logger.getLogger(JNDIContextFactory.class);
   public static final String PORT = "java.naming.factory.port";
   public static final String HOST = "java.naming.factory.host";

   private String initialContextFactory;
   private String providerURL;
   private String binding;
   private String authoritative;
   private String batchSize;
   private String dnsUrl;
   private String language;
   private String objectFactories;
   private String securityAuthentication;
   private String securityCredentials;
   private String securityPrincipal;
   private String securityProtocol;
   private String stateFactories;
   private String urlPkgPrefixes;
   private String referral;
   private String host;
   private String port;
   private ClassLoader delegateClassLoader;
   private File userPropertiesFile;
   private String userPropertiesURL;

   /**
    * 
    */
   public JNDIContextFactory()
   {
      super();
   }

   public JNDIContextFactory(NamingConfig namingConfig) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException
   {
      ClassLoaderManager classLoaderManager = (ClassLoaderManager) SingletonManager.get(ClassLoaderManager.class);

      _setDelegateClassLoader(classLoaderManager.getClassLoader(namingConfig.getClasspathId()));

      LoaderSupport.populateBean(this, namingConfig.getProperties());
   }

   public void _setDelegateClassLoader(ClassLoader loader)
   {
      this.delegateClassLoader = loader;
   }

   public String getHost()
   {
      return host;
   }

   public void setHost(String host)
   {
      this.host = host;
   }

   public String getPort()
   {
      return port;
   }

   public void setPort(String port)
   {
      this.port = port;
   }

   public Properties _getProperties() throws NamingException
   {
      Properties properties = new Properties();

      if (initialContextFactory != null)
      {
         properties.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
      }

      if (providerURL != null)
      {
         properties.put(Context.PROVIDER_URL, providerURL);
      }

      if (referral != null)
      {
         properties.put(Context.REFERRAL, referral);
      }

      if (authoritative != null)
      {
         properties.put(Context.AUTHORITATIVE, authoritative);
      }

      if (batchSize != null)
      {
         properties.put(Context.BATCHSIZE, batchSize);
      }

      if (dnsUrl != null)
      {
         properties.put(Context.DNS_URL, dnsUrl);
      }

      if (language != null)
      {
         properties.put(Context.LANGUAGE, language);
      }

      if (objectFactories != null)
      {
         properties.put(Context.OBJECT_FACTORIES, objectFactories);
      }

      if (securityCredentials != null)
      {
         properties.put(Context.SECURITY_CREDENTIALS, securityCredentials);
      }

      if (securityPrincipal != null)
      {
         properties.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
      }

      if (securityProtocol != null)
      {
         properties.put(Context.SECURITY_PROTOCOL, securityProtocol);
      }

      if (securityAuthentication != null)
      {
         properties.put(Context.SECURITY_AUTHENTICATION, securityAuthentication);
      }

      if (stateFactories != null)
      {
         properties.put(Context.STATE_FACTORIES, stateFactories);
      }

      if (urlPkgPrefixes != null)
      {
         properties.put(Context.URL_PKG_PREFIXES, urlPkgPrefixes);
      }

      if (port != null)
      {
         properties.put(PORT, port);
      }

      if (host != null)
      {
         properties.put(HOST, host);
      }

      final Properties userProperties = new Properties();

      try
      {
         if (userPropertiesURL != null)
         {
            try
            {
               userProperties.load(new URL(userPropertiesURL).openStream());
            }
            catch (IOException ex)
            {
               log.error(ex.getMessage(), ex);

               throw new NamingException("Cannot load properties from " + userPropertiesURL + ": " + ex.getMessage());
            }
         }
         else if (userPropertiesFile != null)
         {
            userProperties.load(new FileInputStream(userPropertiesFile));
         }

         properties.putAll(userProperties);
      }
      catch (Throwable e)
      {
         log.error("loading user properties: " + e.getMessage(), e);
         throw new NamingException(e.getMessage());
      }

      log.debug("properties: " + properties);

      return properties;

   }

   public Context createContext() throws JMSException
   {
      if (initialContextFactory == null)
      {
         throw new HermesException("initialContextFactory not set");
      }

      try
      {
         if (delegateClassLoader == null)
         {
            // log.debug("delegateClassLoader is null") ;

            return new InitialContext(_getProperties());
         }
         else
         {
            // log.debug("using delegateClassLoader " + delegateClassLoader) ;
            Thread.currentThread().setContextClassLoader(delegateClassLoader);

            Context context = null;

            try
            {
               context = new InitialContext(_getProperties());

            }
            finally
            {
               //
               // Don't do this as it invalidates the ClassLoader on this thread
               // so u can't do any JMS work
               //
               // Thread.currentThread().setContextClassLoader(defaultClassLoader);
            }

            return context;
         }
      }
      catch (Exception e)
      {
         log.error(e.getClass().getName() + ": " + e.getMessage(), e);

         if (e.getMessage() != null)
         {
            throw new JMSException("Could not create InitialContext: " + e.getMessage());
         }
         else if (e.getCause() != null && e.getCause().getMessage() != null)
         {
            throw new JMSException("Could not create InitialContext: " + e.getCause().getMessage());
         }
         else
         {
            throw new HermesException("Could not create InitialContext", e);
         }
      }
   }

   /**
    * @return
    */
   public String getInitialContextFactory()
   {
      return initialContextFactory;
   }

   /**
    * @return
    */
   public String getProviderURL()
   {
      return providerURL;
   }

   /**
    * @param string
    */
   public void setInitialContextFactory(String string)
   {
      initialContextFactory = string;
   }

   /**
    * @param string
    */
   public void setProviderURL(String string)
   {
      providerURL = string;
   }

   /**
    * @return
    */
   public String getBinding()
   {
      return binding;
   }

   /**
    * @param string
    */
   public void setBinding(String string)
   {
      binding = string;
   }

   /**
    * @return Returns the authoritative.
    */
   public String getAuthoritative()
   {
      return authoritative;
   }

   /**
    * @param authoritative
    *           The authoritative to set.
    */
   public void setAuthoritative(String authoritative)
   {
      this.authoritative = authoritative;
   }

   /**
    * @return Returns the batchSize.
    */
   public String getBatchSize()
   {
      return batchSize;
   }

   /**
    * @param batchSize
    *           The batchSize to set.
    */
   public void setBatchSize(String batchSize)
   {
      this.batchSize = batchSize;
   }

   /**
    * @return Returns the dnsUrl.
    */
   public String getDnsUrl()
   {
      return dnsUrl;
   }

   /**
    * @param dnsUrl
    *           The dnsUrl to set.
    */
   public void setDnsUrl(String dnsUrl)
   {
      this.dnsUrl = dnsUrl;
   }

   /**
    * @return Returns the language.
    */
   public String getLanguage()
   {
      return language;
   }

   /**
    * @param language
    *           The language to set.
    */
   public void setLanguage(String language)
   {
      this.language = language;
   }

   /**
    * @return Returns the objectFactories.
    */
   public String getObjectFactories()
   {
      return objectFactories;
   }

   /**
    * @param objectFactories
    *           The objectFactories to set.
    */
   public void setObjectFactories(String objectFactories)
   {
      this.objectFactories = objectFactories;
   }

   /**
    * @return Returns the referral.
    */
   public String getReferral()
   {
      return referral;
   }

   /**
    * @param referral
    *           The referral to set.
    */
   public void setReferral(String referral)
   {
      this.referral = referral;
   }

   /**
    * @return Returns the securityAuthentication.
    */
   public String getSecurityAuthentication()
   {
      return securityAuthentication;
   }

   /**
    * @param securityAuthentication
    *           The securityAuthentication to set.
    */
   public void setSecurityAuthentication(String securityAuthentication)
   {
      this.securityAuthentication = securityAuthentication;
   }

   /**
    * @return Returns the securityCredentials.
    */
   public String getSecurityCredentials()
   {
      return securityCredentials;
   }

   /**
    * @param securityCredentials
    *           The securityCredentials to set.
    */
   public void setSecurityCredentials(String securityCredentials)
   {
      this.securityCredentials = securityCredentials;
   }

   /**
    * @return Returns the securityPrincipal.
    */
   public String getSecurityPrincipal()
   {
      return securityPrincipal;
   }

   /**
    * @param securityPrincipal
    *           The securityPrincipal to set.
    */
   public void setSecurityPrincipal(String securityPrincipal)
   {
      this.securityPrincipal = securityPrincipal;
   }

   /**
    * @return Returns the securityProtocol.
    */
   public String getSecurityProtocol()
   {
      return securityProtocol;
   }

   /**
    * @param securityProtocol
    *           The securityProtocol to set.
    */
   public void setSecurityProtocol(String securityProtocol)
   {
      this.securityProtocol = securityProtocol;
   }

   /**
    * @return Returns the stateFactories.
    */
   public String getStateFactories()
   {
      return stateFactories;
   }

   /**
    * @param stateFactories
    *           The stateFactories to set.
    */
   public void setStateFactories(String stateFactories)
   {
      this.stateFactories = stateFactories;
   }

   /**
    * @return Returns the urlPkgPrefixes.
    */
   public String getUrlPkgPrefixes()
   {
      return urlPkgPrefixes;
   }

   /**
    * @param urlPkgPrefixes
    *           The urlPkgPrefixes to set.
    */
   public void setUrlPkgPrefixes(String urlPkgPrefixes)
   {
      this.urlPkgPrefixes = urlPkgPrefixes;
   }

   public File getUserPropertiesFile()
   {
      return userPropertiesFile;
   }

   public void setUserPropertiesFile(File userPropertiesFile)
   {
      this.userPropertiesFile = userPropertiesFile;
   }

   public String getUserPropertiesURL()
   {
      return userPropertiesURL;
   }

   public void setUserPropertiesURL(String userPropertiesURL)
   {
      this.userPropertiesURL = userPropertiesURL;
   }
}
