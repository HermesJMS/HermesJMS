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

package hermes.fix;

import hermes.swing.TimedSwingRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

import com.jidesoft.grid.HierarchicalTableModel;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXSessionTableModel.java,v 1.4 2006/05/06 17:22:56 colincrist
 *          Exp $
 */

public class FIXSessionTableModel extends AbstractTableModel implements HierarchicalTableModel
{
   private static final Logger log = Logger.getLogger(FIXSessionTableModel.class);
   private final Vector<SessionKey> sessions = new Vector<SessionKey>();

   private final Map<SessionKey, FIXMessageTableModel> tableModels = new HashMap<SessionKey, FIXMessageTableModel>();
   private final TimedSwingRunner swingRunner = new TimedSwingRunner(100);

   public FIXSessionTableModel()
   {
      super();
   }

   public void close()
   {
      swingRunner.stop();
      //tableModels.clear();
   }

   @Override
   public Class<?> getColumnClass(int columnIndex)
   {
      return String.class;
   }

   @Override
   public String getColumnName(int column)
   {
      switch (column)
      {
         case 0:
            return "Initiator";
         case 1:
            return "Acceptor";
         case 2:
            return "Messages";
      }

      return "???";
   }

   public int getRowCount()
   {
      return sessions.size();
   }

   public int getColumnCount()
   {
      return 3;
   }

   public long addSession(SessionKey key)
   {
      sessions.add(key);

      fireTableRowsInserted(sessions.size() - 1, sessions.size());

      return sessions.size();
   }

   public SessionKey getSessionKey(int row)
   {
      return sessions.get(row);
   }

   public Object getValueAt(int rowIndex, int columnIndex)
   {
      if (rowIndex < sessions.size())
      {
         switch (columnIndex)
         {
            case 0:
               return sessions.get(rowIndex).getSenderCompID();
            case 1:
               return sessions.get(rowIndex).getTargetCompID();
            case 2:
               return sessions.get(rowIndex).getNumMessages();
         }
      }
      return "???";
   }

   public synchronized void addMessages(Collection<FIXMessage> messages)
   {
      final Map<SessionKey, Collection<FIXMessage>> sessions = new HashMap<SessionKey, Collection<FIXMessage>>();

      for (final FIXMessage message : messages)
      {
         final SessionKey key = new SessionKey(message.getString(SenderCompID.FIELD), message.getString(TargetCompID.FIELD));

         if (!sessions.containsKey(key))
         {
            sessions.put(key, new ArrayList<FIXMessage>());
         }

         sessions.get(key).add(message);
      }

      for (final SessionKey key : sessions.keySet())
      {
         final Collection<FIXMessage> sessionMessages = sessions.get(key);

         if (tableModels.containsKey(key))
         {
            tableModels.get(key).addMessages(sessionMessages);
         }
         else
         {
            log.debug("creating new table for session " + key);

            key.setSessionRole(SessionRole.INITIATOR);

            final FIXMessageTableModel tableModel = new FIXMessageTableModel(key);
            final SessionKey otherSide = new SessionKey(key.getTargetCompID(), key.getSenderCompID(), SessionRole.ACCEPTOR);

            tableModels.put(key, tableModel);
            tableModels.put(otherSide, tableModel);

            final long row = addSession(key);

            tableModel.addTableModelListener(new TableModelListener()
            {
               public void tableChanged(TableModelEvent e)
               {
                  Runnable r = new Runnable()
                  {
                     public void run()
                     {
                        if (tableModel.getRowCount() != key.getNumMessages())
                        {
                           key.setNumMessages(tableModel.getRowCount());
                           fireTableCellUpdated((int) row, 2);
                        }
                     }
                  };
                  swingRunner.invokeLater(tableModel, r);
               }
            });

            tableModel.addMessages(sessionMessages);
         }
      }
   }

   public synchronized Object getChildValueAt(int row)
   {
      if (row <= sessions.size())
      {
         SessionKey key = sessions.get(row);

         log.debug("getChildValueAt(" + row + ") returns table for " + key);

         return tableModels.get(key);
      }
      else
      {
         return null;
      }
   }

   public boolean hasChild(int row)
   {
      return true;
   }

   public boolean isExpandable(int row)
   {
      return true;
   }

   public boolean isHierarchical(int row)
   {
      return true;
   }
}
