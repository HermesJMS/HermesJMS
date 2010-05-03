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
import hermes.browser.HermesBrowser;
import hermes.config.ClasspathGroupConfig;
import hermes.impl.SimpleClassLoaderManager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JTable;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import com.jidesoft.grid.ContextSensitiveCellEditor;
import com.jidesoft.grid.EditorContext;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ClasspathIdCellEdtitor.java,v 1.3 2005/04/22 15:15:24 colincrist Exp $
 */
public class ClasspathIdCellEdtitor extends ContextSensitiveCellEditor
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1038218166129278067L;
	private static final Logger log = Logger.getLogger(ClasspathIdCellEdtitor.class);
    public static final EditorContext CONTEXT = new EditorContext("ClasspathId");

    private String selection = SimpleClassLoaderManager.SYSTEM_LOADER;
    private Map tableToCombo = new LRUMap(10);

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, int, int)
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean arg2, int arg3, int arg4)
    {
        try
        {
            if (tableToCombo.containsKey(table))
            {
                return (Component) tableToCombo.get(table);
            }
            else
            {
                final Vector items = new Vector();

                for (Iterator iter = HermesBrowser.getBrowser().getConfig().getClasspathGroup().iterator(); iter.hasNext();)
                {
                    ClasspathGroupConfig config = (ClasspathGroupConfig) iter.next();

                    items.add(config.getId());
                }
                
                items.add("System") ;

                final JComboBox combo = new JComboBox(items);

                combo.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent arg0)
                    {
                        selection = (String) combo.getSelectedItem();
                    }
                });

                if (value != null)
                {
                    combo.setSelectedItem(value);
                }

                log.debug("value=" + value);

                tableToCombo.put(table, combo);
                return combo;
            }
        }
        catch (HermesException e)
        {
            log.error(e.getMessage(), e);
        }

        return table;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    public Object getCellEditorValue()
    {
        return selection;
    }
}