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

package hermes.browser.model;

import java.util.Map;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MapTableModel.java,v 1.4 2006/05/06 17:22:56 colincrist Exp $
 */

public abstract class MapTableModel extends DefaultTableModel
{
   
   /**
	 * 
	 */
	private static final long serialVersionUID = -3351082537562251893L;

public MapTableModel()
   {
      super();
      // TODO Auto-generated constructor stub
   }

   public MapTableModel(int rowCount, int columnCount)
   {
      super(rowCount, columnCount);
      // TODO Auto-generated constructor stub
   }

   public MapTableModel(Vector columnNames, int rowCount)
   {
      super(columnNames, rowCount);
      // TODO Auto-generated constructor stub
   }

   public MapTableModel(Object[] columnNames, int rowCount)
   {
      super(columnNames, rowCount);
      // TODO Auto-generated constructor stub
   }

   public MapTableModel(Vector data, Vector columnNames)
   {
      super(data, columnNames);
      // TODO Auto-generated constructor stub
   }

   public MapTableModel(Object[][] data, Object[] columnNames)
   {
      super(data, columnNames);
      // TODO Auto-generated constructor stub
   }

   public abstract void setMap(Map map) ;
}
