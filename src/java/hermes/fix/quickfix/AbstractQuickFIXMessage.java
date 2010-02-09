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

import hermes.HermesRuntimeException;
import hermes.fix.FIXMessage;
import hermes.fix.NoSuchFieldException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldNotFound;
import quickfix.InvalidMessage;
import quickfix.Message;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.OnBehalfOfCompID;
import quickfix.field.OnBehalfOfSubID;
import quickfix.field.SenderCompID;
import quickfix.field.SendingTime;
import quickfix.field.TargetCompID;
import quickfix.mina.message.FIXMessageDecoder;

/**
 * This abstract FIX message uses a cache to hold all releated Java objects once
 * the message has been marshalled. When the marshalled message is evicted from
 * the cache, the reset() method is called letting this message perform
 * additional cleanup.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: AbstractQuickFIXMessage.java,v 1.5 2007/02/28 10:47:27 colincrist Exp $
 */

public abstract class AbstractQuickFIXMessage implements FIXMessage
{

   private static Set<Integer> retainedFields;
   private Lock lock = new ReentrantLock();

   static
   {
      retainedFields = new HashSet<Integer>();

      retainedFields.add(SenderCompID.FIELD);
      retainedFields.add(TargetCompID.FIELD);
      retainedFields.add(SendingTime.FIELD);
      retainedFields.add(MsgSeqNum.FIELD);
      retainedFields.add(MsgType.FIELD);
      retainedFields.add(OnBehalfOfCompID.FIELD);
      retainedFields.add(OnBehalfOfSubID.FIELD);
   }

   private static final Logger log = Logger.getLogger(AbstractQuickFIXMessage.class);

  
   class MyDecoderOutput implements ProtocolDecoderOutput
   {
      public void write(Object object)
      {
         try
         {
            getCache().put(AbstractQuickFIXMessage.this, new Message((String) object));
         }
         catch (Exception ex)
         {
            log.error(ex.getMessage(), ex);

            throw new HermesRuntimeException(ex);
         }
      }

      public void flush()
      {
         // TODO Auto-generated method stub
         
      }
   }

   public AbstractQuickFIXMessage(QuickFIXMessageCache cache)
   {
      this.cache = cache;
   }

   public QuickFIXMessageCache getCache()
   {
      return cache;
   }

   private Map<Integer, Field> allFields;
   private Map<Integer, Field> cachedFields = new HashMap<Integer, Field>();
   private DataDictionary dictionary;
   private QuickFIXMessageCache cache;

   private FIXMessageDecoder getDecoder()
   {
      return cache.getDecoder();
   }

   public String getMsgType()
   {
      return getString(MsgType.FIELD);
   }

   public synchronized Set<Integer> getFieldOrder()
   {
      return getAllFields().keySet();
   }

   public boolean fieldExists(int tag)
   {
      if (cachedFields.containsKey(tag))
      {
         return true;
      }

      return getAllFields().containsKey(tag);
   }

   public Map<Integer, Field> getAllFields()
   {     

      try
      {
         lock.lock();
         
         if (allFields == null)
         {
            allFields = new LinkedHashMap<Integer, Field>();

            final Message message = getMessage();

            if (message == null)
            {
               return new HashMap<Integer, Field> () ;
            }
            
            for (final Iterator iterator = message.getHeader().iterator(); iterator.hasNext();)
            {
               Field field = (Field) iterator.next();
               allFields.put(field.getTag(), field);

               if (retainedFields.contains(field.getTag()))
               {
                  cachedFields.put(field.getTag(), field);
               }
            }

            for (final Iterator iterator = message.iterator(); iterator.hasNext();)
            {
               Field field = (Field) iterator.next();
               int tag = field.getTag();

               if (!allFields.containsKey(tag))
               {
                  allFields.put(tag, field);

                  if (retainedFields.contains(field.getTag()))
                  {
                     cachedFields.put(field.getTag(), field);
                  }
               }
            }

            for (final Iterator iterator = message.getTrailer().iterator(); iterator.hasNext();)
            {
               Field field = (Field) iterator.next();
               allFields.put(field.getTag(), field);

               if (retainedFields.contains(field.getTag()))
               {
                  cachedFields.put(field.getTag(), field);
               }
            }
         }
         return allFields;
      }
      finally
      {
         lock.unlock();
      }
   }

   public void reset()
   {
      lock.lock();

      try
      {
         allFields.clear();
         allFields = null;
      }
      finally
      {
         lock.unlock();
      }
   }

   public Object getObject(Field field)
   {
      if (cachedFields.containsKey(field))
      {
         return cachedFields.get(field).getObject();
      }

      Field cached = getAllFields().get(field.getTag());

      if (cached != null)
      {
         return cached.getObject();
      }
      else
      {
         return null;
      }
   }

   public Object getObject(int tag) throws NoSuchFieldException
   {
      if (cachedFields.containsKey(tag))
      {
         return getObject(cachedFields.get(tag));
      }

      if (getAllFields().containsKey(tag))
      {
         return getObject(getAllFields().get(tag));
      }
      else
      {
         throw new NoSuchFieldException(tag);
      }

   }

   public String getString(int field)
   {
      try
      {
         if (cachedFields.containsKey(field))
         {
            return cachedFields.get(field).getObject().toString();
         }

         if (getAllFields().containsKey(field))
         {
            return getAllFields().get(field).getObject().toString();
         }
         else
         {
            throw new FieldNotFound("No such field " + field);
         }
      }
      catch (FieldNotFound e)
      {
         throw new HermesRuntimeException(e);
      }
   }

   public DataDictionary getDictionary()
   {
      return dictionary;
   }

   protected void setDictionary(DataDictionary dictionary)
   {
      this.dictionary = dictionary;
   }

   public String toString()
   {
      return new String(getBytes());
   }

   public Message getMessage()
   {
      getCache().lock();

      try
      {
         if (!getCache().contains(AbstractQuickFIXMessage.this))
         {
            try
            {
               Message message = new Message(new String(getBytes()), dictionary, false);

               getCache().put(this, message);

               if (getDictionary() == null)
               {
                  setDictionary(QuickFIXUtils.getDictionary(message));
               }

               return message;
            }
            catch (InvalidMessage e)
            {
               log.error("Ignoring invalid message: " + e.getMessage(), e) ;
               return null ;
            }
            catch (Exception e)
            {
               throw new HermesRuntimeException(e);
            }
         }
         else
         {
            return getCache().get(this);
         }
      }
      finally
      {
         getCache().unlock();
      }
   }
}
