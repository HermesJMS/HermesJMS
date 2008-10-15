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
import hermes.browser.model.ClasspathGroupTableModel;
import hermes.config.ClasspathGroupConfig;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jidesoft.grid.HierarchicalTable;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ClasspathGroupTable.java,v 1.3 2005/04/08 15:54:20 colincrist
 *          Exp $
 */
public class ClasspathGroupTable extends HierarchicalTable
{
   private static final Logger log = Logger.getLogger(ClasspathGroupTable.class);

   private ClasspathGroupTableModel model;
   private PreferencesDialog dialog;
   private MouseAdapter mouseAdapter;

   public ClasspathGroupTable(PreferencesDialog dialog, ClasspathGroupTableModel model)
   {
      super(model);
      this.dialog = dialog;
      this.model = model;

      setComponentFactory(model);

      init();
   }

   public ClasspathGroupTable(PreferencesDialog dialog)
   {
      this(dialog, null);
   }

   public ClasspathGroupTableModel getClasspathGroupTableModel()
   {
      return model;
   }

   public void setModel(TableModel model)
   {
      super.setModel(model);

      this.model = (ClasspathGroupTableModel) model;
      setComponentFactory((ClasspathGroupTableModel) model);
   }

   public MouseAdapter getMouseAdapter()
   {
      return mouseAdapter;
   }

   private void init()
   {
      final JPopupMenu popupMenu = new JPopupMenu();
      final JMenuItem addItem = new JMenuItem("Add Group");
      final JMenuItem removeItem = new JMenuItem("Remove Group");
      final JMenuItem renameItem = new JMenuItem("Rename");

      popupMenu.add(addItem);
      popupMenu.add(removeItem);
      popupMenu.add(renameItem);

      addItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent arg0)
         {
            try
            {
               doAddGroup();
               dialog.setDirty();
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
            if (getSelectedRowCount() > 0)
            {
               for (int row : getSelectedRows())
               {
                  getClasspathGroupTableModel().removeRow(row);
               }
               dialog.setDirty();
            }
         }
      });

      renameItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (getSelectedRow() != -1)
            {
               doRename();
            }
         }
      });

      mouseAdapter = new MouseAdapter()
      {
         public void mousePressed(MouseEvent e)
         {
            if (SwingUtilities.isRightMouseButton(e))
            {
               removeItem.setEnabled(getClasspathGroupTableModel().getRowCount() != 0);
               renameItem.setEnabled(getClasspathGroupTableModel().getRowCount() != 0);
               popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
         }
      };

      addMouseListener(mouseAdapter);
      getTableHeader().addMouseListener(mouseAdapter);

      if (dialog != null)
      {
         addPropertyChangeListener(new PropertyChangeListener()
         {
            public void propertyChange(PropertyChangeEvent evt)
            {
               //
               // Think this is ok, seems 2 do the job.

               if (evt.getPropertyName().equals("tableCellEditor"))
               {
                  dialog.setDirty();
               }
            }
         });
      }
   }

   public void doRename()
   {
      final ClasspathGroupConfig config = getClasspathGroupTableModel().getRow(getSelectedRow());
      final String name = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "New name:", config.getId());

      if (!config.getId().equals(name))
      {
         config.setId(name);
         dialog.setDirty();
      }
   }

   public void doAddGroup()
   {
      final String name = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Classpath group name:", "");

      if (!StringUtils.isEmpty(name))
      {
         final ClasspathGroupConfig config = new ClasspathGroupConfig();

         config.setId(name);
         getClasspathGroupTableModel().addRow(config);
         expandRow(getClasspathGroupTableModel().getRowCount() - 1) ;
      }
   }

}