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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.log4j.Logger;

/**
 * An interceptor that caches the parameter for a setter and returns it from a getter.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: GetCachingMethodInterceptor.java,v 1.2 2006/07/13 07:35:34 colincrist Exp $
 */

public final class GetCachingMethodInterceptor implements MethodInterceptor
{
   private static final Logger log = Logger.getLogger(GetCachingMethodInterceptor.class) ;
   
   private final Map<String, Object> properties = new HashMap<String, Object>();
   
   public final Object intercept(final Object object, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable
   {
      if (ReflectUtils.isGetter(method))
      {
         log.debug("GetCachingMethodInterceptor getter for " + ReflectUtils.getPropertyName(method) + " returns " + properties.get(ReflectUtils.getPropertyName(method))) ;
         
         return properties.get(ReflectUtils.getPropertyName(method));
      }
     
      log.debug("superName: " + proxy.getSuperName()) ;
      
      final Object rval = proxy.invokeSuper(object, args);

      if (ReflectUtils.isSetter(method))
      {
         String propertyName = ReflectUtils.getPropertyName(method) ;
        
         properties.put(propertyName, args[0]);       
         
         log.debug("GetCachingMethodInterceptor setter for " + ReflectUtils.getPropertyName(method) + " with " + args[0]) ;         
      }

      return rval;
   }
}
