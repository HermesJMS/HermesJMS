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


import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

/**
 * A JMSManager abstract class provides the implementation of a tree of
 * ConnectionFactory, Connection and Session. Implementations of this must
 * provide a consistent implementaion of connect() that traverses up the tree in
 * order to build parent objects.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: JMSManagerImpl.java,v 1.6 2005/07/23 15:54:11 colincrist Exp $
 */

public abstract class JMSManagerImpl implements JMSManager
{
    private List children = new ArrayList();
    public JMSManager parent;

    public abstract void connect() throws JMSException;

    public abstract Object getObject() throws JMSException;

    public JMSManagerImpl()
    {
        super();
    }

    public void addChild(JMSManager child)
    {
        children.add(child);
        child.setParent(this);
    }

    /**
     * Set the parent node, this is called by the Digester
     */
    public void setParent(JMSManager parent)
    {
        this.parent = parent;
    }

    /**
     * @return Returns the parent.
     */
    public JMSManager getParent()
    {
        return parent;
    }
}