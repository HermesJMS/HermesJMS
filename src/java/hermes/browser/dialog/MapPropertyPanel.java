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

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.CellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

import com.jidesoft.grid.PropertyPane;
import com.jidesoft.grid.PropertyTable;
import com.jidesoft.grid.PropertyTableModel;

/**
 * An editable panel of properties.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: MapPropertyPanel.java,v 1.6 2005/08/21 20:48:06 colincrist Exp
 *          $
 */
public class MapPropertyPanel extends JPanel {
	private static final long serialVersionUID = 2789449525269251165L;
	private static final Logger log = Logger.getLogger(MapPropertyPanel.class);

	private List<Runnable> onOK = new ArrayList<Runnable>();
	private Map<String, Object> map;
	private Map<String, Object> changes = new HashMap<String, Object>();
	private boolean editable = false;
	private String title;

	public MapPropertyPanel(String title, Map map, boolean editable) {
		this(map, editable);
		this.title = title;
	}

	public MapPropertyPanel(Map<String, Object> map, boolean editable) {
		this.map = map;
		this.editable = editable;
	}

	public MapPropertyPanel(Map map) {
		this(map, false);
	}

	protected void doOnOK(Runnable r) {
		onOK.add(r);
	}

	protected void onSetProperty(String propertyName, Object propertyValue) {
		log.debug("setting " + propertyName + " = " + propertyValue);

		map.put(propertyName, propertyValue);
		changes.put(propertyName, propertyValue);
	}

	protected JComponent createNorthComponent() {
		if (title == null) {
			return new JLabel();
		} else {
			JLabel classNameLabel = new JLabel(title);
			classNameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			return classNameLabel;
		}
	}

	@SuppressWarnings("unchecked")
	protected JComponent createCenterComponent() {
		List<JidePropertyImpl> model = new ArrayList<JidePropertyImpl>();

		try {

			for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();) {
				final String propertyName = iter.next();

				if (propertyName.equals("class")) {
					// NOP
				} else {
					Object propertyValue = map.get(propertyName);

					if (propertyValue == null) {
						propertyValue = "";
					}

					final JidePropertyImpl pConfig = new JidePropertyImpl(propertyName, propertyName + " [" + getPropertyType(propertyName).getName() + "]",
							getPropertyType(propertyName), propertyValue) {
						/**
				 * 
				 */
						private static final long serialVersionUID = -1529987569514922256L;

						public void setValue(final Object newValue) {
							super.setValue(newValue);

							log.debug("setValue propertyName=" + propertyName + ", newValue=" + newValue);

							doOnOK(new Runnable() {
								public void run() {
									onSetProperty(propertyName, newValue);
								}
							});
						}
					};

					pConfig.setEditable(editable);

					if (editable) {
						onOK.add(new Runnable() {
							public void run() {
								map.put(propertyName, pConfig.getValue());
							}
						});
					}

					model.add(pConfig);
				}
			}
		} catch (IllegalAccessException e) {
			log.error(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			log.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			log.error(e.getMessage(), e);
		}

		//
		// Build the model and create the table...

		PropertyTableModel<JidePropertyImpl> propertyTableModel = new PropertyTableModel<JidePropertyImpl>(model);

		PropertyTable propertyTable = new PropertyTable(propertyTableModel);
		propertyTable.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				if (!e.isTemporary()) {
					JTable t = (JTable) e.getSource();
					CellEditor ce = t.getCellEditor();
					if (ce != null) {
						ce.stopCellEditing();
					}
				}
			}

			public void focusGained(FocusEvent e) {

			}
		});

		propertyTable.expandAll();
		PropertyPane propertyPane = new PropertyPane(propertyTable);

		return propertyPane;
	}

	public void init() {
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		setLayout(new BorderLayout());

		add(createNorthComponent(), BorderLayout.NORTH);
		add(createCenterComponent(), BorderLayout.CENTER);
	}

	public Map<String, Object> getChanges() {
		return changes;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void doOK() {
		for (Iterator<Runnable> iter = onOK.iterator(); iter.hasNext();) {
			Runnable r = iter.next();
			r.run();
		}
	}

	public void doCancel() {
		// NOP
	}

	private Class getPropertyType(String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (map.get(propertyName) == null) {
			return String.class;
		}

		return map.get(propertyName).getClass();
	}

	public void addOKAction(Runnable r) {
		onOK.add(r);
	}

	public boolean isEditable() {
		return editable;
	}
}