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

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: OneRowMapTableModel.java,v 1.2 2006/07/13 07:35:33 colincrist Exp $
 */
public class OneRowMapTableModel extends MapTableModel 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 4853546469858409225L;
	private static final Logger log = Logger.getLogger(OneRowMapTableModel.class) ;
    private Vector row = new Vector();
    private Vector headers = new Vector();

    /**
     * 
     */
    public OneRowMapTableModel(Map map)
    {
        super();

        setMap(map);
    }

    public void setMap(Map map)
    {
        row.clear();
        headers.clear();

        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iter.next();

            headers.add(entry.getKey()) ;

            if (entry.getValue() != null)
            {
                row.add(entry.getValue());
            }
            else
            {
                row.add("");
            }
        }

        fireTableStructureChanged() ;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        if (row == null)
        {
            return 0;
        }
        else
        {
            return row.size();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return 1;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int y, int x)
    {
        return row.elementAt(x);
    }

    public Class getColumnClass(int x)
    {
        return row.elementAt(x).getClass();
    }
    
    public String getColumnName(int arg0)
    {
        String columnName = (String) headers.elementAt(arg0) ;
 
        return  columnName ;
        
    }
}