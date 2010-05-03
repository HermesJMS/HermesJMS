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

package hermes.browser.components;

import hermes.browser.model.QueueWatchTableModel;
import hermes.browser.model.WatchInfo;
import hermes.util.TextUtils;

import java.awt.Color;
import java.awt.Component;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

import com.jidesoft.grid.ExpandablePanel;
import com.jidesoft.grid.HierarchicalTable;

/**
 * @author colincrist@hermesjms.com
 */
public class WatchTable extends HierarchicalTable
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -1072138733454263145L;
private static final Logger log = Logger.getLogger(WatchTable.class);
   private QueueWatchTableModel watchModel;

   public WatchTable(QueueWatchTableModel model, final boolean showAge)
   {
      super(model);

      this.watchModel = model;

      DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer()
      {
         /**
		 * 
		 */
		private static final long serialVersionUID = 2791021835238102467L;
		private FastDateFormat formatter = FastDateFormat.getInstance("EEE, d MMM yyyy HH:mm:ss");

         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
         {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value == null)
            {
               setText("");
            }
            else if (value instanceof Date)
            {

               if (showAge)
               {
                  super.setText(TextUtils.getAge((Date) value));
               }
               else
               {
                  String strDate = formatter.format((Date) value);

                  super.setText(strDate);

               }

            }
            return this;
         }

         protected void setValueXXX(Object value)
         {
            if (value == null)
            {
               super.setText("");

            }
            else if (showAge)
            {
               super.setText(TextUtils.getAge((Date) value));
            }
            else
            {
               super.setText(value.toString());

            }
         }
      };

     
      setDefaultRenderer(Date.class, dateRenderer);

   }

   /**
    * Rows are: Color.RED if an alert level has been breached. Color.ORANGE if
    * an exception has occured gathering the data
    */
   public Component prepareRenderer(TableCellRenderer renderer, int y, int x)
   {
      final Component rval = super.prepareRenderer(renderer, y, x);
      final int originalY = y;
      y = getActualRowAt(y);

      final WatchInfo info = watchModel.getRow(y);
      Component c = null;

      /*
       * I don't understand this bit - unless I get the actual renderer when the
       * component is an ExpandablePanel the first column shades incorrectly.
       */

      if (rval instanceof ExpandablePanel)
      {
         ExpandablePanel ep = (ExpandablePanel) rval;
         c = ep.getActualRenderer();
      }
      else
      {
         c = rval;
      }

      boolean previousInAlert = info.isInAlert();

      if (info != null)
      {
         if (info.getE() != null)
         {
            info.setInAlert(true);
            c.setBackground(new Color(255, 255, 153));
            c.setForeground(Color.BLACK);
         }
         else if (info.getDepthAlert() != 0 && (info.getDepth() > info.getDepthAlert()))
         {
            info.setInAlert(true);
            c.setBackground(new Color(255, 204, 153));
            c.setForeground(Color.BLACK);
         }
         else if (info.getAgeAlert() != 0 && (System.currentTimeMillis() - info.getAgeAlert() > info.getAgeAlert()))
         {
            info.setInAlert(true);
            c.setBackground(new Color(255, 204, 153));
            c.setForeground(Color.BLACK);
         }
         else if (originalY % 2 == 0 && !isCellSelected(originalY, x))
         {
            info.setInAlert(false);
            c.setBackground(Color.LIGHT_GRAY);
            c.setForeground(Color.BLACK);

         }
         else if (!isCellSelected(originalY, x))
         {
            //
            // If not shaded, match the table's background

            info.setInAlert(false);
            c.setBackground(Color.WHITE);
            c.setForeground(Color.BLACK);
         }
      }

      return rval;
   }

}