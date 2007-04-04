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

package hermes.impl;

import hermes.Domain;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Destinations can be obtained in a number of ways, for either simple by
 * creating it from the Session or by locating in JNDI. This interface
 * encapsulates this behaviour.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationManager.java,v 1.2 2004/05/08 15:15:45 colincrist
 *          Exp $
 */

public interface DestinationManager
{
    public Destination getDestination(Session session, String named, Domain domain) throws JMSException ;
}