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

package hermes.ext.openjms;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: OpenJMSAdminFactory.java,v 1.2 2005/05/16 14:45:59 colincrist Exp $
 */
public class OpenJMSAdminFactory implements HermesAdminFactory
{

    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException
    {
        if (connectionFactory instanceof JNDIConnectionFactory)
        {
            JNDIConnectionFactory cf = (JNDIConnectionFactory) connectionFactory;

            return new OpenJMSAdmin(hermes, cf);
        }
        else
        {
            throw new HermesException("Incompatible ConnectionFactory");
        }
    }
}