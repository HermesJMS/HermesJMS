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

package hermes.providers.messages;

import hermes.HermesException;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.collections.iterators.IteratorEnumeration;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageImpl.java,v 1.6 2005/06/28 15:36:01 colincrist Exp $
 */
public class MessageImpl implements Message
{
    private String correlationId;
    private int deliveryMode;
    private Destination destination;
    private long expiration;
    private int priority;
    private boolean redilivered;
    private Destination replyTo;
    private long timestamp = new Date().getTime() ;
    private String type;
    private String messageId = UUID.randomUUID().toString() ;
    private MessageSessionReference session;
    private Map header = new HashMap();

    /**
     *  
     */
    public MessageImpl()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#acknowledge()
     */
    public void acknowledge() throws JMSException
    {
        session.acknowledge(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#clearBody()
     */
    public void clearBody() throws JMSException
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#clearProperties()
     */
    public void clearProperties() throws JMSException
    {
        header.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getBooleanProperty(java.lang.String)
     */
    public boolean getBooleanProperty(String arg0) throws JMSException
    {
        if (header.containsKey(arg0) && header.get(arg0) instanceof Boolean)
        {
            return ((Boolean) header.get(arg0)).booleanValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getByteProperty(java.lang.String)
     */
    public byte getByteProperty(String arg0) throws JMSException
    {
        if (header.containsKey(arg0) && header.get(arg0) instanceof Byte)
        {
            return ((Byte) header.get(arg0)).byteValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getDoubleProperty(java.lang.String)
     */
    public double getDoubleProperty(String arg0) throws JMSException
    {
        if (header.containsKey(arg0) && header.get(arg0) instanceof Double)
        {
            return ((Double) header.get(arg0)).doubleValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getFloatProperty(java.lang.String)
     */
    public float getFloatProperty(String arg0) throws JMSException
    {
        if (header.containsKey(arg0) && header.get(arg0) instanceof String)
        {
            return ((Float) header.get(arg0)).floatValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getIntProperty(java.lang.String)
     */
    public int getIntProperty(String arg0) throws JMSException
    {
        if (header.containsKey(arg0) && header.get(arg0) instanceof Integer)
        {
            return ((Integer) header.get(arg0)).intValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSCorrelationID()
     */
    public String getJMSCorrelationID() throws JMSException
    {
        return correlationId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSCorrelationIDAsBytes()
     */
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException
    {
        return correlationId.getBytes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSDeliveryMode()
     */
    public int getJMSDeliveryMode() throws JMSException
    {
        return deliveryMode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSDestination()
     */
    public Destination getJMSDestination() throws JMSException
    {
        return destination;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSExpiration()
     */
    public long getJMSExpiration() throws JMSException
    {
        return expiration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSMessageID()
     */
    public String getJMSMessageID() throws JMSException
    {
        return messageId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSPriority()
     */
    public int getJMSPriority() throws JMSException
    {
        return priority;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSRedelivered()
     */
    public boolean getJMSRedelivered() throws JMSException
    {
        return redilivered;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSReplyTo()
     */
    public Destination getJMSReplyTo() throws JMSException
    {
        return replyTo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSTimestamp()
     */
    public long getJMSTimestamp() throws JMSException
    {
        return timestamp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSType()
     */
    public String getJMSType() throws JMSException
    {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getLongProperty(java.lang.String)
     */
    public long getLongProperty(String arg0) throws JMSException
    {
        if (header.containsKey(arg0) && header.get(arg0) instanceof Long)
        {
            return ((Long) header.get(arg0)).longValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getObjectProperty(java.lang.String)
     */
    public Object getObjectProperty(String arg0) throws JMSException
    {
        if (header.containsKey(arg0))
        {
            return header.get(arg0);
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getPropertyNames()
     */
    public Enumeration getPropertyNames() throws JMSException
    {
        return new IteratorEnumeration(header.keySet().iterator());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getShortProperty(java.lang.String)
     */
    public short getShortProperty(String arg0) throws JMSException
    {
        if (header.containsKey(arg0) && header.get(arg0) instanceof Short)
        {
            return ((Short) header.get(arg0)).shortValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getStringProperty(java.lang.String)
     */
    public String getStringProperty(String arg0) throws JMSException
    {
        if (header.containsKey(arg0) && header.get(arg0) instanceof String)
        {
            return (String) header.get(arg0);
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String arg0) throws JMSException
    {
        return header.containsKey(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setBooleanProperty(java.lang.String, boolean)
     */
    public void setBooleanProperty(String arg0, boolean arg1) throws JMSException
    {
        header.put(arg0, new Boolean(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setByteProperty(java.lang.String, byte)
     */
    public void setByteProperty(String arg0, byte arg1) throws JMSException
    {
        header.put(arg0, new Byte(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setDoubleProperty(java.lang.String, double)
     */
    public void setDoubleProperty(String arg0, double arg1) throws JMSException
    {
        header.put(arg0, new Double(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setFloatProperty(java.lang.String, float)
     */
    public void setFloatProperty(String arg0, float arg1) throws JMSException
    {
        header.put(arg0, new Float(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setIntProperty(java.lang.String, int)
     */
    public void setIntProperty(String arg0, int arg1) throws JMSException
    {
        header.put(arg0, new Integer(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSCorrelationID(java.lang.String)
     */
    public void setJMSCorrelationID(String arg0) throws JMSException
    {
        correlationId = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSCorrelationIDAsBytes(byte[])
     */
    public void setJMSCorrelationIDAsBytes(byte[] arg0) throws JMSException
    {
        // @@ TODO
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSDeliveryMode(int)
     */
    public void setJMSDeliveryMode(int arg0) throws JMSException
    {
        this.deliveryMode = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSDestination(javax.jms.Destination)
     */
    public void setJMSDestination(Destination arg0) throws JMSException
    {
        if (arg0 == null)
        {
            throw new HermesException("destination is null") ;
        }
        this.destination = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSExpiration(long)
     */
    public void setJMSExpiration(long arg0) throws JMSException
    {
        this.expiration = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSMessageID(java.lang.String)
     */
    public void setJMSMessageID(String messageId) throws JMSException
    {
        this.messageId = messageId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSPriority(int)
     */
    public void setJMSPriority(int arg0) throws JMSException
    {
        this.priority = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSRedelivered(boolean)
     */
    public void setJMSRedelivered(boolean arg0) throws JMSException
    {
        this.redilivered = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSReplyTo(javax.jms.Destination)
     */
    public void setJMSReplyTo(Destination arg0) throws JMSException
    {
        this.replyTo = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSTimestamp(long)
     */
    public void setJMSTimestamp(long arg0) throws JMSException
    {
        this.timestamp = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSType(java.lang.String)
     */
    public void setJMSType(String arg0) throws JMSException
    {
        this.type = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setLongProperty(java.lang.String, long)
     */
    public void setLongProperty(String arg0, long arg1) throws JMSException
    {
        header.put(arg0, new Long(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setObjectProperty(java.lang.String,
     *      java.lang.Object)
     */
    public void setObjectProperty(String arg0, Object arg1) throws JMSException
    {
        header.put(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setShortProperty(java.lang.String, short)
     */
    public void setShortProperty(String arg0, short arg1) throws JMSException
    {
        header.put(arg0, new Short(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setStringProperty(java.lang.String,
     *      java.lang.String)
     */
    public void setStringProperty(String arg0, String arg1) throws JMSException
    {
        header.put(arg0, arg1);
    }

    /**
     * @return Returns the session.
     */
    public MessageSessionReference getSession()
    {
        return session;
    }

    /**
     * @param session
     *            The session to set.
     */
    public void setSession(MessageSessionReference session)
    {
        this.session = session;
    }

}