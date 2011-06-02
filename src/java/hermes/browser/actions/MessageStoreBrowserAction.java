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

package hermes.browser.actions;

import java.io.Serializable;

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.components.EditedMessageHandler;
import hermes.browser.tasks.BrowseMessageStoreTask;
import hermes.browser.tasks.Task;
import hermes.store.MessageStore;
import hermes.util.JMSUtils;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.NamingException;
import javax.swing.Icon;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: RepositoryFileBrowserAction.java,v 1.7 2005/06/20 15:28:38
 *          colincrist Exp $
 */

public class MessageStoreBrowserAction extends QueueBrowseAction {
	

	private static final Logger log = Logger.getLogger(MessageStoreBrowserAction.class);
	private MessageStore messageStore;
	private Destination destination;
	private String selector;

	/**
	 * @param hermes
	 * @param repository
	 * @param listener
	 * @param maxMessages
	 * @throws javax.jms.JMSException
	 */
	public MessageStoreBrowserAction(Hermes hermes, MessageStore messageStore, Destination destination) throws JMSException {
		super(hermes, HermesBrowser.getConfigDAO().createDestinationConfig(messageStore.getId() + destination, Domain.QUEUE), -1, null);

		this.messageStore = messageStore;
		this.destination = destination;
	}

	public MessageStoreBrowserAction(Hermes hermes, MessageStore messageStore, Destination destination, String selector) throws JMSException {
		super(hermes, HermesBrowser.getConfigDAO().createDestinationConfig(messageStore.getId() + destination, Domain.QUEUE), -1, null);

		this.messageStore = messageStore;
		this.destination = destination;
		this.selector = selector;
	}

	@Override
	public boolean isRefreshable() {
		return false;
	}

	@Override
	public EditedMessageHandler getEditedMessageHandler() {
		return new AbstractEditedMessageHandler(getHermes()) {
			
			@Override
			public void onMessage(Message message) {
				{
					try {
						messageStore.update(message);
						messageStore.checkpoint();
						updateMessage(message) ;
					} catch (Exception e) {
						HermesBrowser.getBrowser().showErrorDialog(e);
					}
				}				
			}
		};
	}

	@Override
	public String getTitle() {
		String rval;
		if (destination == null) {
			rval = messageStore.getId();
		} else {
			rval = messageStore.getId() + ": " + JMSUtils.getDestinationName(destination);
		}

		if (selector != null) {
			rval = rval + " " + selector;
		}

		return rval;
	}

	@Override
	public String getDestination() {
		if (destination != null) {
			return JMSUtils.getDestinationName(destination);
		} else {
			return messageStore.getId();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.browser.actions.BrowserAction#createTask()
	 */
	protected Task createTask() throws Exception {
		return new BrowseMessageStoreTask(getHermes(), messageStore, destination, selector);
	}

	public Icon getIcon() {
		if (destination == null) {
			return IconCache.getIcon("hermes.store");
		} else {
			return Domain.getDomain(destination).getIcon();
		}
	}

	public MessageStore getMessageStore() {
		return messageStore;
	}

	public void delete() {
		try {
			HermesBrowser.getBrowser().getActionFactory().createDeleteFromMessageStoreAction(getMessageStore(), getSelectedMessages(), true);
		} catch (JMSException ex) {
			HermesBrowser.getBrowser().showErrorDialog(ex);
		}
	}
}
