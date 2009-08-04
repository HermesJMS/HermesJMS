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

package hermes.ext.seebeyond.jcaps;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * 
 * @author murali
 *
 */
public class SeeBeyondJCAPSAdminFactory implements HermesAdminFactory
{
    private static final Logger log = Logger.getLogger(SeeBeyondJCAPSAdminFactory.class);
    
    /* For JavaCAPS 5.1.0 or later */
    private String logicalHostIP;
    private String logicalHostPort;
    private String logicalHostUser;
    private String logicalHostUserPassword;

    public SeeBeyondJCAPSAdminFactory()
    {
        // NOP
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.ProviderExtension#createSession(javax.jms.ConnectionFactory)
     */
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws NamingException, JMSException
    {
    	return new SeeBeyondJCAPSAdmin(this, hermes, connectionFactory);
    }







	public String getLogicalHostIP() {
		return logicalHostIP;
	}

	public void setLogicalHostIP(String logicalHost) {
		this.logicalHostIP = logicalHost;
	}

	public String getLogicalHostPort() {
		return logicalHostPort;
	}

	public void setLogicalHostPort(String logicalHostPort) {
		this.logicalHostPort = logicalHostPort;
	}

	public String getLogicalHostUser() {
		return logicalHostUser;
	}

	public void setLogicalHostUser(String logicalHostUser) {
		this.logicalHostUser = logicalHostUser;
	}

	public String getLogicalHostUserPassword() {
		return logicalHostUserPassword;
	}

	public void setLogicalHostUserPassword(String logicalHostUserPassword) {
		this.logicalHostUserPassword = logicalHostUserPassword;
	}
}