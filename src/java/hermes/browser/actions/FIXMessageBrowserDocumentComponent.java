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

package hermes.browser.actions;

import hermes.browser.components.NavigableComponent;
import hermes.fix.FIXMessage;
import hermes.fix.FIXMessageListener;
import hermes.fix.FIXMessageTable;
import hermes.fix.FIXMessageTableModel;
import hermes.fix.SessionKey;

import java.awt.Component;
import java.util.Collection;
import java.util.TimerTask;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.codestreet.selector.parser.InvalidSelectorException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXMessageBrowserDocumentComponent.java,v 1.5 2006/08/01 07:29:35 colincrist Exp $
 */

public class FIXMessageBrowserDocumentComponent extends AbstractFIXBrowserDocumentComponent implements FIXMessageListener, FilterableAction, NavigableComponent
{
   private final FIXMessageTableModel tableModel;
   private final FIXMessageTable table;
   private boolean firstMessage = true;

   public FIXMessageBrowserDocumentComponent(SessionKey sessionKey, String title)
   {
      super(title);

      this.tableModel = new FIXMessageTableModel(sessionKey);
      this.table = new FIXMessageTable(sessionKey, tableModel);

      init();
   }
   
   public ListSelectionModel getListSelectionModel()
   {
      return table.getSelectionModel() ;
   }
   
   public boolean isNavigableForward()
   {
      return table.getSelectedRow() < table.getRowCount() - 1 ;
   }
   
   public boolean isNavigableBackward()
   {
     return table.getSelectedRow() > 0 && table.getRowCount() > 1 ;
   }

   public void navigateBackward()
   {
      int currentRow =table.getSelectedRow();

      table.getSelectionModel().setSelectionInterval(currentRow - 1, currentRow - 1);
   }

   public void navigateForward()
   {
      final int currentRow = table.getSelectedRow();

      table.getSelectionModel().setSelectionInterval(currentRow + 1, currentRow + 1);    
   }
   
   public Collection<Object> getSelectedMessages() 
   {
      return table.getSelectedMessages() ;
   }
   
   public FIXMessageTable getTable()
   {
      return table ;
   }

   public void setSelector(String selector) throws InvalidSelectorException
   {
      table.setSelector(selector) ;
   }

   protected void init()
   {
      super.init();
      
      table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
      {      
         public void valueChanged(ListSelectionEvent e)
         {
            doSelectionChanged(table, e) ;
         }
      }) ;
   }

   @Override
   protected Component getHeaderComponent()
   {
      return table;
   }

   @Override
   protected void updateTableRows(final boolean reschedule)
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            synchronized (getCachedRows())
            {
              
                  table.addMessages(getCachedRows());

                  if (firstMessage)
                  {
                     table.getSelectionModel().setSelectionInterval(0, 0);
                     firstMessage = false;
                  }
               

               getCachedRows().clear();
            }

            StringBuffer buffer = new StringBuffer();

            if (!reschedule || isTaskStopped())
            {
               buffer.append("Finished. ");
            }
            else
            {
               switch (tableModel.getRowCount())
               {
                  case 0:
                     buffer.append("No messages found.");
                     break;
                  case 1:
                     buffer.append("1 message found.");
                     break;

                  default:
                     buffer.append(table.getRowCount()).append(" sessions found.");
               }
            }

            if (reschedule)
            {
               setStatusText(buffer.toString());
            }
            else
            {
               setStatusText("Finished. " + buffer.toString());
            }

            if (reschedule)
            {
               TimerTask timerTask = new TimerTask()
               {
                  @Override
                  public void run()
                  {
                     updateTableRows(true);
                  }
               };

               timer.schedule(timerTask, getScreenUpdateTimeout());
            }
         }
      });
   }

   public void onMessage(FIXMessage message)
   {
      synchronized (getCachedRows())
      {
         getCachedRows().add(message);
      }
   }
}
