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

package hermes.impl;

import hermes.Hermes;
import hermes.HermesException;
import hermes.HermesRepository;
import hermes.MessageFactory;
import hermes.providers.file.FileQueue;
import hermes.providers.file.FileQueueBrowser;
import hermes.store.MessageStoreListener;
import hermes.xml.Entry;
import hermes.xml.MessageSet;
import hermes.xml.XMLMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

import org.apache.log4j.Logger;

/**
 * Stores messages on the filesystem
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FileRepository.java,v 1.4 2005/06/28 15:36:15 colincrist Exp $
 */

public class FileRepository implements HermesRepository {
	private static final Logger log = Logger.getLogger(FileRepository.class);
	private static final String SEPARATOR = System.getProperty("file.separator");
	private String fileName;
	private MessageSet xmlMessages;
	private DefaultXMLHelper xmlSupport = new DefaultXMLHelper();

	public FileRepository(File file) throws IOException {
		this.fileName = file.getAbsolutePath();

	}

	public String getToolTipText() {
		return fileName;
	}

	private void read() throws Exception {
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		} else {
			try {
				xmlMessages = xmlSupport.readContent(new FileInputStream(file));
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}

		if (xmlMessages == null) {
			xmlMessages = new MessageSet();
		}

	}

	private void save() throws Exception {
		if (xmlMessages != null) {
			File file = new File(fileName);
			xmlSupport.saveContent(xmlMessages, new FileOutputStream(file));
		}
	}

	public String getId() {
		File file = new File(fileName);

		return file.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.HermesRepository#getMessages(java.lang.String)
	 */
	public Collection getMessages(Hermes hermes) throws JMSException {
		if (hermes == null) {
			throw new HermesException("No Hermes available");
		}

		try {
			if (xmlMessages == null) {
				read();
			}

			return xmlSupport.fromMessageSet(hermes, xmlMessages);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new HermesException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.HermesRepository#addMessages(java.lang.String,
	 * java.util.Collection)
	 */
	public void addMessages(Hermes hermes, Collection messages) throws JMSException {
		if (hermes == null) {
			throw new HermesException("No Hermes available");
		}

		try {
			if (xmlMessages == null) {
				read();
			}

			MessageSet newMessages = xmlSupport.toMessageSet(messages);

			xmlMessages.getEntry().addAll(newMessages.getEntry());

			save();
		} catch (JMSException e) {
			throw e; // Ugh.
		} catch (Exception e) {
			throw new HermesException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.HermesRepository#addMessage(hermes.Hermes, javax.jms.Message)
	 */
	public void addMessage(Hermes hermes, Message message) throws JMSException {
		List tmp = new ArrayList();
		tmp.add(message);

		addMessages(hermes, tmp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.HermesRepository#delete()
	 */
	public void delete() {
		File file = new File(fileName);
		if (file != null) {
			file.delete();
		}

		xmlMessages = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.HermesRepository#size()
	 */
	public int size() {
		if (xmlMessages == null) {
			return 0;
		} else {
			return xmlMessages.getEntry().size();
		}

	}

	/**
	 * This supports the minimum interface of a MessageStore so it can be
	 * browsed in the same way.
	 * 
	 */
	public void addMessageListener(MessageStoreListener listener) {
	}

	public void checkpoint() throws JMSException {
		throw new HermesException("Not Implemented");
	}

	public void close() throws JMSException {
		throw new HermesException("Not Implemented");

	}

	public void delete(Destination d) throws JMSException {
		throw new HermesException("Not Implemented");

	}

	@Override
	public void delete(Set<String> messageIds) throws JMSException {
		try {
			if (xmlMessages == null) {
				read();
			}

			Set<XMLMessage> toRemove = new HashSet<XMLMessage>();

			for (Iterator<Entry> iter = xmlMessages.getEntry().iterator(); iter.hasNext();) {
				XMLMessage xmlMessage = null;
				Entry entry = iter.next();
				if (entry.getBytesMessage() != null) {
					xmlMessage = entry.getBytesMessage();
				} else if (entry.getTextMessage() != null) {
					xmlMessage = entry.getTextMessage();
				} else if (entry.getMapMessage() != null) {
					xmlMessage = entry.getMapMessage();
				} else if (entry.getObjectMessage() != null) {
					xmlMessage = entry.getObjectMessage();
				}

				if (xmlMessage != null) {
					if (messageIds.contains(xmlMessage.getJMSMessageID())) {
						iter.remove();
					}
				}
			}

			save();
		} catch (Exception ex) {
			log.error(ex);
			throw new JMSException(ex.getMessage());
		}

	}

	public int getDepth(Destination d) throws JMSException {
		return 0;
	}

	public Collection<Destination> getDestinations() throws JMSException {
		return Collections.EMPTY_LIST;
	}

	public String getTooltipText() {
		File file = new File(fileName);
		return file.getName();
	}

	public String getURL() {
		File file = new File(fileName);
		return file.getName();
	}

	public void removeMessageListener(MessageStoreListener listener) {
		// TODO Auto-generated method stub

	}

	public void rollback() throws JMSException {
		throw new HermesException("Not Implemented");
	}

	public void store(Message m) throws JMSException {
		throw new HermesException("Not Implemented");

	}

	public QueueBrowser visit() throws JMSException {
		throw new HermesException("Not Implemented");
	}

	public QueueBrowser visit(Destination d) throws JMSException {
		throw new HermesException("Not Implemented");
	}

	public QueueBrowser visit(MessageFactory factory, Destination d, HeaderPolicy headerPolicy) throws JMSException {
		File file = new File(fileName);
		return new FileQueueBrowser(factory, new FileQueue(file));
	}

	public QueueBrowser visit(MessageFactory factory, HeaderPolicy headerPolicy) throws JMSException {
		return visit(factory, null, headerPolicy);
	}

	@Override
	public void delete(Message m) throws JMSException {
		throw new HermesException("Not Implemented");
	}

	@Override
	public void update(Message message) throws Exception {
		if (xmlMessages == null) {
			read() ;
		}
		
		for (int i = 0 ; i < xmlMessages.getEntry().size() ; i++) {
			XMLMessage xmlMessage = null;
			Entry entry = xmlMessages.getEntry().get(i) ;
			if (entry.getBytesMessage() != null) {
				xmlMessage = entry.getBytesMessage();
			} else if (entry.getTextMessage() != null) {
				xmlMessage = entry.getTextMessage();
			} else if (entry.getMapMessage() != null) {
				xmlMessage = entry.getMapMessage();
			} else if (entry.getObjectMessage() != null) {
				xmlMessage = entry.getObjectMessage();
			}

			// ikky but whatever.
			
			if (xmlMessage != null) {
				if (message.getJMSMessageID().equals(xmlMessage.getJMSMessageID())) {
					xmlMessages.getEntry().remove(i) ;
					MessageSet messageSet = xmlSupport.toMessageSet(Arrays.asList(message)) ;
					xmlMessages.getEntry().add(i, messageSet.getEntry().get(0)) ;
				}
			}
		}

		save();
		
	}



}