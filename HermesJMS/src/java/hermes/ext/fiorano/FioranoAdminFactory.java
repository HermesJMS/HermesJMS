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

package hermes.ext.fiorano;

import fiorano.jms.runtime.admin.MQAdminConnection;
import fiorano.jms.runtime.admin.MQAdminConnectionFactory;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * @author colincrist@hermesjms.com
 */
public class FioranoAdminFactory implements HermesAdminFactory
{
    private String adminBinding = "primaryACF" ;
    private String adminUser = "admin" ;
    private String adminPassword = "passwd" ;
    
    
    /* (non-Javadoc)
     * @see hermes.HermesAdminFactory#createSession(hermes.Hermes, javax.jms.ConnectionFactory)
     */
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException
    {
        if (connectionFactory instanceof JNDIConnectionFactory)
        {
            JNDIConnectionFactory jndiCF = (JNDIConnectionFactory) connectionFactory ;
            MQAdminConnectionFactory aCF = (MQAdminConnectionFactory) jndiCF.createContext().lookup(getAdminBinding()) ;
            MQAdminConnection aCon = aCF.createMQAdminConnection(getAdminUser(), getAdminPassword()) ;
            
            return new FioranoAdmin(hermes, aCon) ;
        }
        else
        {
            throw new HermesException("Can only access FioranoMQ via JNDI") ;
        }
    }
    public String getAdminBinding()
    {
        return adminBinding;
    }
    
    public void setAdminBinding(String adminBinding)
    {
        this.adminBinding = adminBinding;
    }
    
    public String getAdminPassword()
    {
        return adminPassword;
    }
    
    public void setAdminPassword(String adminPassword)
    {
        this.adminPassword = adminPassword;
    }
    
    public String getAdminUser()
    {
        return adminUser;
    }
    
    public void setAdminUser(String adminUser)
    {
        this.adminUser = adminUser;
    }
}
