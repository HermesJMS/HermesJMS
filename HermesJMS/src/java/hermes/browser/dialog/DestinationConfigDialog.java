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

import hermes.config.DestinationConfig;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

import javax.jms.Destination;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationConfigDialog.java,v 1.1 2004/05/16 13:04:15
 *          colincrist Exp $
 */

public class DestinationConfigDialog extends StandardDialog
{
   private static final Logger log = Logger.getLogger(DestinationConfigDialog.class) ;
   
   private DestinationPropertyConfigPanel configPanel;

   
   
   /**
    * @throws java.awt.HeadlessException
    */
   public DestinationConfigDialog(Frame owner, String hermesId, Destination bean, DestinationConfig config) throws HeadlessException
   {
      super(owner, "Destination Properties", true);

      configPanel = new DestinationPropertyConfigPanel(hermesId, bean, config);

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.jidesoft.dialog.StandardDialog#createBannerPanel()
    */
   public JComponent createBannerPanel()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.jidesoft.dialog.StandardDialog#createContentPanel()
    */
   public JComponent createContentPanel()
   {
      return configPanel;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.jidesoft.dialog.StandardDialog#createButtonPanel()
    */
   public ButtonPanel createButtonPanel()
   {
      final ButtonPanel buttonPanel = new ButtonPanel(SwingConstants.RIGHT);
      
      AbstractAction okButtonAction = new AbstractAction(UIManager.getString("OptionPane.okButtonText"))
      {
         public void actionPerformed(ActionEvent arg0)
         {
            configPanel.doOK();
            setDialogResult(RESULT_AFFIRMED);
            setVisible(false);
            dispose();
         }
      };

      
      AbstractAction cancelButtonAction = new AbstractAction(UIManager.getString("OptionPane.cancelButtonText"))
      {
         public void actionPerformed(ActionEvent arg0)
         {
            configPanel.doCancel();
            setDialogResult(RESULT_CANCELLED);
            setVisible(false);
            dispose();
         }
      };

      JButton okButton = new JButton(okButtonAction);
      JButton cancelButton = new JButton(cancelButtonAction);

      buttonPanel.addButton(okButton);
      buttonPanel.addButton(cancelButton);
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      return buttonPanel;
   }

   public void addOKAction(Runnable r)
   {
      configPanel.addOKAction(r);
   }

}