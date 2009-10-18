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

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Support for Sun MQ (aka Java MQ or even Open MQ, technically named imq).
 * 
 * @author bovetl
 * @version $Revision$ 
 * @since 01.00.00.00
 */
public class ImqAdminFactory implements HermesAdminFactory
{
   
    private static Log LOG = LogFactory.getLog(ImqAdminFactory.class);
    
    public HermesAdmin createSession(Hermes hermes, javax.jms.ConnectionFactory connectionFactory)
        throws JMSException, NamingException
    {
        LOG.debug("Creating IMQ session");
        
        if(! (connectionFactory instanceof hermes.ext.imq.ConnectionFactory) ) {
            throw new RuntimeException("Provided connection factory is not a hermes.ext.imq.ConnectionFactory"); 
        }
      
        return new ImqAdmin(hermes, (hermes.ext.imq.ConnectionFactory)connectionFactory);        
    }

}
