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

import hermes.browser.HermesBrowser;
import hermes.config.SessionConfig;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Category;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: SessionConfigPanel.java,v 1.4 2004/07/21 19:46:15 colincrist
 *          Exp $
 */
public class SessionConfigPanel extends JPanel
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -5706451976524287590L;
private static final Category cat = Category.getInstance(SessionConfigPanel.class);
   public static final String NEWSESSION = "<new>";

   private PreferencesDialog dialog;
   private SessionConfig sessionConfig;

   private JCheckBox transactedCB = new JCheckBox();
   private JCheckBox auditCB = new JCheckBox();
   private JCheckBox useConsumerForQueueBrowseCB = new JCheckBox();
   // private JTextField reconnectsTF = new JTextField();
   private JComboBox sessionCombo = new JComboBox();

   private Map sessionConfigs = new HashMap();

   private DefaultComboBoxModel sessionComboModel = new DefaultComboBoxModel();

   public SessionConfigPanel(PreferencesDialog dialog)
   {
      this.dialog = dialog;

      init();
   }

   public void init()
   {
      //
      // Basic layout and L&F

      Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);

      setLayout(new GridLayout(2, 4));
      setBorder(BorderFactory.createTitledBorder(border, "Session"));

      sessionCombo.setModel(sessionComboModel);

      JLabel l1 = new JLabel("Session: ");
      l1.setHorizontalAlignment(JLabel.RIGHT);
      add(l1);

      add(sessionCombo);

      JLabel l2 = new JLabel("Use Consumer: ");
      l2.setHorizontalAlignment(JLabel.RIGHT);
      add(l2);

      l2.setToolTipText("Check this if you wish to use a MessageConsumer instead of a QueueBrowser");

      add(useConsumerForQueueBrowseCB);

      JLabel l3 = new JLabel("Audit: ");
      l3.setHorizontalAlignment(JLabel.RIGHT);
      add(l3);

      add(auditCB);

      JLabel l4 = new JLabel("Transacted: ");
      l4.setHorizontalAlignment(JLabel.RIGHT);
      add(l4);

      add(transactedCB);

      sessionCombo.setEditable(true);

      // 
      // Eventing

      auditCB.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent arg0)
         {
            sessionConfig.setAudit(auditCB.isSelected());
            dialog.setDirty();
         }
      });

      transactedCB.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent arg0)
         {
            sessionConfig.setTransacted(transactedCB.isSelected());
            dialog.setDirty();
         }
      });

   }

   public void addListeners()
   {
      sessionComboModel.addListDataListener(new ListDataListener()
      {
         public void contentsChanged(ListDataEvent arg0)
         {
            String sessionId = (String) sessionComboModel.getSelectedItem();

            if (sessionComboModel.getSize() != 0 && dialog.getFactoryConfigBySessionId(sessionId) == null)
            {
               if (sessionConfig.getId().equals(NEWSESSION) || isSessionRename())
               {
                  sessionConfig.setId(sessionId);
               }
               else
               {
                  dialog.refocus(sessionId);
               }
            }
            else
            {
               dialog.refocus(sessionId);
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

   public boolean isSessionRename()
   {
      Object options[] = { "New", "Rename" };

      int n = JOptionPane.showOptionDialog(HermesBrowser.getBrowser(), "Rename this session or create a new one?", "Please select...",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

      if (n == JOptionPane.YES_OPTION)
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   public void addSessionConfig(SessionConfig sessionConfig)
   {
      if (sessionConfig.getId() != null && !sessionConfig.getId().equals(""))
      {
         this.sessionConfig = sessionConfig;

         if (!sessionConfigs.containsKey(sessionConfig.getId()))
         {
            sessionConfigs.put(sessionConfig.getId(), sessionConfig);
            sessionComboModel.addElement(sessionConfig.getId());
         }
      }
   }

   public void setSessionConfig(SessionConfig sessionConfig)
   {
      if (sessionConfig.getId() != null && !sessionConfig.getId().equals(""))
      {
         this.sessionConfig = sessionConfig;

         if (!sessionConfigs.containsKey(sessionConfig.getId()))
         {
            sessionConfigs.put(sessionConfig.getId(), sessionConfig);
            sessionComboModel.addElement(sessionConfig.getId());
         }

         sessionCombo.setSelectedItem(sessionConfig.getId());

         // transactedCB.setName("Transacted");
         useConsumerForQueueBrowseCB.setSelected(sessionConfig.isUseConsumerForQueueBrowse());

         if (sessionConfig.getReconnects() == null)
         {
            sessionConfig.setReconnects(new BigInteger("0"));
         }

         transactedCB.setSelected(sessionConfig.isTransacted());

         auditCB.setSelected(sessionConfig.isAudit());
      }
   }

   public SessionConfig getSessionConfig()
   {
      return sessionConfig;
   }

   public void updateModel()
   {
      if (sessionConfig != null)
      {
         sessionConfig.setId((String) sessionCombo.getSelectedItem());
         sessionConfig.setAudit(sessionConfig.isAudit());
         sessionConfig.setUseConsumerForQueueBrowse(useConsumerForQueueBrowseCB.isSelected());

      }
   }
}