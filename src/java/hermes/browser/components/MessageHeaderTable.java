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

package hermes.browser.components;

import hermes.HermesRuntimeException;
import hermes.browser.actions.BrowserAction;
import hermes.browser.model.MessageHeaderTableModel;
import hermes.swing.HideableTableColumn;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Category;

import com.jidesoft.grid.JideTable;
import com.jidesoft.grid.SortableTable;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageHeaderTable.java,v 1.3 2004/07/30 17:25:13 colincrist
 *          Exp $
 */

public class MessageHeaderTable extends SortableTable
{
   private static final Category cat = Category.getInstance(MessageHeaderTable.class);

   private DataFlavor[] myFlavours;
   private Map<String, HideableTableColumn> userPropertyColumns = new HashMap<String, HideableTableColumn>();

   public MessageHeaderTable(BrowserAction action, MessageHeaderTableModel model)
   {
      super(model);

      

      MessageHeaderTableSupport.init(action, this, myFlavours);

      setDragEnabled(true);
      

      /* TODO: Finish support for adding user properties to the table.
      model.addTableModelListener(new TableModelListener()
      {
         public final void tableChanged(TableModelEvent e)
         {
            if (e.getType() == TableModelEvent.INSERT)
            {
               for (int i = e.getFirstRow(); i < e.getLastRow(); i++)
               {
                  final MessageHeaderTableModel model = (MessageHeaderTableModel) e.getSource();
                  final Message message = model.getMessageAt(i);

                  checkForProperties(message);
               }
            }
         }
      });
      */
   }
  
   private void checkForProperties(Message message)
   {
      try
      {
         for (final Enumeration e = message.getPropertyNames(); e.hasMoreElements();)
         {
            String propertyName = (String) e.nextElement() ;
            
            if (!userPropertyColumns.containsKey(propertyName))
            {
               final HideableTableColumn column = new HideableTableColumn() ;
               column.setHeaderValue(propertyName) ;
               
               getColumnModel().addColumn(column) ;
               
               final MessageHeaderTableModel model = (MessageHeaderTableModel) getModel() ;
               // model.displayColumn(column) ;
            }
         }
      }
      catch (JMSException e)
      {
         throw new HermesRuntimeException(e);
      }
   }

   public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
   {
      return MessageHeaderTableSupport.prepareRenderer(super.prepareRenderer(renderer, row, column), this, renderer, row, column);
   }

   @Override
   public String getToolTipText(MouseEvent event)
   {
      return super.getToolTipText(event);
   }
}