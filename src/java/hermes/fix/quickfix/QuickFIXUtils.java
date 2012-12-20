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

package hermes.fix.quickfix;

import hermes.HermesRuntimeException;
import hermes.fix.FIXException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import quickfix.BooleanField;
import quickfix.CharField;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.DoubleField;
import quickfix.Field;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.IntField;
import quickfix.Message;
import quickfix.StringField;
import quickfix.UtcDateOnlyField;
import quickfix.UtcTimeOnlyField;
import quickfix.UtcTimeStampField;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: QuickFIXUtils.java,v 1.4 2006/07/17 21:20:53 colincrist Exp $
 */

public class QuickFIXUtils {
	private static Map<String, DataDictionary> dictionaryCache = new HashMap<String, DataDictionary>();

	public static String FIX50_DICTIONARY = "FIX.5.0";
	private static Set<String> BEGIN_STRINGS = new HashSet<String>();

	static {
		BEGIN_STRINGS.add(FixVersions.BEGINSTRING_FIX40);
		BEGIN_STRINGS.add(FixVersions.BEGINSTRING_FIX41);
		BEGIN_STRINGS.add(FixVersions.BEGINSTRING_FIX42);
		BEGIN_STRINGS.add(FixVersions.BEGINSTRING_FIX43);
		BEGIN_STRINGS.add(FixVersions.BEGINSTRING_FIX44);
		BEGIN_STRINGS.add(FixVersions.FIX50);
		BEGIN_STRINGS.add(FixVersions.BEGINSTRING_FIXT11);
	}

	public static DataDictionary getDictionary(Message message) throws FieldNotFound, FIXException {
		String beginString = message.getHeader().getString(8);

		return QuickFIXUtils.getDictionary(beginString);
	}

	public static DataDictionary getDictionary(String beginString) throws FIXException {
		DataDictionary dictionary = dictionaryCache.get(beginString);
		if (dictionary == null) {
			if (!BEGIN_STRINGS.contains(beginString)) {
				throw new HermesRuntimeException("Invalid FIX BeginString: '" + beginString + "'.");
			}
			String dictionaryFileName = null ;
			
			if (beginString.equals(FixVersions.BEGINSTRING_FIXT11)) {
				dictionaryFileName = "quickfix/" + FixVersions.FIX50.replaceAll("\\.", "") + ".xml";
			} else {
				dictionaryFileName = "quickfix/" + beginString.replaceAll("\\.", "") + ".xml";
			}

			 
			// the dictionary is loaded from the quickfix.jar file.
			InputStream ddis = Thread.currentThread().getContextClassLoader().getResourceAsStream(dictionaryFileName);
			if (ddis == null) {
				throw new NullPointerException("Data Dictionary file '" + dictionaryFileName + "' not found at root of CLASSPATH.");
			}

			try {
				dictionary = new DataDictionary(ddis);
				dictionaryCache.put(beginString, dictionary);
			} catch (ConfigError configError) {
				throw new HermesRuntimeException("Error loading data dictionary file.", configError);
			}

		}
		return dictionary;
	}

	public static Field getField(Message message, Field field) {
		try {
			if (field instanceof BooleanField) {
				try {
					return message.getField((BooleanField) field);
				} catch (FieldNotFound ex) {
					return message.getHeader().getField((BooleanField) field);
				}
			}

			if (field instanceof CharField) {
				try {
					return message.getField((CharField) field);
				} catch (FieldNotFound ex) {
					return message.getHeader().getField((CharField) field);
				}
			}

			if (field instanceof DoubleField) {
				try {
					return message.getField((DoubleField) field);
				} catch (FieldNotFound ex) {
					return message.getHeader().getField((DoubleField) field);
				}
			}

			if (field instanceof IntField) {
				try {
					return message.getField((IntField) field);
				} catch (FieldNotFound ex) {
					return message.getHeader().getField((IntField) field);
				}
			}

			if (field instanceof StringField) {
				try {
					return message.getField((StringField) field);
				} catch (FieldNotFound ex) {
					return message.getHeader().getField((StringField) field);
				}
			}

			if (field instanceof UtcDateOnlyField) {
				try {
					return message.getField((UtcDateOnlyField) field);
				} catch (FieldNotFound ex) {
					return message.getHeader().getField((UtcDateOnlyField) field);
				}
			}

			if (field instanceof UtcTimeOnlyField) {
				try {
					return message.getField((UtcTimeOnlyField) field);
				} catch (FieldNotFound ex) {
					return message.getHeader().getField((UtcTimeOnlyField) field);
				}
			}

			if (field instanceof UtcTimeStampField) {
				try {
					return message.getField((UtcTimeStampField) field);
				} catch (FieldNotFound ex) {
					return message.getHeader().getField((UtcTimeStampField) field);
				}
			}

			throw new FieldNotFound(field.getClass().getName());
		} catch (FieldNotFound ex) {
			throw new HermesRuntimeException(ex);
		}
	}
}
