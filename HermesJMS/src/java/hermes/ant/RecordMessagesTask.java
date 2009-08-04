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

package hermes.ant;

import hermes.Domain;
import hermes.Hermes;
import hermes.config.DestinationConfig;
import hermes.store.MessageStore;
import hermes.store.MessageStoreManager;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

import org.apache.tools.ant.BuildException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: RecordMessagesTask.java,v 1.1 2005/06/29 11:38:50 colincrist
 *          Exp $
 */

public class RecordMessagesTask extends AbstractTask
{
   private String queue;
   private String topic;
   private String jdbcURL;
   private String storeId;
   private int commitInterval = 1 ;

   public void execute() throws BuildException
   {
      if (queue == null && topic == null)
      {
         throw new BuildException("destination queue or topic is not set");
      }

      if (storeId == null)
      {
         throw new BuildException("storeId not set");
      }

      Hermes myHermes = null;
      QueueBrowser browser = null;
      final DestinationConfig myDestination;
      final MessageStore messageStore;

      try
      {
         final Domain domain = queue != null ? Domain.QUEUE : Domain.TOPIC;
         final String destinationName = queue != null ? queue : topic;

         myHermes = HermesFactory.createHermes(getConfig(), getHermes());
         myDestination = myHermes.getDestinationConfig(destinationName, domain);
         messageStore = jdbcURL == null ? MessageStoreManager.create(storeId) : MessageStoreManager.create(jdbcURL, storeId);
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }

      try
      {
         int messagesRead = 0 ;
         
         browser = myHermes.createBrowser(myDestination);

         for (final Enumeration iter = browser.getEnumeration(); iter.hasMoreElements();)
         {
            final Message m = (Message) iter.nextElement();

            if (m != null)
            {
               messageStore.store(m);
               
               if (++messagesRead % commitInterval == 0)
               {
                  messageStore.checkpoint();
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }
      finally
      {
         try
         {
            messageStore.checkpoint();
            
            if (browser != null)
            {
               browser.close();
            }

            if (myHermes != null)
            {
               myHermes.close();
            }
         }
         catch (JMSException e)
         {
            e.printStackTrace();
         }
      }
   }

   public String getQueue()
   {
      return queue;
   }

   public void setQueue(String queue)
   {
      this.queue = queue;
   }

   public String getTopic()
   {
      return topic;
   }

   public void setTopic(String topic)
   {
      this.topic = topic;
   }

   public String getJdbcURL()
   {
      return jdbcURL;
   }

   public void setJdbcURL(String jdbcURL)
   {
      this.jdbcURL = jdbcURL;
   }

   public String getStoreId()
   {
      return storeId;
   }

   public void setStoreId(String storeId)
   {
      this.storeId = storeId;
   }

   public int getCommitInterval()
   {
      return commitInterval;
   }

   public void setCommitInterval(int commitInterval)
   {
      this.commitInterval = commitInterval;
   }
}
