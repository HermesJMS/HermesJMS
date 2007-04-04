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

package hermes.browser.tasks;

import hermes.Hermes;
import hermes.config.DestinationConfig;
import hermes.impl.StringSearchQueueBrowser;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueBrowser;

/**
 * @author colincrist@hermesjms.com
 */
public class StringSearchBrowseDestinationTask extends BrowseDestinationTask
{
    private String string ;
    private boolean searchUserHeader ;
    
    /**
     * @param hermes
     * @param dConfig
     */
    public StringSearchBrowseDestinationTask(Hermes hermes, DestinationConfig dConfig, String string, boolean seachUserHeader)
    {
        super(hermes, dConfig);
        
        this.string = string ;
        this.searchUserHeader = seachUserHeader ;
    }
    
    public StringSearchBrowseDestinationTask(Hermes hermes, String string, boolean seachUserHeader)
    {
        super(hermes, hermes.getDestinations());
        
        this.string = string ;
        this.searchUserHeader = seachUserHeader ;
    }
    
    public String getTitle()
    {
       return "String" ;
    }

    /* (non-Javadoc)
     * @see hermes.browser.tasks.BrowseDestinationTask#createBrowser(javax.jms.Destination)
     */
    protected QueueBrowser createBrowser(Destination destination, DestinationConfig dConfig) throws JMSException
    {
        return new StringSearchQueueBrowser(super.createBrowser(destination, dConfig), string, false, searchUserHeader) ;
    }
}
