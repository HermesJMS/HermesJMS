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

package hermes.browser.dialog;

import com.jidesoft.converter.ConverterContext;
import com.jidesoft.grid.Property;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JidePropertyImpl.java,v 1.5 2005/02/25 08:41:49 colincrist Exp $
 */
public class JidePropertyImpl extends Property 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6623858418427241496L;
	private Object value;

    /*
     * (non-Javadoc)
     * 
     * @see com.jidesoft.grid.Property#setValue(java.lang.Object)
     */
    public void setValue(Object value)
    {
        this.value = value;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jidesoft.grid.Property#getValue()
     */
    public Object getValue()
    {
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jidesoft.grid.Property#hasValue()
     */
    public boolean hasValue()
    {
        return value != null;
    }

    /**
     * @param arg0
     */
    public JidePropertyImpl(String name, Object value)
    {
        super(name);
        
        this.value = value ;
    }

    /**
     * @param arg0
     * @param arg1
     */
    public JidePropertyImpl(String name, String description, Object value)
    {
        super(name, description);
        
        this.value = value ;
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     */
    public JidePropertyImpl(String name, String description, Class type, Object value)
    {
        super(name, description, type);
        
        this.value = value ;
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public JidePropertyImpl(String name, String description, Class type, String category, Object value)
    {
        super(name, description, type, category);
        
        this.value = value ;
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @param arg4
     */
    public JidePropertyImpl(String name, String description, Class type, String category, ConverterContext context, Object value)
    {
        super(name, description, type, category, context);
        
        this.value = value ;
    }
}