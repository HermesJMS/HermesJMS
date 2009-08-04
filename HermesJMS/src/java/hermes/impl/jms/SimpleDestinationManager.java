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

package hermes.impl.jms;

import hermes.Domain;
import hermes.impl.DestinationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicSession;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: SimpleDestinationManager.java,v 1.1 2004/07/21 20:25:40
 *          colincrist Exp $
 */
public class SimpleDestinationManager implements DestinationManager
{
   private static final Logger log = Logger.getLogger(SimpleDestinationManager.class);

   private static class Cache
   {
      Map topics = new HashMap();
      Map queues = new HashMap();
   }

   private WeakHashMap sessions = new WeakHashMap();

   protected Destination createDesintaion(final Session session, final String named, final Domain domain) throws JMSException
   {
      Destination rval = null;
      try
      {

         if (domain == Domain.QUEUE)
         {
            rval = session.createQueue(named);
         }
         else if (domain == Domain.TOPIC)
         {
            rval = session.createTopic(named);
         }
         else
         {
            rval = session.createQueue(named);
         }
      }
      catch (NoSuchMethodError e)
      {
         log.debug("session seems to be pre JMS 1.1");
      }
      catch (AbstractMethodError e)
      {
         log.debug("session seems to be pre JMS 1.1");
      }

      if (rval == null)
      {
         if (domain == Domain.QUEUE)
         {
            rval = ((QueueSession) session).createQueue(named);
         }
         else if (domain == Domain.TOPIC)
         {
            rval = ((TopicSession) session).createTopic(named);
         }
         else
         {
            try
            {
               rval = ((QueueSession) session).createQueue(named);
            }
            catch (ClassCastException e)
            {
               rval = ((TopicSession) session).createTopic(named);
            }
         }
      }
      return rval;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.impl.DestinationManager#getDestination(javax.jms.Session)
    */
   public synchronized Destination getDestination(final Session session, final String named, final Domain domain) throws JMSException
   {
      Cache cache = (Cache) sessions.get(session);
      Destination rval = null;
      Map cacheMap = null;

      if (cache == null)
      {
         cache = new Cache();
         sessions.put(session, cache);
      }

      if (domain == Domain.QUEUE)
      {
         cacheMap = cache.queues;
      }
      else
      {
         cacheMap = cache.topics;
      }

      if (cacheMap.containsKey(named))
      {
         rval = (Destination) cacheMap.get(named);
      }
      else
      {
         rval = createDesintaion(session, named, domain);

         cacheMap.put(named, rval);
      }

      return rval;
   }

}