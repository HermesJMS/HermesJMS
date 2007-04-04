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

package hermes.swing;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: URLRenderer.java,v 1.3 2006/07/17 21:20:53 colincrist Exp $
 */

public class URLRenderer extends DefaultTableCellRenderer
{
   private String data;

   public URLRenderer()
   {
      super();
   }

   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
   {
      data = value != null ? value.toString() : "" ;

      Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

      String link = "<html>" + "<a href=\"x\">" + data + "</a>" + "</html>";

      setText(link);

      return this;
   }

   public String getData()
   {
      return data;
   }

}
