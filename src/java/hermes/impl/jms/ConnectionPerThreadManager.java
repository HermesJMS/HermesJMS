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

import hermes.Hermes;
import hermes.impl.ConnectionManager;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public class ConnectionPerThreadManager extends ConnectionManagerSupport implements ConnectionManager
{
    private static final Logger log = Logger.getLogger(ConnectionPerThreadManager.class);
    private ThreadLocal connectionTL = new ThreadLocal();
    private ThreadLocal clientIDTL = new ThreadLocal() ;
    private int connectionId = 0 ;

    /**
     *  
     */
    public ConnectionPerThreadManager()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.impl.JMSManager#connect()
     */
    public void connect() throws JMSException
    {
        Connection oldConnection = (Connection) connectionTL.get();

        if (oldConnection != null)
        {
            try
            {
                oldConnection.close();
            }
            catch (JMSException e)
            {
                log.error("closing old connection: " + e.getMessage(), e);
            }
        }

        connectionTL.set(createConnection());
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.impl.JMSManager#getObject()
     */
    public Object getObject() throws JMSException
    {
        return getConnection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.impl.ConnectionManager#close()
     */
    public void close() throws JMSException
    {
        if (connectionTL.get() != null)
        {
            try
            {
                Connection connection = (Connection) connectionTL.get();
                connection.close();
            }
            finally
            {
                connectionTL.set(null);
                Hermes.events.notifyDisconnected(getHermes()) ;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.impl.ConnectionManager#getConnection()
     */
    public Connection getConnection() throws JMSException
    {
        if (connectionTL.get() == null)
        {
            connect();
        }

        return (Connection) connectionTL.get();
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.impl.ConnectionManager#getType()
     */
    public Policy getType()
    {
        return Policy.CONNECTION_PER_THREAD;
    }
    
    public String getClientID()
    {
        if (super.getClientID() == null)
        {
            return null ;
        }
        else if (clientIDTL.get() != null)
        {
            return (String) clientIDTL.get() ;
        }
        else
        {
            synchronized (this) 
            {
                String clientID = super.getClientID() + "-" + connectionId++ ;
                clientIDTL.set(clientID) ;
                
                return clientID ;
            }
        }
    }
}
