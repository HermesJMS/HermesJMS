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

import hermes.browser.HermesUI;
import hermes.browser.ProxyHermesUI;
import hermes.config.DestinationConfig;
import hermes.config.SessionConfig;
import hermes.impl.DestinationManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.TimerTask;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * Hermes is a messaging framwork based on JMS. This is the core interface for
 * all messaging. Hermes manages the creation of the required javax.jms.Session
 * sessions and ensure that sessions are not shared between threads, that is a
 * new session is created for each thread that uses a Hermes.
 * <p>
 * A Hermes implementation is obtained via the specialised JNDI provider:
 * <p>
 * <code>
 * Properties props = new Properties() ;
 * <br>            
 * props.put(Context.INITIAL_CONTEXT_FACTORY, HermesInitialContextFactory.class.getName()) ;
 * <br>
 * props.put(Context.PROVIDER_URL, "hermes.xml") ;
 * <br>
 * props.put("hermes.loader", JAXBHermesLoader.class.getName()) ;
 * <br><br>                     
 * Context ctx = new InitialContext(props) ;               
 * <br>
 * Hermes hermes = (Hermes) ctx.lookup("queues") ;
 * <br>
 * </code>
 * <p>
 * Note:
 * <p>
 * 1. The provider URL depends on the loader in use. Currently only a JAXB
 * loader is available that reads from XML files. The URL in this case locates
 * the file and if its a file the file specificer is optional.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: Hermes.java,v 1.67 2007/02/28 10:47:29 colincrist Exp $
 */

public interface Hermes extends MessageFactory, HermesAdmin
{
    /**
     * Version information for the entire Hermes product.
     */
    public final String VERSION = "v1.15 build 200911"; 
    
    /**
     * Access to a proxy for the user interface that works even in the absense of a GUI (e.g. when running as an ant task
     */

    public final HermesUI ui = new ProxyHermesUI() ;
    
    public final EventManager events = new EventManager() ;
    
    /**
     * Unsubscribe from a durable subscription
     * 
     * @param topic
     */
    public void unsubscribe(String name) throws JMSException;

    /**
     * Close any consumer open...
     */ 
    public void close(Destination destination, String selector) throws JMSException ;
    
    /**
     * Get the configuration for the session. You should not modify the configuration.
     */
    public SessionConfig getSessionConfig() ;
    
    /**
     * Close the resources associated with this Hermes.
     */
    public void close() throws JMSException;

    /**
     * Commit work on this thread since last commit or rollback.
     */
    public void commit() throws JMSException;

    /**
     * Create a browser for this destination, note this works for Topic as well
     * as Queues
     */
    public QueueBrowser createBrowser(DestinationConfig d) throws JMSException;

/**
     * Create a browser for this destination, note this works for Topic as well
     * as Queues
     */
    public QueueBrowser createBrowser(Destination d) throws JMSException;
    
    /**
     * Create a browser for this destination, note this works for Topic as well
     * as Queues
     */
    public QueueBrowser createBrowser(Destination d, String selector) throws JMSException;

    /**
     * Create a browser for this destination searching the payload and header properties according the the regular expression, 
     * note this works for Topic as well as Queues
     */
    public QueueBrowser createRegexBrowser(Destination d, String regex) throws JMSException;

    /**
     * Create a browser for this destination searching the payload and header properties according the the regular expression, 
     * note this works for Topic as well as Queues
     */
    public QueueBrowser createRegexBrowser(Destination d, String selector, String regex) throws JMSException;

    /**
     * Get the ConnectionFactory associated with this Hermes
     */
    public ConnectionFactory getConnectionFactory() throws JMSException;

    /**
     * Get the Connection associated with this Hermes. It is not recommended
     * that you call start() or stop() on the JMS connection unless you really
     * know what you're doing. The Connection may be being shared accross
     * multiple threads or there may be a dedicated one per thread depending on
     * the sharing policy in place.
     */
    public Connection getConnection() throws JMSException;
    
    /**
     * Get the underlying Session.
     * 
     * @return
     * @throws JMSException
     */
    public Session getSession() throws JMSException ;

    /**
     * Test the transport by creating a session and then closing everything,
     * will return true or throw an exception.
     */
    public boolean isConnectable() throws JMSException;

    /**
     * Get the list of any statically configured destinations
     */
    public Iterator getDestinations();

    /**
     * Get the domain from any static configuration, falling back to instanceof if that does not exist.
     */
    public Domain getDomain(Destination destination) throws JMSException ;
    
    /**
     * Add a new destination configuration
     */
    public void addDestinationConfig(DestinationConfig dConfig) throws JMSException;

    /**
     * Remove a destination configuration 
     */
    public void removeDestinationConfig(DestinationConfig dConfig) throws JMSException ;
    
    /**
     * Get the configuration meta-data for a destination
     */
    public DestinationConfig getDestinationConfig(String d, Domain domain) throws JMSException;

    /**
     * Get the ID
     */
    public String getId();

    /**
     * Consume a message, blocking until one arrives
     */
    public Message receive(Destination d) throws JMSException;

    /**
     * Consume a message with a selector, blocking until one arrives
     */
    public Message receive(Destination d, String selector) throws JMSException;

    /**
     * Consume a message, blocking for ms milliseconds, returning null if no
     * message is consumed in this time
     */
    public Message receive(Destination d, long ms) throws JMSException;

    /**
     * Consume a message with a selector, blocking for ms milliseconds,
     * returning null if no message is consumed in this time
     */
    public Message receive(Destination d, long ms, String selector) throws JMSException;

    /**
     * Poll to see if a message is available, returning null if one is not
     */
    public Message receiveNoWait(Destination d) throws JMSException;

    /**
     * Poll to see if a message is available with a selector, returning null if
     * one is not
     */
    public Message receiveNoWait(Destination d, String selector) throws JMSException;

    /**
     * Rollback all work since the last commit or rollback on this thread
     */
    public void rollback() throws JMSException;

    /**
     * Send a message to a destination
     */
    public void send(Destination d, Message m) throws JMSException;

    /**
     * Set the message listener for a given destination
     */
    public void setMessageListener(Destination d, MessageListener l) throws JMSException;

    /**
     * Is this Hermes in the Queue domain?
     */
    public boolean isQueue() throws JMSException;

    /**
     * Is this Hermes in the Topic domain?
     */
    public boolean isTopic() throws JMSException;

    /**
     * Get provider metadata
     */
    public ProviderMetaData getMetaData() throws JMSException;

    /**
     * Create a copy of a message. The message payload and all the user
     * properties are copied, properties beginning with "JMS" are not copied as
     * they are assumed to be provider properties.
     */
    public Message duplicate(Message message) throws JMSException;

    /**
     * Create a copy of a message. The message payload and all the user
     * properties are copied, properties beginning with "JMS" are not copied as
     * they are assumed to be provider properties.
     */
    public Message duplicate(Destination to, Message message) throws JMSException;

    /**
     * Add a timer to occur on the same thread as message delivery. Note that
     * this is <b>not </b> designed as a general purpose timer mechanism, rather
     * only so that timers may be fired on the same thread as asynchronous
     * message delivery.
     */
    public void schedule(TimerTask task, long delay, boolean repeating) throws JMSException;

    /**
     * Cancel a timer
     */
    public void cancel(final TimerTask task) throws JMSException;

    /**
     * Is this Hermes transacted or not?
     */
    public boolean getTransacted() throws JMSException;

    /**
     * Get a dispatcher for you to manage your own message dispatching, if the
     * named dispatcher does not exist then one is created for you.
     */
    public HermesDispatcher getDispatcher(String name) throws JMSException;

    /**
     * Request asynchronous message delivery on a specific dispatcher
     */
    public void setMessageListener(HermesDispatcher dispatcher, Destination d, MessageListener ml) throws JMSException;

    /**
     * Invoke this Runnable on the default dispatch thread
     */
    public void invoke(Runnable runnable) throws JMSException;

    /**
     * Invole this Runnable on the default dispatch thread and wait for it to
     * complete
     */
    public void invokeAndWait(Runnable runnable) throws JMSException;

    /**
     * Invoke this Runnable on all dispatch threads.
     */
    public void invokeAll(Runnable runnable) throws JMSException;

    /**
     * Invoke this Runnable on all dispatch threads in turn and wait for them to
     * complete
     */
    public void invokeAllAndWait(Runnable runnable) throws JMSException;

    /**
     * Convert some messages to XML as a string
     */
    public String toXML(Message message) throws JMSException;

    /**
     * Convert some messages to XML and wite to a OutputStream
     */
    public void toXML(Message message, OutputStream ostream) throws JMSException, IOException;

    /**
     * Convert some messages to XML as a string
     */
    public String toXML(Collection messages) throws JMSException;

    /**
     * Convert some messages to XML and wite to a OutputStream
     */
    public void toXML(Collection messages, OutputStream ostream) throws JMSException, IOException;

    /**
     * Create some messages from an XML string
     */
    public Collection fromXML(String document) throws JMSException;

    /**
     * Create some messages from XML from an InputStream
     */
    public Collection fromXML(InputStream istream) throws JMSException, IOException;
    
    /** 
     * Get the destination mananger to locate topics and queues in JNDI or on the session.
     * 
     * @return
     */
    public DestinationManager getDestinationManager() ;
    
    /**
     * Create a queue with the given name
     */
    public Queue createQueue(String queueName) throws JMSException, NamingException ;
    
    /**
     * Create a topic with the given name
     */
    public Topic createTopic(String topicName) throws JMSException, NamingException;
    
    /**
     * Force a reconnect with a different username and password 
     */
    public void reconnect(String username, String password) throws JMSException;
    
    /**
     * Create JNDI context that this Hermes has been configured with. Returns the context 
     * or null is none exists. It is your responsibility to close the context when done.
     */    
    public Context createContext() throws NamingException, JMSException ;
}
