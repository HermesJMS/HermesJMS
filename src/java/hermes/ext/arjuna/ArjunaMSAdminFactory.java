/* 
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

package hermes.ext.arjuna;

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
 * Administration plugin for ArjunaMS.
 * 
 * @author arnaud.simon@arjuna.com, colincrist@hermesjms.com  last changed by: $Author: colincrist $
 * @version $Id: ArjunaMSAdminFactory.java,v 1.5 2005/05/01 11:23:53 colincrist Exp $
 */
public class ArjunaMSAdminFactory implements HermesAdminFactory
{
    private final static Logger log = Logger.getLogger(ArjunaMSAdminFactory.class) ;

    private String adminBinding = "Admin" ; // binding in JNDI to find the admin interface.
    
    public ArjunaMSAdminFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see hermes.HermesAdminFactory#createSession(hermes.Hermes, javax.jms.ConnectionFactory)
     */
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException
    {
       if (connectionFactory instanceof JNDIConnectionFactory)
       {
          JNDIConnectionFactory jndiCF = (JNDIConnectionFactory) connectionFactory ;
          return new ArjunaMSAdmin(hermes, this, jndiCF.createContext()) ;
       }
       else
       {
          throw new HermesException("Provider is not ArjunaMS") ;
       }
    }

    /**
     * Gets the name of the binding in JNDI to find the Admin interface.
     * 
     * @return
     */
    public String getAdminBinding()
    {
        return adminBinding;
    }
    /**
     * Sets the name of the binding in JNDI to find the Admin interface.
     * @param adminBinding
     */
    public void setAdminBinding(String adminBinding)
    {
        this.adminBinding = adminBinding;
    }
}
