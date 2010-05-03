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

package hermes.fix.quickfix;

import hermes.fix.FIXMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.map.LRUMap;

import quickfix.Message;
import quickfix.mina.message.FIXMessageDecoder;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: QuickFIXMessageCache.java,v 1.1 2006/07/26 09:47:56 colincrist
 *          Exp $
 */

public class QuickFIXMessageCache
{
   private LRUMap messages;
   private Set<FIXMessage> toReset = new HashSet<FIXMessage>();
   private Lock lock = new ReentrantLock();

   private Map<Thread, FIXMessageDecoder> decoders = new HashMap<Thread, FIXMessageDecoder>();

   public QuickFIXMessageCache()
   {
      this(1024);
   }

   public FIXMessageDecoder getDecoder()
   {
      synchronized (decoders)
      {
         FIXMessageDecoder decoder = decoders.get(Thread.currentThread());

         if (decoder == null)
         {
            decoder = new FIXMessageDecoder();
            decoders.put(Thread.currentThread(), decoder);
         }

         return decoder;
      }
   }

   public QuickFIXMessageCache(int size)
   {
      messages = createLRUMap(size);
   }

   private LRUMap createLRUMap(int size)
   {
      return new LRUMap(size)
      {
         /**
		 * 
		 */
		private static final long serialVersionUID = -3353399913762901038L;

		@Override
         protected boolean removeLRU(LinkEntry entry)
         {
            //
            // When the message is removed from the cache clear its fields.

            final FIXMessage message = (FIXMessage) entry.getKey();

            // We cannot call reset here, the next time a put occurs, we'll
            // reset it.

            synchronized (toReset)
            {
               toReset.add(message);
            }

            return super.removeLRU(entry);
         }
      };
   }

   public void setSize(int size)
   {
      lock.lock();

      try
      {
         final LRUMap newMessages = createLRUMap(size);

         messages.putAll(newMessages);
         messages = newMessages;
      }
      finally
      {
         lock.unlock();
      }
   }

   public int getSize()
   {
      lock.lock();

      try
      {
         if (messages == null)
         {
            return 0;
         }
         else
         {
            return messages.maxSize();
         }
      }
      finally
      {
         lock.unlock();
      }
   }

   public void close()
   {
      lock.lock();

      try
      {
         messages.clear();
         decoders.clear();
         
      }
      finally
      {
         lock.unlock();
      }
      
      synchronized (toReset)
      {
         toReset.clear();
      }
   }

   public void lock()
   {
      lock.lock() ;
   }
   
   public void unlock()
   {
      lock.unlock() ;
   }
   
   public boolean contains(FIXMessage key)
   {
      lock.lock();

      try
      {
         return messages.containsKey(key);
      }
      finally
      {
         lock.unlock();
      }
   }

   public void put(FIXMessage key, Message value)
   {
      lock.lock();

      try
      {
         messages.put(key, value);         
      }
      finally
      {
         lock.unlock();
      }
      
      synchronized (toReset)
      {
         if (toReset.contains(key))
         {
            toReset.remove(key) ;
         }
         
         for (FIXMessage m : toReset)
         {
            m.reset();
         }

         toReset.clear();
      }

   }

   public Message get(FIXMessage key)
   {
      lock.lock();

      try
      {
         return (Message) messages.get(key);
      }
      finally
      {
         lock.unlock();
      }
   }
}
