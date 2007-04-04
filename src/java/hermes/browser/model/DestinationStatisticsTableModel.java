package hermes.browser.model;

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

import hermes.Hermes;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.Vector;

import javax.jms.JMSException;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationStatisticsTableModel.java,v 1.1 2004/07/21 19:46:13
 *          colincrist Exp $
 */

public class DestinationStatisticsTableModel extends AbstractTableModel
{
    private static class Statistics
    {
        public String destination;
        public int messagesRead;
        public int maxRate;
        public int messagesReadInPeriod;
        public long startTime = System.currentTimeMillis();
    }

    private Map stats = new TreeMap();
    private Hermes hermes;
    private Vector rows = new Vector();
    private TimerTask updateTask;
    private Timer timer;
    private boolean keepRunning = true;
    private static final String[] columns = { "Destination", "Total Read", "Max (per second)"};

    public DestinationStatisticsTableModel(Hermes hermes, Timer timer)
    {
        this.timer = timer;
        this.hermes = hermes;

    }

    public String getColumnName(int column)
    {
        return columns[column];
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
        return stats.size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return 3;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public synchronized Object getValueAt(int arg0, int arg1)
    {

        Statistics row = (Statistics) rows.elementAt(arg0);

        if (row != null)
        {
            switch (arg1)
            {
                case 0:
                    return row.destination;

                case 1:
                    return new Integer(row.messagesRead);

                case 2:
                    return new Integer(row.maxRate);

            }
        }

        return null;
    }

    public synchronized void onMessage(javax.jms.Message m)
    {
        if (updateTask == null)
        {
            updateTask = new TimerTask()
            {
                public void run()
                {
                    DestinationStatisticsTableModel.this.run();
                }
            };

            timer.schedule(updateTask, 1000, 1000);
        }

        try
        {
            Statistics destinationStats = null;

            if (m.getJMSDestination() != null)
            {
                if (!stats.containsKey(m.getJMSDestination().toString()))
                {
                    destinationStats = new Statistics();
                    destinationStats.destination = hermes.getDestinationName(m.getJMSDestination());

                    stats.put(m.getJMSDestination().toString(), destinationStats);

                    rows.clear();

                    for (Iterator iter = stats.keySet().iterator(); iter.hasNext();)
                    {
                        rows.add(stats.get(iter.next()));
                    }
                }
                else
                {
                    destinationStats = (Statistics) stats.get(m.getJMSDestination().toString());
                }

                destinationStats.messagesReadInPeriod++;
            }
        }
        catch (JMSException ex)
        {

        }
    }

    public void run()
    {
        for (int i = 0; i < rows.size(); i++)
        {
            Statistics row = (Statistics) rows.elementAt(i);

            if (row.messagesReadInPeriod > 0)
            {
                if (row.messagesReadInPeriod > row.maxRate)
                {
                    row.maxRate = row.messagesReadInPeriod;
                }

                row.messagesRead += row.messagesReadInPeriod;
                row.messagesReadInPeriod = 0;

            }
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                fireTableDataChanged();
            }
        });

        if (!keepRunning)
        {
            updateTask.cancel();
        }
    }

    public void stop()
    {
        keepRunning = false;
    }

}