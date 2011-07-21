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

import hermes.swing.RowValueProvider;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.collections.list.TreeList;
import org.apache.log4j.Logger;

import quickfix.Field;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.SenderCompID;
import quickfix.field.SenderSubID;
import quickfix.field.SendingTime;
import quickfix.field.TargetSubID;

import com.codestreet.selector.parser.IValueProvider;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXMessageTableModel.java,v 1.4 2006/05/06 17:22:56 colincrist
 *          Exp $
 */

public class FIXMessageTableModel extends AbstractTableModel implements RowValueProvider {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3958105974757909932L;
	private static final Logger log = Logger.getLogger(FIXMessageTableModel.class);
	public static final String DIRECTION = " ";

	// public static final String RAW_MESSAGE = "Raw Message" ;

	private final List<FIXMessage> messages = new TreeList();
	private final Vector<Field> fields = new Vector<Field>();
	private final Vector<Class> classes = new Vector<Class>();
	private SessionKey initiatorSessionKey;

	public FIXMessageTableModel(SessionKey initiatorSessionKey) {
		super();

		this.initiatorSessionKey = initiatorSessionKey;

		fields.add(null);
		fields.add(new MsgSeqNum());
		fields.add(new SendingTime());
		fields.add(new MsgType());
		fields.add(new SenderSubID());
		fields.add(new TargetSubID());

		classes.add(Integer.class);
		classes.add(String.class);
		classes.add(String.class);
		classes.add(String.class);
		classes.add(String.class);
		classes.add(String.class);
	}

	public IValueProvider getValueProviderForRow(int row) {
		return new FIXMessageValueProvider(getMessageAt(row));
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return classes.get(columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0) {
			return " ";
		} else {
			return fields.get(column).getClass().getSimpleName();
		}
	}

	public int getColumnCount() {
		return fields.size();
	}

	public int getRowCount() {
		return messages.size();
	}

	public FIXMessage getMessageAt(int row) {
		if (row >= 0) {
			return messages.get(row);
		} else {
			return null;
		}
	}

	public void addMessages(Collection<FIXMessage> newMessages) {
		if (newMessages.size() > 0) {
			messages.addAll(newMessages);
			fireTableRowsInserted(messages.size() - newMessages.size(), messages.size());
		}
	}

	public SessionRole getRole(FIXMessage message) {
		final String senderCompID = message.getString(SenderCompID.FIELD);

		if (initiatorSessionKey.getSenderCompID().equals(senderCompID)) {
			return SessionRole.INITIATOR;
		} else {
			return SessionRole.ACCEPTOR;
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex > messages.size()) {
			return null;
		}

		final FIXMessage message = messages.get(rowIndex);

		if (columnIndex == 0) {
			if (getRole(message) == SessionRole.INITIATOR) {
				return "-->";
			} else {
				return "<--";
			}
		} else {

			final Field field = fields.get(columnIndex);
			final Object fieldValue = message.getObject(field);

			if (fieldValue != null && fieldValue instanceof String) {
				String valueName = message.getDictionary().getValueName(field.getTag(), (String) fieldValue);

				if (valueName == null) {
					return fieldValue;
				} else {
					return valueName;
				}
			} else {
				return fieldValue;
			}
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public void clear() {
		messages.clear();
		fields.clear();
		classes.clear();
		
	}

}
