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
import hermes.store.MessageStore;
import hermes.store.MessageStoreManager;
import hermes.util.JMSUtils;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

import org.apache.tools.ant.BuildException;

/**
 * This task replays messages from a JDBC message store to JMS.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ReplayMessagesTask.java,v 1.2 2005/07/22 17:02:23 colincrist
 *          Exp $
 */

public class ReplayMessagesTask extends AbstractTask
{
   private String queue;
   private String topic;
   private String jdbcURL;
   private String storeId;
   private int commitInterval = 1;
   private boolean timed = false;

   private List<DestinationSelectionType> storeDestinations = new ArrayList<DestinationSelectionType>();

   public void addConfigured(DestinationSelectionType storeDestination)
   {
      storeDestinations.add(storeDestination);
   }

   public void execute() throws BuildException
   {
      if (storeId == null)
      {
         throw new BuildException("storeId not set");
      }

      final Hermes myHermes;
      final Destination toDestination;
      final MessageStore messageStore;

      try
      {
         myHermes = HermesFactory.createHermes(getConfig(), getHermes());

         if (queue != null || topic != null)
         {
            final Domain domain = queue != null ? Domain.QUEUE : Domain.TOPIC;
            final String destinationName = queue != null ? queue : topic;

            toDestination = myHermes.getDestination(destinationName, domain);
         }
         else
         {
            toDestination = null;
         }

         messageStore = jdbcURL == null ? MessageStoreManager.create(storeId) : MessageStoreManager.create(jdbcURL, storeId);
      }
      catch (Exception e)
      {
         throw new BuildException(e);
      }

      try
      {
         if (storeDestinations.size() == 0)
         {
            if (toDestination == null)
            {
               log("Replaying all messages from store " + messageStore.getId() + " to " + myHermes.getId());

            }
            doReplay(messageStore, myHermes, toDestination);
         }
         else
         {
            for (final DestinationSelectionType storeDestinationConfig : storeDestinations)
            {
               final Destination storeDestination = myHermes.getDestination(storeDestinationConfig.getName(), storeDestinationConfig.asDomain());

               doReplay(messageStore, storeDestination, myHermes, toDestination);
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

   /**
    * Replay all messages from the message store to the given Hermes on their
    * original destinations.
    * 
    * @param store
    * @param hermes
    * @throws JMSException
    */
   private void doReplay(MessageStore store, Hermes hermes) throws JMSException
   {
      doReplay(store, null, hermes, null);
   }

   /**
    * Replay all messages from the message store to the given Hermes and
    * destination .
    * 
    * @param store
    * @param hermes
    * @param hermesDestination
    * @throws JMSException
    */
   private void doReplay(MessageStore store, Hermes hermes, Destination hermesDestination) throws JMSException
   {
      doReplay(store, null, hermes, hermesDestination);
   }

   /**
    * Replay messages from the message store and originating destination to the
    * given Hermes and destination.
    * 
    * @param store
    * @param storeDestination
    * @param hermes
    * @param hermesDestination
    * @throws JMSException
    */
   private void doReplay(MessageStore store, Destination storeDestination, Hermes hermes, Destination hermesDestination) throws JMSException
   {
      final MessageStore.HeaderPolicy headerPolicy = hermesDestination == null ? MessageStore.HeaderPolicy.DESTINATION_ONLY
            : MessageStore.HeaderPolicy.NO_HEADER;
      final QueueBrowser browser = storeDestination == null ? store.visit(hermes, headerPolicy) : store.visit(hermes, storeDestination, headerPolicy);

      int messagesSent = 0;
      long lastTimestamp = 0;

      for (Enumeration iter = browser.getEnumeration(); iter.hasMoreElements();)
      {
         final Message message = (Message) iter.nextElement();

         if (message != null)
         {
            if (hermesDestination == null)
            {
               hermes.send(message.getJMSDestination(), message);
            }
            else
            {
               hermes.send(hermesDestination, message);
            }

            if (++messagesSent % commitInterval == 0 && hermes.getTransacted())
            {
               hermes.commit();
            }

            if (timed && lastTimestamp != 0)
            {
               long delay = message.getJMSTimestamp() - lastTimestamp;

               if (delay > 0)
               {
                  try
                  {
                     Thread.sleep(delay);
                  }
                  catch (InterruptedException e)
                  {
                     // NOP
                  }
               }

               lastTimestamp = message.getJMSTimestamp();
            }
         }
      }

      hermes.commit();
      JMSUtils.closeQuietly(browser);
      hermes.close();
   }

   public boolean isTimed()
   {
      return timed;
   }

   public void setTimed(boolean timed)
   {
      this.timed = timed;
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

   public int getCommitInterval()
   {
      return commitInterval;
   }

   public void setCommitInterval(int commitInterval)
   {
      this.commitInterval = commitInterval;
   }

   public String getStoreId()
   {
      return storeId;
   }

   public void setStoreId(String storeId)
   {
      this.storeId = storeId;
   }

}
