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

import hermes.HermesRuntimeException;
import hermes.browser.components.FitScrollPane;
import hermes.impl.DestinationConfigKeyWrapper;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.grid.HierarchicalTableComponentFactory;
import com.jidesoft.grid.HierarchicalTableModel;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TreeLikeHierarchicalPanel;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: QueueWatchTableModel.java,v 1.2 2004/07/30 17:25:14 colincrist
 *          Exp $
 */
public class QueueWatchTableModel extends DefaultTableModel implements HierarchicalTableModel, PropertyChangeListener, HierarchicalTableComponentFactory
{
    private static final Logger log = Logger.getLogger(QueueWatchTableModel.class);
    public static final int HERMES_POS = 0;
    public static final int DESTINATION_POS = 1;
    public static final int DURABLE_POS = 2 ;
    public static final int DEPTH_POS = 3;
    public static final int OLDEST_POS = 4;
    private Map<DestinationConfigKeyWrapper, WatchInfo> watchInfoByKey = new HashMap<DestinationConfigKeyWrapper, WatchInfo>();
    private Map childComponentByWatchInfo = new HashMap();

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return 5;
    }

    public WatchInfo getRowByKey(DestinationConfigKeyWrapper key)
    {
        if (watchInfoByKey.containsKey(key))
        {
            return (WatchInfo) watchInfoByKey.get(key);
        }
        else
        {
            throw new HermesRuntimeException("cannot find WatchInfo key=" + key + " in set " + watchInfoByKey.keySet()) ;
        }
    }

    public void removeAll()
    {
        dataVector.clear();
        watchInfoByKey.clear();

        fireTableDataChanged();
    }

    public synchronized void addRow(WatchInfo info)
    {
        dataVector.add(info);
        watchInfoByKey.put(info.getKey(), info);

        fireTableRowsInserted(dataVector.size() - 1, dataVector.size());
        info.addPropertyChangeListener(this);
    }

    public void removeRow(WatchInfo info)
    {
        int index = dataVector.indexOf(info);

        if ( index > 0)
        {
            dataVector.remove(info);

            fireTableRowsDeleted(index, index + 1);
        }

        watchInfoByKey.remove(info.getKey());
        childComponentByWatchInfo.remove(info);
        info.removePropertyChangeListener(this);
    }

    public void fireRowChanged(WatchInfo info)
    {
        int index = dataVector.indexOf(info);

        log.debug("row " + index + " changed");

        fireTableRowsUpdated(index, index);
    }

    public WatchInfo getRow(int row)
    {
        if ( row >= dataVector.size())
        {
            return null;
        }
        else
        {
            return (WatchInfo) dataVector.elementAt(row);
        }
    }

    public Iterator iterator()
    {
        return dataVector.iterator();
    }

    public Collection rows()
    {
        return dataVector;
    }
    
    public Collection getDestinationWatchConfigs()
    {
        ArrayList rval = new ArrayList() ;
        
        for (Iterator iter = dataVector.iterator(); iter.hasNext();)
        {
            WatchInfo info = (WatchInfo) iter.next();
           
            rval.add(info.getConfig()) ;
        }
        
        return rval ;
    }

    public WatchInfo findWatchInfo(String hermesId, String destinationName)
    {
        for (Iterator iter = dataVector.iterator(); iter.hasNext();)
        {
            WatchInfo info = (WatchInfo) iter.next();

            if ( info.getHermesId().equals(hermesId) && info.getConfig().getName().equals(destinationName))
            {
                return info;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int y, int x)
    {
        synchronized (this)
        {
            WatchInfo info = (WatchInfo) dataVector.elementAt(y);

            switch (x)
            {
            case HERMES_POS:
                return info.getHermesId();

            case DESTINATION_POS:
                return info.getConfig().getName();
                
            case DURABLE_POS:
               return info.getConfig().isDurable() ? info.getConfig().getClientID() : "N/A" ;

            case DEPTH_POS:
                return new Integer(info.getDepth());

            case OLDEST_POS:
                if ( info.getOldest() == 0)
                {
                    return null;
                }
                else
                {
                    return new Date(info.getOldest());
                }
            }

            return "Unknown";
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int y, int arxg1)
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return dataVector.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    public Class getColumnClass(int column)
    {
        switch (column)
        {
        case HERMES_POS:
            return String.class;
        case DESTINATION_POS:
            return String.class;
        case DURABLE_POS:
           return String.class ;
        case DEPTH_POS:
            return Integer.class;
        case OLDEST_POS:
            return Date.class;
        }

        return Object.class;
    }

    private JTable createTable(WatchInfo info)
    {
        SortableTable table = new SortableTable()
        {
            public boolean isCellEditable(int arg0, int arg1)
            {
                return false;
            }
        };

        final Map statistics = info.getStatistics() ;
        final MapTableModel model = statistics.size() > 10 ?  new KeyValueMapTableModel(statistics) : new OneRowMapTableModel(statistics); 
        
        info.addPropertyChangeListener(WatchInfo.STATISTICS, new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                if ( evt.getNewValue() != null && evt.getNewValue() instanceof Map)
                {
                    model.setMap((Map) evt.getNewValue());
                }
            }
        });

        table.setModel(model);
        table.setSortable(true) ;
        return table;
    }

    /**
     * Gets the statistics table for the given row, its created lazily and
     * cachd.
     */
    public synchronized Object getChildValueAt(int y)
    {
        return getRow(y);

    }

    public Component createChildComponent(HierarchicalTable arg0, Object object, int row)
    {
        WatchInfo info = (WatchInfo) object;
        JComponent rval;

        if ( !childComponentByWatchInfo.containsKey(info))
        {
            rval = new TreeLikeHierarchicalPanel(new FitScrollPane(createTable(info)));

            childComponentByWatchInfo.put(info, rval);
        }
        else
        {
            rval = (JComponent) childComponentByWatchInfo.get(info);
        }

        return rval;
    }

    public void destroyChildComponent(HierarchicalTable arg0, Component arg1, int arg2)
    {
        // TODO Auto-generated method stub

    }

    public boolean hasChild(int y)
    {
        return true;
    }

   public boolean isExpandable(int row) 
    {
      return isHierarchical(row) ;
    }
   
    public boolean isHierarchical(int y)
    {
        return true;
    }
    
    public boolean hasAlert()
    {
        /*
         * @@TODO this is terribly inefficient if ever anyone watches a large number of queues!
         */
        for (Iterator  iter = watchInfoByKey.entrySet().iterator() ; iter.hasNext() ;)
        {
            Map.Entry entry = (Map.Entry) iter.next() ;
            WatchInfo info = (WatchInfo) entry.getValue() ;
            
            if (info.isInAlert())
            {
                return true ;
            }
        }
        
        return false ;
    }

    public void propertyChange(PropertyChangeEvent event)
    {
        final WatchInfo watchInfo = (WatchInfo) watchInfoByKey.get((DestinationConfigKeyWrapper) event.getSource());

        if ( watchInfo != null)
        {
            if ( event.getPropertyName().equals(WatchInfo.STATISTICS))
            {
                // NOP, listener registered in getChildComponent().
            }
            else
            {
                log.debug("property=" + event.getPropertyName() + " old=" + event.getOldValue() + " new=" + event.getNewValue());
                fireRowChanged(watchInfo);
            }
        }
        else
        {
            log.error("propertyChange: cannot find WatchInfo key= " + event.getSource());
        }
    }
}