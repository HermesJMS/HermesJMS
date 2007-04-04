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

import java.util.Map;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: TypedMapTableModel.java,v 1.1 2005/09/20 17:51:46 colincrist Exp $
 */
public class TypedMapTableModel extends AbstractTableModel
{
   private static final Logger log = Logger.getLogger(TypedMapTableModel.class);
   private static String[] header = { "Name", "Type", "Value" };

   private static class Row
   {
      String name;
      Object value;
      Class type;
   }

   private Vector<Row> rows = new Vector<Row>();
   private Class[] types;
   private Class defaultClass;

   /**
    * 
    */
   public TypedMapTableModel(Map<String, Object> map, Class[] types, Class defaultClass)
   {
      super();

      this.types = types;
      this.defaultClass = defaultClass;

      setMap(map);
   }

   public void setMap(Map<String, Object> map)
   {
      rows.clear();

      for (Map.Entry<String, Object> entry : map.entrySet())
      {
         Row row = new Row();

         row.name = entry.getKey();

         if (entry.getValue() != null)
         {
            row.value = entry.getValue();
            row.type = row.value.getClass();
         }
         else
         {
            row.value = null;
            row.type = defaultClass;
         }
      }
      fireTableStructureChanged();
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableModel#getColumnCount()
    */
   public int getColumnCount()
   {
      return 3;
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
      Row row = rows.elementAt(y);

      switch (x)
      {
         case 0:
            return row.name;
         case 1:
            return row.type ;
         case 2:
            return row.value;
         default:
            return null;
      }
   }

   @Override
   public void setValueAt(Object value, int y, int x)
   {
     if (y < rows.size())
     {
        Row row = rows.elementAt(y) ;
      
        switch (x)
        {
           case 0:
              row.name = (String) value ;
              break ;
           case 1:
              row.type = (Class) value ;
              break ;
           case 2:
              row.value = value ;
              break ;
              
        }
     }
   }

   public Class getColumnClass(int x)
   {
      return String.class ;
   }

   public String getColumnName(int x)
   {
      return header[x] ;
   }
}