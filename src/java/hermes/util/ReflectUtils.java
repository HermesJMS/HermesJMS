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

package hermes.util;

import hermes.SystemProperties;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import org.apache.log4j.Logger;

/**
 * Some useful reflection utilities.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ReflectUtils.java,v 1.3 2006/07/26 09:47:56 colincrist Exp $
 */

public class ReflectUtils
{
   private static final Set<String> nonCompliantPackages = new HashSet<String> () ;
   
   static
   {
      String prefixes = System.getProperty(SystemProperties.NON_COMPLIANT_PACKAGES, SystemProperties.DEFAULT_NON_COMPLIANT_PACKAGES) ;
      
      for (StringTokenizer tokens = new StringTokenizer(prefixes, ",") ; tokens.hasMoreTokens() ;)
      {
         nonCompliantPackages.add(tokens.nextToken()) ;
      }
   }
   
   private static final Logger log = Logger.getLogger(ReflectUtils.class) ;
   
   /**
    * Is this method public and not static?
    * 
    * @param m
    * @return
    */
   private static boolean isPublicAndNonStatic(Method m)
   {
      return !Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()) ;
   }
   
   /**
    * Is this method a getter? It is not 100% generic as I assume an array paramter discounts it as being a setter.
    * 
    * @param m
    * @return
    */
   public static boolean isGetter(Method m)
   {
      return m.getName().startsWith("get") && isPublicAndNonStatic(m) && m.getParameterTypes().length == 0 ;
   }

   /**
    * Is this method a getter? It is not 100% generic as I assume an array paramter discounts it as being a getter.
    * 
    * @param m
    * @return
    */
   public static boolean isSetter(Method m)
   {
      return m.getName().startsWith("set") && isPublicAndNonStatic(m) && m.getParameterTypes().length == 1 && !m.getParameterTypes()[0].isArray();
   }

   /**
    * Return the property type for a setter or setter (i.e. the return type or argument) 
    * 
    * @param m
    * @return
    */
   public static Class getPropertyType(Method m)
   {
      if (isSetter(m))
      {
         return m.getParameterTypes()[0] ;
      }
      else if (isGetter(m))
      {
         return m.getReturnType() ;
      }
      else
      {
         return Void.class ;
      }
   }
   
   /**
    * Return the property name for a getter or setter. 
    * 
    * @param m
    * @return
    */
   public static String getPropertyName(Method m)
   {
      String s = m.getName().substring(3, m.getName().length()) ;
      
     
      return Character.toLowerCase(s.charAt(0)) + s.substring(1);
   }
   
   public static boolean getterExists(Class clazz, String propertyName)
   {
      final String methodName = "get" + propertyName ;
      
      try
      {
         final Method method = clazz.getMethod(methodName, new Class[] {} ) ;
         
         return true ;
      }
      catch (NoSuchMethodException e)
      {
        return false ;
      }
   }
   
   /**
    * This is complicated, we code generate a new class to mix-in the methods missing get methods from a bean and then 
    * intercept the getters and setters, caching the succesful set call and returning it from the get call.
    * 
    * @param clazz
    * @return
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   public static Object mixinGetterMethodsAndInstantiate(Class clazz) throws InstantiationException, IllegalAccessException
   {
      //
      // Generate a bean class that has a full set of get/set methods for all properties. The getters will
      // in fact never get called as the intercepter will return the cached result of the previous setter.
      
      final Method[] methods = clazz.getMethods() ;
      final BeanGenerator beanGenerator = new BeanGenerator();
    
      beanGenerator.setSuperclass(clazz);

      for (int i = 0; i < methods.length; i++)
      {
         final Method m = methods[i];

         if (ReflectUtils.isSetter(m))
         {
            log.debug("fixing property for " + methods[i].getName() + " type= " + ReflectUtils.getPropertyType(m));

            beanGenerator.addProperty(ReflectUtils.getPropertyName(m), ReflectUtils.getPropertyType(m));
         }
      }

      final Class newClazz = (Class) beanGenerator.createClass();
      
      //
      // Enhance the class by placing a GetCachingMethodInterceptor intercepter on all the getter and setter methods.
      
      final Callback[] callbacks = new Callback[] { new GetCachingMethodInterceptor(), NoOp.INSTANCE };

      return (ConnectionFactory) Enhancer.create(newClazz, new Class[] { ConnectionFactory.class, QueueConnectionFactory.class, TopicConnectionFactory.class }, new CallbackFilter()
      {
         public int accept(Method m)
         {
            if (ReflectUtils.isGetter(m) || ReflectUtils.isSetter(m))
            {
               return 0;
            }
            else
            {
               return 1;
            }
         }

      }, callbacks);
     
   }
   
   public static ConnectionFactory createConnectionFactory(Class clazz) throws InstantiationException, IllegalAccessException
   {
      final ConnectionFactory factory = (ConnectionFactory) clazz.newInstance();

      if (nonCompliantPackages.contains(clazz.getPackage().getName()))
      {
         log.debug("found a non Java Bean compliant class, " + clazz.getName() + ", generating a new class and implementing an around advice to mixin getters");

         //return factory ;
         
         return (ConnectionFactory) mixinGetterMethodsAndInstantiate(clazz) ;
      }
      else
      {
         return factory;
      }
   }
}
