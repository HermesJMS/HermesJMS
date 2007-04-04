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
import hermes.HermesException;
import hermes.impl.ConnectionManager;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.log4j.Category;

/**
 * Manager for a shared JMS Connection for all threads
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ConnectionSharedManager.java,v 1.1 2004/07/21 20:25:40
 *          colincrist Exp $
 */

public class ConnectionSharedManager extends ConnectionManagerSupport implements ConnectionManager
{
   private static final Category cat = Category.getInstance(ConnectionSharedManager.class);

   private Connection connection;

   /**
    * ConnectionSharedManager constructor.
    */
   public ConnectionSharedManager()
   {
      super();
   }

   /**
    * Connect with try and create the connection, if one exists it will be
    * closed.
    */
   public void connect() throws JMSException
   {
      ConnectionFactory connectionFactory = (ConnectionFactory) parent.getObject();

      if (connectionFactory == null)
      {
         throw new HermesException("No ConnectionFactory has been created for this provider");
      }
      try
      {
         if (connection != null)
         {
            connection.close();
            connection = null;
         }
      }
      catch (JMSException ex)
      {
         cat.warn("previous connection throw exception during close: " + ex.getMessage());
      }

      cat.debug("creating connection from factory: " + parent);

      connection = createConnection();

   }

   /**
    * Get the connection, connecting as necesary
    */
   public Connection getConnection() throws JMSException
   {
      synchronized (this)
      {
         if (connection == null)
         {
            connect();
         }
      }

      return connection;
   }

   public void close() throws JMSException
   {
      synchronized (this)
      {
         if (connection != null)
         {
            Connection tmpConnection = connection;

            if (System.getProperty("hermes.dontCloseConnections") == null)
            {
               try
               {
                  connection = null;
                  tmpConnection.close();

                  getConnectionFactoryManager().close();
               }
               finally
               {
                  Hermes.events.notifyDisconnected(getHermes());
               }
            }
         }
      }
   }

   /**
    * Get the managed object, in this case the JMS Connection.
    */
   public Object getObject() throws JMSException
   {
      return getConnection();
   }

   public Policy getType()
   {
      return Policy.SHARED_CONNECTION;
   }
}