/* 
 * Copyright 2003,2004,2005 Colin Crist
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

package hermes.browser.dialog.message;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: EditableMessageHeaderTableModel.java,v 1.2 2005/08/15 20:37:37
 *          colincrist Exp $
 */

public class EditableMessageHeaderTableModel implements TableModel
{
   private static class HeaderProperty
   {
      private String name;
      private Object value = null;
      private boolean changed = false;

      HeaderProperty(String name, Object value)
      {
         this.name = name;
         this.value = value;
      }

      HeaderProperty(String name)
      {
         this.name = name;
      }

      void setValue(Object value)
      {
         this.value = value;
         changed = true;
      }

      void setClass(Class clazz)
      {
         // this.clazz = clazz ;
      }

      void populate(Message message) throws JMSException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
      {
         if (changed)
         {
            message.setObjectProperty(name, value);
         }
      }
   }

   private Vector<HeaderProperty> rows = new Vector<HeaderProperty>();
   private List<TableModelListener> listeners = new ArrayList<TableModelListener>();

   public int getRowCount()
   {
      return rows.size();
   }

   public int getColumnCount()
   {
      return 3;
   }

   public String getColumnName(int columnIndex)
   {
      switch (columnIndex)
      {
         case 0:
            return "Name";
         case 1:
            return "Class";
         case 2:
            return "Value";
         default:
            return "Error";
      }
   }

   public Class<?> getColumnClass(int columnIndex)
   {
      return String.class;
   }

   public boolean isCellEditable(int rowIndex, int columnIndex)
   {
      return true;
   }

   public Object getValueAt(int rowIndex, int columnIndex)
   {
      return null;
   }

   public void setValueAt(Object aValue, int rowIndex, int columnIndex)
   {
      // TODO Auto-generated method stub

   }

   public void addTableModelListener(TableModelListener l)
   {
      // TODO Auto-generated method stub

   }

   public void removeTableModelListener(TableModelListener l)
   {
      // TODO Auto-generated method stub

   }

}
