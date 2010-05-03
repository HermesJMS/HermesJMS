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

import hermes.JNDIContextFactory;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.config.NamingConfig;
import hermes.config.PropertySetConfig;
import hermes.impl.LoaderSupport;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.grid.Property;
import com.jidesoft.grid.PropertyPane;
import com.jidesoft.grid.PropertyTable;
import com.jidesoft.grid.PropertyTableModel;
import com.jidesoft.swing.JideScrollPane;

/**
 * Editor dialog for one of a list of NamingConfigs.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: EditNamingConfigDialog.java,v 1.4 2005/05/01 11:23:53 colincrist Exp $
 */
public class EditNamingConfigDialog extends StandardDialog
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -3108020658142737582L;
	private static final Logger log = Logger.getLogger(EditNamingConfigDialog.class);
    private static Vector selectionHistory = new Vector();

    private JPanel topPanel = new JPanel();
    private List namingConfigs;
    private String selectedConfig;
    private Map namingConfigsByName = new HashMap();
    private JComboBox comboBox;
    private JideScrollPane scrollPane = new JideScrollPane();
    private JNDIContextFactory bean;
    private Property classpathIdProperty;
    private String currentSelection;

    private NamingConfig newConfig;

    /**
     * @param parent
     * @param name
     * @param modal
     */
    public EditNamingConfigDialog(Frame parent, String selectedConfig, List namingConfigs)
    {
        super(parent, "JNDI InitialContext", true);

        this.namingConfigs = namingConfigs;
        this.selectedConfig = selectedConfig;

        setDefaultAction(new AbstractAction()
        {
            /**
			 * 
			 */
			private static final long serialVersionUID = -8231223040278773071L;

			public void actionPerformed(ActionEvent e)
            {
                onOK();
            }
        });
    }

    public EditNamingConfigDialog(Frame parent, NamingConfig newConfig, List namingConfigs)
    {
        this(parent, (String) null, namingConfigs);

        this.newConfig = newConfig;
        this.selectedConfig = newConfig.getId();

        namingConfigsByName.put(newConfig.getId(), newConfig);
    }

    protected void onOK()
    {
        try
        {
            final NamingConfig config = (NamingConfig) namingConfigsByName.get(comboBox.getSelectedItem());
            final Map map = PropertyUtils.describe(bean);

            config.setClasspathId(classpathIdProperty.getValue().toString());

            if (config.getProperties() == null)
            {
                config.setProperties(new PropertySetConfig());
            }
            else
            {
                config.getProperties().getProperty().clear();
            }

            HermesBrowser.getConfigDAO().populatePropertySet(map, config.getProperties());

            if (config == newConfig)
            {
                namingConfigs.add(config);
            }

            HermesBrowser.getBrowser().saveConfig();
            HermesBrowser.getBrowser().loadConfig();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);

            HermesBrowser.getBrowser().showErrorDialog(e);
        }
    }

    public JComponent createBannerPanel()
    {
        JLabel label = new JLabel(IconCache.getIcon(IconCache.JNDI_LARGE), JLabel.RIGHT);

        return label;
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

    private ComboBoxModel createComboBoxModel()
    {
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        if (newConfig != null)
        {
            model.addElement(newConfig.getId());
        }

        for (Iterator iter = namingConfigs.iterator(); iter.hasNext();)
        {
            NamingConfig config = (NamingConfig) iter.next();

            namingConfigsByName.put(config.getId(), config);
            model.addElement(config.getId());

            if (selectedConfig == null)
            {
                selectedConfig = config.getId();
            }
        }

        return model;

    }

    public JComponent createContentPanel()
    {
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        comboBox = new JComboBox(createComboBoxModel());

        comboBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doSelectionChanged();
            }
        });

        topPanel.add(comboBox, BorderLayout.NORTH);
        topPanel.add(scrollPane, BorderLayout.CENTER);

        comboBox.setSelectedItem(selectedConfig);

        return topPanel;
    }

    @SuppressWarnings("unchecked")
	public void doSelectionChanged()
    {
        try
        {
            final String selectedConfig = (String) comboBox.getSelectedItem();
            final NamingConfig config = (NamingConfig) namingConfigsByName.get(selectedConfig);
            final PropertySetConfig propertySet = config.getProperties();

            if (currentSelection == null || !currentSelection.equals(selectedConfig))
            {
                currentSelection = selectedConfig;

                bean = new JNDIContextFactory();

                LoaderSupport.populateBean(bean, propertySet);

                final Map properties = PropertyUtils.describe(bean);
                final List list = new ArrayList();

                classpathIdProperty = new Property("loader", "Classpath Loader to use.", String.class)
                {
                    /**
					 * 
					 */
					private static final long serialVersionUID = -3071689960943636606L;
					private String classpathId = config.getClasspathId();

                    public void setValue(Object value)
                    {
                        classpathId = value.toString();
                    }

                    public Object getValue()
                    {
                        return classpathId;
                    }

                    public boolean hasValue()
                    {
                        return true;
                    }
                };

                classpathIdProperty.setEditorContext(ClasspathIdCellEdtitor.CONTEXT);

                list.add(classpathIdProperty);

                for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();)
                {
                    final Map.Entry entry = (Map.Entry) iter.next();
                    final String propertyName = (String) entry.getKey();
                    final Object propertyValue = entry.getValue() != null ? entry.getValue() : "";

                    if (!propertyName.equals("class") && !propertyName.equals("name"))
                    {
                        Property displayProperty = new Property(propertyName, propertyName, PropertyUtils.getPropertyType(bean, propertyName))
                        {
                            /**
							 * 
							 */
							private static final long serialVersionUID = 1751773758147906036L;

							public void setValue(Object value)
                            {
                                try
                                {
                                    PropertyUtils.setProperty(bean, propertyName, value);
                                }
                                catch (Exception e)
                                {
                                    log.error(e.getMessage(), e);
                                }
                            }

                            public Object getValue()
                            {
                                try
                                {
                                    return PropertyUtils.getProperty(bean, propertyName);
                                }
                                catch (Exception e)
                                {
                                    log.error(e.getMessage(), e);
                                }

                                return null;
                            }

                            public boolean hasValue()
                            {
                                return true;
                            }
                        };

                        list.add(displayProperty);
                    }
                }

                final PropertyTableModel model = new PropertyTableModel(list);
                final PropertyTable table = new PropertyTable(model);

                table.setAutoResizeMode(PropertyTable.AUTO_RESIZE_ALL_COLUMNS);

                PropertyPane pane = new PropertyPane(table);

                pane.addPropertyChangeListener(new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent evt)
                    {

                    }
                });

                model.expandAll();

                scrollPane.setViewportView(pane);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);

            HermesBrowser.getBrowser().showErrorDialog(e);
        }
    }
}