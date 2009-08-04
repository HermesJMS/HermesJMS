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

package hermes.impl;

import hermes.Hermes;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: QueueBrowserWithConsumer.java,v 1.2 2006/07/13 07:35:31
 *          colincrist Exp $
 */

public class QueueBrowserWithConsumer implements QueueBrowser
{
   private static final Logger log = Logger.getLogger(QueueBrowserWithConsumer.class);

   private Hermes hermes;
   private long timeout;
   private Queue queue;
   private Message currentMessage;
   private String selector;

   private class MyEnumeration implements Enumeration
   {
      public boolean hasMoreElements()
      {
         try
         {
            if (hermes != null && currentMessage == null)
            {
               if (selector != null)
               {
                  currentMessage = hermes.receive(queue, timeout);
               }
               else
               {
                  currentMessage = hermes.receive(queue, timeout, selector);
               }
            }

            return currentMessage != null;
         }
         catch (JMSException e)
         {
            log.debug("receive() threw exception, terminating browse: " + e.getMessage(), e);

            try
            {
               close();
            }
            catch (JMSException e2)
            {
               // NOP
            }
         }

         return false;
      }

      public Object nextElement()
      {
         try
         {
            if (currentMessage != null)
            {
               return currentMessage;
            }
            else
            {
               if (hasMoreElements())
               {
                  return currentMessage;
               }
               else
               {
                  return null;
               }
            }
         }
         finally
         {
            currentMessage = null;
         }
      }
   }

   public QueueBrowserWithConsumer(Hermes hermes, Queue queue, String selector, long timeout)
   {
      super();

      this.hermes = hermes;
      this.queue = queue;
      this.timeout = timeout;
      this.selector = selector;

   }

   public Queue getQueue() throws JMSException
   {
      return queue;
   }

   public String getMessageSelector() throws JMSException
   {
      return selector;
   }

   public Enumeration getEnumeration() throws JMSException
   {
      return new MyEnumeration();
   }

   public synchronized void close() throws JMSException
   {
      if (hermes != null)
      {
         if (hermes.getTransacted())
         {
            hermes.rollback();
         }

         hermes.close(queue, selector);
         hermes.close();
         
         hermes = null;
         currentMessage = null;
      }
   }

}
