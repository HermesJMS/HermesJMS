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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.jidesoft.swing.JideButton;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FilterablePanel.java,v 1.2 2006/05/26 10:08:21 colincrist Exp $
 */

public class FilterablePanel extends JPanel
{
   private JideButton deleteButton = new JideButton(IconCache.getIcon("hermes.delete"));
   private JideButton saveButton = new JideButton(IconCache.getIcon("hermes.save"));
   private JideButton clearButton = new JideButton(IconCache.getIcon("hermes.clear"));

   private Vector<String> filters = new Vector<String>();

   public FilterablePanel()
   {
      super();
      init();
   }

   private void init()
   {
      try
      {
         filters.add("");
         filters.addAll(HermesBrowser.getBrowser().getConfig().getFilters());
      }
      catch (HermesException e1)
      {
         HermesBrowser.getBrowser().showErrorDialog("Unable to load filter history", e1);
      }

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      final JComboBox comboBox = new JComboBox(filters);
      comboBox.setEditable(true);

      add(new JLabel(" Filter: ", IconCache.getIcon("hermes.filter.sql"), SwingConstants.LEADING));

      add(comboBox);
      add(clearButton);
      add(deleteButton);
      add(saveButton);

      clearButton.setToolTipText("Clear any applied filter");
      deleteButton.setToolTipText("Delete this filter from saved filters");
      saveButton.setToolTipText("Add this filter to saved filters");

      comboBox.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            doFilter((String) comboBox.getSelectedItem());
         }
      });

      deleteButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (comboBox.getSelectedIndex() >= 0)
            {
               if (!TextUtils.isEmpty((String) comboBox.getSelectedItem()) && filters.contains(comboBox.getSelectedItem()))
               {
                  comboBox.removeItemAt(comboBox.getSelectedIndex());
                  updateFilterConfig();
               }
            }
         }
      });

      clearButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            comboBox.setSelectedItem("") ;
         }
      });

      saveButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            final String filter = (String) comboBox.getSelectedItem();

            if (!TextUtils.isEmpty(filter))
            {
               if (!filters.contains(filter))
               {
                  comboBox.addItem(filter);
                  updateFilterConfig();
               }
            }
         }
      });

      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
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

   private void updateFilterConfig()
   {
      try
      {
         HermesConfig config = HermesBrowser.getBrowser().getConfig();
         
         config.getFilters().clear();
         
         for (String filter : filters)
         {
            if (!TextUtils.isEmpty(filter))
            {
               config.getFilters().add(filter);
            }
         }

         HermesBrowser.getBrowser().backupConfig();
         HermesBrowser.getBrowser().saveConfig();
      }
      catch (HermesException e)
      {
         HermesBrowser.getBrowser().showErrorDialog("Unable to update configuration", e);
      }

   }

}
