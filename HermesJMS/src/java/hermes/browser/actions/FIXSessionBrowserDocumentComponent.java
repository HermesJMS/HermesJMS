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

package hermes.browser.actions;

import hermes.HermesRuntimeException;
import hermes.browser.components.FitScrollPane;
import hermes.fix.FIXMessageTable;
import hermes.fix.FIXMessageTableModel;
import hermes.fix.FIXSessionTable;
import hermes.fix.FIXSessionTableModel;
import hermes.fix.SessionKey;
import hermes.fix.quickfix.QuickFIXMessageCache;
import hermes.swing.ProxyListSelectionModel;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import com.codestreet.selector.parser.InvalidSelectorException;
import com.jidesoft.document.DocumentComponentListener;
import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.grid.HierarchicalTableComponentFactory;
import com.jidesoft.grid.ListSelectionModelGroup;
import com.jidesoft.grid.TreeLikeHierarchicalPanel;

/**
 * HermesAction to perform the browse of a queue or topic.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FIXBrowserDocumentComponent.java,v 1.1 2006/04/30 12:55:05
 *          colincrist Exp $
 */

public class FIXSessionBrowserDocumentComponent extends AbstractFIXBrowserDocumentComponent implements HierarchicalTableComponentFactory, FilterableAction
{
   private static final Logger log = Logger.getLogger(FIXSessionBrowserDocumentComponent.class);
   private boolean firstMessage = true;
   private ListSelectionEvent lastSelected;
   private final ListSelectionModelGroup listSelectionGroup = new ListSelectionModelGroup();
   private final Map<SessionKey, FIXMessageTable> tables = new HashMap<SessionKey, FIXMessageTable>();

   private FIXSessionTable sessionTable;
   private FIXSessionTableModel sessionTableModel = new FIXSessionTableModel();
 private ListSelectionListener sessionTableListSelectionListener ;
 private TableModelListener sessionTableModelListener ;
   private JPopupMenu popup;
   private Map<FIXMessageTable, ListSelectionListener> childTableListSelectionListeners = new HashMap<FIXMessageTable, ListSelectionListener> () ;
   

   private SessionKey selectedSessionKey;
   private ProxyListSelectionModel proxySelectionModel = new ProxyListSelectionModel();
   private QuickFIXMessageCache messageCache = new QuickFIXMessageCache();

   public FIXSessionBrowserDocumentComponent(String title)
   {
      super(title);

      sessionTable = new FIXSessionTable(sessionTableModel);
      sessionTable.setComponentFactory(this);

      init();
   }

   public ListSelectionModel getListSelectionModel()
   {
      return proxySelectionModel;
   }

   public boolean isNavigableForward()
   {
      if (selectedSessionKey != null)
      {
         FIXMessageTable table = tables.get(selectedSessionKey);
         return table.getSelectedRow() < table.getRowCount() - 1;
      }
      else
      {
         return false;
      }
   }

   public boolean isNavigableBackward()
   {
      if (selectedSessionKey != null)
      {
         FIXMessageTable table = tables.get(selectedSessionKey);
         return table.getSelectedRow() > 0 && table.getRowCount() > 1;
      }
      else
      {
         return false;
      }
   }

   public void navigateBackward()
   {
      if (selectedSessionKey != null)
      {
         FIXMessageTable table = tables.get(selectedSessionKey);
         int currentRow = table.getSelectedRow();

         table.getSelectionModel().setSelectionInterval(currentRow - 1, currentRow - 1);
      }
   }

   public void navigateForward()
   {
      if (selectedSessionKey != null)
      {
         FIXMessageTable table = tables.get(selectedSessionKey);
         final int currentRow = table.getSelectedRow();

         table.getSelectionModel().setSelectionInterval(currentRow + 1, currentRow + 1);
      }
   }

   public Collection<Object> getSelectedMessages()
   {
      if (selectedSessionKey != null)
      {
         FIXMessageTable table = tables.get(selectedSessionKey);
         return table.getSelectedMessages();
      }
      else
      {
         return Collections.EMPTY_LIST;
      }
   }

   @Override
   protected void doClose()
   {
      super.doClose();  
      
      sessionTable.getModel().removeTableModelListener(sessionTableModelListener) ;
      sessionTable.getSelectionModel().removeListSelectionListener(sessionTableListSelectionListener) ;
      
      for (Map.Entry<FIXMessageTable, ListSelectionListener> entry : childTableListSelectionListeners.entrySet())        
      {
         listSelectionGroup.remove(entry.getKey().getSelectionModel()) ;
         
         entry.getKey().getSelectionModel().removeListSelectionListener(entry.getValue()) ;
      }
      
      sessionTable.close();
      
      sessionTable = null;

      messageCache.close();

      for (DocumentComponentListener l : getDocumentComponentListeners())
      {
         removeDocumentComponentListener(l);
      }
   }

   public void decrementSelection()
   {
      int currentRow = sessionTable.getSelectedRow();

      sessionTable.getSelectionModel().setSelectionInterval(currentRow - 1, currentRow - 1);
   }

   public void incrementSelection()
   {
      final int currentRow = sessionTable.getSelectedRow();

      sessionTable.getSelectionModel().setSelectionInterval(currentRow + 1, currentRow + 1);
   }

   public boolean hasSelection()
   {
      return sessionTable.getSelectedRowCount() > 0;
   }

   public void setSelector(String selector) throws InvalidSelectorException
   {
      for (FIXMessageTable table : tables.values())
      {
         table.setSelector(selector);
      }
   }

   public synchronized Component createChildComponent(HierarchicalTable table, Object value, int row)
   {
      final FIXMessageTableModel model = (FIXMessageTableModel) value;

      if (model == null)
      {
         throw new HermesRuntimeException("No table");
      }

      FIXMessageTable childTable = null;
      SessionKey sessionKey = sessionTableModel.getSessionKey(row);

      if (tables.containsKey(sessionKey))
      {
         childTable = tables.get(sessionKey);
      }
      else
      {
         childTable = new FIXMessageTable(sessionKey, model);
         tables.put(sessionTableModel.getSessionKey(row), childTable);

         final FIXMessageTable fChildTable = childTable;

         proxySelectionModel.add(childTable.getSelectionModel());

         ListSelectionListener childTableListSelectionListener = new ListSelectionListener()
         {
            public void valueChanged(ListSelectionEvent e)
            {
               doSelectionChanged(fChildTable, e);
            }
         } ;
         
         
         childTable.getSelectionModel().addListSelectionListener(childTableListSelectionListener);
         childTableListSelectionListeners.put(childTable, childTableListSelectionListener) ;         
      }

      final FitScrollPane fitPane = new FitScrollPane(childTable);
      final TreeLikeHierarchicalPanel hPanel = new TreeLikeHierarchicalPanel(fitPane);

      listSelectionGroup.add(childTable.getSelectionModel());

      return hPanel;
   }

   public void doSelectionChanged(FIXMessageTable table, ListSelectionEvent e)
   {
      super.doSelectionChanged(table, e);

      //
      // Hmm.
   }

   public synchronized void destroyChildComponent(HierarchicalTable table, Component component, int row)
   {
      final FIXMessageTable childTable = (FIXMessageTable) tables.get(sessionTableModel.getSessionKey(row));

      listSelectionGroup.remove(childTable.getSelectionModel());
      proxySelectionModel.remove(childTable.getSelectionModel());
   }

   @Override
   protected Component getHeaderComponent()
   {
      return sessionTable;
   }

   protected void init()
   {
      super.init();

      sessionTableModelListener = new TableModelListener()
      {
         public void tableChanged(TableModelEvent e)
         {
            if (e.getType() == TableModelEvent.INSERT)
            {
               log.debug("faking createChildComponent for row " + e.getFirstRow());
               createChildComponent(sessionTable, sessionTable.getChildValueAt(e.getFirstRow()), e.getFirstRow());
            }
         }
      } ;
      
      sessionTable.getModel().addTableModelListener(sessionTableModelListener);

      sessionTableListSelectionListener = new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            if (sessionTable.getSelectedRow() >= 0 && sessionTable.getSelectedRow() < sessionTable.getRowCount())
            {
               selectedSessionKey = sessionTable.getSessionKey(sessionTable.getSelectedRow());
               proxySelectionModel.forward(e);
            }
         }
      } ;
      
      sessionTable.getSelectionModel().addListSelectionListener(sessionTableListSelectionListener);
   }

   /**
    * Called by the timer, it will update the UI with the new rows of consumes
    * messages, updating the status panels accordingly. Returns true if the
    * action is still running, otherwise false, allowing the timer to switch
    * itself off.
    */

   protected void updateTableRows(final boolean reschedule)
   {
      if (sessionTable == null)
      {
         return;
      }

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            synchronized (getCachedRows())
            {
               if (sessionTable != null)
               {
                  sessionTable.addMessages(getCachedRows());

                  if (firstMessage)
                  {
                     sessionTable.getSelectionModel().setSelectionInterval(0, 0);
                     firstMessage = false;
                  }

                  setCachedRows(new ArrayList());
               }
            }

            StringBuffer buffer = new StringBuffer();

            if (!reschedule || isTaskStopped())
            {
               buffer.append("Finished. ");
            }
            else
            {

               switch (sessionTableModel.getRowCount())
               {
                  case 0:
                     buffer.append("No sessions found.");
                     break;
                  case 1:
                     buffer.append("1 session found.");
                     break;

                  default:
                     buffer.append(sessionTableModel.getRowCount()).append(" sessions found.");
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

   public QuickFIXMessageCache getMessageCache()
   {
      return messageCache;
   }

}