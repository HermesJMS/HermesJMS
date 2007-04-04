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

package hermes.providers.messages;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ObjectMessageImpl.java,v 1.3 2004/09/16 20:30:49 colincrist Exp $
 */
public class ObjectMessageImpl extends MessageImpl implements ObjectMessage
{
    private Serializable object;

    /**
     *  
     */
    public ObjectMessageImpl()
    {
        super();
    }

    /**
     *  
     */
    public ObjectMessageImpl(Serializable object)
    {
        super();

        this.object = object;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.ObjectMessage#getObject()
     */
    public Serializable getObject() throws JMSException
    {
        return object;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.ObjectMessage#setObject(java.io.Serializable)
     */
    public void setObject(Serializable arg0) throws JMSException
    {
        object = arg0;
    }

}