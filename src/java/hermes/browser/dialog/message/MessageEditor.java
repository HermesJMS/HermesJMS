/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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

package hermes.browser.dialog.message;

import hermes.Hermes;
import hermes.HermesRuntimeException;
import hermes.browser.HermesBrowser;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.swing.JideSwingUtilities;

/**
 * @author colincrist@hermesjms.com
 */
public class MessageEditor extends StandardDialog
{
   private static final long serialVersionUID = -5465641794588176447L;
   private static final Logger log = Logger.getLogger(MessageEditor.class);

   private Hermes hermes;
   private Message message;
   private boolean rval;

   public MessageEditor(Hermes hermes, Message message) throws JMSException
   {
      super(HermesBrowser.getBrowser());

      this.hermes = hermes;
      this.message = hermes.duplicate(message);

      setMinimumSize(new Dimension(300, 400));
   }

   public Message getMessage()
   {
      return message;
   }

   @Override
   public JComponent createBannerPanel()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean edit()
   {
      pack();
      JideSwingUtilities.centerWindow(this);
      show();
      return rval;
   }

   @Override
   public ButtonPanel createButtonPanel()
   {
      final ButtonPanel buttonPanel = new ButtonPanel();
      final JButton okButton = new JButton("OK");
      final JButton cancelButton = new JButton("Cancel");

      buttonPanel.addButton(okButton);
      buttonPanel.addButton(cancelButton);

      okButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            rval = true;
            dispose();
         }
      });

      cancelButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            rval = false;
            dispose();
         }
      });

      buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      return buttonPanel;
   }

   @Override
   public JComponent createContentPanel()
   {
      try
      {
         EditableMessageHeaderTableModel tableModel = new EditableMessageHeaderTableModel(message);
         MessageHeaderTable table = new MessageHeaderTable(tableModel);

         return table;
      }
      catch (JMSException ex)
      {
         throw new HermesRuntimeException(ex);
      }

   }

}
