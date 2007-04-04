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

import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;

/**
 * A generic message payload getter/setter. Hermes defines a simplified model of
 * all the JMS messages so that throughout the code (mainly the browser) any
 * message can be treated like any other - i.e. no specific handling required
 * for Text, Object Bytes etc tec.
 * 
 * A message contains:
 * 
 * 1) A set of properties. These are the JMS and user properties combined. 2) A
 * set of tags: o) TextMessage: text=getText() o) ObjectMessage: text=toString() +
 * dot separated path into the bean for each leaf attribute. o) MapMessage:
 * text=toString() + tag for each map entry. o) StreamMessage: text=toString() +
 * entry name = 1....n for each object in the stream. o) BytesMessage
 * text=toString() + bytes= the bytes
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: MessageContentHandler.java,v 1.1 2004/05/01 15:52:34 colincrist
 *          Exp $
 */

public interface MessageContentHandler
{
    /**
     * Get all available property names
     */
    public Set getPropertyNames() throws JMSException;

    /**
     * Get a specific property
     */
    public Object getProperty(String propertyName) throws JMSException;

    /**
     * Get all the properties
     */
    public Map getProperties() throws JMSException;

    /**
     * Get the JMSMessageID
     */
    public String getJMSMessageID() throws JMSException;

}