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

package test.jms;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;

import junit.framework.TestCase;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

/**
 * Push 100 messages to a queue and then immediately use a queue browser to
 * check the queue depth then delete the messages with a consumer and selector.
 * All work is performed on the same connection/session.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: TestQueueBrowserForActiveMQ.java,v 1.3 2006/07/13 07:35:33 colincrist Exp $
 */

public class TestQueueBrowserForActiveMQ extends TestCase
{
   private static final Logger log = Logger.getLogger(TestQueueBrowserForActiveMQ.class) ;
   
   public void testWithTemporaryQueue() throws Exception
   {
      for (int i = 0; i < 100; i++)
      {
         log.debug("testWithTemporaryQueue run=" + i) ;
         doTestQueueBrowse(true);
      }
   }

   public void testWithQueue() throws Exception
   {
      for (int i = 0; i < 100; i++)
      {
         log.debug("testWithQueue run=" + i) ;
         doTestQueueBrowse(false);
      }
   }

   public void doTestQueueBrowse(boolean useTemporaryQueue) throws Exception
   {
      final int numMessages = 100;
      final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();

      connectionFactory.setBrokerURL("tcp://localhost:61616");

      final Connection connection = connectionFactory.createConnection();
      final Session session = connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);

      connection.start();

      final Queue queue = useTemporaryQueue ? session.createTemporaryQueue() : session.createQueue("test." + System.currentTimeMillis());
      final MessageProducer sender = session.createProducer(queue);

      for (int i = 0; i < numMessages; i++)
      {
         final Message m = session.createTextMessage("Message #" + i);
         m.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
         sender.send(m);
      }

      session.commit();

      // Thread.sleep(1000) ;

      final QueueBrowser browser = session.createBrowser(queue);
      final StringBuffer sql = new StringBuffer();
      int depth = 0;

      for (final Enumeration iter = browser.getEnumeration(); iter.hasMoreElements();)
      {
         final Message message = (Message) iter.nextElement();

         

         sql.append("JMSMessageID = \'").append(message.getJMSMessageID()).append("\'");

         if (iter.hasMoreElements())
         {
            sql.append(" or ");
         }

         depth++;
      }
      
      browser.close() ;

      assertEquals("useTemporaryQueue=" + useTemporaryQueue + " queue browse depth equals put size", numMessages, depth);

      final MessageConsumer consumer = session.createConsumer(queue, sql.toString());

      Message message = null;
      int messagesReceived = 0;

      while ((message = consumer.receive(1000)) != null && messagesReceived != numMessages)
      {
         messagesReceived++;
      }

      session.commit();
      session.close() ;
      connection.close() ;

      assertEquals("useTemporaryQueue=" + useTemporaryQueue + " consumed size equals put size", numMessages, messagesReceived);
   }
}
