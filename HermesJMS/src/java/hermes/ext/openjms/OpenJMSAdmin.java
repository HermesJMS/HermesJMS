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

package hermes.ext.openjms;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.exolab.jms.administration.AdminConnectionFactory;
import org.exolab.jms.administration.JmsAdminServerIfc;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: OpenJMSAdmin.java,v 1.6 2006/06/21 07:46:19 colincrist Exp $
 */
public class OpenJMSAdmin extends HermesAdminSupport implements HermesAdmin
{
   private static final Logger log = Logger.getLogger(OpenJMSAdmin.class);
   private JNDIConnectionFactory connectionFactory;
   private JmsAdminServerIfc adminServer;

   /**
    *  
    */
   public OpenJMSAdmin(Hermes hermes, JNDIConnectionFactory connectionFactory)
   {
      super(hermes);

      this.connectionFactory = connectionFactory;
   }

   private synchronized JmsAdminServerIfc getAdminServer() throws JMSException
   {
      try
      {
         if (adminServer == null)
         {
            if (connectionFactory.getSecurityPrincipal() == null)
            {
               adminServer = AdminConnectionFactory.create(connectionFactory.getProviderURL());
            }
            else
            {
               adminServer = AdminConnectionFactory.create(connectionFactory.getProviderURL(), connectionFactory.getSecurityPrincipal(), connectionFactory
                     .getSecurityCredentials());
            }
         }
      }
      catch (MalformedURLException e)
      {
         throw new HermesException(e);
      }

      return adminServer;
   }

   @Override
   protected synchronized Collection discoverDurableSubscriptions(String topicName, String jndiName) throws JMSException
   {
      final Vector durables = getAdminServer().getDurableConsumers(topicName);

      if (durables.size() > 0)
      {
         final Collection<DestinationConfig> rval = new ArrayList<DestinationConfig>();

         for (final Object o : durables)
         {
            final String durableName = (String) o;
            final DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig();

            dConfig.setClientID(durableName);
            dConfig.setName(jndiName == null ? topicName : jndiName);
            dConfig.setDomain(Domain.TOPIC.getId());
            dConfig.setDurable(true);

            rval.add(dConfig);
         }

         return rval;
      }
      else
      {
         return Collections.EMPTY_SET;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesAdmin#getDepth(javax.jms.Destination)
    */
   public synchronized int getDepth(DestinationConfig dConfig) throws JMSException
   {
      if (dConfig.getDomain() == Domain.QUEUE.getId())
      {
         return getAdminServer().getQueueMessageCount(getRealDestinationName(dConfig));
      }
      else
      {
         if (dConfig.getDomain() == Domain.TOPIC.getId() && dConfig.isDurable())
         {
            return getAdminServer().getDurableConsumerMessageCount(dConfig.getName(), dConfig.getClientID());
         }
         else
         {
            throw new HermesException("Cannot provide non-durable topic depth");
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesAdmin#close()
    */
   public synchronized void close() throws JMSException
   {
      if (adminServer != null)
      {
         try
         {
            adminServer.close();
         }
         finally
         {
            adminServer = null;
         }
      }
   }
}