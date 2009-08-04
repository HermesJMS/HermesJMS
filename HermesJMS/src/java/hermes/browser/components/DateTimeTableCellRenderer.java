/* 
 * Copyright 2007 Colin Crist
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

import java.awt.Component;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class DateTimeTableCellRenderer extends DefaultTableCellRenderer
{

   private FastDateFormat formatter ;
  
   
   public DateTimeTableCellRenderer()
   {
      formatter = FastDateFormat.getInstance("EEE, d MMM yyyy HH:mm:ss") ;
   }
      
   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
   {
      super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
      if ( value instanceof Date ){
    
      String strDate = formatter.format((Date)value);
     
      this.setText( strDate );
      }
      return this ;
   }

}
