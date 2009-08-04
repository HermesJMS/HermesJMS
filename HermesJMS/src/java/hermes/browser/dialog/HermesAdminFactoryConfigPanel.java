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

package hermes.browser.dialog;

import hermes.HermesAdminFactory;
import hermes.SingletonManager;
import hermes.browser.HermesBrowser;
import hermes.browser.model.PropertySetTableModel;
import hermes.config.ProviderExtConfig;
import hermes.impl.ClassLoaderManager;
import hermes.impl.ConfigDAO;
import hermes.impl.ConfigDAOImpl;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.xml.bind.JAXBException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import com.jidesoft.grid.SortableTable;
import com.jidesoft.swing.JideScrollPane;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: HermesAdminFactoryConfigPanel.java,v 1.1 2004/07/30 17:25:15
 *          colincrist Exp $
 */

public class HermesAdminFactoryConfigPanel extends JPanel
{
   private static final Logger log = Logger.getLogger(HermesAdminFactoryConfigPanel.class) ;
   private static final String ADMIN_FACTORY = "Plug In";
   private static final String REMOVE_PROPERTY = "Remove property";
   private static final String ADD_PROPERTY = "Add property";
   private static final Category cat = Category.getInstance(HermesAdminFactoryConfigPanel.class);

   private PreferencesDialog dialog;
   private ProviderExtConfig config;

   private SortableTable propertyTable = new SortableTable();
   private JComboBox afCombo = new JComboBox();
   private JideScrollPane propertyTableSP = new JideScrollPane();
   private JComboBox propertySelectionComboBox;

   private DefaultComboBoxModel cfComboModel = new DefaultComboBoxModel();
   private PropertySetTableModel propertyTableModel;
   private HermesAdminFactory bean;

   private JPopupMenu popupMenu = new JPopupMenu();
   private JMenuItem addItem = new JMenuItem(ADD_PROPERTY);
   private JMenuItem removeItem = new JMenuItem(REMOVE_PROPERTY);

   public HermesAdminFactoryConfigPanel(PreferencesDialog dialog)
   {
      this.dialog = dialog;

      init();
   }

   public void init()
   {
      final Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createTitledBorder(border, ADMIN_FACTORY));

      afCombo.setModel(cfComboModel);

      propertyTableSP.setViewportView(propertyTable);
      propertyTable.setSortable(true);
      add(afCombo, BorderLayout.NORTH);
      add(propertyTableSP, BorderLayout.CENTER);

      popupMenu.add(addItem);
      popupMenu.add(removeItem);

      addItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent arg0)
         {
            try
            {
               propertyTableModel.insertRow();
            }
            catch (Exception ex)
            {
               cat.error(ex.getMessage(), ex);
            }
         }
      });

      removeItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent arg0)
         {
            if (propertyTable.getSelectedRow() != -1)
            {
               propertyTableModel.removeRow(propertyTable.getSelectedRow());
            }
         }
      });

      final MouseAdapter m = new MouseAdapter()
      {
         public void mousePressed(MouseEvent e)
         {
            if (SwingUtilities.isRightMouseButton(e))
            {
               if (e.getComponent() == propertyTableSP)
               {
                  removeItem.setEnabled(false);
               }
               else
               {
                  removeItem.setEnabled(true);
               }

               if (propertySelectionComboBox.getModel().getSize() == 0)
               {
                  addItem.setEnabled(false);
               }
               else
               {
                  addItem.setEnabled(true);
               }

               popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
         }
      };

      propertyTableSP.addMouseListener(m);
      propertyTable.addMouseListener(m);
      propertyTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

      cfComboModel.addListDataListener(new ListDataListener()
      {
         public void contentsChanged(ListDataEvent arg0)
         {
            final String className = HermesBrowser.getConfigDAO().getAdminClassForPlugIn((String) cfComboModel.getSelectedItem());
            final ClassLoaderManager classLoaderManager = (ClassLoaderManager) SingletonManager.get(ClassLoaderManager.class);

            try
            {
               if (propertyTableModel != null)
               {
                  final ClassLoader classLoader = classLoaderManager.getClassLoader(dialog.getSelectedLoader());
                  Thread.currentThread().setContextClassLoader(classLoader);

                  bean = (HermesAdminFactory) classLoader.loadClass(className).newInstance();
                  propertyTableModel.setBean(bean);
                  updateCellEditor();
                  dialog.setDirty();
               }
            }
            catch (Throwable e)
            {
               HermesBrowser.getBrowser().showErrorDialog(
                     "Unable to locate this plugin.\nSelect the loader the JMS provider classes are in before choosing the plugin.");
               cfComboModel.setSelectedItem(ConfigDAO.DEFAULT_PLUGIN);
            }
         }

         public void intervalAdded(ListDataEvent arg0)
         {
            // NOP
         }

         public void intervalRemoved(ListDataEvent arg0)
         {
            // NOP
         }
      });

   }

   public void setConfig(String classPathId, ProviderExtConfig newConfig)
   {
      this.config = newConfig;

      if (cfComboModel.getIndexOf(ConfigDAOImpl.DEFAULT_PLUGIN) == -1)
      {
         cfComboModel.addElement(ConfigDAOImpl.DEFAULT_PLUGIN);
      }

      for (Iterator iter = HermesBrowser.getConfigDAO().getAdminFactories().iterator(); iter.hasNext();)
      {
         String adminFactoryName = (String) iter.next();
         String pluginName = HermesBrowser.getConfigDAO().getPlugInName(adminFactoryName);

         if (cfComboModel.getIndexOf(pluginName) == -1)
         {
            cfComboModel.addElement(pluginName);
         }
      }

      cfComboModel.setSelectedItem(HermesBrowser.getConfigDAO().getPlugInName(config.getClassName()));

      try
      {
         ClassLoaderManager classLoaderManager = (ClassLoaderManager) SingletonManager.get(ClassLoaderManager.class);
         ClassLoader classLoader = classLoaderManager.getClassLoader(classPathId);
         Thread.currentThread().setContextClassLoader(classLoader);

         bean = (HermesAdminFactory) classLoader.loadClass(config.getClassName()).newInstance();
      }
      catch (Throwable e)
      {
         cat.error(e.getMessage(), e);
      }

      try
      {
         propertyTableModel = new PropertySetTableModel(bean, config.getProperties(), new HashSet());
         propertyTable.setModel(propertyTableModel);

         updateCellEditor();

         if (propertyTableModel.getProperties().size() == 0)
         {
            addItem.setEnabled(false);
            removeItem.setEnabled(false);
         }
      }
      catch (Throwable ex)
      {
         cat.error(ex.getMessage(), ex);
      }

      propertyTableModel.addTableModelListener(new TableModelListener()
      {
         public void tableChanged(TableModelEvent arg0)
         {
            dialog.setDirty();
         }
      });

   }

   public void updateCellEditor() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
   {
      if (propertyTable.getColumnModel().getColumnCount() > 0)
      {
         final TableColumn propertyNameColumn = propertyTable.getColumnModel().getColumn(0);

         propertySelectionComboBox = new JComboBox();
         boolean isJNDI = false;

         try
         {
            Map properties = BeanUtils.describe(bean);

            log.debug("bean= "+ properties) ;
            
            for (Iterator iter = propertyTableModel.getValidProperties().iterator(); iter.hasNext();)
            {
               String name = (String) iter.next();

               propertySelectionComboBox.addItem(name);
            }
         }
         catch (InvocationTargetException e)
         {
            cat.error(e.getTargetException().getMessage(), e.getTargetException());
         }

         propertyNameColumn.setCellEditor(new DefaultCellEditor(propertySelectionComboBox));
      }
   }

   public void updateModel()
   {
      if (propertyTableModel != null && config != null)
      {
         try
         {
            config.setClassName(HermesBrowser.getConfigDAO().getAdminClassForPlugIn(afCombo.getSelectedItem().toString()));

            config.getProperties().getProperty().clear();
            config.getProperties().getProperty().addAll(propertyTableModel.getProperties());
         }
         catch (JAXBException e)
         {
            log.error(e.getMessage(), e);
         }
      }
   }
}