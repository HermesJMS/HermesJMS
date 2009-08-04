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

package hermes.browser.dialog.message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: UserHeaderComboBoxModel.java,v 1.1 2005/08/14 16:33:37 colincrist Exp $
 */

public class UserHeaderComboBoxModel implements ComboBoxModel, ListModel
{
   private String selectedItem;
   private List<ListDataListener> listeners = new ArrayList<ListDataListener>();
   private String[] options = new String[] { "String", "Long", "Double", "Date", "Boolean", "Float" };
   private Class[] classes = new Class[] { String.class, Long.class, Double.class, Date.class, Float.class };

   public void setSelectedItem(Object selectedItem)
   {
      this.selectedItem = (String) selectedItem;
   }

   public Object getSelectedItem()
   {
      return selectedItem;
   }

   public int getSize()
   {
      return options.length;
   }

   public Object getElementAt(int index)
   {
      return options[index];
   }

   public Class getElementClassAt(int index)
   {
      return classes[index];
   }

   public void addListDataListener(ListDataListener l)
   {
      listeners.add(l);
   }

   public void removeListDataListener(ListDataListener l)
   {
      listeners.remove(l);
   }
}
