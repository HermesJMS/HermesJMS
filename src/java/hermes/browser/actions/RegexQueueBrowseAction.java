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
import hermes.browser.tasks.RegexBrowseDestinationTask;
import hermes.browser.tasks.Task;

import javax.jms.JMSException;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: RegexQueueBrowseAction.java,v 1.5 2005/08/07 09:02:52 colincrist Exp $
 */

public class RegexQueueBrowseAction extends QueueBrowseAction
{
    private String regex ;

    /**
     * @param hermes
     * @param destinationName
     * @param listener
     * @param maxMessages
     * @throws JMSException
     */
    public RegexQueueBrowseAction(Hermes hermes, Domain domain, String regex, int maxMessages) throws JMSException
    {
        super(hermes, HermesBrowser.getConfigDAO().createDestinationConfig("*", domain), maxMessages);
        
        this.regex = regex ;
    }
    
    public RegexQueueBrowseAction(Hermes hermes, String destinationName, Domain domain, String regex, int maxMessages) throws JMSException
    {
        super(hermes, HermesBrowser.getConfigDAO().createDestinationConfig(destinationName, domain), maxMessages);
        
        this.regex = regex ;
    }
    
    /* (non-Javadoc)
     * @see hermes.browser.actions.BrowserAction#createTask()
     */
    protected Task createTask() throws Exception
    {
        if (getDestination() == null)
        {
            return new RegexBrowseDestinationTask(getHermes(), getHermes().getDestinations(), regex) ;
        }
        else
        {
            return new RegexBrowseDestinationTask(getHermes(), getHermes().getDestinationConfig(getDestination(), getDomain()), regex) ;
        }
    }
}
