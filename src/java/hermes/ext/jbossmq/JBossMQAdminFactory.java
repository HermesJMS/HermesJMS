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

package hermes.ext.jbossmq;

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
 * @author colincrist@hermesjms.com
 * @version $Id: JBossMQAdminFactory.java,v 1.1 2004/07/30 17:25:15 colincrist
 *          Exp $
 */
public class JBossMQAdminFactory implements HermesAdminFactory
{
    private static final Logger log = Logger.getLogger(JBossMQAdminFactory.class);

    private String rmiAdaptorBinding;

    public JBossMQAdminFactory()
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
        if (connectionFactory instanceof JNDIConnectionFactory)
        {
            JNDIConnectionFactory jndiCF = (JNDIConnectionFactory) connectionFactory;

            return new JBossMQAdmin(this, hermes, jndiCF, jndiCF._getConnectionFactory());
        }
        else
        {
            throw new HermesException("Provider is not JBossMQ");
        }
    }

    public String getRmiAdaptorBinding()
    {
        return rmiAdaptorBinding;
    }

    public void setRmiAdaptorBinding(String rmiAdaptorBinding)
    {
        this.rmiAdaptorBinding = rmiAdaptorBinding;
    }
}