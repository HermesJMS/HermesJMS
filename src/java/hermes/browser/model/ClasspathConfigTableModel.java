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

import hermes.config.ClasspathConfig;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ClasspathConfigTableModel.java,v 1.1 2004/07/21 19:46:13
 *          colincrist Exp $
 */
public class ClasspathConfigTableModel extends DefaultTableModel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3566589829758021732L;
	private static final Logger log = Logger.getLogger(ClasspathConfigTableModel.class) ;
    private List rows ;
 
    public ClasspathConfigTableModel(List rows)
    {
        addColumn("Library");
        addColumn("ConnectionFactory") ;

        this.rows = rows ;
    }

 
    public ClasspathConfig getRowAt(int row)
    {
        return (ClasspathConfig) rows.get(row) ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        if (rows == null)
        {
            return 0;
        }
        else
        {
            return rows.size();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int y, int x)
    {
        ClasspathConfig cConfig = (ClasspathConfig) rows.get(y);

        if (x == 0)
        {
            return cConfig.getJar();
        }
        else 
        {          
            return new Boolean(!cConfig.isNoFactories());
        }
    }

    public boolean isCellEditable(int y, int x)
    {
        return false;
    }

    public void setValueAt(Object value, int y, int x)
    {
        ClasspathConfig cConfig = (ClasspathConfig) rows.get(y);

        log.debug("value=" + value) ;
        
        if (x == 0)
        {
            cConfig.setJar((String) value);
        }
        else
        {
            cConfig.setNoFactories(((Boolean) value).booleanValue()) ;
        }

        fireTableRowsUpdated(y, y) ;
    }

    public void removeRows(int[] r)
    {
    	Object[] objects = new Object[r.length] ;
    	
    	for (int i = 0 ; i < r.length ; i++)
    	{
    		objects[i] = rows.get(r[i]) ;
    	}
    	
    	for (int i = 0 ; i < objects.length ; i++)
    	{
    		rows.remove(objects[i]) ;
    	}

    	fireTableDataChanged() ;
    }
    
    public void removeRow(int y)
    {
        Object element = rows.remove(y);

        fireTableRowsDeleted(y, y);       
    }

    public void addItem(ClasspathConfig cConfig)
    {
        if (rows.indexOf(cConfig.getJar()) == -1)
        {
            rows.add(cConfig);        
            fireTableRowsInserted(rows.size() -1, rows.size()) ;
        }        
    }
    
    public Class getColumnClass(int columnIndex)
    {
       if (columnIndex == 0)
       {
           return String.class ;
       }
       else
       {
           return Boolean.class ;
       }
    }
}