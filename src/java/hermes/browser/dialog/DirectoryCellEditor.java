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
import java.io.File;

import javax.swing.JTable;
import javax.swing.JTextField;

import com.jidesoft.grid.ContextSensitiveCellEditor;
import com.jidesoft.grid.EditorContext;
import com.jidesoft.swing.FolderChooser;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DirectoryCellEditor.java,v 1.1 2004/05/01 15:52:36 colincrist
 *          Exp $
 */
public class DirectoryCellEditor extends ContextSensitiveCellEditor
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -6692802620713140973L;

public static final EditorContext CONTEXT = new EditorContext("Directory");

   private FolderChooser chooser;
   private File file ;
   
   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
    *      java.lang.Object, boolean, int, int)
    */
   public Component getTableCellEditorComponent(JTable table, Object arg1, boolean arg2, int arg3, int arg4)
   {
      final JTextField textField = new JTextField();
      file = (File) arg1;

      if (file != null)
      {
         textField.setText(file.getAbsolutePath());

         chooser = new FolderChooser((File) arg1);

         chooser.setSelectedFile(file);
         
         if (chooser.showDialog(table, "Select directory") == FolderChooser.APPROVE_OPTION)
         {
            file = chooser.getSelectedFile();
         }
      }

      return textField;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.CellEditor#getCellEditorValue()
    */
   public Object getCellEditorValue()
   {
      return file;
   }

}