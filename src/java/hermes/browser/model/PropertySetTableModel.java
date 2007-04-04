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

import hermes.browser.HermesBrowser;
import hermes.config.PropertyConfig;
import hermes.config.PropertySetConfig;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.xml.bind.JAXBException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: PropertySetTableModel.java,v 1.2 2004/07/30 17:25:14 colincrist
 *          Exp $
 */

public class PropertySetTableModel extends AbstractTableModel
{
    private static Set ignore = new HashSet();
    private static final Logger log = Logger.getLogger(BeanTableModel.class);
    private PropertySetConfig propertySet;
    private Map filter;
    private Vector rows = new Vector();
    private String[] columns = { "Property", "Value"};
    private Object bean;
    private Map beanProperties;

    static
    {
        ignore.add("class");
    }

    public PropertySetTableModel(Object bean, PropertySetConfig propertySet, Set filter) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException
    {
        this.propertySet = propertySet;

        Set iterSet = null;
        SortedMap sortMap = new TreeMap();

        if (propertySet.getProperty() != null)
        {

            for (Iterator iter = propertySet.getProperty().iterator(); iter.hasNext();)
            {
                PropertyConfig property = (PropertyConfig) iter.next();

                if (!ignore.contains(property.getName()))
                {
                    Object propertyValue = property.getValue();

                    if (propertyValue == null)
                    {
                        propertyValue = "null";
                    }

                    sortMap.put(property.getName(), propertyValue);
                }
            }

            for (Iterator iter2 = sortMap.entrySet().iterator(); iter2.hasNext();)
            {
                Map.Entry entry = (Map.Entry) iter2.next();

                Vector row = new Vector();

                row.add(entry.getKey());
                row.add(entry.getValue());

                rows.add(row);
            }
        }

        setBean(bean);
    }

    public void setBean(Object bean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        beanProperties = BeanUtils.describe(bean);

        for (Iterator iter = ignore.iterator(); iter.hasNext();)
        {
            beanProperties.remove((iter.next()));
        }

        for (Iterator iter = rows.iterator(); iter.hasNext();)
        {
            Vector row = (Vector) iter.next();

            String propertyName = (String) row.elementAt(0);

            if (propertyName == null || propertyName.equals("") || !isValidProperty(propertyName))
            {
                log.debug(propertyName + " is not a valid property for " + bean.getClass().getName() + " - removed");

                iter.remove();
            }
        }

        fireTableDataChanged();
    }

    public Set getValidProperties()
    {
        return beanProperties.keySet();
    }

    public boolean isValidProperty(String propertyName)
    {
        return getValidProperties().contains(propertyName);
    }

    public void insertRow() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        Vector row = new Vector();

        row.add("");
        row.add("");

        rows.add(row);

        fireTableRowsInserted(rows.size() - 1, rows.size());
    }

    public void removeRow(int index)
    {
        if (index < rows.size())
        {
            Vector r = (Vector) rows.remove(index);

        }

        fireTableDataChanged();
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
        return true;
    }

    public void setValueAt(Object value, int y, int x)
    {
        Vector row = (Vector) rows.elementAt(y);
        String propertyName;
        Object propertyValue;

        if (x == 0)
        {
            propertyName = (String) value;
            propertyValue = row.elementAt(1);

            if (isValidProperty(propertyName))
            {
                row.set(0, value);
            }
            else
            {
                log.error(propertyName + " is not a valid property for " + bean.getClass().getName());
            }
        }
        else
        {
            propertyName = (String) row.elementAt(0);
            propertyValue = value;
            row.set(1, value);
        }

        log.debug("set (cached) " + propertyName + "=" + propertyValue.toString());

        fireTableCellUpdated(y, x);
    }

    public List getProperties() throws JAXBException
    {
        List list = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();)
        {
            Vector row = (Vector) iter.next();

            String key = (String) row.elementAt(0);
            String value = (String) row.elementAt(1);

            if (key != null && !key.equals(""))
            {
                PropertyConfig pConfig = HermesBrowser.getConfigDAO().getFactory().createPropertyConfig();

                pConfig.setName(key);
                pConfig.setValue(value);

                list.add(pConfig);
            }
            else
            {
                iter.remove();
            }
        }

        return list;
    }

}