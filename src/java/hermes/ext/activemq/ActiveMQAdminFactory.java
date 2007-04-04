/* 
 * Copyright 2003,2004,2005 Colin Crist
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

package hermes.ext.activemq;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.HermesException;

import java.net.MalformedURLException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.management.remote.JMXServiceURL;
import javax.naming.NamingException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ActiveMQAdminFactory.java,v 1.1 2006/05/26 10:08:20 colincrist
 *          Exp $
 */

public class ActiveMQAdminFactory implements HermesAdminFactory
{
   private static String DEFAULT_URL = "service:jmx:rmi:///jndi/rmi://localhost:1616/jmxrmi";

   private String serviceURL;
   private String brokerName = "localhost" ;
   private String username ;
   private String password ;
   
   public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException
   {
      try
      {
         return createHermesAdmin(hermes);
      }
      catch (MalformedURLException e)
      {
         throw new HermesException(e);
      }
   }

   private ActiveMQAdmin createHermesAdmin(Hermes hermes) throws MalformedURLException
   {
      if (serviceURL == null)
      {
         return new ActiveMQAdmin(this, hermes, brokerName, new JMXServiceURL(DEFAULT_URL));
      }
      else
      {
         return new ActiveMQAdmin(this, hermes, brokerName, new JMXServiceURL(serviceURL));
      }
   }

   public String getBrokerName()
   {
      return brokerName;
   }

   public void setBrokerName(String brokerName)
   {
      this.brokerName = brokerName;
   }

   public String getServiceURL()
   {
      return serviceURL;
   }

   public void setServiceURL(String serviceURL)
   {
      this.serviceURL = serviceURL;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }
}
