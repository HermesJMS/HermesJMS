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

import hermes.Hermes;

import javax.jms.Connection;
import javax.jms.JMSException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ConnectionManager.java,v 1.8 2006/10/29 07:37:41 colincrist Exp $
 */
public interface ConnectionManager extends JMSManager
{
   enum Policy
   {
      SHARED_CONNECTION, CONNECTION_PER_THREAD ;
   }
   
   public void setHermes(Hermes hermes) ;
   
    /**
     * Close the connection.
     * 
     * @throws JMSException
     */
    public void close() throws JMSException;

    /**
     * Connect will try and create the connection, if one exists it will be
     * closed.
     */
    public void connect() throws JMSException;

    /**
     * Get the connection, connecting as necesary
     */
    public Connection getConnection() throws JMSException;

    /**
     * Get the managed object, in this case the JMS Connection.
     */
    public Object getObject() throws JMSException;

    /**
     * Set the password for the connection.
     */
    public void setPassword(String password);

    /**
     * Set the username for the connection.
     */
    public void setUsername(String username);
    
    /**
     * Set the clientID on this 
     */
    public void setClientID(String clientID) ;
    
    public Policy getType() ;
    
}