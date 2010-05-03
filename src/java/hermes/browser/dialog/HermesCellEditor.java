/* 
 * Copyright 2008 Colin Crist
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

import hermes.Hermes;
import hermes.HermesRuntimeException;
import hermes.browser.HermesBrowser;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.swing.JComboBox;
import javax.swing.JTable;

import org.apache.log4j.Logger;

import com.jidesoft.grid.ContextSensitiveCellEditor;
import com.jidesoft.grid.EditorContext;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */
public class HermesCellEditor extends ContextSensitiveCellEditor
{
   private static final Logger log = Logger.getLogger(HermesCellEditor.class);
   private static final long serialVersionUID = 2889094946400633095L;

   public static final EditorContext CONTEXT = new EditorContext(Hermes.class.getName());

   public String selection = "System";

   /*
    * (non-Javadoc)
    * 
    * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
    *      java.lang.Object, boolean, int, int)
    */
   public Component getTableCellEditorComponent(JTable arg0, Object arg1, boolean arg2, int arg3, int arg4)
   {
      try
      {
         final Vector<String> options = new Vector<String>();
         final Context ctx = HermesBrowser.getBrowser().getContext();

         for (NamingEnumeration names = ctx.listBindings(""); names.hasMore();)
         {
            final Binding b = (Binding) names.next();

            if (b.getObject() instanceof Hermes)
            {
               options.add(b.getName());
            }
         }      

         final JComboBox combo = new JComboBox(options);

         combo.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent arg0)
            {

               selection = (String) combo.getSelectedItem();

            }
         });

         return combo;
      }
      catch (NamingException ex)
      {
         throw new HermesRuntimeException(ex);
      }
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