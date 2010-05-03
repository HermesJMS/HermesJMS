/* 
 * Copyright 2003,2004,2005 Colin Crist
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

package hermes.fix;

import hermes.swing.Colours;
import hermes.swing.URLRenderer;
import hermes.swing.URLRendererMouseListener;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import com.jidesoft.grid.SortableTable;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXMessageViewTable.java,v 1.1 2006/05/13 14:06:55 colincrist
 *          Exp $
 */

public class FIXMessageViewTable extends SortableTable
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -7557105531629435884L;
private FIXMessageViewTableModel model;

   public FIXMessageViewTable(FIXMessageViewTableModel model)
   {
      super(model);

      this.model = model;

      setSortable(true);

      int block = 25;

      getColumn(FIXMessageViewTableModel.FIELD).setPreferredWidth(block);
      getColumn(FIXMessageViewTableModel.NAME).setPreferredWidth(block * 4);
      getColumn(FIXMessageViewTableModel.VALUE).setPreferredWidth(block * 4);
      getColumn(FIXMessageViewTableModel.DESCRIPTION).setPreferredWidth(block * 8);

      getColumn(FIXMessageViewTableModel.FIELD).setCellRenderer(new URLRenderer());
      getColumn(FIXMessageViewTableModel.NAME).setCellRenderer(new URLRenderer());

      addMouseListener(new URLRendererMouseListener(this));
      
      setSelectionMode(ListSelectionModel.SINGLE_SELECTION) ;
   }

   public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
   {
      final Component c = super.prepareRenderer(renderer, row, column);
      final FIXMessageViewTableModel.RowType rowType = model.getRowType(getActualRowAt(row));

      if (!isCellSelected(row, column))
      {
         switch (rowType)
         {
            case HEADER:
               c.setBackground(Colours.WHITESMOKE);
               break;

            case TRAILER:
               c.setBackground(Colours.WHITESMOKE);
               break;

            case APPLICATION:
               c.setBackground(Colours.POWDER_BLUE); 
               break;
         }
      }

      switch (column)
      {
         case 0:
            ((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
            break;
      }

      c.setForeground(Color.BLACK);

      return c;
   }
}
