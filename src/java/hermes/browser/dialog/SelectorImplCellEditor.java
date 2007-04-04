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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JTable;

import com.jidesoft.grid.ContextSensitiveCellEditor;
import com.jidesoft.grid.EditorContext;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: SelectorImplCellEditor.java,v 1.2 2006/07/13 07:35:32 colincrist Exp $
 */
public class SelectorImplCellEditor extends ContextSensitiveCellEditor
{
    private static final long serialVersionUID = 2889094946400633095L;
    private static Object options[] = { SelectorImpl.JAMSEL};

    public static final EditorContext CONTEXT = new EditorContext("SelectorImpl");

    public SelectorImpl selection = SelectorImpl.JAMSEL ;

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, int, int)
     */
    public Component getTableCellEditorComponent(JTable arg0, Object arg1, boolean arg2, int arg3, int arg4)
    {
        final JComboBox combo = new JComboBox(options);

        combo.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                selection = (SelectorImpl) combo.getSelectedItem();
            }
        });

        return combo;
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