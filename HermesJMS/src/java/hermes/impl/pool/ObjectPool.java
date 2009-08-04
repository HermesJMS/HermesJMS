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

import hermes.HermesException;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * A simple object pool implementation.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ObjectPool.java,v 1.2 2005/07/23 15:54:12 colincrist Exp $
 */

public abstract class ObjectPool<T>
{
   private static final Logger log = Logger.getLogger(ObjectPool.class) ;
   private List<T> pool = new ArrayList<T>();
   private int poolSize ;

   public ObjectPool(int poolSize)
   {
      this.poolSize = poolSize;
   }

   public ObjectPool()
   {
      this(2) ;
   }

   /** 
    * Before an object is placed back into the pool for reuse this final check occurs and the
    * object is only placed back if it returns true.
    * 
    * @param object
    * @return
    */
   protected abstract boolean beforeCheckin(T object) ; 
   
   /** 
    * Called to create new object when the pool is empty.
    * 
    * @return
    * @throws HermesException
    */
   protected abstract T makeObject() throws HermesException;

   /**
    * Called to create a proxy for an object 
    * 
    * @param object
    * @return
    */
   protected abstract T newProxyInstance(T object);

   /**
    * Called to perform any cleaning up of resources when an object is no longer needed
    * 
    * @param object
    */
   protected abstract void closeObject(T object);

   /**
    * Called to check out an object, if the pool is empty a fresh object is created.
    * 
    * @return
    * @throws HermesException
    */
   protected T checkOutObject() throws HermesException
   {
      synchronized (pool)
      {
         if (pool.size() == 0)
         {            
            log.debug("checkOutObject: pool is empty, creating new object...") ;
            
            return newProxyInstance(makeObject());
         }
         else
         {
            // log.debug("checkOutObject: pools size is " + pool.size() + " returning existing object...") ;
            
            return newProxyInstance(pool.remove(0));
         }
      }
   }
   
   /**
    * Called to check in an object, if the pool is full the object is closed and discarded.
    * 
    * @param object
    * @throws HermesException
    */
   protected void checkInObject(T object) throws HermesException
   {
      synchronized (pool)
      {
         if (pool.size() >= poolSize)
         {
            log.debug("checkInObject: pool is full, closing object...") ;
            
            closeObject(object);
         }
         else
         {
            if (beforeCheckin(object))
            {
               // log.debug("checkInObject: returning object to pool...") ;
               pool.add(object);
            }
            else
            {
               log.debug("checkInObject: beforeCheckin returned false, discarding object...") ;
            }
            
         }
      }
   }

   public T get() throws HermesException
   {
      return checkOutObject();
   }

}
