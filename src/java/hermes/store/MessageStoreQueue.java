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

package hermes.store;

import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageStoreQueue.java,v 1.2 2005/07/15 15:11:02 colincrist Exp $
 */

public class MessageStoreQueue implements Queue
{
   private String name;

   public MessageStoreQueue(String name)
   {
      this.name = name;
   }

   public String getQueueName() throws JMSException
   {
      return name;
   }

   @Override
   public boolean equals(Object obj)
   {
      try
      {
         if (obj instanceof MessageStoreQueue)
         {
            return name.equals(((MessageStoreQueue) obj).getQueueName());
         }
         else
         {
            return false;
         }
      }
      catch (JMSException ex)
      {
         return false;
      }
   }

   @Override
   public int hashCode()
   {
      return name.hashCode() ;
   }

}
