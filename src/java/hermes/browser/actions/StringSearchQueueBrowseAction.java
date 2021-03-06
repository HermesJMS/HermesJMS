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

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.tasks.StringSearchBrowseDestinationTask;
import hermes.browser.tasks.Task;
import hermes.config.DestinationConfig;

import javax.jms.JMSException;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: StringSearchQueueBrowseAction.java,v 1.5 2005/08/07 09:02:52 colincrist Exp $
 */

public class StringSearchQueueBrowseAction extends QueueBrowseAction
{
	private static final DestinationConfig SEARCH_ALL = HermesBrowser.getConfigDAO().createDestinationConfig("*", Domain.QUEUE) ;
    private String string ;
    private boolean searchUserHeader ;

    /**
     * @param hermes
     * @param destinationName
     * @param listener
     * @param maxMessages
     * @throws JMSException
     */
    public StringSearchQueueBrowseAction(Hermes hermes, DestinationConfig config,  String string, boolean searchUserHeader, int maxMessages) throws JMSException
    {
        super(hermes, config, maxMessages, string);
        
        this.string = string ;
        this.searchUserHeader = searchUserHeader ;
    }
    
    public StringSearchQueueBrowseAction(Hermes hermes, String string, boolean searchUserHeader, int maxMessages) throws JMSException
    {
        super(hermes, SEARCH_ALL,  maxMessages, string);
        
        this.string = string ;
        this.searchUserHeader = searchUserHeader ;
    }
    
    
    /* (non-Javadoc)
     * @see hermes.browser.actions.BrowserAction#createTask()
     */
    protected Task createTask() throws Exception
    {
        if (getDestinationConfig() == SEARCH_ALL)
        {
            return new StringSearchBrowseDestinationTask(getHermes(), string, searchUserHeader, getTitle()) ;
        }
        else
        {
            return new StringSearchBrowseDestinationTask(getHermes(), getDestinationConfig(), string, searchUserHeader, getTitle()) ;
        }
    }
}
