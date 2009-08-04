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

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * The model for the actions table.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ActionsPanelTableModel.java,v 1.1 2004/07/21 19:46:13
 *          colincrist Exp $
 */

public class ActionsPanelTableModel extends AbstractTableModel
{
    private final String[] columnNames = { "", "", "Destination"};
    private final Vector rows = new Vector();

    public boolean isCellEditable(int row, int column)
    {
        return false;
    }

    public Class getColumnClass(int column)
    {
        return (getValueAt(0, column) != null) ? getValueAt(0, column).getClass() : null;
    }

    public String getColumnName(int column)
    {
        return null; // columnNames[column];
    }

    public Object getValueAt(int row, int col)
    {
        synchronized (rows)
        {
            return (row > rows.size()) ? null : ((Vector) rows.elementAt(row)).elementAt(col);
        }
    }

    public int getRowCount()
    {
        return rows.size();
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public void addRow(final Vector row)
    {
        synchronized (rows)
        {
            rows.add(row);
            fireTableDataChanged();
        }
    }

    public void removeRow(final Vector row)
    {
        synchronized (rows)
        {
            rows.remove(row);
            fireTableDataChanged();
        }
    }

    public Vector getRow(int i)
    {
        synchronized (rows)
        {
            return (i > rows.size()) ? null : (Vector) rows.elementAt(i);
        }
    }

    public Vector getDataVector()
    {
        return rows;
    }
}