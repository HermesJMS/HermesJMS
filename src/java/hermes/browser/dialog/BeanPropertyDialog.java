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

import hermes.HermesRuntimeException;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;

/**
 * A simple dialog that shows the properties of a bean - only deals with basic types.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationConfigDialog.java,v 1.1 2004/05/16 13:04:15
 *          colincrist Exp $
 */

public class BeanPropertyDialog extends StandardDialog
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3921945865610783296L;
	private BeanPropertyPanel beanPanel;

    /**
     * @throws java.awt.HeadlessException
     */
    public BeanPropertyDialog(Frame owner, Object bean, boolean editable) throws HeadlessException
    {
        super(owner, "Properties", true);

        try
        {
            beanPanel = new BeanPropertyPanel(bean, editable, false);
            beanPanel.init() ;
        }
        catch (IllegalAccessException e)
        {
           throw new HermesRuntimeException(e) ;
        }
        catch (InvocationTargetException e)
        {
            throw new HermesRuntimeException(e) ;
        }
        catch (NoSuchMethodException e)
        {
            throw new HermesRuntimeException(e) ;
        }
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
        return beanPanel;
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
            /**
			 * 
			 */
			private static final long serialVersionUID = 2353403995331123497L;

			public void actionPerformed(ActionEvent arg0)
            {
                beanPanel.doOK();
                setDialogResult(RESULT_AFFIRMED);
                setVisible(false);
                dispose();
            }
        };

        AbstractAction cancelButtonAction = new AbstractAction(UIManager.getString("OptionPane.cancelButtonText"))
        {
            /**
			 * 
			 */
			private static final long serialVersionUID = 3126103743620232352L;

			public void actionPerformed(ActionEvent arg0)
            {
                beanPanel.doCancel();
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

        okButton.setEnabled(beanPanel.isEditable());

        return buttonPanel;
    }

    public void addOKAction(Runnable r)
    {
        beanPanel.addOKAction(r);
    }

}