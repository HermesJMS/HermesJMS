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

import hermes.impl.jms.ContextImpl;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.apache.log4j.Logger;

import COM.activesw.api.client.BrokerAdminClient;
import COM.activesw.api.client.BrokerClientInfo;
import COM.activesw.api.client.BrokerConnectionDescriptor;

import com.wm.broker.jms.AdminFactory;
import com.wm.broker.jms.QueueAdmin;
import com.wm.broker.jms.QueueConnectionFactoryAdmin;

/**
 * Factory to interrogate a broker and return a context containing all the client queues as JMS queues along with
 * a QueueConnectionFactory for each client group.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: WMEInitialContextFactory.java,v 1.2 2004/09/30 21:20:12 colincrist Exp $
 */
public class WMEInitialContextFactory implements InitialContextFactory
{
    private static final Logger log = Logger.getLogger(WMEInitialContextFactory.class);
    private static final String URN = "wme";
    private static final Set ignoreClientGroups = new HashSet();

    static
    {
        ignoreClientGroups.add("admin");
    }

    /**
     * 
     */
    public WMEInitialContextFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    public Context getInitialContext(Hashtable environment) throws NamingException
    {
        if (!environment.containsKey(Context.PROVIDER_URL))
        {
            throw new NamingException(Context.PROVIDER_URL + " is not available");
        }

        String providerURL = (String) environment.get(Context.PROVIDER_URL);

        if (!providerURL.startsWith(URN))
        {
            throw new NamingException("providerURL URN is not " + URN);
        }

        /*
         * Parse the URL is a noddy way.
         */

        String hostName = null;
        String brokerName = null;
        String port = null;

        providerURL = providerURL.substring(providerURL.indexOf('/'));

        for (StringTokenizer tokens = new StringTokenizer(providerURL, ":/@"); tokens.hasMoreTokens();)
        {
            String element = tokens.nextToken();

            if (element != null && !element.equals(""))
            {
                if (brokerName == null)
                {
                    brokerName = element;
                }
                else if (hostName == null)
                {
                    hostName = element;
                }
                else if (port == null)
                {
                    port = element;
                }
            }
        }

        if (hostName == null || brokerName == null || port == null)
        {
            throw new NamingException("providerURL (" + providerURL + ") is malformed, format is wme://brokerName@hostname:port");
        }

        try
        {
            BrokerConnectionDescriptor bcd = new BrokerConnectionDescriptor();

            bcd.setConnectionShare(true);
            
            try
            {
                bcd.setConnectionShareLimit(100); // some arbitary number to handle concurrent connection.
            }
            catch (NoSuchMethodError ex)
            {
                // Old version of WebMethods
            }
            
            bcd.setSharedEventOrdering(BrokerConnectionDescriptor.SHARED_ORDER_NONE);

            BrokerAdminClient adminClient = BrokerAdminClient.newOrReconnectAdmin(hostName + ":" + port, brokerName, "admin" + System.currentTimeMillis(), "admin",
                    WMEInitialContextFactory.class.getName(), bcd);

            Context ctx = new ContextImpl(environment);

            /*
             * Lookup all the queues.
             */
            
            final String[] clientIds = adminClient.getClientIds();

            for (int i = 0; i < clientIds.length; i++)
            {
                final BrokerClientInfo info = adminClient.getClientInfoById(clientIds[i]);

                if (!ignoreClientGroups.contains(info.client_group))
                {
                    final QueueAdmin queue = AdminFactory.newQueue();

                    queue.setName(info.client_id);
                    queue.setClientGroup(info.client_group);
 
                    ctx.bind(info.client_id, queue);
                }
            }

            /*
             * Create a connection factory for each client group where there is a queue.
             */
            
            final String[] clientGroups = adminClient.getClientGroupNames() ; 
            
            for (int i = 0 ; i < clientGroups.length ; i++)
            {
                final String clientGroup = (String) clientGroups[i] ;
                final QueueConnectionFactoryAdmin cfAdmin = AdminFactory.newQueueConnectionFactory();

                cfAdmin.setBrokerHost(hostName + ":" + port);
                cfAdmin.setBrokerName(brokerName);
                cfAdmin.setConnectionClientGroup(clientGroup);
                cfAdmin.setConnectionClientId("hermes-" + System.currentTimeMillis());
              
                ctx.bind(clientGroup, cfAdmin);
            }

            adminClient.destroy();

            return ctx;
        }
        catch (Exception e)
        {
            NamingException ex = new NamingException(e.getMessage());

            ex.setRootCause(e);

            throw ex;
        }

    }
}