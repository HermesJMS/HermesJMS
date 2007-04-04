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

package hermes.impl.jms;

import hermes.config.SessionConfig;
import hermes.impl.DestinationManager;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.log4j.Logger;

/**
 * @author cristco last changed by: $Author: colincrist $
 * @version $Id: PooledSessionManager.java,v 1.8 2005/10/29 15:15:08 colincrist Exp $
 */
public class PooledSessionManager extends AbstractSessionManager
{
    private static final Logger log = Logger.getLogger(PooledSessionManager.class) ;
   
    
    /**
     * @param destinationManager
     */
    public PooledSessionManager(DestinationManager destinationManager, SessionConfig config)
    {
        super(destinationManager, config);
    }

   
    
    public void closeConsumer(Destination d, String selector) throws JMSException
    {
        // TODO Auto-generated method stub

    }
    /* (non-Javadoc)
     * @see hermes.impl.SessionManager#getSession()
     */
    public Session getSession() throws JMSException
    {
       return null ;
        
    }

    /* (non-Javadoc)
     * @see hermes.impl.SessionManager#getConsumer(javax.jms.Destination)
     */
    public MessageConsumer getConsumer(Destination d) throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see hermes.impl.SessionManager#getConsumer(javax.jms.Destination, java.lang.String)
     */
    public MessageConsumer getConsumer(Destination d, String selector) throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see hermes.impl.JMSManager#connect()
     */
    public void connect() throws JMSException
    {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see hermes.impl.SessionManager#getQueueProducer()
     */
    private MessageProducer getQueueProducer() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see hermes.impl.SessionManager#getTopicProducer()
     */
    private MessageProducer getTopicProducer() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see hermes.impl.SessionManager#close()
     */
    public void close() throws JMSException
    {
        // TODO Auto-generated method stub

    }

    public MessageProducer getProducer() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }
    public void unsubscribe(String name) throws JMSException
    {
        // TODO Auto-generated method stub

    }
    public boolean isOpen()
    {
        // TODO Auto-generated method stub
        return false;
    }
}
