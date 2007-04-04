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

import hermes.browser.HermesBrowser;
import hermes.browser.dialog.PreferencesDialog;
import hermes.browser.model.ClasspathConfigTableModel;
import hermes.browser.model.ClasspathGroupTableModel;
import hermes.config.ClasspathConfig;
import hermes.config.ClasspathGroupConfig;
import hermes.config.impl.ClasspathConfigImpl;
import hermes.swing.SwingUtils;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.jidesoft.grid.JideTable;

/**
 * @author cristco last changed by: $Author: colincrist $
 * @version $Id: ClasspathConfigTable.java,v 1.4 2004/09/12 10:50:05 colincrist
 *          Exp $
 */
public class ClasspathConfigTable extends JideTable
{
    private static final Logger log = Logger.getLogger(ClasspathConfigTable.class);
    private static final int COPY = 1;
    private static final int MOVE = 2;
    private ClasspathConfigTableModel model;
    private PreferencesDialog dialog;
    private File lastDirectory;
    private MouseAdapter mouseAdapter;
    private Collection groupConfigs;
    private ClasspathGroupTableModel parentModel;

    private class IdMenuListener implements MenuListener
    {
        private JMenu menu;
        int action;

        private IdMenuListener(JMenu menu, int action)
        {
            this.menu = menu;
            this.action = action;
        }

        public void menuCanceled(MenuEvent e)
        {
            // NOP
        }

        public void menuDeselected(MenuEvent e)
        {
            // NOP
        }

        public void menuSelected(MenuEvent e)
        {
            menu.removeAll();

            for (Iterator iter = groupConfigs.iterator(); iter.hasNext();)
            {
                final ClasspathGroupConfig config = (ClasspathGroupConfig) iter.next();
                final JMenuItem item = new JMenuItem(config.getId());
                menu.add(item);

                item.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        int rows[] = getSelectedRows();

                        for (int i = 0; i < rows.length; i++)
                        {
                            int row = rows[i];

                            ClasspathConfig cpConfig = model.getRowAt(row);

                            switch (action)
                            {
                            case COPY:
                                copy(cpConfig, config);
                                break;

                            case MOVE:
                                copy(cpConfig, config);
                                model.removeRow(row);
                                break;
                            }
                        }
                    }
                });
            }
        }
    }

    public ClasspathConfigTable(ClasspathGroupTableModel parentModel, List groupConfigs, PreferencesDialog dialog, ClasspathConfigTableModel model)
    {
        this.model = model;
        this.dialog = dialog;
        this.groupConfigs = groupConfigs;
        this.parentModel = parentModel;

        setModel(model);
        init();
    }

    private void init()
    {
        final JPopupMenu popupMenu = new JPopupMenu();
        final JMenuItem addItem = new JMenuItem("Add JAR(s)");
        final JMenuItem removeItem = new JMenuItem("Remove JAR(s)");
        final JMenu copyItem = new JMenu("Copy to");
        final JMenu moveTo = new JMenu("Move to");

        popupMenu.add(addItem);
        popupMenu.add(removeItem);
        popupMenu.add(copyItem);
        popupMenu.add(moveTo);

        copyItem.addMenuListener(new IdMenuListener(copyItem, COPY));
        moveTo.addMenuListener(new IdMenuListener(moveTo, MOVE));

        addItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                try
                {
                    doAddJAR();
                }
                catch (Exception ex)
                {
                    log.error(ex.getMessage(), ex);
                }
            }
        });

        removeItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                if (getSelectedRowCount() != -1)
                {
                   int[] rows = getSelectedRows() ;
                   
                    for (int i = 0; i < rows.length; i++)
                    {
                        model.removeRow(rows[i]);
                    }
                    
                    dialog.setDirty();
                }
            }
        });

        mouseAdapter = new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                if ( SwingUtilities.isRightMouseButton(e))
                {
                    removeItem.setEnabled(model.getRowCount() != 0);

                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };

        addMouseListener(mouseAdapter);
        getTableHeader().addMouseListener(mouseAdapter);

        if ( dialog != null)
        {
            addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent evt)
                {
                    //
                    // Think this is ok, seems 2 do the job.

                    if ( evt.getPropertyName().equals("tableCellEditor"))
                    {
                        dialog.setDirty();
                    }
                }
            });
        }
        
        setAutoscrolls(true) ;
    }

    private void copy(ClasspathConfig config, ClasspathGroupConfig target)
    {
        ClasspathConfigTableModel toModel = parentModel.getChildModel(target);

        try
        {
            toModel.addItem(HermesBrowser.getConfigDAO().duplicate(config));
        }
        catch (JAXBException e)
        {
            HermesBrowser.getBrowser().showErrorDialog(e);
        }

    }

    public MouseAdapter getMouseAdapter()
    {
        return mouseAdapter;
    }
    
    public void scrollRectToVisible(Rectangle aRect) {
       SwingUtils.scrollRectToVisible(this, aRect);
   }


    public void doAddJAR()
    {
        JFileChooser fileChooser;

        if ( lastDirectory != null)
        {
            fileChooser = new JFileChooser(lastDirectory);
        }
        else
        {
            fileChooser = new JFileChooser();
        }

        fileChooser.setMultiSelectionEnabled(true);

        if ( fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION)
        {
            final Object options[] = { "Scan", "Don't scan" };
            final int n = JOptionPane
                    .showOptionDialog(
                            HermesBrowser.getBrowser(),
                            "Hermes will scan the JAR(s) for JMS connection factories when you confirm this dialog.\nYou only need to do this if you're not using JNDI and it can take some time if the library is very large.\nIf using BEA WebLogic select \"Don't scan\"",
                            "Please choose", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            final File[] files = fileChooser.getSelectedFiles();

            for (int i = 0; i < files.length; i++)
            {
               final ClasspathConfig lConfig = new ClasspathConfigImpl();

                lastDirectory = new File(files[i].getPath());

                lConfig.setJar(files[i].getAbsolutePath());

                if ( n == JOptionPane.YES_OPTION)
                {
                    lConfig.setNoFactories(false);
                }
                else
                {
                    lConfig.setNoFactories(true);
                }

                model.addItem(lConfig);     
                
            }

            dialog.setDirty();
        }
    }
}