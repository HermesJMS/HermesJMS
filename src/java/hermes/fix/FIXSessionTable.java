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


import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.jidesoft.grid.HierarchicalTable;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXSessionTable.java,v 1.8 2006/08/01 07:29:35 colincrist Exp $
 */

public class FIXSessionTable extends HierarchicalTable
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 6072049287321615441L;

private static final Logger log = Logger.getLogger(FIXSessionTable.class);

   private FIXSessionTableModel model;

   public FIXSessionTable(FIXSessionTableModel model)
   {
      super(model);

      this.model = model;

      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setSingleExpansion(false);
      setAutoscrolls(true);

   }

   public SessionKey getSessionKey(int row)
   {
      return model.getSessionKey(getActualRowAt(row)) ;
   }
   
   public Object getChildValueAt(int row)
   {
      return model.getChildValueAt(row);
   }

   public void addMessages(final Collection<FIXMessage> messages)
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            model.addMessages(messages);
         }
      });
   }

   @Override
   public ListSelectionModel getSelectionModel()
   {
      return super.getSelectionModel() ;
   }
   
   public void close()
   {
      model.close();
   }
}
