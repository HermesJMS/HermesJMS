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

import java.util.Iterator;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldMap;
import quickfix.FieldNotFound;
import quickfix.FieldType;
import quickfix.Group;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXMessageViewTableModel.java,v 1.5 2006/08/01 07:29:35
 *          colincrist Exp $
 */

public class FIXMessageViewTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final Logger log = Logger.getLogger(FIXMessageViewTableModel.class);

	private static final long serialVersionUID = 7400479603426161337L;
	public static final String FIELD = "Field";
	public static final String NAME = "Name";
	public static final String VALUE = "Value";
	public static final String DESCRIPTION = "Description";

	private String[] columns = { FIELD, NAME, VALUE, DESCRIPTION };

	private FIXMessage message;
	private Vector<RowDef> rows = new Vector<RowDef>();

	public enum RowType {
		HEADER, APPLICATION, TRAILER
	}

	private class RowDef {
		RowType type;
		int tag;
		public String fieldName;
		public String fieldValue;
		public Object fieldValueName = "";
	}

	public FIXMessageViewTableModel(FIXMessage message) throws FIXException, FieldNotFound {
		super();
		this.message = message;

		String msgType = message.getMsgType();

		processFieldMap("", message.getDictionary(), msgType, message.getMessage().getHeader());
		processFieldMap("", message.getDictionary(), msgType, message.getMessage());
		processFieldMap("", message.getDictionary(), msgType, message.getMessage().getTrailer());
	}

	private boolean isGroupCountField(DataDictionary dd, Field field) {
		return dd.getFieldTypeEnum(field.getTag()) == FieldType.NumInGroup;
	}

	private void processFieldMap(String prefix, DataDictionary dd, String msgType, FieldMap fieldMap) {

		Iterator fieldIterator = fieldMap.iterator();
		while (fieldIterator.hasNext()) {
			Field field = (Field) fieldIterator.next();
			try {
				if (!isGroupCountField(dd, field)) {
					String value = fieldMap.getString(field.getTag());
					if (dd.hasFieldValue(field.getTag())) {
						value = dd.getValueName(field.getTag(), fieldMap.getString(field.getTag())) + " (" + value + ")";
					}
					RowDef rowDef = new RowDef();
					if (dd.isHeaderField(field.getField())) {
						rowDef.type = RowType.HEADER;
					} else if (dd.isTrailerField(field.getField())) {
						rowDef.type = RowType.TRAILER;
					} else {
						rowDef.type = RowType.APPLICATION;
					}
					String fieldName = dd.getFieldName(field.getTag());
					rowDef.tag = field.getTag();
					rowDef.fieldName = prefix + (fieldName == null ? "" : fieldName);
					rowDef.fieldValue = value == null ? "" : value;
					try {
						rowDef.fieldValueName = dd.getValueName(field.getTag(), fieldMap.getString(field.getTag()));
					} catch (Throwable ex) {

					}

					rows.add(rowDef);
				}
			} catch (FieldNotFound f) {
				log.error(f);
			}
		}

		Iterator groupsKeys = fieldMap.groupKeyIterator();
		while (groupsKeys.hasNext()) {
			int groupCountTag = ((Integer) groupsKeys.next()).intValue();

			RowDef rowDef = new RowDef();
			rowDef.type = RowType.APPLICATION;
			rowDef.tag = groupCountTag;
			rowDef.fieldName = prefix + ((dd.getFieldName(groupCountTag) == null) ? "" : dd.getFieldName(groupCountTag));
			rowDef.fieldValue = Integer.toString(fieldMap.getGroupCount(groupCountTag));
			rows.add(rowDef);

			Group g = new Group(groupCountTag, 0);
			int i = 1;
			while (fieldMap.hasGroup(i, groupCountTag)) {
				try {
					fieldMap.getGroup(i, g);
					processFieldMap(prefix + "  ", dd, msgType, g);
				} catch (FieldNotFound ex) {
					log.error(ex);
				}
				i++;

			}
		}
	}

	public int getRowCount() {
		if (rows != null) {
			return rows.size();
		} else {
			return 0;
		}
	}

	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Integer.class;
		} else {
			return String.class;
		}
	}

	public RowType getRowType(int row) {
		RowDef rowDef = rows.get(row);

		return rowDef.type;
	}

	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		RowDef row = rows.get(rowIndex);

		switch (columnIndex) {
		case 0:
			return row.tag;
		case 1:
			return row.fieldName;
		case 2:
			return row.fieldValue;
		case 3:
			return row.fieldValueName;

		default:
			return "";
		}
	}

}
