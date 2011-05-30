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

import java.util.Collection;
import java.util.Set;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesRepository;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.tasks.BrowseRepositoryFileTask;
import hermes.browser.tasks.Task;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.Icon;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: RepositoryFileBrowserAction.java,v 1.9 2006/04/28 09:59:37
 *          colincrist Exp $
 */

public class RepositoryFileBrowserAction extends QueueBrowseAction {
	private static final Logger log = Logger.getLogger(RepositoryFileBrowserAction.class);
	private HermesRepository repository;

	/**
	 * @param hermes
	 * @param repository
	 * @param listener
	 * @param maxMessages
	 * @throws javax.jms.JMSException
	 */
	public RepositoryFileBrowserAction(Hermes hermes, HermesRepository repository, int maxMessages) throws JMSException {
		super(hermes, HermesBrowser.getConfigDAO().createDestinationConfig(repository.getId(), Domain.QUEUE), maxMessages, null);

		this.repository = repository;
	}

	public RepositoryFileBrowserAction(HermesRepository repository, int maxMessages) throws JMSException {
		super(null, HermesBrowser.getConfigDAO().createDestinationConfig(repository.getId(), Domain.QUEUE), maxMessages, null);

		this.repository = repository;
	}

	@Override
	public boolean isRefreshable() {
		return false;
	}

	public void delete() {
		try {
			Set<String> messages = getSelectedMessageIDs() ;
			repository.delete(messages) ;
			refresh() ;
		} catch (JMSException e) {
			log.error(e) ;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.browser.actions.BrowserAction#createTask()
	 */
	protected Task createTask() throws Exception {
		return new BrowseRepositoryFileTask(getHermes(), repository);
	}

	public Icon getIcon() {
		return IconCache.getIcon("hermes.file.xml");

	}
}
