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

/**
 * @author colincrist@hermesjms.com
 * @version $Id: HermesDispatcher.java,v 1.3 2004/09/16 20:30:41 colincrist Exp $
 */

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;

public interface HermesDispatcher
{
    public void invoke(Runnable runnable) throws JMSException;

    public void invokeAndWait(Runnable runnable) throws JMSException;

    public void setMessageListener(Destination from, MessageListener ml) throws JMSException;

    public void close() throws JMSException;
}