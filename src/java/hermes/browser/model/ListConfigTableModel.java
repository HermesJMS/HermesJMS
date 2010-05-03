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

import java.util.Collection;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ListConfigTableModel.java,v 1.1 2004/07/21 19:46:13 colincrist
 *          Exp $
 */

public class ListConfigTableModel extends AbstractTableModel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 673479572522683550L;
	private Vector rows = new Vector();

    public ListConfigTableModel()
    {

    }

    public Collection getRows()
    {
        return rows;
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
        return rows.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int arg0, int arg1)
    {
        return rows.elementAt(arg0);
    }

    public boolean isCellEditable(int y, int x)
    {
        return true;
    }

    public void setValueAt(Object value, int y, int x)
    {
        String currentValue = (String) rows.elementAt(y);

        rows.setElementAt(value, y);
        fireTableDataChanged();
    }
    
    public void moveRowUp(int y) 
    {
        if (rows.size() > 1 && rows.size() -1  !=  y)
        {
            Object row = rows.remove(y) ;
            rows.insertElementAt(row, y+1) ;
            fireTableDataChanged();
        }
    }
    
    public void moveRowDown(int y)
    {
        if (rows.size() > 1 && y != 0)
        {
            Object row = rows.remove(y) ;
            rows.insertElementAt(row, y-1) ;  
            fireTableDataChanged();
        }
    }

    public void removeRow(int y)
    {
        Object element = rows.elementAt(y);

        rows.remove(y);
        fireTableDataChanged();
    }

    public void addItem(String name)
    {
        if (rows.indexOf(name) == -1)
        {
            rows.add(name);

            fireTableDataChanged();
        }
    }

    public TableColumnModel getColumnModel()
    {
        DefaultTableColumnModel m = new DefaultTableColumnModel();
        TableColumn c = new TableColumn();

        m.addColumn(c);

        return m;

    }

}