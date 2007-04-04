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

package hermes.ext.wme;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.JNDIQueueConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

import COM.activesw.api.client.BrokerAdminClient;
import COM.activesw.api.client.BrokerConnectionDescriptor;
import COM.activesw.api.client.BrokerException;

import com.wm.broker.jms.QueueConnectionFactoryAdmin;

/**
 * Provider Extension for WebMethods Enterprise
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: WMEAdminFactory.java,v 1.2 2004/09/30 21:20:12 colincrist Exp $
 */
public class WMEAdminFactory implements HermesAdminFactory
{
    /* (non-Javadoc)
     * @see hermes.ext.HermesAdminFactory#createSession(hermes.Hermes, javax.jms.ConnectionFactory)
     */
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException
    {
        QueueConnectionFactoryAdmin cfAdmin = null;

        if (connectionFactory instanceof JNDIQueueConnectionFactory)
        {
            JNDIQueueConnectionFactory jndiCF = (JNDIQueueConnectionFactory) connectionFactory;

            return createSession(hermes, jndiCF._getConnectionFactory());
        }
        if (connectionFactory instanceof QueueConnectionFactoryAdmin)
        {
            try
            {
                cfAdmin = (QueueConnectionFactoryAdmin) connectionFactory;
                final BrokerConnectionDescriptor bcd = new BrokerConnectionDescriptor();

                bcd.setConnectionShare(true);
                
                try
                {
                    bcd.setConnectionShareLimit(100); // some arbitary number to handle concurrent connection.
                }
                catch (NoSuchMethodError ex)
                {
                    // Old version of WebMethods
                }     

                final BrokerAdminClient adminClient = BrokerAdminClient.newOrReconnectAdmin(cfAdmin.getBrokerHost(), cfAdmin.getBrokerName(), "admin"
                        + System.currentTimeMillis(), "admin", WMEAdminFactory.class.getName(), bcd);

                return new WMEAdmin(hermes, adminClient);
            }
            catch (BrokerException e)
            {
                throw new HermesException(e);
            }
        }
        else
        {
            throw new HermesException("Provider is not WebMethods JMS");
        }
    }

    /* (non-Javadoc)
     * @see hermes.ProviderExtension#canBlock()
     */
    public boolean canBlock() throws JMSException
    {
        return false;
    }
}