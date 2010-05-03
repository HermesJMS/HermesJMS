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

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXMessageViewTableModel.java,v 1.5 2006/08/01 07:29:35 colincrist Exp $
 */

public class FIXMessageViewTableModel extends AbstractTableModel
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 7400479603426161337L;
public static final String FIELD = "Field" ;
   public static final String NAME = "Name" ;
   public static final String VALUE = "Value" ;
   public static final String DESCRIPTION = "Description" ;
   
   private String[] columns = { FIELD, NAME, VALUE, DESCRIPTION };

   private FIXMessage message;
   private Vector<RowDef> rows = new Vector<RowDef>();

   public enum RowType
   {
      HEADER, APPLICATION, TRAILER
   }

   private class RowDef
   {
      RowType type;
      int tag ;
   }

   public FIXMessageViewTableModel(FIXMessage message)
   {
      super();
      this.message = message;

      for (int i : message.getFieldOrder())
      {
         RowDef rowDef = new RowDef();

         rowDef.tag = i;

         if (message.getDictionary().isHeaderField(i))
         {
            rowDef.type = RowType.HEADER;
         }
         else if (message.getDictionary().isTrailerField(i))
         {
            rowDef.type = RowType.TRAILER;
         }
         else
         {
            rowDef.type = RowType.APPLICATION;
         }

         rows.add(rowDef);
      }

   }

   public int getRowCount()
   {
      if (rows != null)
      {
         return rows.size();
      }
      else
      {
         return 0;
      }
   }

   public int getColumnCount()
   {
      return columns.length;
   }

   @Override
   public Class<?> getColumnClass(int columnIndex)
   {
     if (columnIndex == 0)
     {
        return Integer.class ;
     }
     else
     {
        return String.class ;
     }
   }

   public RowType getRowType(int row)
   {
      RowDef rowDef = rows.get(row) ;
      
      return rowDef.type ;
   }
   
   @Override
   public String getColumnName(int column)
   {
      return columns[column] ; 
   }

   public Object getValueAt(int rowIndex, int columnIndex)
   {
      RowDef row = rows.get(rowIndex);

      switch (columnIndex)
      {
         case 0:
            return row.tag ;

         case 1:
            return message.getDictionary().getFieldName(row.tag) ;

         case 2:
            try
            {
            return message.getObject(row.tag);
            }
            catch (NoSuchFieldException ex)
            {
               return ex.getMessage() ;
            }

         case 3:
            return message.getDictionary().getValueName(row.tag, message.getString(row.tag)) ;

         default:
            return "";
      }
   }

}
