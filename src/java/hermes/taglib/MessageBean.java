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

package hermes.taglib;

import hermes.Hermes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageBean.java,v 1.4 2005/05/03 16:18:41 colincrist Exp $
 */
public class MessageBean
{
    private static final Logger log = Logger.getLogger(MessageBean.class);
    private Message message;
    private Hermes hermes;
    private String id;
    private String attributeId;
    private Collection content = new ArrayList();
    private Collection header = new ArrayList();

    /**
     *  
     */
    public MessageBean(String attributeId, Hermes hermes, Message message)
    {
        super();

        this.message = message;
        this.hermes = hermes;
        this.attributeId = attributeId;
    }

    public MessageBean(String attributeId, Hermes hermes, String id, Message message)
    {
        this(attributeId, hermes, message);

        this.id = id;
    }

    public String getId() throws JMSException
    {
        if (id == null)
        {
            return message.getJMSMessageID();
        }
        else
        {
            return id;
        }
    }

    public long getJMSTimestamp() throws JMSException
    {
        return message.getJMSTimestamp();
    }

    public String getJMSDestination() throws JMSException
    {
        return hermes.getDestinationName(message.getJMSDestination());
    }

    public String getMessageType()
    {
        if (message instanceof TextMessage)
        {
            return "TextMessage";
        }

        if (message instanceof MapMessage)
        {
            return "MapMessage";
        }

        if (message instanceof ObjectMessage)
        {
            return "ObjectMessage";
        }

        if (message instanceof BytesMessage)
        {
            return "BytesMessage";
        }

        if (message instanceof StreamMessage)
        {
            return "StreamMessage";
        }

        return "Unknown";
    }

    /**
     * @return Returns the message.
     */
    public Message getMessage()
    {
        return message;
    }

    /**
     * @return Returns the attributeId.
     */
    public String getAttributeId()
    {
        return attributeId;
    }

    /**
     * @param attributeId
     *            The attributeId to set.
     */
    public void setAttributeId(String attributeId)
    {
        this.attributeId = attributeId;
    }

    /**
     * @return Returns the content.
     */
    public Collection getContent()
    {
        try
        {
            if (content.size() == 0)
            {
                if (message instanceof MapMessage)
                {
                    MapMessage mapMessage = (MapMessage) message;

                    for (Enumeration iter = mapMessage.getMapNames(); iter.hasMoreElements();)
                    {
                        String propertyName = (String) iter.nextElement();
                        Object propertyValue = mapMessage.getObject(propertyName);

                        if (propertyValue == null)
                        {
                            propertyValue = "";
                        }

                        content.add(new TagValueBean(propertyName, propertyValue.toString()));
                    }
                }
                else if (message instanceof TextMessage)
                {
                    TextMessage textMessage = (TextMessage) message;

                    content.add(new TagValueBean("text", textMessage.getText()));
                }
                else if (message instanceof ObjectMessage)
                {
                    ObjectMessage objectMessage = (ObjectMessage) message;

                    content.add(new TagValueBean("object", objectMessage.getObject().toString()));
                }
                else
                {
                    content.add(new TagValueBean("error", "Cannot display " + getMessageType()));
                }
            }
        }
        catch (Throwable e)
        {
            log.error(e.getMessage());

            content.add(new TagValueBean("error", e.getMessage()));
        }

        return content;
    }

    public Collection getFullContent()
    {
        return getContent();
    }

    public Collection getHeader()
    {
        if (header.size() == 0)
        {
            try
            {
                header.add(new TagValueBean("JMSDestination", hermes.getDestinationName(message.getJMSDestination())));
                header.add(new TagValueBean("JMSType", message.getJMSType()));

                for (Enumeration e = message.getPropertyNames(); e.hasMoreElements();)
                {
                    String propertyName = (String) e.nextElement();
                    Object propertyValue = message.getObjectProperty(propertyName);

                    header.add(new TagValueBean(propertyName, propertyValue.toString()));
                }
            }
            catch (JMSException e)
            {
                log.error(e.getMessage());
            }
        }

        return header;
    }

    public String getXML()
    {
        try
        {
            Collection c = new ArrayList();

            c.add(getMessage());

            return hermes.toXML(c);
        }
        catch (JMSException e)
        {
            log.error(e.getMessage(), e);

            return e.getMessage();
        }
    }

    /**
     * @param content
     *            The content to set.
     */
    public void setContent(Collection content)
    {
        this.content = content;
    }

}