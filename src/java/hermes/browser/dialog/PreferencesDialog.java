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

import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.config.ConnectionConfig;
import hermes.config.FactoryConfig;
import hermes.config.HermesConfig;
import hermes.config.PropertySetConfig;
import hermes.config.SessionConfig;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Category;

import com.jidesoft.swing.JideSwingUtilities;
import com.jidesoft.swing.JideTabbedPane;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: PreferencesDialog.java,v 1.11 2004/10/28 21:34:02 colincrist
 *          Exp $
 */

public class PreferencesDialog extends AbstractOptionDialog
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 5057331477007320264L;

private static final Category cat = Category.getInstance(PreferencesDialog.class);

   private HermesConfig model;
   private Map sessionToFactoryMap = new HashMap();
   private Set newFactories = new HashSet();
   private ConnectionFactoryConfigPanel connectionFactoryConfigPanel = new ConnectionFactoryConfigPanel(this);
   private ConnectionConfigPanel connectionConfigPanel = new ConnectionConfigPanel(this);
   private SessionConfigPanel sessionConfigPanel = new SessionConfigPanel(this);
   private DestinationConfigPanel destinationConfigPanel = new DestinationConfigPanel(this);
   private ProviderConfigPanel providerConfigPanel = new ProviderConfigPanel(this);
   private GeneralRendererConfigPanel rendererConfigPanel = new GeneralRendererConfigPanel(this);
   private HermesAdminFactoryConfigPanel adminConfigPanel = new HermesAdminFactoryConfigPanel(this);
   private JideTabbedPane topPanel;
   private JPanel sessionPanel;
   private JPanel factoryPanel;
   private String firstSessionId;
   private String currentSessionId;
   private Component lastSelectedTab;
   private Component currentSelectedTab;

   public String getSelectedLoader()
   {
      return connectionFactoryConfigPanel.getLoader();
   }

   /**
    * @param arg0
    * @throws java.awt.HeadlessException
    */
   public PreferencesDialog(Frame arg0) throws HeadlessException
   {
      super(arg0, "Sessions", true);

   }

   public FactoryConfig getFactoryConfigBySessionId(String id)
   {
      return (FactoryConfig) sessionToFactoryMap.get(id);
   }

   public void refocus(String sessionId)
   {
      cat.debug("refocusing on " + sessionId);

      providerConfigPanel.setHermesConfig(model);
      rendererConfigPanel.setHermesConfig(model);

      if (sessionId != null && !sessionId.equals("") && !sessionId.equals(currentSessionId))
      {
         try
         {
            currentSessionId = sessionId;

            FactoryConfig factoryConfig = getFactoryConfigBySessionId(sessionId);

            if (factoryConfig == null)
            {
               factoryConfig = HermesBrowser.getConfigDAO().createDefaultFactoryConfig(sessionId);
               factoryConfig.getDestination().addAll(destinationConfigPanel.getDestinations());
               sessionToFactoryMap.put(sessionId, factoryConfig);

               newFactories.add(factoryConfig);

               setDirty();
            }

            connectionFactoryConfigPanel.setFactoryConfig(model, factoryConfig);
            destinationConfigPanel.setFactoryConfig(factoryConfig);
            connectionConfigPanel.setConnectionConfig((ConnectionConfig) factoryConfig.getConnection().get(0));
            sessionConfigPanel.setSessionConfig((SessionConfig) connectionConfigPanel.getConnectionConfig().getSession().get(0));

            if (model.isDisplayFactoryAdmin() && adminConfigPanel != null)
            {
               adminConfigPanel.setConfig(factoryConfig.getClasspathId(), factoryConfig.getExtension());
            }
         }
         catch (Throwable ex)
         {
            cat.error(ex.getMessage(), ex);

            HermesBrowser.getBrowser().showErrorDialog(ex);
         }
      }
   }

   public void init()
   {
      try
      {
         model = HermesBrowser.getBrowser().getConfig();

         topPanel = new JideTabbedPane();
         sessionPanel = new JPanel();
         factoryPanel = new JPanel();

         for (Iterator iter = model.getFactory().iterator(); iter.hasNext();)
         {
            FactoryConfig factoryConfig = (FactoryConfig) iter.next();

            if (factoryConfig.getConnection().size() == 0)
            {
               factoryConfig.getConnection().add(new ConnectionConfig());
            }

            if (factoryConfig.getProvider().getProperties() == null)
            {
               factoryConfig.getProvider().setProperties(new PropertySetConfig());
            }

            connectionFactoryConfigPanel.setFactoryConfig(model, factoryConfig);
            destinationConfigPanel.setFactoryConfig(factoryConfig);

            for (Iterator iter3 = factoryConfig.getConnection().iterator(); iter3.hasNext();)
            {
               ConnectionConfig connectionConfig = (ConnectionConfig) iter3.next();

               connectionConfigPanel.setConnectionConfig(connectionConfig);

               for (Iterator iter4 = connectionConfig.getSession().iterator(); iter4.hasNext();)
               {
                  SessionConfig sessionConfig = (SessionConfig) iter4.next();

                  if (firstSessionId == null)
                  {
                     firstSessionId = sessionConfig.getId();
                  }

                  sessionConfigPanel.addSessionConfig(sessionConfig);
                  sessionToFactoryMap.put(sessionConfig.getId(), factoryConfig);
               }
            }
         }

         sessionPanel.setLayout(new BorderLayout());

         factoryPanel.setLayout(new GridLayout(3, 1));

         if (model.isDisplayFactoryAdmin() && adminConfigPanel != null)
         {
            factoryPanel.setLayout(new GridLayout(3, 1));
            factoryPanel.add(adminConfigPanel);
         }
         else
         {
            factoryPanel.setLayout(new GridLayout(2, 1));
         }

         factoryPanel.add(connectionFactoryConfigPanel);
         factoryPanel.add(destinationConfigPanel);

         sessionPanel.add(sessionConfigPanel, BorderLayout.NORTH);
         sessionPanel.add(factoryPanel, BorderLayout.CENTER);
         sessionPanel.add(connectionConfigPanel, BorderLayout.SOUTH);

         topPanel.add("Sessions", sessionPanel);
         topPanel.add("Providers", providerConfigPanel);

         if (HermesBrowser.getRendererManager().getRenderers().size() > 0)
         {
            topPanel.add("Renderers", rendererConfigPanel);
         }

         // topPanel.add("General", generalConfigPanel) ;

         topPanel.setTabPlacement(JTabbedPane.BOTTOM);

         super.init();

         sessionConfigPanel.addListeners();

         refocus(firstSessionId);

         if (model.isDisplayFactoryAdmin())
         {
            setSize(500, 700);
         }
         else
         {
            setSize(500, 600);
         }

         topPanel.addChangeListener(new ChangeListener()
         {
            public void stateChanged(ChangeEvent arg0)
            {
               lastSelectedTab = currentSelectedTab;
               currentSelectedTab = topPanel.getSelectedComponent();

               if (lastSelectedTab == providerConfigPanel)
               {
                  if (providerConfigPanel.isModelChanged())
                  {
                     if (JOptionPane
                           .showConfirmDialog(providerConfigPanel, "You must apply any changes made to the ClasspathGroups before continuing, apply now?",
                                 "Please confirm.", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                     {
                        updateData(true);
                     }
                  }
               }
            }

         });

         JideSwingUtilities.centerWindow(this);
      }
      catch (HermesException e)
      {
         cat.error(e.getMessage(), e);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.metastuff.hermes.browser.AbstractOptionDialog#updateData(boolean)
    */
   protected void updateData(boolean toModel)
   {
      if (toModel)
      {

         providerConfigPanel.updateModel();

         //
         // Must come before any session based configuration

         connectionFactoryConfigPanel.updateModel();
         connectionConfigPanel.updateModel();
         sessionConfigPanel.updateModel();
         rendererConfigPanel.updateModel();
         destinationConfigPanel.updateModel();

         if (model.isDisplayFactoryAdmin() && adminConfigPanel != null)
         {
            adminConfigPanel.updateModel();
         }

         // Data from the ConnectionFactory...

         if (newFactories.size() > 0)
         {
            for (Iterator iter = newFactories.iterator(); iter.hasNext();)
            {
               final FactoryConfig config = (FactoryConfig) iter.next();

               cat.debug("new factory config for class=" + config.getProvider().getClassName());
               model.getFactory().add(config);
            }

            newFactories.clear();
         }

         try
         {
            //
            // Cleanup any sessions that have stoopid names

            for (Iterator iter = HermesBrowser.getBrowser().getConfig().getFactory().iterator(); iter.hasNext();)
            {
               FactoryConfig fConfig = (FactoryConfig) iter.next();

               ConnectionConfig cConfig = (ConnectionConfig) fConfig.getConnection().get(0);

               for (Iterator iter2 = cConfig.getSession().iterator(); iter2.hasNext();)
               {
                  SessionConfig sConfig = (SessionConfig) iter2.next();

                  if (sConfig.getId() == null || sConfig.getId().equals(""))
                  {
                     iter2.remove();
                  }
               }
            }
         }
         catch (HermesException e)
         {
            cat.error(e.getMessage(), e);
         }

         try
         {
            HermesBrowser.getBrowser().backupConfig();
         }
         catch (Exception ex)
         {
            HermesBrowser.getBrowser().showErrorDialog("Unable to backup the configuration: ", ex);

            cat.error(ex.getMessage(), ex);
         }

         try
         {
            HermesBrowser.getBrowser().saveConfig();
         }
         catch (Exception ex)
         {
            HermesBrowser.getBrowser().showErrorDialog("Unable to save this configuration: ", ex);

            try
            {
               HermesBrowser.getBrowser().restoreConfig();
            }
            catch (Exception ex2)
            {
               HermesBrowser.getBrowser().showErrorDialog("Unable to restore this configuration: ", ex2);
            }
         }

         try
         {
            HermesBrowser.getBrowser().loadConfig();
         }
         catch (Exception ex)
         {
            HermesBrowser.getBrowser().showErrorDialog("Unable to load this configuration: ", ex);

            cat.error(ex.getMessage(), ex);

         }

         try
         {
            model = HermesBrowser.getBrowser().getConfig();
         }
         catch (HermesException e)
         {
            HermesBrowser.getBrowser().showErrorDialog(e);
         }

         refocus(currentSessionId);
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.metastuff.hermes.browser.AbstractOptionDialog#initBodyPane()
    */
   protected Container initBodyPane()
   {
      if (firstSessionId != null)
      {
         refocus(firstSessionId);
      }

      return topPanel;
   }

   /**
    * @return Returns the destinationConfigPanel.
    */
   public DestinationConfigPanel getDestinationConfigPanel()
   {
      return destinationConfigPanel;
   }
}