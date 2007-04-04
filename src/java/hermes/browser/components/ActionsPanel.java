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

import hermes.browser.IconCache;
import hermes.browser.JTableButtonMouseListener;
import hermes.browser.JTableButtonRenderer;
import hermes.browser.model.ActionsPanelTableModel;
import hermes.browser.tasks.Task;
import hermes.browser.tasks.TaskListener;
import hermes.browser.tasks.TaskSupport;
import hermes.swing.SwingRunner;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.apache.log4j.Category;

/**
 * The panel that shows the list of currently running actions
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ActionsPanel.java,v 1.7 2005/05/19 16:16:10 colincrist Exp $
 */

public class ActionsPanel extends JTable implements TaskListener
{
    private static final long serialVersionUID = 1L;
    private static final Category cat = Category.getInstance(ActionsPanel.class);

    private final ActionsPanelTableModel actionsTableModel = new ActionsPanelTableModel();
    private final Map<Task, Vector> rowInfo = new HashMap<Task, Vector>();
    private final Map<Vector, Task> taskInfo = new HashMap<Vector, Task>();

    public ActionsPanel()
    {
        super();

        init();
        TaskSupport.addGlobalListener(this);
    }

    /**
     * Plumb all the Swing stuff together
     */

    public void init()
    {
        setModel(actionsTableModel);
        setDefaultRenderer(JButton.class, new JTableButtonRenderer(getDefaultRenderer(JButton.class)));

        addMouseListener(new JTableButtonMouseListener(this));

        getColumnModel().getColumn(0).setMaxWidth(20);
        getColumnModel().getColumn(1).setMaxWidth(20);
        setRowHeight(20);

        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if ( e.getClickCount() == 2)
                {
                    maybeChangeFocus(e);
                }
            }
        });
    }

    /**
     * If there is a double click on the row then switch that window to the
     * front
     */
    public void maybeChangeFocus(MouseEvent e)
    {

    }

    public void onStarted(final Task task)
    {
        SwingRunner.invokeLater(new Runnable()
        {
            public void run()
            {
                final Vector<Object> row = new Vector<Object>();
                final JButton stopButton = new JButton(IconCache.getIcon("hermes.stop"));
                
                stopButton.setToolTipText("Stop");

                row.add(task.getIcon());
                row.add(stopButton);
                row.add(task.getTitle());

                rowInfo.put(task, row);
                taskInfo.put(row, task);

                if ( task.isRunning())
                {
                    actionsTableModel.addRow(row);

                    stopButton.addMouseListener(new MouseAdapter()
                    {
                        public void mouseClicked(MouseEvent e)
                        {
                            task.stop();
                        }
                    });
                }
            }
        });
    }

    public void onStatus(Task task, String status)
    {

    }

    public void onStopped(final Task task)
    {
        SwingRunner.invokeLater(new Runnable()
        {
            public void run()
            {
                final Vector oldRow = (Vector) rowInfo.get(task);

                if ( task != null)
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            actionsTableModel.removeRow(oldRow);
                            rowInfo.remove(task);
                            taskInfo.remove(oldRow);
                        }
                    });
                }
            }
        });

    }

    public void onThrowable(Task task, Throwable t)
    {
        // TODO Auto-generated method stub
    }
}