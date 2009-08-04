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

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Category;

/**
 * A helper to format beans into HTML
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HTMLBeanHelper.java,v 1.4 2004/09/16 20:30:42 colincrist Exp $
 */

public class HTMLBeanHelper
{
    private static final Category cat = Category.getInstance(HTMLBeanHelper.class);

    public static String format(String text, Object bean)
    {
        
        Map properties ;

        try
        {
            properties = BeanUtils.describe(bean);
        }
        catch (Exception ex)
        {
            cat.error("getting properties:  " + ex.getMessage(), ex);

            return bean.getClass().getName() + ": cannot get properties: " + ex.getMessage();
        }

        return format(text, properties) ;
    }
    
    public static String format(Map properties)
    {
        return format(null, properties) ;
    }
    
    public static String format(String text, Map properties)
    {
        String rval = text;
        StringBuffer buffer = new StringBuffer();
        
        if (properties != null)
        {
            if (text == null)
            {
                buffer.append("<html>");

                for (Iterator iter = properties.keySet().iterator(); iter.hasNext();)
                {
                    String key = (String) iter.next();
                    Object val = properties.get(key);

                    if (!key.equals("reference")) // TODO huh?
                    {
                        buffer.append(key).append("=").append(val);
                        
                        if (iter.hasNext())
                        {
                            buffer.append("<br>") ;
                        }
                    }
                }

                buffer.append("</html>");
            }
            else
            {
                for (Iterator iter = properties.keySet().iterator(); iter.hasNext();)
                {
                    String key = (String) iter.next();
                    Object val = properties.get(key);
                    rval = rval.replaceAll("\\$" + key, (val == null) ? "null" : val.toString());
                }

                return rval;
            }
        }

        return buffer.toString();
    }
}