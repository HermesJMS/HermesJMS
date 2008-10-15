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

import hermes.browser.ConfigDialogProxy;
import hermes.browser.HermesBrowser;
import hermes.browser.MessageRenderer;
import hermes.browser.MessageRenderer.Config;
import hermes.browser.model.ListConfigTableModel;
import hermes.config.HermesConfig;
import hermes.config.RendererConfig;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: GeneralRendererConfigPanel.java,v 1.4 2004/07/21 19:46:15
 *          colincrist Exp $
 */
public class GeneralRendererConfigPanel extends AbstractRendererConfigPanel
{
   private static final Logger log = Logger.getLogger(GeneralRendererConfigPanel.class);
   private JScrollPane bottomSP = new JScrollPane();
   private JTable classTable = new JTable();
   private Map configChanges = new HashMap();
   private ListConfigTableModel configModel;
   private PreferencesDialog dialog;
   private HermesConfig hermesConfig;
   private JLabel infoLabel1 = new JLabel();
   private Set newLoaderConfigs = new HashSet();
   private JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

   private JScrollPane topSP = new JScrollPane();

   public GeneralRendererConfigPanel(PreferencesDialog dialog)
   {
      this.dialog = dialog;

      init();
   }

   public void doRendererSelected(ListSelectionEvent e)
   {
      if (!e.getValueIsAdjusting() && classTable.getSelectedRow() >= 0)
      {
         String rendererName = (String) configModel.getValueAt(classTable.getSelectedRow(), 0);

         try
         {
            final MessageRenderer renderer = HermesBrowser.getRendererManager().getRendererByName(rendererName);
            final ConfigDialogProxy proxy = (ConfigDialogProxy) configChanges.get(renderer.getClass().getName());

            JComponent configPanel = null;

            if (proxy != null && (configPanel = renderer.getConfigPanel(proxy)) != null)
            {
               bottomSP.setViewportView(configPanel);

               configChanges.put(renderer.getClass().getName(), proxy);
            }
            else
            {

               bottomSP.setViewportView(new JLabel("No properties"));
            }
         }
         catch (Exception ex)
         {
            add(new JLabel(ex.getMessage()), BorderLayout.SOUTH);

            log.error(ex.getMessage(), ex);
         }
      }
   }

   private void init()
   {
      Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
      JPanel topPanel = new JPanel();

      topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      topPanel.setLayout(new GridLayout(1, 2));

      infoLabel1.setText("Message Renderers");

      setLayout(new BorderLayout());
      setBorder(BorderFactory.createTitledBorder(border, "Renderers"));

      topPanel.add(infoLabel1);
      topSP.setViewportView(classTable);

      classTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

      add(topPanel, BorderLayout.NORTH);
      add(splitPane, BorderLayout.CENTER);

      splitPane.add(topSP, "top");
      splitPane.add(bottomSP, "bottom");
      splitPane.setDividerLocation(200);

      // splitPane.setShowGripper(true) ;

      classTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            doRendererSelected(e);
         }
      });

   }

   public void initState()
   {
      configChanges.clear();

      for (final MessageRenderer renderer : HermesBrowser.getRendererManager().getRenderers())
      {
         try
         {
            final String className = renderer.getClass().getName();
            final Map properties = BeanUtils.describe(renderer);
            final MessageRenderer.Config rendererConfig = renderer.createConfig();

            if (rendererConfig != null)
            {
               BeanUtils.populate(rendererConfig, properties);

               final ConfigDialogProxy proxy = new ConfigDialogProxy()
               {
                  public Config getConfig()
                  {
                     return rendererConfig;
                  }

                  public void setDirty()
                  {
                     dialog.setDirty();
                  }
               };

               configChanges.put(className, proxy);
            }
         }
         catch (Exception e)
         {
            log.error(e.getMessage(), e);
         }
      }
   }

   public void setHermesConfig(HermesConfig hermesConfig)
   {
      this.hermesConfig = hermesConfig;

      initState();

      configModel = new ListConfigTableModel();

      for (MessageRenderer rConfig : HermesBrowser.getRendererManager().getRenderers())
      {

         configModel.addItem(rConfig.getDisplayName());

      }

      classTable.setModel(configModel);

      DefaultTableColumnModel cm = new DefaultTableColumnModel();
      cm.addColumn(new TableColumn());

      classTable.setColumnModel(cm);
   }

   public void updateModel()
   {
      if (configModel != null && hermesConfig != null)
      {
         hermesConfig.getRenderer().clear();

         for (Iterator iter = configModel.getRows().iterator(); iter.hasNext();)
         {
            RendererConfig rConfig = new RendererConfig();
            MessageRenderer renderer = HermesBrowser.getRendererManager().getRendererByName((String) iter.next()) ;
            rConfig.setClassName(renderer.getClass().getName());
            

            hermesConfig.getRenderer().add(rConfig);

            ConfigDialogProxy proxy = (ConfigDialogProxy) configChanges.get(rConfig.getClassName());

            if (proxy != null)
            {
               try
               {
                  Map props = BeanUtils.describe(proxy.getConfig());

                  HermesBrowser.getConfigDAO().setRendererProperties(hermesConfig, rConfig.getClassName(), props);
               }
               catch (Exception e)
               {
                  log.error(e.getMessage(), e);
               }
            }
         }
      }
   }

}