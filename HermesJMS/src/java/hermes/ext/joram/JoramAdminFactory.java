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

package hermes.ext.joram;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.JNDIConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: JoramAdminFactory.java,v 1.2 2004/10/05 07:42:30 colincrist Exp $
 */
public class JoramAdminFactory implements HermesAdminFactory
{
    private String username = "root" ;
    private String password = "root" ;
    private String hostname = "localhost" ;
    private int port = 16010 ;
    private int cnxTimer = 30 ;
  
   
    /**
     * 
     */
    public JoramAdminFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see hermes.HermesAdminFactory#createSession(hermes.Hermes, javax.jms.ConnectionFactory)
     */
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException
    {
        return new JoramAdmin(this, hermes, (JNDIConnectionFactory) connectionFactory) ;
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
    
    public int getCnxTimer()
    {
        return cnxTimer;
    }
    public void setCnxTimer(int cnxTimer)
    {
        this.cnxTimer = cnxTimer;
    }
    public String getHostname()
    {
        return hostname;
    }
    public void setHostname(String hostname)
    {
        this.hostname = hostname;
    }
    public int getPort()
    {
        return port;
    }
    public void setPort(int port)
    {
        this.port = port;
    }
}
