/* 
 * Copyright 2003,2004 Peter Lee, Colin Crist
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

package hermes.ext.weblogic;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * Administration plugin for WebLogicJMS.
 * 
 * Factory properties must be properly configure for the WebLogic plugin to work
 * correctly.
 * 
 * @author leepops@sourceforge.net  last changed by: $Author $
 * @version $Id: WebLogicJMSAdminFactory.java,v 1.5 2005/05/01 11:23:53 colincrist Exp $
 */
public class WebLogicJMSAdminFactory implements HermesAdminFactory
{
    private final static Logger log = Logger.getLogger(WebLogicJMSAdminFactory.class) ;

	private String webLogicDomain = "mydomain";
	private String jmsServer = "MyJMS Server";
	private String webLogicServer = "MyServer";
    
    public WebLogicJMSAdminFactory()
    {
        super();
    }

    /**
     * Creates a WebLogicJMSAdmin instance representing the session created to administer the provider.
     * @see hermes.HermesAdminFactory#createSession(hermes.Hermes, javax.jms.ConnectionFactory)
     */
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException
    {
       if (connectionFactory instanceof JNDIConnectionFactory)
       {
          JNDIConnectionFactory jndiCF = (JNDIConnectionFactory) connectionFactory ;
          return new WebLogicJMSAdmin(hermes, this, jndiCF.createContext()) ;
       }
       else
       {
          throw new HermesException("Provider is not WebLogicJMS") ;
       }
    }

	/**
	 * Gets the name of the relevant JMSServer instance. This is shown in the WebLogic console
	 * under &gt;domainname&lt;/Services/JMS/Servers.
	 * @return
	 */
	public String getJmsServer()
	{
		return this.jmsServer;
	}

	/**
	 * Gets the name of the relevant WebLogic domain. This is shown in the WebLogic console
	 * as &gt;domainname&lt;.
	 * @return
	 */
	public String getWebLogicDomain()
	{
		return this.webLogicDomain;
	}

	/**
	 * Gets the name of the relevant WebLogic Server instance. This is shown in the WebLogic console
	 * under &gt;domainname&lt;/Servers.
	 * @return
	 */
	public String getWebLogicServer()
	{
		return this.webLogicServer;
	}

	/**
	 * Sets the name of the relevant JMSServer instance. This is shown in the WebLogic console
	 * under &gt;domainname&lt;/Services/JMS/Servers.
	 * @param string
	 */
	public void setJmsServer(String string)
	{
		this.jmsServer = string;
	}

	/**
	 * Sets the name of the relevant WebLogic domain. This is shown in the WebLogic console
	 * as &gt;domainname&lt;.
	 * @param string
	 */
	public void setWebLogicDomain(String string)
	{
		this.webLogicDomain = string;
	}

	/**
	 * Sets the name of the relevant WebLogic Server instance. This is shown in the WebLogic console
	 * under &gt;domainname&lt;/Servers.
	 * @param string
	 */
	public void setWebLogicServer(String string)
	{
		this.webLogicServer = string;
	}

}
