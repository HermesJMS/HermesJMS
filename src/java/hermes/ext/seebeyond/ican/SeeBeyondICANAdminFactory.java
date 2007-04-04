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

package hermes.ext.seebeyond.ican;

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
public class SeeBeyondICANAdminFactory implements HermesAdminFactory
{
    private static final Logger log = Logger.getLogger(SeeBeyondICANAdminFactory.class);
    
    /* For ICAN 5.0.5 */
    private String environmentName;
    private String logicalhostName;
    private String jmsIQManagerName;
    private String repositoryHost;
    private String repositoryPort;

    public SeeBeyondICANAdminFactory()
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
    	return new SeeBeyondICANAdmin(this, hermes, connectionFactory);
    }



	public String getEnvironmentName() {
		return environmentName;
	}

	public void setEnvironmentName(String name) {
		environmentName = (name == null ? null : name.trim());
	}

	public String getJmsIQManagerName() {
		return jmsIQManagerName;
	}

	public void setJmsIQManagerName(String name) {
		jmsIQManagerName = (name == null ? null : name.trim());
	}

	public String getLogicalhostName() {
		return logicalhostName;
	}

	public void setLogicalhostName(String name) {
		logicalhostName = (name == null ? null : name.trim());
	}

	public String getRepositoryHost() {
		return repositoryHost;
	}

	public void setRepositoryHost(String repositoryurl) {
		repositoryHost = (repositoryurl == null ? null : repositoryurl.trim());
	}

	public String getRepositoryPort() {
		return repositoryPort;
	}

	public void setRepositoryPort(String repositoryPort) {
		this.repositoryPort = repositoryPort;
	}

}