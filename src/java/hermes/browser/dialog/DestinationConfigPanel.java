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

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.model.DestinationConfigTableModel;
import hermes.browser.tasks.EditDestinationPropertiesTask;
import hermes.config.ConnectionConfig;
import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;
import hermes.config.SessionConfig;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.apache.log4j.Category;

import com.jidesoft.grid.SortableTable;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationConfigPanel.java,v 1.7 2004/07/30 17:25:15
 *          colincrist Exp $
 */
public class DestinationConfigPanel extends JPanel
{
   /**
	 *
	 */
	private static final long serialVersionUID = -9161130471970874426L;
private static final String IMPORT = "Import...";
   private static final String REMOVE = "Remove";
   private static final String ADD_SIMPLE = "Add...";
   private static final String EDIT_SIMPLE = "Edit...";
   private static final String DESTINATIONS = "Destinations";
   private static final Category cat = Category.getInstance(DestinationConfigPanel.class);
   private static File lastImportDirectory = null;

   private PreferencesDialog dialog;
   private FactoryConfig factoryConfig;

   private SortableTable destinationTable = new SortableTable();
   private JScrollPane destinationTableSP = new JScrollPane();
   private DestinationConfigTableModel destinationTableModel;
   private MouseAdapter mouseAdapter;
   private Map configs = new HashMap();

   public DestinationConfigPanel(PreferencesDialog dialog)
   {
      this.dialog = dialog;

      init();
   }

   public void reset() {
       while(destinationTableModel.getRowCount() > 0) {
           destinationTableModel.removeRow(0);
       }
       updateModel();
   }

   public void init()
   {
      Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);

      setBorder(BorderFactory.createTitledBorder(border, DESTINATIONS));
      setLayout(new GridLayout(1, 1));
      destinationTableSP.setViewportView(destinationTable);
      destinationTable.setSortable(true);
      add(destinationTableSP);

      final JPopupMenu popupMenu = new JPopupMenu();
      final JMenuItem add1Item = new JMenuItem(ADD_SIMPLE);

      final JMenuItem removeItem = new JMenuItem(REMOVE);
      final JMenuItem importItem = new JMenuItem(IMPORT);

      popupMenu.add(add1Item);

      popupMenu.add(removeItem);
      popupMenu.add(importItem);

      importItem.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent arg0)
         {
            if (JOptionPane.showConfirmDialog(DestinationConfigPanel.this, "The input file must contain a single topic or queue name per line",
                  "Import topics/queues", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
            {
               JFileChooser fileChooser;

               if (lastImportDirectory == null)
               {
                  fileChooser = new JFileChooser();
               }
               else
               {
                  fileChooser = new JFileChooser(lastImportDirectory);
               }

               if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION)
               {
                  lastImportDirectory = new File(fileChooser.getSelectedFile().getPath());

                  try
                  {
                     LineNumberReader reader = new LineNumberReader(new FileReader(fileChooser.getSelectedFile()));
                     String line;

                     while ((line = reader.readLine()) != null)
                     {
                        DestinationConfig dConfig = new DestinationConfig();
                        dConfig.setName(line.trim());
                        dConfig.setDomain(Domain.QUEUE.getId());

                        destinationTableModel.addItem(dConfig);
                     }
                  }
                  catch (IOException e)
                  {
                     cat.error(e.getMessage(), e);
                  }
               }

               dialog.setDirty();
            }
         }
      });

      add1Item.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent arg0)
         {
            try
            {
               ConnectionConfig cConfig = (ConnectionConfig) factoryConfig.getConnection().get(0);
               SessionConfig sConfig = (SessionConfig) cConfig.getSession().get(0);
               DestinationConfig dConfig = new DestinationConfig() ;
               dConfig.setDomain(Domain.QUEUE.getId()) ;
               dConfig.setName("NEW") ;

               destinationTableModel.addItem(dConfig) ;
               doEdit(sConfig.getId(), dConfig);
               dialog.setDirty();
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
            for (int row : destinationTable.getSelectedRows())
            {
               if (row >= 0)
               {
                  int realRow = destinationTable.getActualRowAt(row);
                  destinationTableModel.removeRow(realRow);
                  dialog.setDirty();
               }
            }
         }
      });

      mouseAdapter = new MouseAdapter()
      {
         public void mousePressed(MouseEvent e)
         {
            if (SwingUtilities.isRightMouseButton(e))
            {
               if (e.getComponent() == destinationTableSP)
               {
                  removeItem.setEnabled(false);
               }
               else
               {
                  removeItem.setEnabled(true);
               }

               popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
            else if (e.getClickCount() == 2)
            {
               int row = destinationTable.getActualRowAt(destinationTable.getSelectedRow());

               if (row >= 0)
               {
                  ConnectionConfig cConfig = (ConnectionConfig) factoryConfig.getConnection().get(0);
                  SessionConfig sConfig = (SessionConfig) cConfig.getSession().get(0);
                  doEdit(sConfig.getId(), destinationTableModel.getRowConfig(row));
               }
            }
         }
      };

      destinationTable.addMouseListener(mouseAdapter);
      destinationTableSP.addMouseListener(mouseAdapter);

      destinationTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
   }

   public void setFactoryConfig(FactoryConfig factoryConfig)
   {
      this.factoryConfig = factoryConfig;

      destinationTableModel = new DestinationConfigTableModel(factoryConfig.getDestination());

      destinationTable.setModel(destinationTableModel);
   }

   public void doEdit(String hermesId, DestinationConfig dConfig)
   {
      try
      {
         Hermes.ui.getThreadPool().invokeLater(
               new EditDestinationPropertiesTask((Hermes) HermesBrowser.getBrowser().getLoader().getContext().lookup(hermesId), dConfig, new Runnable()
               {

                  public void run()
                  {
                     // Hack.

                     destinationTableModel.fireTableDataChanged() ;
                  }

               }));
      }
      catch (NamingException e)
      {
         HermesBrowser.getBrowser().showErrorDialog(e);
      }
   }

   public Collection getDestinations()
   {
      return destinationTableModel == null ? Collections.EMPTY_LIST : destinationTableModel.getRows();
   }

   public void updateModel()
   {
      if (factoryConfig != null)
      {
         factoryConfig.getDestination().clear();
         factoryConfig.getDestination().addAll(destinationTableModel.getRows());
      }
      else
      {
         cat.error("updateModel() factoryConfig is null");
      }
   }
}