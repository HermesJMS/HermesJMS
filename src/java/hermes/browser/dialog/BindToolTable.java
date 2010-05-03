/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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

import hermes.browser.model.BindToolTableModel;

import java.awt.Component;

import javax.swing.table.TableCellRenderer;

import com.jidesoft.grid.JideTable;

public class BindToolTable extends JideTable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6141993553358788905L;

	public BindToolTable(BindToolTableModel model)
    {
        super(model) ;

        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    public Component prepareRenderer(TableCellRenderer renderer, int y, int x)
    {
        Component rval = super.prepareRenderer(renderer, y, x);
        
        if (x == 0)
        {
            getColumnModel().getColumn(0).setPreferredWidth(rval.getPreferredSize().width) ;
            getColumnModel().getColumn(0).setMaxWidth(rval.getPreferredSize().width) ;
            getColumnModel().getColumn(0).setMinWidth(rval.getPreferredSize().width) ;
        }
        
        return rval ;
    }
}
