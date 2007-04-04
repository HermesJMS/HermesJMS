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

import hermes.browser.HermesBrowser;
import hermes.config.ClasspathConfig;
import hermes.config.PropertyConfig;
import hermes.config.PropertySetConfig;
import hermes.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.jms.ConnectionFactory;
import javax.swing.ProgressMonitor;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * Helper for mucking about with ClassLoaders
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: LoaderSupport.java,v 1.29 2006/07/13 07:35:31 colincrist Exp $
 */
public class LoaderSupport
{
   private static final Logger log = Logger.getLogger(LoaderSupport.class);

   public static class DebugClassLoader extends URLClassLoader
   {
      /**
       * @param arg0
       */
      public DebugClassLoader(URL[] arg0)
      {
         super(arg0);
      }

      /**
       * @param arg0
       * @param arg1
       */
      public DebugClassLoader(URL[] arg0, ClassLoader arg1)
      {
         super(arg0, arg1);
      }

      /**
       * @param arg0
       * @param arg1
       * @param arg2
       */
      public DebugClassLoader(URL[] arg0, ClassLoader arg1, URLStreamHandlerFactory arg2)
      {
         super(arg0, arg1, arg2);
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
       */
      protected synchronized Class loadClass(String arg0, boolean arg1) throws ClassNotFoundException
      {
         if (arg0.equals("fr.dyade.aaa.jndi2.client.NamingContextFactory"))
         {
            log.debug("loadClass(" + arg0 + ", " + arg1 + ") from " + toString());
         }

         return super.loadClass(arg0, arg1);
      }

      public String toString()
      {
         StringBuffer rval = new StringBuffer();

         rval.append("DebugClassLoader: ");

         for (int i = 0; i < getURLs().length; i++)
         {
            URL url = getURLs()[i];

            rval.append(url.toString());

            if (i != getURLs().length - 1)
            {
               rval.append(", ");
            }
         }

         if (getParent() instanceof DebugClassLoader)
         {
            rval.append(", parent=" + getParent().toString());
         }

         return rval.toString();
      }

      protected Class findClass(String name) throws ClassNotFoundException
      {
         return super.findClass(name);
      }
   }

   

   /**
    * Return ClassLoader given the list of ClasspathConfig instances. The
    * resulting loader can then be used to instantiate providers from those
    * libraries.
    */
   static ClassLoader createClassLoader(List loaderConfigs, URL[] extraUrls, ClassLoader classLoader) throws IOException
   {
      int index = 0;
      int size = loaderConfigs.size();

      if (extraUrls != null)
      {
         size += extraUrls.length;
      }

      URL[] urls = new URL[size];
      StringBuffer debug = new StringBuffer("URLClassLoader: ");

      for (Iterator iter = loaderConfigs.iterator(); iter.hasNext();)
      {
         ClasspathConfig lConfig = (ClasspathConfig) iter.next();

         URL url = null;

         if (lConfig.getJar().startsWith("http"))
         {
            url = new URL(TextUtils.replaceClasspathVariables(lConfig.getJar()));
         }
         else
         {
            url = new File(TextUtils.replaceClasspathVariables(lConfig.getJar())).toURL();

         }

         urls[index++] = url;

         debug.append(url.toString());

         if (iter.hasNext())
         {
            debug.append(", ");
         }
      }

      if (extraUrls != null)
      {
         for (int i = 0; i < extraUrls.length; i++)
         {
            urls[index++] = extraUrls[i];
            debug.append(", " + extraUrls[i].toString());
         }
      }

      log.debug(debug.toString());

      return new DebugClassLoader(urls, classLoader);
   }

   static ClassLoader createClassLoader(List loaderConfigs, ClassLoader classLoader) throws IOException
   {
      return createClassLoader(loaderConfigs, null, classLoader);

   }

   /**
    * Return ClassLoader given the list of ClasspathConfig instances. The
    * resulting loader can then be used to instantiate providers from those
    * libraries.
    */
   static List lookForFactories(final List loaderConfigs, final ClassLoader baseLoader) throws IOException
   {
      final List rval = new ArrayList();

      for (Iterator iter = loaderConfigs.iterator(); iter.hasNext();)
      {
         final ClasspathConfig lConfig = (ClasspathConfig) iter.next();

         if (lConfig.getFactories() != null)
         {
            log.debug("using cached " + lConfig.getFactories());

            for (StringTokenizer tokens = new StringTokenizer(lConfig.getFactories(), ","); tokens.hasMoreTokens();)
            {
               rval.add(tokens.nextToken());
            }
         }
         else if (lConfig.isNoFactories())
         {
            log.debug("skipping " + lConfig.getJar());
         }
         else
         {
            Runnable r = new Runnable()
            {
               public void run()
               {
                  final List localFactories = new ArrayList();
                  boolean foundFactory = false;
                  StringBuffer factoriesAsString = null;

                  try
                  {
                     log.debug("searching " + lConfig.getJar());

                     ClassLoader l = createClassLoader(loaderConfigs, baseLoader);
                     JarFile jarFile = new JarFile(lConfig.getJar());
                     ProgressMonitor monitor = null;
                     int entryNumber = 0;

                     if (HermesBrowser.getBrowser() != null)
                     {
                        monitor = new ProgressMonitor(HermesBrowser.getBrowser(), "Looking for factories in " + lConfig.getJar(), "Scanning...", 0, jarFile
                              .size());
                        monitor.setMillisToDecideToPopup(0);
                        monitor.setMillisToPopup(0);
                        monitor.setProgress(0);
                     }

                     for (Enumeration iter = jarFile.entries(); iter.hasMoreElements();)
                     {
                        ZipEntry entry = (ZipEntry) iter.nextElement();
                        entryNumber++;

                        if (monitor != null)
                        {
                           monitor.setProgress(entryNumber);
                           monitor.setNote("Checking entry " + entryNumber + " of " + jarFile.size());
                        }

                        if (entry.getName().endsWith(".class"))
                        {
                           String s = entry.getName().substring(0, entry.getName().indexOf(".class"));

                           s = s.replaceAll("/", ".");

                           try
                           {
                              if (s.startsWith("hermes.browser") || s.startsWith("hermes.impl") || s.startsWith("javax.jms"))
                              {
                                 // NOP
                              }
                              else
                              {
                                 Class clazz = l.loadClass(s);

                                 if (!clazz.isInterface())
                                 {

                                    if (implementsOrExtends(clazz, ConnectionFactory.class))
                                    {

                                       foundFactory = true;
                                       localFactories.add(s);

                                       if (factoriesAsString == null)
                                       {
                                          factoriesAsString = new StringBuffer();
                                          factoriesAsString.append(clazz.getName());
                                       }
                                       else
                                       {
                                          factoriesAsString.append(",").append(clazz.getName());
                                       }
                                       log.debug("found " + clazz.getName());
                                    }
                                 }

                                 /**
                                  * TODO: remove Class clazz = l.loadClass(s);
                                  * Class[] interfaces = clazz.getInterfaces();
                                  * for (int i = 0; i < interfaces.length; i++) {
                                  * if
                                  * (interfaces[i].equals(TopicConnectionFactory.class) ||
                                  * interfaces[i].equals(QueueConnectionFactory.class) ||
                                  * interfaces[i].equals(ConnectionFactory.class)) {
                                  * foundFactory = true; localFactories.add(s);
                                  * if (factoriesAsString == null) {
                                  * factoriesAsString = new
                                  * StringBuffer(clazz.getName()); } else {
                                  * factoriesAsString.append(",").append(clazz.getName()); }
                                  * log.debug("found " + clazz.getName()); } }
                                  */
                              }
                           }
                           catch (Throwable t)
                           {
                              // NOP
                           }
                        }
                     }
                  }
                  catch (IOException e)
                  {
                     log.error("unable to access jar/zip " + lConfig.getJar() + ": " + e.getMessage(), e);
                  }

                  if (!foundFactory)
                  {
                     lConfig.setNoFactories(true);
                  }
                  else
                  {
                     lConfig.setFactories(factoriesAsString.toString());
                     rval.addAll(localFactories);
                  }

               }
            };

            r.run();

         }
      }

      return rval;
   }

   /**
    * Indicates if a class or interface implements or extends the specified
    * interfaces or classes. If the specified <CODE>Class</CODE> is a class,
    * this method will recursively test if this class, its superclass or one of
    * the implemented interfaces of this class implements the specified
    * interface.<BR>
    * If the specified <CODE>Class</CODE> is an interface, this method will
    * recursively test if this interface or one of the implemented interfaces of
    * this interface implements the specified interface.<BR>
    * 
    * @param clazz
    *           the class or interface in question
    * @param testInterface
    *           the class or interface to test against
    * @return <CODE>true</CODE> if the specified interfaces is implemented by
    *         this class or one of its super-classes or interfaces
    */
   public static boolean implementsOrExtends(Class clazz, Class testInterface)
   {
      Class[] implementedInterfaces = clazz.getInterfaces();

      // test interface
      if (clazz.equals(testInterface))
      {
         return true; // possibly the end of the recursion
      }

      for (int i = 0; i < implementedInterfaces.length; i++)
      {
         if (implementsOrExtends(implementedInterfaces[i], testInterface))
         {
            return true; // recursion
         }
      }

      // maybe the superclass implements this interface ?
      Class superClass = clazz.getSuperclass();
      if (superClass != null && implementsOrExtends(superClass, testInterface))
      {
         return true; // recursion
      }

      return false;
   }

   public static void populateBean(Object bean, PropertySetConfig propertySet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException
   {
      if (propertySet != null)
      {
         Set appliedProperties = new HashSet();

         for (Iterator iter = propertySet.getProperty().iterator(); iter.hasNext();)
         {
            PropertyConfig propertyConfig = (PropertyConfig) iter.next();

            if (appliedProperties.contains(propertyConfig.getName()))
            {
               iter.remove();
            }
            else
            {
               try
               {
                  
                  BeanUtils.setProperty(bean, propertyConfig.getName(), TextUtils.replaceClasspathVariables(propertyConfig.getValue()));

                  appliedProperties.add(propertyConfig.getName());
                  
                  log.debug("set " + bean.getClass().getName() + " " + propertyConfig.getName() + "=" +  TextUtils.replaceClasspathVariables(propertyConfig.getValue())) ;
               }
               catch (InvocationTargetException t)
               {
                  log.error("unable to set property name=" + propertyConfig.getName() + " value=" + propertyConfig.getValue() + " on object of class "
                        + bean.getClass().getName() + ": " + t.getCause().getMessage(), t.getCause());
               }
            }
         }
      }
   }
}