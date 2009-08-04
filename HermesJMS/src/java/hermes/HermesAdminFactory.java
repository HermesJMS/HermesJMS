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

package hermes;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * A HermesAdminFactory allows us to implement and provide an API that can cover
 * the deficiencies in JMS.
 * 
 * Each Hermes can be configured with a HermesAdminFactory that uses the native
 * API to create the HermesAdmin allowing admin calls on the transport.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HermesAdminFactory.java,v 1.1 2004/07/30 17:25:13 colincrist
 *          Exp $
 */
public interface HermesAdminFactory
{
    /**
     * Create the session against the provider.
     * 
     * @param connectionFactory
     * @return @throws
     *         JMSException
     */
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException;
}