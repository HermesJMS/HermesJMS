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

package hermes.ext.hornetq;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.JNDIConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * @author David Cole : dlcole@gmail.com
 */
public class HornetQAdminFactory implements HermesAdminFactory {
	private static final Logger log = Logger.getLogger(HornetQAdminFactory.class);

	private String jmxUrl;
	private String mBeanServerConnectionJndi;

	public HornetQAdminFactory() {
		super();
		log.info("ctor " + Thread.currentThread().getContextClassLoader()) ;
		log.info(getClass().getClassLoader()) ;
	}

	/**
	 * @see hermes.ProviderExtension#createSession(javax.jms.ConnectionFactory)
	 */
	public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws NamingException,
			JMSException {
		
		log.info(Thread.currentThread().getContextClassLoader()) ;
		log.info(getClass().getClassLoader()) ;
		
		if (connectionFactory instanceof JNDIConnectionFactory) {
			JNDIConnectionFactory jndiCF = (JNDIConnectionFactory) connectionFactory;

			return new HornetQAdmin(this, hermes, jndiCF, jndiCF._getConnectionFactory());
		}
		else {
			return new HornetQAdmin(this, hermes, null, connectionFactory);
		}
	}

	public String getJmxUrl() {
		return jmxUrl;
	}

	/**
	 * Set's the URL providing access to the JMX Server<br>
	 * An example for JBoss would be of the form: service:jmx:rmi:///jndi/rmi://localhost:1090/jmxconnector
	 * 
	 * @param jmxUrl
	 */
	public void setJmxUrl(String jmxUrl) {
		this.jmxUrl = jmxUrl;
	}

	/**
	 * @return the mBeanServerConnectionJndi
	 */
	public String getMBeanServerConnectionJndi() {
		return mBeanServerConnectionJndi;
	}

	/**
	 * @param mBeanServerConnectionJndi the mBeanServerConnectionJndi to set
	 */
	public void setMBeanServerConnectionJndi(String mBeanServerConnectionJndi) {
		this.mBeanServerConnectionJndi = mBeanServerConnectionJndi;
	}
}