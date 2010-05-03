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

import org.apache.commons.collections.list.TreeList;
import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: KeyValueMapTableModel.java,v 1.2 2006/07/13 07:35:33 colincrist Exp $
 */
public class KeyValueMapTableModel extends  MapTableModel 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2553024436665624466L;
	private static final Logger log = Logger.getLogger(KeyValueMapTableModel.class) ;
    private Map map ;
    private TreeList headers = new TreeList() ;

    /**
     * 
     */
    public KeyValueMapTableModel(Map map)
    {
        super();

        setMap(map);
    }

    public void setMap(Map map)
    {
        this.map = map ;
        headers.clear();

        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iter.next();

            headers.add(entry.getKey()) ;

        }

        fireTableStructureChanged() ;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return 2 ;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
       if (map != null)
       {
        return map.size() ;
       }
       else
       {
          return 0 ;
       }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int y, int x)
    {
        String key = (String) headers.get(y) ;
        
        if (x == 0)
        {
           return key ;
        }
        else
        {
           if (map.get(key) != null)
           {
              return map.get(key).toString() ;
           }
           else
           {
              return "null" ;
           }
        }
    }

    public Class getColumnClass(int x)
    {
        return String.class ;
    }
    
    public String getColumnName(int arg0)
    {
        if (arg0 == 0)
        {
           return "Property" ;
        }
        else
        {
           return "Value" ;
        }   
    }
}