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

package hermes;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: NullConnectionFactory.java,v 1.2 2004/05/08 15:15:48 colincrist
 *          Exp $
 */
public class NullConnectionFactory implements TopicConnectionFactory, QueueConnectionFactory
{

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.TopicConnectionFactory#createTopicConnection()
     */
    public TopicConnection createTopicConnection() throws JMSException
    {
        throw new JMSException("You must select a real ConnectionFactory for this provider");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.TopicConnectionFactory#createTopicConnection(java.lang.String,
     *      java.lang.String)
     */
    public TopicConnection createTopicConnection(String arg0, String arg1) throws JMSException
    {
        throw new JMSException("You must select a real ConnectionFactory for this provider");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueConnectionFactory#createQueueConnection()
     */
    public QueueConnection createQueueConnection() throws JMSException
    {
        throw new JMSException("You must select a real ConnectionFactory for this provider");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueConnectionFactory#createQueueConnection(java.lang.String,
     *      java.lang.String)
     */
    public QueueConnection createQueueConnection(String arg0, String arg1) throws JMSException
    {
        throw new JMSException("You must select a real ConnectionFactory for this provider");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.ConnectionFactory#createConnection()
     */
    public Connection createConnection() throws JMSException
    {
        throw new JMSException("You must select a real ConnectionFactory for this provider");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.ConnectionFactory#createConnection(java.lang.String,
     *      java.lang.String)
     */
    public Connection createConnection(String arg0, String arg1) throws JMSException
    {
        throw new JMSException("You must select a real ConnectionFactory for this provider");
    }
}