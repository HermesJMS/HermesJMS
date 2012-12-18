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

import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

/**
 * QueueBrowser that facades another QueueBrowser and does an additional level
 * of filtering based on a regular expression. The regex is performed on either
 * MapMessages or TextMessages and on the header properties of all messages.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: RegexQueueBrowser.java,v 1.4 2005/09/28 15:34:41 colincrist Exp
 *          $
 */
public class RegexQueueBrowser implements QueueBrowser {
	private static final Logger log = Logger.getLogger(RegexQueueBrowser.class);
	private final Pattern pattern;
	private final boolean searchJMSHeader;
	private final boolean searchUserHeader;
	private final QueueBrowser browser;

	class RegexEnumeration implements Enumeration {
		private final Enumeration iter;

		RegexEnumeration(Enumeration iter) {
			this.iter = iter;
		}

		@Override
		public boolean hasMoreElements() {
			return iter.hasMoreElements();
		}

		@Override
		public Object nextElement() {
			while (iter.hasMoreElements()) {
				Message m = (Message) iter.nextElement();

				if (m != null) {
					if (matches(m)) {
						return m;
					}
				} else {
					return null;
				}
			}

			return null;
		}
	}

	/**
	 * Create a new RegexQueueBrowser to filter messages from the underlying
	 * QueueBrowser.
	 * 
	 * @param browser
	 *            the underlying queue browser
	 * @param regex
	 *            the regular expression
	 * @param searchJMSHeader
	 *            whether to search the JMS header properties
	 * @param searchUserHeader
	 *            whether to search the user properties
	 */
	public RegexQueueBrowser(QueueBrowser browser, String regex, boolean searchJMSHeader, boolean searchUserHeader) {
		this.browser = browser;
		this.searchJMSHeader = searchJMSHeader;
		this.searchUserHeader = searchUserHeader;

		pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.QueueBrowser#getQueue()
	 */
	@Override
	public Queue getQueue() throws JMSException {
		return browser.getQueue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.QueueBrowser#getMessageSelector()
	 */
	@Override
	public String getMessageSelector() throws JMSException {
		return browser.getMessageSelector();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.QueueBrowser#getEnumeration()
	 */
	@Override
	public Enumeration getEnumeration() throws JMSException {
		return new RegexEnumeration(browser.getEnumeration());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.QueueBrowser#close()
	 */
	@Override
	public void close() throws JMSException {
		browser.close();
	}

	public boolean matches(Message message) {
		try {
			if (searchJMSHeader) {

			}

			if (searchUserHeader) {
				for (Enumeration headerNames = message.getPropertyNames(); headerNames.hasMoreElements();) {
					try {
						String key = (String) headerNames.nextElement();
						Object value = message.getObjectProperty(key);

						if (key != null) {
							if (pattern.matcher(key).lookingAt()) {
								return true;
							}

						}
						if (value != null) {
							if (pattern.matcher(value.toString()).lookingAt()) {
								return true;
							}
						}
					} catch (JMSException ex) {
						log.error(ex.getMessage(), ex);
					}
				}
			}

			if (message instanceof TextMessage) {
				try {
					final String text = ((TextMessage) message).getText();
					if (text != null) {
						return pattern.matcher(text).lookingAt();
					}
				} catch (JMSException ex) {
					log.error(ex.getMessage(), ex);
				}
			} else if (message instanceof MapMessage) {
				try {
					MapMessage map = (MapMessage) message;

					for (Enumeration mapNames = map.getMapNames(); mapNames.hasMoreElements();) {
						String key = (String) mapNames.nextElement();
						Object value = map.getObject(key);

						if (pattern.matcher(key).lookingAt()) {
							return true;
						}

						if (value != null) {
							if (pattern.matcher(value.toString()).lookingAt()) {
								return true;
							}
						}
					}
				} catch (JMSException ex) {
					log.error(ex.getMessage(), ex);
				}
			} else if (message instanceof ObjectMessage) {
				try {
					ObjectMessage om = (ObjectMessage) message;
					String toString = om.getObject().toString();

					if (pattern.matcher(toString).lookingAt()) {
						return true;
					}
				} catch (Throwable t) {
					log.error(t.getMessage(), t);
				}
			}

			return false;
		} catch (JMSException e) {
			log.error(e.getMessage(), e);
		}

		return false;
	}
}