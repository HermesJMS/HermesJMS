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

package hermes.impl;

import hermes.Domain;
import hermes.Hermes;
import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;
import hermes.config.SessionConfig;

import java.util.Collection;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.NamingException;

/**
 * Interface to all session based functionality - different implementation may
 * include threadlocal or pooled JMS sessions.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SessionManager.java,v 1.17 2005/10/29 15:15:08 colincrist Exp $
 */
public interface SessionManager extends JMSManager
{
   public DestinationManager getDestinationManager() ;
   
    public MessageProducer getProducer() throws JMSException;

    public void unsubscribe(String name) throws JMSException ;

    public Destination getDestination(String named, Domain domain) throws JMSException, NamingException;

    public SessionConfig getConfig()  ;
    /**
     * Create the session, if it exits then close the existing one.
     * 
     * @throws JMSException
     */
    public void connect() throws JMSException;

    public void closeConsumer(Destination d, String selector) throws JMSException ;
    
    public boolean isOpen() ;
    
    public void reconnect(String username, String password) throws JMSException ;
    /**
     * Close the session, the next method that requires the session must arrange
     * to have it recreated (make getSession() recreate it is null is the
     * simplest option.
     * 
     * @throws JMSException
     */
    public void close() throws JMSException;

    /**
     * Get the parent (i.e. ConnectionManager).
     * 
     * @return @throws
     *         JMSException
     */
    public ConnectionManager getConnectionManager() throws JMSException;

    /**
     * Get the session, creating is as needed.
     * 
     * @return @throws
     *         JMSException
     */
    public Session getSession() throws JMSException;

    /**
     * Get a consumer for a destination. The consumer is cached thread local.
     */
    public abstract MessageConsumer getConsumer(Destination d) throws JMSException;

    /**
     * Get a consumer for a destination and a selector. The consumer is cached
     * thread local.
     */
    public MessageConsumer getConsumer(Destination d, String selector) throws JMSException;

    /**
     * Whats the JMS acknowledgement mode in force on the managed session
     * 
     * @return
     */
    public int getAcknowledgeMode();

    /**
     * Get some identifier for this session manager
     */
    public String getId();

    /**
     * Set the JMS transaction policy
     */
    public void setTransacted(boolean transacted);

    /**
     * Whats the JMS transaction policy in force on the managed session.
     */
    public boolean isTransacted();

    /**
     * Set the ID.
     */
    public void setId(String id);

    /**
     * Set the number of times we want to try and reconnect
     */
    public void setReconnects(int reconnects);

    /**
     * Set the period to try reconnects
     */
    public void setReconnectTimeout(Long reconnectTimeout);

    /**
     * Get the connection factory manager, that is the parent.
     */
    public ConnectionFactoryManagerImpl getConnectionFactoryManager();

    /**
     * @return
     */
    public boolean isAudit();

    /**
     * @return
     */
    public String getAuditDirectory();

    /**
     * @param b
     */
    public void setAudit(boolean b);

    /**
     * @param string
     */
    public void setAuditDirectory(String string);

    /**
     * When reconnects is set > 1 this timeout is used between reconnet attempts
     */
    public long getReconnectTimeout();

    /**
     * When reconnects is set > 1 this timeout is used between reconnet attempts
     */
    public void setReconnectTimeout(long reconnectTimeout);

    public int getReconnects();

  

    public void setFactoryConfig(FactoryConfig factoryConfig);

    public FactoryConfig getFactoryConfig();

    /**
     * Get the connection factory.
     */
    public ConnectionFactory getConnectionFactory() throws JMSException;

    /**
     * Get the connection.
     */
    public Connection getConnection() throws JMSException;

    /**
     * Create browser, can be used with topics too.
     */
    public QueueBrowser createBrowser(Hermes hermes, Destination destination, String selector) throws JMSException;

    public QueueBrowser createBrowser(Hermes hermes, DestinationConfig dConfig) throws JMSException ;
    /**
     * Get an iterator over all the <i>statically configured </i> destinations.
     */
    public Collection getDestinations();

    /**
     * Get the destination specific configuration
     */
    public DestinationConfig getDestinationConfig(String d, Domain domain) throws JMSException;

}