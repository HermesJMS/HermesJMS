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

import hermes.util.TextUtils;

import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import com.codestreet.selector.ISelector;
import com.codestreet.selector.Selector;
import com.codestreet.selector.parser.IIdentifierExtension;
import com.codestreet.selector.parser.IValueProvider;
import com.codestreet.selector.parser.InvalidSelectorException;
import com.codestreet.selector.parser.Result;
import com.jidesoft.grid.AbstractTableFilter;
import com.jidesoft.grid.Filter;
import com.jidesoft.grid.FilterableTableModel;

/**
 * A JIDE FilterableTableModel that uses <link href="http://jamsel.sourceforge.net/">JAMSEL</link>
 * to give SQL92 filters on the table content. An implementation of RowValueProvider must be given
 * that can pull out values from the table row for property names in the SQL.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SQL92FilterableTableModel.java,v 1.3 2006/05/26 10:08:21 colincrist Exp $
 */

public class SQL92FilterableTableModel extends FilterableTableModel
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -6078216473511901668L;
private static final Logger log = Logger.getLogger(SQL92FilterableTableModel.class) ;
   private RowValueProvider rowValueProvider;
   private ISelector selector;
   private IIdentifierExtension extension ;

   public SQL92FilterableTableModel(TableModel model, RowValueProvider rowValueProvider, IIdentifierExtension extension)
   {
      this(model, extension);
      this.rowValueProvider = rowValueProvider ;
   }
   
   public SQL92FilterableTableModel(TableModel model, IIdentifierExtension extension)
   {
      super(model);
      this.extension = extension ;
   }
   
   public SQL92FilterableTableModel(TableModel model)
   {
      super(model);
   }

   public void setSelector(String selectorString) throws InvalidSelectorException
   {
      clearFilters();

      if (rowValueProvider == null)
      {
         throw new InvalidSelectorException("No RowValueProvider defined") ;
      }
      
      if (!TextUtils.isEmpty(selectorString))
      {
         if (extension != null)
         {
            selector = Selector.getInstance(selectorString, extension);
         }
         else
         {
            selector = Selector.getInstance(selectorString);
         }
                 
         Filter filter = new AbstractTableFilter()
         {
            /**
			 * 
			 */
			private static final long serialVersionUID = 7094288732603611045L;

			public boolean isValueFiltered(Object arg0)
            {              
               final IValueProvider values = rowValueProvider.getValueProviderForRow(getRowIndex()) ;
             
               return !(selector.eval(values, null) == Result.RESULT_TRUE) ;
            }
         };

         addFilter(filter) ;
      }
     
      setFiltersApplied(true);
   }


   public RowValueProvider getRowValueProvider()
   {
      return rowValueProvider;
   }

   public void setRowValueProvider(RowValueProvider rowValueProvider)
   {
      this.rowValueProvider = rowValueProvider;
   }
}
