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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.apache.commons.collections.iterators.IteratorEnumeration;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MapMessageImpl.java,v 1.4 2004/09/16 20:30:49 colincrist Exp $
 */
public class MapMessageImpl extends MessageImpl implements MapMessage
{
    private Map body = new HashMap();

    /**
     *  
     */
    public MapMessageImpl()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#clearBody()
     */
    public void clearBody() throws JMSException
    {
        body.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getBoolean(java.lang.String)
     */
    public boolean getBoolean(String arg0) throws JMSException
    {
        if (body.containsKey(arg0) && body.get(arg0) instanceof Boolean)
        {
            return ((Boolean) body.get(arg0)).booleanValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getByte(java.lang.String)
     */
    public byte getByte(String arg0) throws JMSException
    {
        if (body.containsKey(arg0) && body.get(arg0) instanceof Byte)
        {
            return ((Byte) body.get(arg0)).byteValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getBytes(java.lang.String)
     */
    public byte[] getBytes(String arg0) throws JMSException
    {
        if (body.containsKey(arg0) && body.get(arg0) instanceof byte[])
        {
            return ((byte[]) body.get(arg0));
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getChar(java.lang.String)
     */
    public char getChar(String arg0) throws JMSException
    {
        if (body.containsKey(arg0) && body.get(arg0) instanceof Character)
        {
            return ((Character) body.get(arg0)).charValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getDouble(java.lang.String)
     */
    public double getDouble(String arg0) throws JMSException
    {
        if (body.containsKey(arg0) && body.get(arg0) instanceof Double)
        {
            return ((Double) body.get(arg0)).doubleValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getFloat(java.lang.String)
     */
    public float getFloat(String arg0) throws JMSException
    {
        if (body.containsKey(arg0) && body.get(arg0) instanceof Float)
        {
            return ((Float) body.get(arg0)).floatValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getInt(java.lang.String)
     */
    public int getInt(String arg0) throws JMSException
    {
        if (body.containsKey(arg0) && body.get(arg0) instanceof Integer)
        {
            return ((Integer) body.get(arg0)).intValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getLong(java.lang.String)
     */
    public long getLong(String arg0) throws JMSException
    {
        if (body.containsKey(arg0) && body.get(arg0) instanceof Long)
        {
            return ((Long) body.get(arg0)).longValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getMapNames()
     */
    public Enumeration getMapNames() throws JMSException
    {
        return new IteratorEnumeration(body.keySet().iterator());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getObject(java.lang.String)
     */
    public Object getObject(String arg0) throws JMSException
    {
        if (body.containsKey(arg0))
        {
            return body.get(arg0);
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getShort(java.lang.String)
     */
    public short getShort(String arg0) throws JMSException
    {
        if (body.containsKey(arg0) && body.get(arg0) instanceof Short)
        {
            return ((Short) body.get(arg0)).shortValue();
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#getString(java.lang.String)
     */
    public String getString(String arg0) throws JMSException
    {
        if (body.containsKey(arg0) && body.get(arg0) instanceof String)
        {
            return (String) body.get(arg0);
        }
        else
        {
            throw new HermesException("No such property " + arg0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#itemExists(java.lang.String)
     */
    public boolean itemExists(String arg0) throws JMSException
    {
        return body.containsKey(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setBoolean(java.lang.String, boolean)
     */
    public void setBoolean(String arg0, boolean arg1) throws JMSException
    {
        body.put(arg0, new Boolean(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setByte(java.lang.String, byte)
     */
    public void setByte(String arg0, byte arg1) throws JMSException
    {
        body.put(arg0, new Byte(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setBytes(java.lang.String, byte[])
     */
    public void setBytes(String arg0, byte[] arg1) throws JMSException
    {
        body.put(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setBytes(java.lang.String, byte[], int, int)
     */
    public void setBytes(String arg0, byte[] arg1, int arg2, int arg3) throws JMSException
    {
        body.put(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setChar(java.lang.String, char)
     */
    public void setChar(String arg0, char arg1) throws JMSException
    {
        body.put(arg0, new Character(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setDouble(java.lang.String, double)
     */
    public void setDouble(String arg0, double arg1) throws JMSException
    {
        body.put(arg0, new Double(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setFloat(java.lang.String, float)
     */
    public void setFloat(String arg0, float arg1) throws JMSException
    {
        body.put(arg0, new Float(arg1));

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setInt(java.lang.String, int)
     */
    public void setInt(String arg0, int arg1) throws JMSException
    {
        body.put(arg0, new Integer(arg1));

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setLong(java.lang.String, long)
     */
    public void setLong(String arg0, long arg1) throws JMSException
    {
        body.put(arg0, new Long(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setObject(java.lang.String, java.lang.Object)
     */
    public void setObject(String arg0, Object arg1) throws JMSException
    {
        body.put(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setShort(java.lang.String, short)
     */
    public void setShort(String arg0, short arg1) throws JMSException
    {
        body.put(arg0, new Short(arg1));
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MapMessage#setString(java.lang.String, java.lang.String)
     */
    public void setString(String arg0, String arg1) throws JMSException
    {
        body.put(arg0, arg1);
    }

}