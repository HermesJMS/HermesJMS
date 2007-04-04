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

import hermes.browser.MessageRenderer;
import hermes.config.DestinationConfig;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

/**
 * Here is where we stick stuff that either JMS does not support or its very
 * inefficent to do via JMS. Mainly admin functionality.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HermesAdmin.java,v 1.16 2005/08/15 20:37:32 colincrist Exp $
 */
public interface HermesAdmin
{
    public Collection discoverDestinationConfigs() throws JMSException ;
    
    /**
     * Get the depth of messages waiting to be consumed from a queue/topic.
     */
    public int getDepth(DestinationConfig dest) throws JMSException;

    /**
     * Gets the age of the oldest message on a destination without browsing it.
     * 
     * @param dest
     * @return @throws
     *         JMSException
     */
    public long getAge(DestinationConfig dest) throws JMSException;

    /**
     * Close any associated sessions, use of any session related method after
     * calling this will re-open the session.
     * 
     */
    public void close() throws JMSException ;

    /**
     * Truncate the destination.
     * 
     * @param dest
     * @return @throws
     *         JMSException
     */
    public int truncate(DestinationConfig dest) throws JMSException;
    
    /**
     * Experimental.
     * 
     * @param destination
     * @return @throws
     *         JMSException
     */
    public Map getStatistics(DestinationConfig dConfig) throws JMSException;

    /**
     * Experimental.
     * 
     * @param destinations
     * @return @throws
     *         JMSException
     */
    public Collection getStatistics(Collection destinations) throws JMSException;
    
    /**
     * A way to specify a provider specific way of rendering messages. 
     * 
     * @return
     * @throws JMSException
     */
    public MessageRenderer getMessageRenderer() throws JMSException ;
    
    /**
     * @@TODO, unused, needs to be removed.
     * 
     * @param iter
     * @return
     * @throws JMSException
     */
    public Enumeration createBrowserProxy(Enumeration iter) throws JMSException ;
    
    /**
     * Create a browser for messages queued for a durable subscription.
     * 
     * @param topic
     * @param name
     * @return
     * @throws JMSException
     */
    public QueueBrowser createDurableSubscriptionBrowser(DestinationConfig dConfig) throws JMSException ;
}