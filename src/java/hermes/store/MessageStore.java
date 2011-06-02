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

import hermes.MessageFactory;

import java.io.IOException;
import java.util.Collection;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

/**
 * An interface for a store (mostly JDBC) of JMS messages.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: MessageStore.java,v 1.7 2005/08/21 20:47:56 colincrist Exp $
 */

public interface MessageStore
{
   enum HeaderPolicy
   {
      MESSAGEID_AND_DESTINATION, MESSAGEID_ONLY, DESTINATION_ONLY, NO_HEADER;
   }

   public String getURL() ;
   /**
    * Get the name/id of this store. The name is unique
    * 
    * @return the name/id of this store.
    */
   public String getId();

   /**
    * Get some short descriptive text for use in a tooltop or other user hint.
    * 
    * @return a string to use as a user hint.
    */
   public String getTooltipText();

   /**
    * Returns a collection of a all the destinations that are strored in this
    * store. Note the implementation class fo the JMS interface is not defined.
    * 
    * @return a collection of topics and queues.
    * @throws JMSException
    * @throws IOException
    */
   public Collection<Destination> getDestinations() throws JMSException;

   /**
    * Visit every message in the store.
    * 
    * @return an iterator over every message in the store.
    * @throws JMSException
    * @throws IOException
    */
   public QueueBrowser visit() throws JMSException;

   /**
    * Visit every message that originated from the given JMS queue or topic.
    * 
    * @param d
    *           the originating queue or topic
    * @return iterator over every message from that queue or topic.
    * @throws JMSException
    * @throws IOException
    */
   public QueueBrowser visit(Destination d) throws JMSException;

   /**
    * As visit() but supplies a factory to use to create messages, queues and
    * topics.
    * 
    * @see Iterator<Message> visit()
    * @param factory
    * @return
    * @throws JMSException
    * @throws IOException
    */
   public QueueBrowser visit(MessageFactory factory, HeaderPolicy headerPolicy) throws JMSException;

   /**
    * As visit(Destination d) but supplies a factory to use to create messages,
    * queues and topics.
    * 
    * @see Iterator<Message> visit(Destination d)
    * @param factory
    * @return
    * @throws JMSException
    * @throws IOException
    */
   public QueueBrowser visit(MessageFactory factory, Destination d, HeaderPolicy headerPolicy) throws JMSException;

   /**
    * Store the message in the message store.
    * 
    * @param m
    * @throws JMSException
    * @throws IOException
    */
   public void store(Message m) throws JMSException;

   /**
    * Delete this message store and all messages in it. Further use of the
    * message store is not defined.
    */
   public void delete() throws JMSException;

   /**
    * Delete the given message from the message store.
    * 
    * @param m
    * @throws JMSException
    * @throws IOException
    */
   public void delete(Message m) throws JMSException;

   /**
    * Delete all messages from the message store that originated on the given
    * queue or topic.
    * 
    * @param d
    * @throws JMSException
    * @throws IOException
    */
   public void delete(Destination d) throws JMSException;

   /**
    * Get the depth (i.e. number of messages) stored for a given queue/topic.
    * 
    * @param d
    * @return
    * @throws JMSException
    */
   public int getDepth(Destination d) throws JMSException;

   /**
    * Checkpoint the store. All messages added or removed since the last
    * checkpoint/rollback will be committed.
    * 
    * @throws JMSException
    * @throws IOException
    */
   public void checkpoint() throws JMSException;

   /**
    * Rollback the store. All messages added or removed since the last
    * checkpoint/rollback will be rolled back.
    * 
    * @throws JMSException
    * @throws IOException
    */
   public void rollback() throws JMSException;

   /**
    * Close the store and any resources. Further use of the store is not
    * defined.
    * 
    * @throws JMSException
    * @throws IOException
    */
   public void close() throws JMSException;

   /**
    * Listen to events from the store.
    * 
    * @param listener
    */
   public void addMessageListener(MessageStoreListener listener);

   /**
    * Remove event listener from the store.
    * 
    * @param listener
    */
   public void removeMessageListener(MessageStoreListener listener);
   
   public void update(Message message) throws Exception;
}
