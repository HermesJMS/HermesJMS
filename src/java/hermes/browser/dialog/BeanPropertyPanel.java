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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

/**
 * A panel showing a beans simple properties.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: BeanPropertyPanel.java,v 1.4 2005/07/19 07:15:01 colincrist Exp
 *          $
 */
public class BeanPropertyPanel extends MapPropertyPanel {
	private static final long serialVersionUID = 4415416943574076763L;
	private static final Logger log = Logger.getLogger(BeanPropertyPanel.class);
	private List onOK = new ArrayList();
	private Object bean;
	private boolean setProperty = true;

	public BeanPropertyPanel(Object bean, boolean editable, boolean setProperty) throws IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {
		super(PropertyUtils.describe(bean), editable);

		
		this.bean = bean;
		this.setProperty = setProperty;
	}

	public BeanPropertyPanel(Object bean) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		this(bean, false, true);
	}

	protected JComponent createNorthComponent() {
		JLabel classNameLabel = new JLabel("Class: " + bean.getClass().getName());
		classNameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return classNameLabel;
	}

	protected void onSetProperty(String propertyName, Object propertyValue) {
		try {
			if (setProperty) {
				PropertyUtils.setProperty(bean, propertyName, propertyValue);
			}

			super.onSetProperty(propertyName, propertyValue);
		} catch (Throwable e) {
			HermesBrowser.getBrowser().showErrorDialog("Cannot set property " + propertyName, e);
		}
	}
}