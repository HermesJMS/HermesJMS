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

package hermes.mgt;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Topic;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: BrokerProxy.java,v 1.1 2006/07/11 06:26:38 colincrist Exp $
 */

public interface BrokerProxy
{
   public Queue[] getQueues()  throws JMSException ;
   
   public Topic[] getTopics()  throws JMSException ;
   
   public QueueBrowser createBrowser(Queue queue) throws JMSException ;
   
   public QueueBrowser createBrowser(Topic topic, String subscription) throws JMSException ;
   
   public int getDepth(Queue queue) throws JMSException ;
   
   public int getDepth(Topic topic, String subscription) throws JMSException ;
   
   public void createQueue(String name) throws JMSException ;
   
   public void createTopic(String topic) throws JMSException ;
   
   public void close(DurableSubscriptionInfo info) throws JMSException ;
   
   public void close(ConnectionInfo info) throws JMSException ;
   
   public DurableSubscriptionInfo[] getDurableSubscriptions() throws JMSException ;
   
   public DurableSubscriptionInfo[] getDurableSubscriptions(String userName) throws JMSException ;
   
   public DurableSubscriptionInfo[] getDurableSubscriptions(Topic topic) throws JMSException ;
   
   public DurableSubscriptionInfo[] getDurableSubscriptions(Topic topic, String userName) throws JMSException ;
}
