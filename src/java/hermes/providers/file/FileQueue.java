/* 
 * Copyright 2003,2004 Colin Crist
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

package hermes.providers.file;

import hermes.HermesException;
import hermes.MessageFactory;
import hermes.impl.DefaultXMLHelper;
import hermes.impl.XMLHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;

import com.codestreet.selector.ISelector;
import com.codestreet.selector.Selector;
import com.codestreet.selector.jms.ValueProvider;
import com.codestreet.selector.parser.InvalidSelectorException;
import com.codestreet.selector.parser.Result;

/**
 * An XML file provider. Not pretty.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FileQueue.java,v 1.5 2007/02/28 10:47:29 colincrist Exp $
 */

public class FileQueue implements Queue, TemporaryQueue {
	private static XMLHelper xmlHelper = new DefaultXMLHelper();
	private File file;
	private String property;
	private String EMPTY_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<content/>\n";


	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public FileQueue(String fileName) throws HermesException {
		file = new File(fileName) ;
		if (!file.exists()) {
			try {
				file.createNewFile() ;
				FileWriter fs = new FileWriter(file) ;
				fs.append(EMPTY_FILE) ;
				fs.close() ;
			} catch (IOException e) {
				throw new HermesException(e) ;
			}
		}
	}
	public FileQueue(File file) {
		this.file = file;
	}

	public FileQueue() throws IOException {
		this.file = File.createTempFile("hermes-queue", ".xml");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.Queue#getQueueName()
	 */
	public String getQueueName() throws JMSException {
		return file.getName();
	}

	public Collection getMessages(MessageFactory messageFactory) throws JMSException, IOException {
		return xmlHelper.fromXML(messageFactory, new FileInputStream(file));
	}

	public Collection getMessages(MessageFactory messageFactory, String selector) throws JMSException, IOException {
		final Collection messages = xmlHelper.fromXML(messageFactory, new FileInputStream(file));

		if (selector != null) {
			try {
				final ISelector selectorImpl = Selector.getInstance(selector);
				for (final Iterator iter = messages.iterator(); iter.hasNext();) {
					final Message message = (Message) iter.next();
					if (selectorImpl.eval(ValueProvider.valueOf(message), null) == Result.RESULT_FALSE) {
						iter.remove();
					}
				}
			} catch (InvalidSelectorException e) {
				throw new HermesException(e);
			}
		}

		return messages;
	}

	public void addMessages(MessageFactory messageFactory, Collection messages) throws JMSException, IOException {
		Collection c = getMessages(messageFactory, null);
		c.addAll(messages);

		xmlHelper.toXML(c, new FileOutputStream(file));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.TemporaryQueue#delete()
	 */
	public void delete() throws JMSException {
		file.delete();
	}

	public void delete(MessageFactory messageFactory, Collection<String> todelete) throws FileNotFoundException, JMSException {
		final Collection<Message> messages = xmlHelper.fromXML(messageFactory, new FileInputStream(file));
		for (Iterator<Message> iter = messages.iterator(); iter.hasNext();) {
			Message m = iter.next();
			if (todelete.contains(m.getJMSMessageID())) {
				iter.remove();
			}
		}
	}

	public int hashCode() {
		return file.hashCode();
	}

	public boolean equals(Object other) {
		if (other instanceof FileQueue) {
			FileQueue otherQueue = (FileQueue) other;

			return otherQueue.hashCode() == hashCode();
		}

		return false;
	}

}