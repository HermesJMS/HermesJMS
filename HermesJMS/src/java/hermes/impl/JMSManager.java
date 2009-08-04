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

import javax.jms.JMSException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JMSManager.java,v 1.5 2006/01/14 12:59:12 colincrist Exp $
 */
public interface JMSManager
{
   
   public void close() throws JMSException ;
   
    public void connect() throws JMSException;

    public Object getObject() throws JMSException;

    public void addChild(JMSManager child);

    /**
     * Set the parent node, this is called by the Digester
     */
    public void setParent(JMSManager parent);

    public JMSManager getParent();
}