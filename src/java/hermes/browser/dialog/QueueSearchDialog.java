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
import hermes.browser.actions.BrowserAction;
import hermes.config.DestinationConfig;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.jms.JMSException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: QueueSearchDialog.java,v 1.6 2005/06/20 15:28:35 colincrist Exp $
 */
public class QueueSearchDialog extends StandardDialog
{
    private static final Logger log = Logger.getLogger(QueueSearchDialog.class);
    private static Vector selectionHistory = new Vector();

    private JPanel topPanel = new JPanel();
    private Hermes hermes;   
    private JLabel info = new JLabel("Enter the string or regular expression to search messages for.");;
    private JComboBox stringCombo;
    private JCheckBox stringCheckBox = new JCheckBox("Simple string search");
    private JCheckBox regexCheckBox = new JCheckBox("Regular expression search");
    private JCheckBox userHeaderCheckBox = new JCheckBox("Search user header properties");
    private JCheckBox jmsHeaderCheckBox = new JCheckBox("Search JMS header poperties");
    private DestinationConfig destinationConfig ;

    /**
     * @param parent
     * @param name
     * @param modal
     */
    public QueueSearchDialog(Frame parent, Hermes hermes, DestinationConfig destinationConfig)
    {
        super(parent, "Search queue/topic", true);

        this.hermes = hermes;
        this.destinationConfig = destinationConfig;
        

        setDefaultAction(new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        });
    }

    public QueueSearchDialog(Frame parent, Hermes hermes)
    {
        super(parent, "Search all queues on " + hermes.getId(), true);

        this.hermes = hermes;

        setDefaultAction(new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        });
    }

    protected void onOK()
    {
        log.debug("h=" + getSize().getHeight() + " w=" + getSize().getWidth());

        String selection = stringCombo.getSelectedItem().toString();

        if (selection != null && !selection.equals(""))
        {
            try
            {
                BrowserAction action;

                String destinationName = destinationConfig != null ? destinationConfig.getName() : null ;
                Domain domain = destinationConfig != null ? Domain.getDomain(destinationConfig.getDomain()) : Domain.QUEUE ;
                
                if (regexCheckBox.isSelected())
                {
                    action = HermesBrowser.getBrowser().getActionFactory().createRegexQueueBrowseAction(hermes, destinationName, domain, selection);
                }
                else
                {
                    action = HermesBrowser.getBrowser().getActionFactory().createStringSeachQueueBrowseAction(hermes, destinationName, domain, selection,
                            userHeaderCheckBox.isSelected());
                }

                selectionHistory.add(selection) ;
            }
            catch (JMSException e)
            {
                log.error(e.getMessage(), e);

                HermesBrowser.getBrowser().showErrorDialog(e);
            }
        }
    }

    public JComponent createBannerPanel()
    {
        return new JLabel();
    }

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
                onOK();
                dispose();

            }
        });

        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        });

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    public JComponent createContentPanel()
    {
        Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);

        topPanel.setLayout(new BorderLayout());

        //
        // The combo box panel...

        JPanel comboPanel = new JPanel();

        comboPanel.setLayout(new GridLayout(1, 1));
        comboPanel.setBorder(BorderFactory.createTitledBorder(border, "Search for"));
        stringCombo = new JComboBox(selectionHistory);
        stringCombo.setEditable(true);

        comboPanel.add(stringCombo);

        //
        // The check box panel...

        JPanel checkBoxPanel = new JPanel();

        checkBoxPanel.setBorder(BorderFactory.createTitledBorder(border, "Options"));

        checkBoxPanel.setLayout(new GridLayout(4, 1));
        checkBoxPanel.add(stringCheckBox);
        checkBoxPanel.add(regexCheckBox);
        checkBoxPanel.add(userHeaderCheckBox);
        checkBoxPanel.add(jmsHeaderCheckBox);

        topPanel.add(comboPanel, BorderLayout.NORTH);
        topPanel.add(checkBoxPanel, BorderLayout.SOUTH);

        // 
        // Actions...

        stringCheckBox.setSelected(true);
        regexCheckBox.setSelected(false);

        stringCheckBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                regexCheckBox.setSelected(!stringCheckBox.isSelected());
            }
        });

        regexCheckBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                stringCheckBox.setSelected(!regexCheckBox.isSelected());
            }
        });

        userHeaderCheckBox.setSelected(false);
        jmsHeaderCheckBox.setSelected(false);
        jmsHeaderCheckBox.setEnabled(false);

        setSize(new Dimension(430, 250));
        setResizable(false);

        return topPanel;
    }
}