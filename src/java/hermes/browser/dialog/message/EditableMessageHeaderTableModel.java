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

import hermes.HermesRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: EditableMessageHeaderTableModel.java,v 1.2 2005/08/15 20:37:37
 *          colincrist Exp $
 */

public class EditableMessageHeaderTableModel implements TableModel
{
   private static final Logger log = Logger.getLogger(EditableMessageHeaderTableModel.class) ;
   private static final String[] JMS_PROPERTIES = { "JMSMessageID", "JMSType", "JMSReplyTo", "JMSCorrelationID", "JMSExpiration", "JMSPriority" };
   private Message message;

   private Vector<String> properties = new Vector<String>();
   private List<TableModelListener> listeners = new ArrayList<TableModelListener>();

   public EditableMessageHeaderTableModel(Message message) throws JMSException
   {
      this.message = message;

      for (String jmsProperty : JMS_PROPERTIES)
      {
         properties.add(jmsProperty);
      }

      for (Enumeration iter = message.getPropertyNames(); iter.hasMoreElements();)
      {
         String propertyName = (String) iter.nextElement();
         properties.add(propertyName);
      }
   }

   public int getRowCount()
   {
      return properties.size();
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
      try
      {
         switch (columnIndex)
         {
            case 0:
               return properties.get(rowIndex);
            case 1:
               return BeanUtils.getProperty(message, properties.get(rowIndex));
            case 2:
               final Object o = BeanUtils.getProperty(message, properties.get(rowIndex));
               if (o != null)
               {
                  return o.getClass().getName();
               }
               else
               {
                  return "Object.class";
               }
            default:
               return "Error";
         }
      }
      catch (Exception ex)
      {
         throw new HermesRuntimeException(ex);
      }
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
