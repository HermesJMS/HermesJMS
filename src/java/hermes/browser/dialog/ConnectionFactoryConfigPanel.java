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
import hermes.HermesRuntimeException;
import hermes.JNDIConnectionFactory;
import hermes.JNDIQueueConnectionFactory;
import hermes.JNDITopicConnectionFactory;
import hermes.NullConnectionFactory;
import hermes.SingletonManager;
import hermes.browser.HermesBrowser;
import hermes.browser.model.PropertySetTableModel;
import hermes.config.FactoryConfig;
import hermes.config.HermesConfig;
import hermes.config.ProviderConfig;
import hermes.impl.ClassLoaderManager;
import hermes.impl.SimpleClassLoaderManager;
import hermes.providers.file.FileConnectionFactory;
import hermes.util.ReflectUtils;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.jidesoft.grid.SortableTable;
import com.jidesoft.swing.JideScrollPane;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ConnectionFactoryConfigPanel.java,v 1.7 2004/07/30 17:25:15
 *          colincrist Exp $
 */

public class ConnectionFactoryConfigPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8695311283530530183L;
	private static final String CLASS = "class";
	private static final String CONNECTION_FACTORY = "Connection Factory";
	private static final String REMOVE_PROPERTY = "Remove property";
	private static final String ADD_PROPERTY = "Add property";
	private static final Logger log = Logger.getLogger(ConnectionFactoryConfigPanel.class);
	private static final Set<String> defaultFactories = new HashSet<String>();

	private final PreferencesDialog dialog;
	private FactoryConfig factoryConfig;

	private final SortableTable propertyTable = new SortableTable();

	private final JComboBox connectionFactoryComboBox = new JComboBox();
	private final DefaultComboBoxModel connectionFactoryComboBoxModel = new DefaultComboBoxModel();

	private final JideScrollPane propertyTableSP = new JideScrollPane();
	private JComboBox propertySelectionComboBox;
	private final JComboBox classLoaderComboBox = new JComboBox();

	private PropertySetTableModel propertyTableModel;
	private ConnectionFactory bean;

	static {
		defaultFactories.add(NullConnectionFactory.class.getName());
		defaultFactories.add(JNDITopicConnectionFactory.class.getName());
		defaultFactories.add(JNDIQueueConnectionFactory.class.getName());
		defaultFactories.add(JNDIConnectionFactory.class.getName());
		defaultFactories.add(FileConnectionFactory.class.getName());
	}

	public ConnectionFactoryConfigPanel(PreferencesDialog dialog) {
		this.dialog = dialog;

		init();
	}

	public String getLoader() {
		return (String) classLoaderComboBox.getSelectedItem();
	}

	public void init() {
		final Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(border, CONNECTION_FACTORY));

		connectionFactoryComboBox.setModel(connectionFactoryComboBoxModel);

		propertyTableSP.setViewportView(propertyTable);
		propertyTable.setSortable(true);

		JPanel topPanel = new JPanel();
		BoxLayout layout = new BoxLayout(topPanel, BoxLayout.LINE_AXIS);
		topPanel.setLayout(layout);

		JLabel classLabel = new JLabel("Class:", JLabel.RIGHT);
		JLabel loaderLabel = new JLabel("Loader:", JLabel.RIGHT);

		classLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		loaderLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		topPanel.add(classLabel);
		topPanel.add(connectionFactoryComboBox);
		topPanel.add(loaderLabel);
		topPanel.add(classLoaderComboBox);

		add(topPanel, BorderLayout.NORTH);
		add(propertyTableSP, BorderLayout.CENTER);

		final JPopupMenu popupMenu = new JPopupMenu();
		final JMenuItem addItem = new JMenuItem(ADD_PROPERTY);
		final JMenuItem removeItem = new JMenuItem(REMOVE_PROPERTY);

		popupMenu.add(addItem);
		popupMenu.add(removeItem);

		addItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					propertyTableModel.insertRow();
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		});

		removeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (propertyTable.getSelectedRow() != -1) {
					propertyTableModel.removeRow(propertyTable.getSelectedRow());
				}
			}
		});

		final MouseAdapter m = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					if (e.getComponent() == propertyTableSP) {
						removeItem.setEnabled(false);
					} else {
						removeItem.setEnabled(true);
					}

					if (propertySelectionComboBox.getModel().getSize() == 0) {
						addItem.setEnabled(false);
					} else {
						addItem.setEnabled(true);
					}

					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		};

		propertyTableSP.addMouseListener(m);
		propertyTable.addMouseListener(m);
		propertyTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		connectionFactoryComboBoxModel.addListDataListener(new ListDataListener() {
			@Override
			public void contentsChanged(ListDataEvent arg0) {
				final String className = (String) connectionFactoryComboBoxModel.getSelectedItem();

				try {
					if (propertyTableModel != null) {
						String classLoaderId = (String) classLoaderComboBox.getSelectedItem();
						ClassLoaderManager classLoaderManager = (ClassLoaderManager) SingletonManager.get(ClassLoaderManager.class);
						ClassLoader classLoader = classLoaderManager.getClassLoader(classLoaderId);

						bean = ReflectUtils.createConnectionFactory(classLoader.loadClass(className));

						propertyTableModel.setBean(bean);
						updateCellEditor();
						dialog.setDirty();
					}
				} catch (Throwable e) {
					HermesBrowser.getBrowser().showErrorDialog(e);

					log.error(e.getMessage(), e);
				}
			}

			@Override
			public void intervalAdded(ListDataEvent arg0) {
				// NOP
			}

			@Override
			public void intervalRemoved(ListDataEvent arg0) {
				// NOP
			}
		});

	}

	private void updateConectionFactories() {
		String classpathGroupId = (String) classLoaderComboBox.getSelectedItem();
		ClassLoaderManager classLoaderManager = (ClassLoaderManager) SingletonManager.get(ClassLoaderManager.class);
		if (classpathGroupId == null) {
			classpathGroupId = factoryConfig.getClasspathId();
		}

		Set<String> factories = new HashSet<String>();

		if (classLoaderManager.getFactories(classpathGroupId) != null) {
			factories.addAll(classLoaderManager.getFactories(classpathGroupId));
		}

		factories.addAll(defaultFactories);

		connectionFactoryComboBoxModel.removeAllElements();

		if (!factories.contains(factoryConfig.getProvider().getClassName())) {
			log.error("factory lost!");
		} else {
			connectionFactoryComboBoxModel.addElement(factoryConfig.getProvider().getClassName());
			connectionFactoryComboBoxModel.setSelectedItem(factoryConfig.getProvider().getClassName());
		}

		for (final String factoryClassName : factories) {
			if (connectionFactoryComboBoxModel.getIndexOf(factoryClassName) == -1) {
				connectionFactoryComboBoxModel.addElement(factoryClassName);
			}
		}
	}

	public FactoryConfig getFactoryConfig() {
		return factoryConfig;
	}

	public void setFactoryConfig(final HermesConfig hConfig, FactoryConfig newFactory) {
		//
		// If there is a new factory being defined, copy over the OLD class for
		// reuse.

		if (newFactory.getProvider() == null) {
			newFactory.setProvider(new ProviderConfig());
			newFactory.getProvider().setClassName(factoryConfig.getProvider().getClassName());
		}

		this.factoryConfig = newFactory;

		if (factoryConfig.getProvider().getClassName() == null) {
			factoryConfig.getProvider().setClassName(NullConnectionFactory.class.getName());
		}

		if (factoryConfig.getClasspathId() == null) {
			factoryConfig.setClasspathId(SimpleClassLoaderManager.SYSTEM_LOADER);
		}

		final DefaultComboBoxModel classLoaderModel = new DefaultComboBoxModel();
		final ClassLoaderManager classLoaderManager = (ClassLoaderManager) SingletonManager.get(ClassLoaderManager.class);

		for (Iterator iter = classLoaderManager.getIds().iterator(); iter.hasNext();) {
			classLoaderModel.addElement(iter.next());
		}

		classLoaderModel.addElement(SimpleClassLoaderManager.SYSTEM_LOADER);

		classLoaderComboBox.setModel(classLoaderModel);
		classLoaderComboBox.setSelectedItem(factoryConfig.getClasspathId());

		updateConectionFactories();

		classLoaderModel.addListDataListener(new ListDataListener() {
			@Override
			public void contentsChanged(ListDataEvent e) {
				try {
					updateConectionFactories();
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}

			@Override
			public void intervalAdded(ListDataEvent e) {
				// NOP
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				// NOP
			}
		});

		ConnectionFactory bean = null;

		ClassLoader classLoader = getClass().getClassLoader();

		try {
			Class clazz = classLoaderManager.getClassLoader(factoryConfig.getClasspathId()).loadClass(factoryConfig.getProvider().getClassName());
			bean = ReflectUtils.createConnectionFactory(clazz);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			HermesBrowser.getBrowser().showErrorDialog("Cannot find class " + factoryConfig.getProvider().getClassName() + " in loader " + factoryConfig.getClasspathId());
		}

		if (bean != null) {
			try {

				if (factoryConfig.getProvider().getProperties() == null) {
					try {
						factoryConfig.getProvider().setProperties(HermesBrowser.getBrowser().getConfigDAO().createPropertySet());
					} catch (HermesException ex) {
						throw new HermesRuntimeException(ex);
					}
				}

				propertyTableModel = new PropertySetTableModel(bean, factoryConfig.getProvider().getProperties(), new HashSet());
				propertyTable.setModel(propertyTableModel);

				updateCellEditor();
			} catch (Throwable ex) {
				HermesBrowser.getBrowser().showErrorDialog("Problems accessing " + factoryConfig.getProvider().getClassName(), ex);
			}
		}

		propertyTableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent arg0) {
				dialog.setDirty();
			}
		});
	}

	public void updateCellEditor() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (propertyTable.getColumnModel().getColumnCount() > 0 && bean != null) {
			final TableColumn propertyNameColumn = propertyTable.getColumnModel().getColumn(0);

			propertySelectionComboBox = new JComboBox();
			boolean isJNDI = false;

			for (Iterator iter = propertyTableModel.getValidProperties().iterator(); iter.hasNext();) {
				String name = (String) iter.next();

				propertySelectionComboBox.addItem(name);
			}

			propertyNameColumn.setCellEditor(new DefaultCellEditor(propertySelectionComboBox));
		}
	}

	public void updateModel() {
		try {
			// propertyTable.getCellEditor().stopCellEditing() ;

			if (propertyTableModel != null && bean != null) {
				factoryConfig.getProvider().getProperties().getProperty().clear();
				factoryConfig.getProvider().getProperties().getProperty().addAll(propertyTableModel.getProperties());
				factoryConfig.setClasspathId((String) classLoaderComboBox.getSelectedItem());
				factoryConfig.getProvider().setClassName((String) connectionFactoryComboBox.getSelectedItem());
			}
		} catch (JAXBException e) {
			log.error(e.getMessage(), e);
		}
	}
}