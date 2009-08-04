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

import hermes.Hermes;
import hermes.browser.tasks.BrowseDestinationTask;
import hermes.browser.tasks.Task;
import hermes.config.DestinationConfig;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: QueueBrowseAction.java,v 1.11 2005/08/15 20:37:31 colincrist Exp $
 */
public class QueueBrowseAction extends BrowserAction
{
    private static final Logger log = Logger.getLogger(QueueBrowseAction.class);
    private QueueBrowser browser;
    private volatile boolean continueAfterException = false;

    /**
     * @param hermes
     * @param destinationName
     * @param listener
     * @param maxMessages
     * @throws javax.jms.JMSException
     */
    public QueueBrowseAction(Hermes hermes, DestinationConfig dConfig, int maxMessages, String postfix) throws JMSException
    {
        super(hermes, dConfig, maxMessages, postfix);       
    }
    
    /* (non-Javadoc)
     * @see hermes.browser.actions.BrowserAction#createTask()
     */
    protected Task createTask() throws Exception
    {
        if (getDestination() == null)
        {
            return new BrowseDestinationTask(getHermes(), getHermes().getDestinations()) ;
        }
        else
        {
            return new BrowseDestinationTask(getHermes(), getConfig()) ;
        }
    }

   @Override
   public boolean isRefreshable()
   {
     return true ;
   }
    
    
}