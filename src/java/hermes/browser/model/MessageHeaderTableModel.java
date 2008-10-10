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

import hermes.Hermes;
import hermes.HermesException;
import hermes.HermesRuntimeException;
import hermes.browser.HermesBrowser;
import hermes.swing.HideableTableColumn;
import hermes.swing.RowValueProvider;
import hermes.util.JMSUtils;

import java.util.Date;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.codestreet.selector.jms.ValueProvider;
import com.codestreet.selector.parser.IValueProvider;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageHeaderTableModel.java,v 1.2 2004/07/26 19:03:36
 *          colincrist Exp $
 */

public class MessageHeaderTableModel extends DefaultTableModel implements RowValueProvider
{
   private static final Logger log = Logger.getLogger(MessageHeaderTableModel.class);
   private final Vector<Vector> rows = new Vector<Vector>();
   private final Vector<Message> messages = new Vector<Message>();
   private final Hermes hermes;
   private String destinationName;
   private Vector<HideableTableColumn> userProperties = new Vector<HideableTableColumn>();

   public MessageHeaderTableModel(Hermes hermes, String destinationName)
   {
      this.hermes = hermes;
      this.destinationName = destinationName;

      addColumn("#");
      addColumn("JMSMessageId");
      addColumn("JMSDestination");
      addColumn("JMSTimestamp");
      addColumn("JMSType");
      addColumn("JMSReplyTo");
      addColumn("JMSCorrelationID");
      addColumn("JMSExpiration");
      addColumn("JMSPriority");
   }

   public IValueProvider getValueProviderForRow(int row)
   {
     return ValueProvider.valueOf(messages.get(row)) ;
   }

   public Message getMessageAt(int row)
   {
      return (Message) messages.elementAt(row);
   }

   public void setDestinationName(String destinationName)
   {
      this.destinationName = destinationName;
   }

   public boolean isCellEditable(int row, int column)
   {
      return false;
   }

   public void clear()
   {
      rows.clear();
      messages.clear() ;

      fireTableDataChanged();
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableModel#getColumnCount()
    */
   public int getColumnCount()
   {
      return 9 + userProperties.size();
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

      return rows.size();
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableModel#getValueAt(int, int)
    */
   public Object getValueAt(int y, int x)
   {
      if (rows.size() > y)
      {
         Vector row = (Vector) rows.elementAt(y);

         return row.elementAt(x);
      }
      else
      {
         return null;
      }
   }

   public void removeFirstRow()
   {
      rows.remove(0);
      messages.remove(0);

      fireTableRowsDeleted(0, 1);
   }

   private Vector createRow(int messageIndex, Message message) throws JMSException
   {
      final Vector<Object> newRow = new Vector<Object>();

      newRow.add(new Long(messageIndex));
      newRow.add(message.getJMSMessageID());

      //
      // Some providers, e.g. WebSphereMQ, when dealing with messages put from
      // a non-JMS sender don't include the destination
      // on the message so if its not there we'll use the configured name.

      try
      {
      if (message.getJMSDestination() != null)
      {
         newRow.add(JMSUtils.getDestinationName(message.getJMSDestination()));
      }
      else
      {
         newRow.add(destinationName);
      }
      }
      catch (Exception ex)
      {
         log.error("cannot get JMSDestination: "+ ex.getMessage()) ;
         newRow.add("Unknown") ;
      }

      try
      {
         newRow.add(new Date(message.getJMSTimestamp())); 
      }
      catch (Exception ex)
      {
         newRow.add(new Date());

         log.error("no JMSTimestamp in message: " + ex.getMessage());
      }

      try
      {
         newRow.add(message.getJMSType());
      }
      catch (Exception ex)
      {
         log.error("no JMSType in message: " + ex.getMessage());

         newRow.add("");
      }

      try
      {
         if (message.getJMSReplyTo() != null)
         {
            newRow.add(JMSUtils.getDestinationName(message.getJMSReplyTo()));
         }
         else
         {
            newRow.add("");
         }
      }
      catch (Exception ex)
      {
         log.error("no JMSReplyTo in message: " + ex.getMessage());

         newRow.add("");
      }

      try
      {
         newRow.add(message.getJMSCorrelationID());
      }
      catch (Exception ex)
      {
         log.error("no JMSCorrelationID in message: " + ex.getMessage());

         newRow.add("");
      }

      try
      {
         newRow.add(new Long(message.getJMSExpiration()));
      }
      catch (Exception ex)
      {
         log.error("no JMSExpiration in message: " + ex.getMessage());

         newRow.add("");
      }
      
      try
      {
         newRow.add(message.getJMSPriority());
      }
      catch (Exception ex)
      {
         log.error("no JMSPriority in message: " + ex.getMessage());

         newRow.add("");
      }

      return newRow;
   }

   public void setFinalMessageIndex(int messageIndex)
   {
      try
      {
      // log.debug("setFinalMessageIndex, messageIndex=" + messageIndex);

      if (messageIndex == -1)
      {
         if (rows.size() > 0)
         {
            rows.clear();
            messages.clear();
            fireTableDataChanged();
         }
      }
      else if (messageIndex != rows.size() - 1 && (HermesBrowser.getBrowser().getMaxMessagesInBrowserPane() == -1 || messageIndex < HermesBrowser.getBrowser().getMaxMessagesInBrowserPane() ))
      {
         int lastRow = rows.size() - 1;

         while (rows.size() != messageIndex + 1)
         {
            if (rows.size() > 0)
            {
               rows.remove(rows.size() - 1);
               messages.remove(messages.size() - 1);
            }
            else
            {
               break ;
            }
         }

         fireTableDataChanged();
      }
      }
      catch (HermesException ex)
      {
         throw new HermesRuntimeException(ex) ;      
      }
   }

   public void addMessage(int messageIndex, Message message) throws JMSException
   {
      if (messageIndex < rows.size())
      {
         Message existingMessage = messages.elementAt(messageIndex);

         // log.debug("comparing " + existingMessage.getJMSMessageID() + " and "
         // + message.getJMSMessageID());

         if (existingMessage.getJMSMessageID().equals(message.getJMSMessageID()))
         {
            // log.debug("add message (exists) index=" + messageIndex + " id=" +
            // message.getJMSMessageID());

            // The same message in the same index position, ignore it...

            return;
         }
         else
         {
            // log.debug("add message (change) index=" + messageIndex + "
            // size()= " + rows.size() + " id=" + message.getJMSMessageID());

            rows.setElementAt(createRow(messageIndex, message), messageIndex);
            messages.setElementAt(message, messageIndex);
            fireTableRowsUpdated(messageIndex, messageIndex);

            return;
         }
      }
      else
      {
         // log.debug("add message (append) index=" + messageIndex + " id=" +
         // message.getJMSMessageID());
      }

      rows.add(createRow(messageIndex, message));
      messages.add(message);

      fireTableRowsInserted(messageIndex, messageIndex);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableModel#getColumnName(int)
    */
   public String getColumnName(int arg0)
   {
      switch (arg0)
      {
         case 0:
            return "#";
         case 1:
            return "JMSMessageID";
         case 2:
            return "JMSDestination";
         case 3:
            return "JMSTimestamp";
         case 4:
            return "JMSType";
         case 5:
            return "JMSReplyTo";
         case 6:
            return "JMSCorrelationID";
         case 7:
            return "JMSExpiration";
         case 8:
            return "JMSPriority" ;
      }

      return super.getColumnName(arg0);
   }

   public Class getColumnClass(int column)
   {
      switch (column)
      {
         case 0:
            return Long.class;
         case 1:
            return String.class;
         case 2:
            return String.class;
         case 3:
            return String.class;
         case 4:
            return String.class;
         case 5:
            return String.class;
         case 6:
            return String.class;
         case 7:
            return Long.class;
         case 8:
            return Integer.class ;
      }

      return super.getColumnClass(column);
   }

   public Object getChildValueAt(int row)
   {
      return getMessageAt(row);
   }
}