/* 
 * Copyright 2009 Laurent Bovet, Swiss Post IT
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

package hermes.ext.imq ;

import javax.jms.Connection;
import javax.jms.JMSException;

import com.sun.messaging.ConnectionConfiguration;

public class ConnectionFactory implements javax.jms.ConnectionFactory
{
    private com.sun.messaging.ConnectionFactory sunMqConnectionFactory;
    
    private String imqBrokerHostName;
    private String imqBrokerHostPort;
    private String imqDefaultUsername;
    private String imqDefaultPassword;    
    
    public void init() throws JMSException {
        sunMqConnectionFactory = new com.sun.messaging.ConnectionFactory();

        if(imqBrokerHostName == null || imqBrokerHostPort == null) {
            throw new RuntimeException("You must set all the following connection properties: imqBrokerHostName, imqBrokerHostPort.");
        }
        
        sunMqConnectionFactory.setProperty(
                ConnectionConfiguration.imqBrokerHostName,
                imqBrokerHostName);
        sunMqConnectionFactory.setProperty(
                ConnectionConfiguration.imqBrokerHostPort,
                imqBrokerHostPort);
        
        if(imqDefaultUsername != null ) {
            sunMqConnectionFactory.setProperty(
                ConnectionConfiguration.imqDefaultUsername,
                imqDefaultUsername);
        }
        
        if(imqDefaultPassword != null ) {
            sunMqConnectionFactory.setProperty(
                ConnectionConfiguration.imqDefaultPassword,
                imqDefaultPassword);
        }
    }
    
    public Connection createConnection() throws JMSException
    {
        init();
        return sunMqConnectionFactory.createConnection();
    }

    public Connection createConnection(String arg0, String arg1) throws JMSException
    {
        init();
        return sunMqConnectionFactory.createConnection(arg0, arg1);
    }

    public String getImqBrokerHostName()
    {
        return imqBrokerHostName;
    }

    public void setImqBrokerHostName(String imqBrokerHostName)
    {
        this.imqBrokerHostName = imqBrokerHostName;
    }

    public String getImqBrokerHostPort()
    {
        return imqBrokerHostPort;
    }

    public void setImqBrokerHostPort(String imqBrokerHostPort)
    {
        this.imqBrokerHostPort = imqBrokerHostPort;
    }

    public String getImqDefaultUsername()
    {
        return imqDefaultUsername;
    }

    public void setImqDefaultUsername(String imqDefaultUsername)
    {
        this.imqDefaultUsername = imqDefaultUsername;
    }

    public String getImqDefaultPassword()
    {
        return imqDefaultPassword;
    }

    public void setImqDefaultPassword(String imqDefaultPassword)
    {
        this.imqDefaultPassword = imqDefaultPassword;
    }

    
}
