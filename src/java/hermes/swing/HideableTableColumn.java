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

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: HideableTableColumn.java,v 1.1 2005/06/17 14:35:04 colincrist Exp $
 */

public class HideableTableColumn extends TableColumn
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 5734446336194089640L;
private boolean visible = true;
   private int visibleWidth = 0;

   public HideableTableColumn()
   {
      super();
   }

   public HideableTableColumn(boolean visible)
   {
      super();
      
      this.visible = visible ;
   }

   public HideableTableColumn(int modelIndex, int width, TableCellRenderer cellRenderer, TableCellEditor cellEditor, boolean visible)
   {
      super(modelIndex, width, cellRenderer, cellEditor);
      
      this.visible = visible ;
   }

   public HideableTableColumn(int modelIndex, int width, boolean visible)
   {
      super(modelIndex, width);
      
      this.visible = visible ;
   }

   public HideableTableColumn(int modelIndex, boolean visible)
   {
      super(modelIndex);
      
      this.visible = visible ;
   }

   public boolean isVisiible()
   {
      return visible;
   }

   public void setVisible(boolean visible)
   {
      this.visible = visible;

      if (visible)
      {
         setWidth(visibleWidth);
      }
      else
      {
         visibleWidth = getWidth();
         setWidth(0);
      }
   }

  
   @Override
   public Object getHeaderValue()
   {
      // TODO Auto-generated method stub
      return super.getHeaderValue();
   }
   
}
