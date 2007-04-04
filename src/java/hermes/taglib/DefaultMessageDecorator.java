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

package hermes.taglib;

import hermes.Hermes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.jms.Message;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultMessageDecorator.java,v 1.1 2004/05/01 15:52:36
 *          colincrist Exp $
 */
public class DefaultMessageDecorator implements MessageDecorator
{

    private static final Collection emptyCollection = new ArrayList();

    /**
     *  
     */
    public DefaultMessageDecorator()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.taglib.MessageDecorator#decorate(javax.jms.Message)
     */
    public Object decorate(String attrId, Hermes hermes, String id, Message message, Collection params, Map headerMap)
    {
        return new MessageBean(attrId, hermes, id, message);
    }

}