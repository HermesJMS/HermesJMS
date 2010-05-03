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

package hermes.browser.model;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Category;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: BeanTableModel.java,v 1.5 2004/10/05 07:42:28 colincrist Exp $
 */

public class BeanTableModel extends DefaultTableModel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3773435148846671053L;
	private static Set ignore = new HashSet();
    private static final Category cat = Category.getInstance(BeanTableModel.class);
    private Object bean;
    private Map filter;
    private Vector rows = new Vector();
    private String[] columns = { "Property", "Value"};
    private Map changes = new HashMap();

    static
    {
        ignore.add("class");
    }

    public BeanTableModel(Object bean, Map filter) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Map properties = PropertyUtils.describe(bean);
        Set iterSet = null;

        this.bean = bean;

        if (filter == null)
        {
            iterSet = properties.keySet();
        }
        else
        {
            iterSet = filter.keySet();
        }

        for (Iterator iter = iterSet.iterator(); iter.hasNext();)
        {
            String propertyName = (String) iter.next();

            if (properties.containsKey(propertyName) && !ignore.contains(propertyName))
            {
                Object propertyValue = properties.get(propertyName);

                if (propertyValue == null)
                {
                    propertyValue = "null";
                }

                Vector row = new Vector();

                row.add(propertyName);
                row.add(propertyValue);

                rows.add(row);
            }
        }

    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return rows.size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return columns.length;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int arg0, int arg1)
    {
        Vector row = (Vector) rows.elementAt(arg0);

        return row.elementAt(arg1);

    }

    public String getColumnName(int x)
    {
        return columns[x];
    }

    public boolean isCellEditable(int y, int x)
    {
        return (x == 1);
    }

    public void setValueAt(Object value, int y, int x)
    {
        Vector row = (Vector) rows.elementAt(y);
        String propertyName = (String) row.elementAt(0);

        row.set(x, value);
        changes.put(propertyName, value);
    }

    public void updateBean() throws InvocationTargetException, IllegalAccessException, IllegalAccessException, NoSuchMethodException
    {
        for (Iterator iter = changes.keySet().iterator(); iter.hasNext();)
        {
            String key = (String) iter.next();
            Object value = changes.get(key);

            cat.debug("setting property=" + key + " value=" + value);

            PropertyUtils.setProperty(bean, key, value);
        }

        changes.clear();
    }
}