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

package hermes.browser;

import java.awt.Component;

import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JRadioButtonRenderer.java,v 1.1 2004/05/01 15:52:35 colincrist
 *          Exp $
 */

public class JRadioButtonRenderer implements TableCellRenderer
{
    private TableCellRenderer defaultRenderer;

    public JRadioButtonRenderer(TableCellRenderer defaultRenderer)
    {
        this.defaultRenderer = defaultRenderer;
    }

    /**
     * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object,
     *      boolean, boolean, int, int)
     */

    public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4, int arg5)
    {
        if (arg1 instanceof JRadioButton)
        {
            return (Component) arg1;
        }
        else
        {
            return defaultRenderer.getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5);
        }
    }

}