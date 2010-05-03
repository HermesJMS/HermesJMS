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

import hermes.Domain;
import hermes.config.DestinationConfig;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationConfigTableModel.java,v 1.1 2004/07/21 19:46:13
 *          colincrist Exp $
 */

public class DestinationConfigTableModel extends DefaultTableModel
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 7156548642269559562L;
private static final Logger log = Logger.getLogger(DestinationConfigTableModel.class);
   private Vector rows = new Vector();
   private Map queues = new HashMap();
   private Map topics = new HashMap();

   public DestinationConfigTableModel(List dList)
   {
      addColumn("Name");
      addColumn("ShortName");
      addColumn("Domain");

      for (Iterator iter = dList.iterator(); iter.hasNext();)
      {
         final DestinationConfig dConfig = (DestinationConfig) iter.next();

         addItem(dConfig);
      }
   }

   public DestinationConfig getConfig(String name, Domain domain)
   {
      if (domain == Domain.QUEUE)
      {
         return (DestinationConfig) queues.get(name);
      }
      else
      {
         return (DestinationConfig) topics.get(name);
      }
   }

   public boolean hasConfig(String name)
   {
      return queues.containsKey(name) || topics.containsKey(name);
   }

   public Collection getRows()
   {
      return rows;
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
      if (rows != null)
      {
         return rows.size();
      }
      else
      {
         return 0;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableModel#getValueAt(int, int)
    */
   public Object getValueAt(int y, int x)
   {
      DestinationConfig dConfig = (DestinationConfig) rows.elementAt(y);

      switch (x)
      {
         case 0:
            return dConfig.getName();

         case 1:
            return dConfig.getShortName();

         case 2:
            if (dConfig.getDomain() == Domain.QUEUE.getId())
            {
               return "QUEUE";
            }
            else
            {
               return "TOPIC";
            }
         default:
            return null;

      }
   }

   public boolean isCellEditable(int y, int x)
   {
      return false;
   }

   public void setValueAt(Object value, int y, int x)
   {
      // NOP
   }

   public void removeRow(int y)
   {
      DestinationConfig dConfig = (DestinationConfig) rows.elementAt(y);

      rows.remove(dConfig);

      if (dConfig.getDomain() == (Domain.QUEUE.getId()))
      {
         queues.remove(dConfig);
      }
      else
      {
         topics.remove(dConfig);
      }

      fireTableDataChanged();
   }

   public DestinationConfig getRowConfig(int row)
   {
      return (DestinationConfig) rows.elementAt(row);
   }

   public void addItem(DestinationConfig dConfig)
   {
      if (dConfig != null && dConfig.getName() != null && !dConfig.getName().equals(""))
      {
         Map configs = dConfig.getDomain() == (Domain.QUEUE.getId()) ? queues : topics;

         if (configs.containsKey(dConfig.getName()))
         {
            configs.put(dConfig.getName(), dConfig);

            for (Iterator iter = rows.iterator(); iter.hasNext();)
            {
               DestinationConfig row = (DestinationConfig) iter.next();

               if (row.getName().equals(dConfig.getName()))
               {
                  iter.remove();
               }
            }

            rows.add(dConfig);
         }
         else
         {
            rows.add(dConfig);
            configs.put(dConfig.getName(), dConfig);
         }

         fireTableRowsInserted(rows.size(), rows.size()) ;
      }
   }

   public Collection getDestinations()
   {
      return rows;
   }

   public void refresh()
   {
      fireTableDataChanged() ;
   }
}