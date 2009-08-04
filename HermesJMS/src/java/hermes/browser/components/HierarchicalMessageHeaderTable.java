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

package hermes.browser.components;

import hermes.browser.actions.BrowserAction;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.log4j.Category;

import com.jidesoft.grid.HierarchicalTable;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: HierarchicalMessageHeaderTable.java,v 1.2 2004/07/30 17:25:13
 *          colincrist Exp $
 */
public class HierarchicalMessageHeaderTable extends HierarchicalTable
{
    private static final Category cat = Category.getInstance(HierarchicalMessageHeaderTable.class);
    private DataFlavor[] myFlavours;

    public HierarchicalMessageHeaderTable(BrowserAction action, TableModel model)
    {
        super(model);

        setSortable(true);
        setDragEnabled(true);
        //MessageHeaderTableSupport.init(action, this, myFlavours);
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
    {
        return MessageHeaderTableSupport.prepareRenderer(super.prepareRenderer(renderer, row, column), this, renderer, row, column);
    }

}