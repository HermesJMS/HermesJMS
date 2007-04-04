/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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

package hermes.ext.sonicmq;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.JNDIConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * Plugin for SonicMQ
 * 
 * @author colincrist@hermesjms.com
 */
public class SonicMQAdminFactory implements HermesAdminFactory
{
    private String connectionURL;
    private String defaultUser;
    private String defaultPassword;
    private String domain;
    private String container;
    private String broker;
    private String brokerName = "Broker1" ;
    private long timeout = -1 ;

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdminFactory#createSession(hermes.Hermes,
     *      javax.jms.ConnectionFactory)
     */
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException
    {
            if (connectionFactory instanceof JNDIConnectionFactory)
            {
                JNDIConnectionFactory jndiCF = (JNDIConnectionFactory) connectionFactory;

                if ( connectionURL == null)
                {
                    connectionURL = jndiCF.getProviderURL();
                }

                if ( defaultUser == null)
                {
                    defaultUser = jndiCF.getSecurityPrincipal();
                }

                if ( defaultPassword == null)
                {
                    defaultPassword = jndiCF.getSecurityCredentials();
                }
            }
            else if (connectionFactory instanceof progress.message.jclient.ConnectionFactory)
            {
                progress.message.jclient.ConnectionFactory sonicCF = (progress.message.jclient.ConnectionFactory) connectionFactory ;
                
                if (connectionURL == null)
                {
                    connectionURL = sonicCF.getBrokerURL() ;
                }
                
                if (defaultUser == null)
                {
                    defaultUser = sonicCF.getDefaultUser() ;
                }
                
                if (defaultPassword == null)
                {
                    defaultPassword = sonicCF.getDefaultPassword() ;
                }
            }
            
            return new SonicMQAdmin(hermes, this) ;
            
            
       
    }
    
    public String getBroker()
    {
        return broker;
    }
    
    public void setBroker(String broker)
    {
        this.broker = broker;
    }
    public String getBrokerName()
    {
        return brokerName;
    }
    public void setBrokerName(String brokerName)
    {
        this.brokerName = brokerName;
    }
    public String getConnectionURL()
    {
        return connectionURL;
    }
    public void setConnectionURL(String connectionURL)
    {
        this.connectionURL = connectionURL;
    }
    public String getContainer()
    {
        return container;
    }
    public void setContainer(String container)
    {
        this.container = container;
    }
    public String getDefaultPassword()
    {
        return defaultPassword;
    }
    public void setDefaultPassword(String defaultPassword)
    {
        this.defaultPassword = defaultPassword;
    }
    public String getDefaultUser()
    {
        return defaultUser;
    }
    public void setDefaultUser(String defaultUser)
    {
        this.defaultUser = defaultUser;
    }
    public String getDomain()
    {
        return domain;
    }
    public void setDomain(String domain)
    {
        this.domain = domain;
    }
    public long getTimeout()
    {
        return timeout;
    }
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }
}