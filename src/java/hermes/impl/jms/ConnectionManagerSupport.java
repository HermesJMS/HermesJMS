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
import hermes.JNDIQueueConnectionFactory;
import hermes.ProviderFailureException;
import hermes.impl.ConnectionFactoryManager;
import hermes.impl.ConnectionManager;
import hermes.impl.JMSManagerImpl;
import hermes.util.JMSUtils;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public abstract class ConnectionManagerSupport extends JMSManagerImpl implements ConnectionManager
{
   private static final Logger log = Logger.getLogger(ConnectionManagerSupport.class);
   private String clientID;
   private String password;
   private String username;
   private Hermes hermes ;
 
   /**
    * 
    */
   public ConnectionManagerSupport()
   {
      super();
     
   }

   public Hermes getHermes()
   {
      return hermes ;
   }
   
   public void setHermes(Hermes hermes)
   {
      this.hermes = hermes ;
   }

   /**
    * Set the clientID on this
    */
   public void setClientID(String clientID)
   {
      this.clientID = clientID;
   }

   /**
    * Set the username for the connection.
    */
   public void setUsername(String username)
   {
      this.username = username;
   }

   /**
    * Set the password for the connection.
    */
   public void setPassword(String password)
   {
      this.password = password;
   }

   protected Connection createConnection() throws JMSException
   {
     return createConnection(username, password) ;
   }
   
   protected Connection createConnection(String username, String password) throws JMSException
   {
      final ConnectionFactory connectionFactory = (ConnectionFactory) parent.getObject();
      Connection rval = null;

      try
      {
         /**
          * Hack so that although the interface seems to be JMS 1.1, if its
          * WebMethods only use the JMS 1.0.2b methods.
          */
         boolean isWebMethodsHack = false;

         if (connectionFactory instanceof JNDIQueueConnectionFactory)
         {
            JNDIQueueConnectionFactory jndiCF = (JNDIQueueConnectionFactory) connectionFactory;

            if (jndiCF.getInitialContextFactory() != null && jndiCF.getInitialContextFactory().equals("hermes.ext.wme.WMEInitialContextFactory"))
            {
               isWebMethodsHack = true;
            }
         }

         if (!isWebMethodsHack)
         {
            if (username == null)
            {
               rval = connectionFactory.createConnection();
            }
            else
            {
               rval = connectionFactory.createConnection(username, password);
            }

            log.debug("connection created with JMS 1.1 interface");
         }
      }
      catch (NoSuchMethodError ex)
      {
         // NOP
      }
      catch (AbstractMethodError ex)
      {
         // NOP
      }

      if (rval == null)
      {
         if (JMSUtils.isQueue(connectionFactory))
         {
            if (username == null)
            {
               rval = ((QueueConnectionFactory) connectionFactory).createQueueConnection();
            }
            else
            {
               rval = ((QueueConnectionFactory) connectionFactory).createQueueConnection(username, password);
            }
         }
         else
         {
            if (username == null)
            {
               rval = ((TopicConnectionFactory) connectionFactory).createTopicConnection();
            }
            else
            {
               rval = ((TopicConnectionFactory) connectionFactory).createTopicConnection(username, password);
            }
         }

         log.debug("connection created with JMS 1.0.2b interface");
      }

      if (rval != null)
      {
         if (getClientID() != null && !getClientID().equals(""))
         {
            log.debug("calling setClientID(" + getClientID() + ") on the new connection");
            rval.setClientID(getClientID());
         }

         log.debug("calling start() on the new Connection");

         rval.start();
         
         Hermes.events.notifyConnected(hermes) ;
      }
      else
      {
         throw new ProviderFailureException("Internal provider error\nThe provider returned a null connection");
      }

      return rval;
   }

   public ConnectionFactoryManager getConnectionFactoryManager()
   {
      return (ConnectionFactoryManager) getParent();
   }

   public String getClientID()
   {
      return clientID;
   }
}
