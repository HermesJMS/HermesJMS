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

import hermes.config.ConnectionConfig;
import hermes.config.SessionConfig;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Category;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ConnectionConfigPanel.java,v 1.3 2004/07/21 19:46:15 colincrist
 *          Exp $
 */
public class ConnectionConfigPanel extends JPanel
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6476633587707010668L;
	private static final String CLIENTID = "ClientID: " ;
    private static final String PASSWORD = "Password: ";
    private static final String USERNAME = "User: ";
    private static final String REQUIRES_AUTHENTICATION = "Authenticate: ";
    private static final String CONNECTION = "Connection";
    private static final Category cat = Category.getInstance(ConnectionConfigPanel.class);

    private PreferencesDialog dialog;
    private ConnectionConfig connectionConfig;

    private JCheckBox sharedCB = new JCheckBox("Shared") ;
    private JCheckBox hasPasswordCB = new JCheckBox();
    private JTextField clientIDTF = new JTextField() ;
    private JTextField usernameTF = new JTextField();
    private JTextField passwordTF = new JPasswordField();

    public ConnectionConfigPanel(PreferencesDialog dialog)
    {
        super();

        this.dialog = dialog;

        init();
    }

    public void init()
    {
        setLayout(new GridLayout(1, 7));
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), CONNECTION));

        JLabel l4 = new JLabel(CLIENTID) ;
        l4.setHorizontalAlignment(JLabel.RIGHT) ;
        add(l4) ;
        add(clientIDTF) ;
        
        hasPasswordCB.setHorizontalAlignment(JLabel.RIGHT) ;
        hasPasswordCB.setText(USERNAME) ;
        add(hasPasswordCB);
        add(usernameTF);
        
        JLabel l3 = new JLabel(PASSWORD);
        l3.setHorizontalAlignment(JLabel.RIGHT);
        add(l3);
        add(passwordTF);

        usernameTF.setEnabled(false);
        passwordTF.setEnabled(false);

        sharedCB.setHorizontalAlignment(JCheckBox.RIGHT) ;
        add(sharedCB) ;
        
        final ActionListener dirtyListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                dialog.setDirty();
            }
        };

        usernameTF.addActionListener(dirtyListener);
        passwordTF.addActionListener(dirtyListener);

        hasPasswordCB.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                if (hasPasswordCB.isSelected())
                {
                    usernameTF.setEnabled(true);
                    passwordTF.setEnabled(true);
                }
                else
                {
                    usernameTF.setEnabled(false);
                    passwordTF.setEnabled(false);
                }

                dialog.setDirty();
            }
        });
    }

    public void setConnectionConfig(ConnectionConfig connectionConfig)
    {
        this.connectionConfig = connectionConfig;

        if (connectionConfig.getUsername() == null)
        {
            hasPasswordCB.setSelected(false);
        }
        else
        {
            hasPasswordCB.setSelected(true);
        }

        if (hasPasswordCB.isSelected())
        {
            usernameTF.setEnabled(true);
            passwordTF.setEnabled(true);
        }
        else
        {
            usernameTF.setEnabled(false);
            passwordTF.setEnabled(false);
        }

        sharedCB.setSelected(!connectionConfig.isConnectionPerThread()) ;
        usernameTF.setText(connectionConfig.getUsername());
        passwordTF.setText(connectionConfig.getPassword());
        clientIDTF.setText(connectionConfig.getClientID()) ;

        if (connectionConfig.getSession().size() == 0)
        {
            connectionConfig.getSession().add(new SessionConfig());
        }
    }

    public ConnectionConfig getConnectionConfig()
    {
        return connectionConfig;
    }

    public void updateModel()
    {
        if (connectionConfig != null)
        {
            connectionConfig.setConnectionPerThread(!sharedCB.isSelected()) ;
            
            if (hasPasswordCB.isSelected())
            {
                connectionConfig.setUsername(usernameTF.getText());
                connectionConfig.setPassword(passwordTF.getText());
            }
            else
            {
                connectionConfig.setUsername(null);
                connectionConfig.setPassword(null);
            }
            
            connectionConfig.setClientID(clientIDTF.getText()) ;
        }
    }
}