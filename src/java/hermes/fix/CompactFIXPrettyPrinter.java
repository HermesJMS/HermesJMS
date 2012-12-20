/* 
 * Copyright 2007 Colin Crist
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

import java.io.StringWriter;
import java.util.Iterator;

import quickfix.DataDictionary;
import quickfix.Field;
import quickfix.FieldMap;
import quickfix.FieldNotFound;
import quickfix.FieldType;
import quickfix.Group;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class CompactFIXPrettyPrinter implements FIXPrettyPrinter {
	public String print(FIXMessage message) {
		StringBuffer rval = new StringBuffer();
		StringBuffer line = new StringBuffer();

		line.append(message.getString(SenderCompID.FIELD)).append(" -> ").append(message.getString(TargetCompID.FIELD)).append(":");

		try {
			line = processFieldMap(rval, line, message.getDictionary(), message.getMsgType(), message.getMessage().getHeader());
			line = processFieldMap(rval, line, message.getDictionary(), message.getMsgType(), message.getMessage());
			line = processFieldMap(rval, line, message.getDictionary(), message.getMsgType(), message.getMessage().getTrailer());
			return rval.toString();
		} catch (Exception ex) {
			return ex.getMessage();
		}
	}

	private boolean isGroupCountField(DataDictionary dd, Field field) {
		return dd.getFieldTypeEnum(field.getTag()) == FieldType.NumInGroup;
	}

	private StringBuffer processFieldMap(StringBuffer writer, StringBuffer lineWriter, DataDictionary dd, String msgType, FieldMap fieldMap) throws FieldNotFound {
		Iterator fieldIterator = fieldMap.iterator();

		while (fieldIterator.hasNext()) {
			Field field = (Field) fieldIterator.next();

			if (!isGroupCountField(dd, field)) {
				String value = fieldMap.getString(field.getTag());
				if (dd.hasFieldValue(field.getTag())) {
					value = dd.getValueName(field.getTag(), fieldMap.getString(field.getTag())) + " (" + value + ")";
				}
				StringWriter tagtext = new StringWriter();

				String fieldName = dd.getFieldName(field.getTag());
				if (fieldName == null) {
					fieldName = "Unknown" ;
				}
				int tag = field.getTag();
				String fieldValue = value == null ? "" : value;
				String fieldValueName = null;
				try {
					fieldValueName = dd.getValueName(field.getTag(), fieldMap.getString(field.getTag()));
				} catch (Throwable ex) {
					fieldValueName = "" ;
				}

				tagtext.append(fieldName).append("<").append(Integer.toString(tag)).append(">=").append(fieldValue);
				if (fieldValueName != null) {
					tagtext.append("<").append(fieldValueName).append(">");
				}

				if (lineWriter.length() != 0) {
					lineWriter.append(" ");
				} else {
					lineWriter.append("    ");
				}

				lineWriter.append(tagtext.toString());

				if (lineWriter.length() > 80) {
					writer.append(lineWriter).append("\n");
					lineWriter = new StringBuffer();
				}
			}
		}

		Iterator groupsKeys = fieldMap.groupKeyIterator();
		while (groupsKeys.hasNext()) {
			int groupCountTag = ((Integer) groupsKeys.next()).intValue();
			int groupCount = fieldMap.getGroupCount(groupCountTag);

			if (lineWriter.length() != 0) {
				lineWriter.append(" ");
			} else {
				lineWriter.append("    ");
			}
			lineWriter.append(dd.getFieldName(groupCountTag)).append("<").append(Integer.toString(groupCountTag)).append(">=").append(Integer.toString(groupCount));

			// System.out.println(prefix + dd.getFieldName(groupCountTag) +
			// ": count = " + fieldMap.getInt(groupCountTag));
			Group g = new Group(groupCountTag, 0);
			int i = 1;
			while (fieldMap.hasGroup(i, groupCountTag)) {
				fieldMap.getGroup(i, g);
				lineWriter = processFieldMap(writer, lineWriter, dd, msgType, g);
				i++;
			}
		}
		return lineWriter;
	}

}
