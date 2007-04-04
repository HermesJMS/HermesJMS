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

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.naming.NamingException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageFactory.java,v 1.4 2005/06/20 15:28:38 colincrist Exp $
 */

public interface MessageFactory
{
    /**
     * Create a javax.jms.BytesMessage
     */
    public BytesMessage createBytesMessage() throws JMSException;

    /**
     * Create a javax.jms.MapMessage
     */
    public MapMessage createMapMessage() throws JMSException;

    /**
     * Create a javax.jms.ObjectMessage
     */
    public ObjectMessage createObjectMessage() throws JMSException;

    /**
     * Create a javax.jms.ObjectMessage
     */
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException;

    /**
     * Create a javax.jms.StreamMessage
     */
    public StreamMessage createStreamMessage() throws JMSException;

    /**
     * Create a javax.jms.TextMessage
     */
    public TextMessage createTextMessage() throws JMSException;

    /**
     * Create a javax.jms.BytesMessage
     */
    public TextMessage createTextMessage(String text) throws JMSException;

    /**
     * Create a javax.jms.Message
     */
    public Message createMessage() throws JMSException;

    /**
     * Get the destination, the domain depends on the ConnectionFactory
     */
    public Destination getDestination(String name, Domain domain) throws JMSException, NamingException;

    /**
     * Get the destination name
     */
    public String getDestinationName(Destination to) throws JMSException;

}