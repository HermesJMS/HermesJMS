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

package hermes.renderers;

import hermes.browser.ConfigDialogProxy;
import hermes.browser.MessageRenderer.Config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.jidesoft.grid.Property;
import com.jidesoft.grid.PropertyPane;
import com.jidesoft.grid.PropertyTable;
import com.jidesoft.grid.PropertyTableModel;

/**
 * Some helper functions for creating MessageRenderers.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: RendererHelper.java,v 1.5 2005/05/16 16:41:37 colincrist Exp $
 */
public class RendererHelper
{

    private static final Logger cat = Logger.getLogger(RendererHelper.class);

    /**
     * Create a default renderer for a Config that just contains simple
     * properties (i.e. not 1:m relationships), the dialog will be a normal
     * property pane.
     * 
     * @param dialogProxy
     * @return @throws
     *         IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static JComponent createDefaultConfigPanel(final ConfigDialogProxy dialogProxy) throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException
    {
        final Config theConfig = dialogProxy.getConfig();
        final List<Property> list = new ArrayList<Property>();
        final Map properties = PropertyUtils.describe(theConfig);

        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iter.next();
            final String propertyName = (String) entry.getKey();
            final Object propertyValue = entry.getValue();

            if (!propertyName.equals("class") && !propertyName.equals("name"))
            {
                Property displayProperty = new Property(propertyName, theConfig.getPropertyDescription(propertyName), propertyValue.getClass())
                {
                    /**
					 * 
					 */
					private static final long serialVersionUID = -4650355524853942976L;

					public void setValue(Object value)
                    {
                        try
                        {
                            dialogProxy.setDirty() ;
                            
                            PropertyUtils.setProperty(theConfig, propertyName, value);
                        }
                        catch (Exception e)
                        {
                            cat.error(e.getMessage(), e);
                        }
                    }

                    public Object getValue()
                    {
                        try
                        {
                            return PropertyUtils.getProperty(theConfig, propertyName);
                        }
                        catch (Exception e)
                        {
                            cat.error(e.getMessage(), e);
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

        PropertyTableModel model = new PropertyTableModel(list);
        PropertyTable table = new PropertyTable(model);

        table.setAutoResizeMode(PropertyTable.AUTO_RESIZE_ALL_COLUMNS);

        PropertyPane pane = new PropertyPane(table);

        pane.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                dialogProxy.setDirty() ;
            }
        }) ;
        
        model.expandAll();

        return pane;
    }

}