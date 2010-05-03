/* 
 * Copyright 2003,2004,2005 Colin Crist
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

package hermes.swing;

import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.FilterableAction;
import hermes.config.HermesConfig;
import hermes.util.TextUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.document.DocumentComponentEvent;
import com.jidesoft.document.DocumentComponentListener;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.MultilineLabel;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FilterInputPanel.java,v 1.3 2006/07/13 07:35:31 colincrist Exp $
 */

public class FilterInputPanel extends DockableFrame implements DocumentComponentListener
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -525161612413763300L;
private static final Logger log = Logger.getLogger(FilterInputPanel.class);
   private JideButton comboFilterButton = new JideButton(IconCache.getIcon("hermes.filter.sql"));
   private JideButton comboRemoveFilterButton = new JideButton(IconCache.getIcon("hermes.delete"));
   private JideButton textFilterButton = new JideButton(IconCache.getIcon("hermes.filter.sql"));
   private JideButton textSaveFilterButton = new JideButton(IconCache.getIcon("hermes.save"));
   private Vector<String> filterHistory = new Vector<String>();
   private JComboBox filterCombo;
   private JTextField filterField = new JTextField();

   public FilterInputPanel()
   {
      super("Filters", IconCache.getIcon("hermes.filter.sql"));
      
      init();

      getContext().setInitMode(DockContext.STATE_AUTOHIDE);
      getContext().setInitSide(DockContext.DOCK_SIDE_EAST);

      setAvailableButtons(DockableFrame.BUTTON_AUTOHIDE | DockableFrame.BUTTON_FLOATING);

      comboFilterButton.setToolTipText("Run SQL filter.");
      textFilterButton.setToolTipText("Run SQL filter.");
      textSaveFilterButton.setToolTipText("Save this filter.");
      comboRemoveFilterButton.setToolTipText("Delete this filter.");

      setEnabled(false);
   }

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);

      filterCombo.setEnabled(enabled);
      filterField.setEnabled(enabled);
      comboFilterButton.setEnabled(enabled);
      textFilterButton.setEnabled(enabled);
      textSaveFilterButton.setEnabled(enabled);
      comboRemoveFilterButton.setEnabled(enabled);

   }

   private String getDescription()
   {
      return "Filters are SQL92 expressions that can filter out messages that have been browsed from a JMS queue, topic, message file or a FIX session log. In the expression you can use:\n\n"
            + "Any JMS header properties.\n\n" + "JMS MapMessage properties.\n\n" + "Any FIX tag, either by number or tag name.";
   }

   private void init()
   {
      try
      {
         filterHistory.addAll(HermesBrowser.getBrowser().getConfig().getFilters()) ;
      }
      catch (HermesException e1)
      {
        HermesBrowser.getBrowser().showErrorDialog("Unable to load filter history", e1) ;
      }
      
      getContentPane().setLayout(new BorderLayout());
      CollapsiblePanes panes = new CollapsiblePanes();

      CollapsiblePane descriptionPane = new CollapsiblePane("Using Filters");
      MultilineLabel label = new MultilineLabel(getDescription());
      label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
      descriptionPane.setContentPane(label);

      try
      {
         descriptionPane.setCollapsed(true);
      }
      catch (PropertyVetoException e)
      {
         log.warn(e.getMessage(), e);
      }

      CollapsiblePane comboPane = new CollapsiblePane("Saved Filters");
      comboPane.getContentPane().setLayout(new BoxLayout(comboPane.getContentPane(), BoxLayout.X_AXIS));
      JPanel comboPanel = new JPanel();
      comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
      comboPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
      comboPane.setEmphasized(true);

      try
      {
         comboPane.setCollapsed(true);
      }
      catch (PropertyVetoException e)
      {
         log.warn(e.getMessage(), e);
      }

      filterCombo = new JComboBox(filterHistory);
      filterCombo.setEditable(false);
      comboPanel.add(filterCombo);
      comboPanel.add(comboFilterButton);
      comboPanel.add(comboRemoveFilterButton);
      comboPane.setContentPane(comboPanel);

      CollapsiblePane fieldPane = new CollapsiblePane("New Filter");
      JPanel fieldPanel = new JPanel();
      fieldPane.setEmphasized(true);
      fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.X_AXIS));
      fieldPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
      fieldPanel.add(filterField);
      fieldPanel.add(textFilterButton);
      fieldPanel.add(textSaveFilterButton);
      fieldPane.setContentPane(fieldPanel);

      panes.add(fieldPane);
      panes.add(comboPane);
      panes.add(descriptionPane);

      panes.addExpansion();

      filterField.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            doFilter(filterField.getText());
         }
      });

      filterCombo.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            doFilter((String) filterCombo.getSelectedItem());
         }
      });

      comboFilterButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            doFilter((String) filterCombo.getSelectedItem());
         }
      });

      textFilterButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            doFilter(filterField.getText());
         }
      });

      comboRemoveFilterButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (filterCombo.getSelectedIndex() >= 0)
            {
               filterCombo.removeItemAt(filterCombo.getSelectedIndex());
               updateFilterConfig() ;
            }
         }
      });

      textSaveFilterButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (!TextUtils.isEmpty(filterField.getText()))
            {
               if (!filterHistory.contains(filterField.getText()))
               {
                  filterCombo.addItem(filterField.getText());
                  updateFilterConfig() ;
               }
            }
         }
      });

      getContentPane().add(new JScrollPane(panes), BorderLayout.CENTER);
   }

   public void documentComponentDocked(DocumentComponentEvent arg0)
   {
      // TODO Auto-generated method stub
      
   }

   public void documentComponentFloated(DocumentComponentEvent arg0)
   {
      // TODO Auto-generated method stub
      
   }

   private void doFilter(String filter)
   {
      if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof FilterableAction)
      {
         FilterableAction filterableAction = (FilterableAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();

         try
         {
            filterableAction.setSelector(filter);
         }
         catch (Throwable e)
         {
            HermesBrowser.getBrowser().showErrorDialog("Invalid selector: ", e);
         }
      }
   }

   public void documentComponentActivated(DocumentComponentEvent event)
   {
      if (event.getDocumentComponent() instanceof FilterableAction)
      {
         setEnabled(true);
      }
      else
      {
         setEnabled(false);
      }
   }
   
   private void updateFilterConfig() 
   {
      try
      {
         HermesConfig config = HermesBrowser.getBrowser().getConfig() ;
         config.getFilters().clear() ;
         config.getFilters().addAll(filterHistory) ;
         
         
         HermesBrowser.getBrowser().backupConfig() ;
         HermesBrowser.getBrowser().saveConfig() ;
      }
      catch (HermesException e)
      {
        HermesBrowser.getBrowser().showErrorDialog("Unable to update configuration", e) ;
      }
      
   }

   public void documentComponentClosed(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentClosing(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentDeactivated(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentMoved(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentMoving(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentOpened(DocumentComponentEvent arg0)
   {
      // NOP
   }
}
