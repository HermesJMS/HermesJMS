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

package hermes.impl.pool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * A proxy for objects in the pool.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ObjectPoolProxy.java,v 1.1 2005/07/22 17:02:22 colincrist Exp $
 */

public class ObjectPoolProxy<T> implements InvocationHandler
{
   private final String returnToPoolMethod ;
   private final ObjectPool<T> pool ;
   private final T object ;
   
   public ObjectPoolProxy(T object, ObjectPool<T> pool, String returnToPoolMethod)
   {
      this.pool = pool ;
      this.returnToPoolMethod = returnToPoolMethod ;
      this.object = object ;
   }
   
   public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
   {
      if (method.getName().equals(returnToPoolMethod))
      {
         pool.checkInObject(object) ;
         return null ;
      }
      else
      {
        return method.invoke(object, args) ;
      }
   }
}
