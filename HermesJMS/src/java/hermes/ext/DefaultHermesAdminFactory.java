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

package hermes.ext;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultHermesAdminFactory.java,v 1.1 2004/07/30 17:25:15
 *          colincrist Exp $
 */

public class DefaultHermesAdminFactory implements HermesAdminFactory
{

    /*
     * (non-Javadoc)
     * 
     * @see hermes.ProviderExtension#createSession(hermes.Hermes,
     *      javax.jms.ConnectionFactory)
     */
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException
    {
        return new DefaultHermesAdmin(hermes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.ProviderExtension#canBlock()
     */
    public boolean canBlock() throws JMSException
    {
        return false;
    }

    public boolean isConfigDynamic()
    {
        return false;
    }

}