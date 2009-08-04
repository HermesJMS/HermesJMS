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
import hermes.impl.RegexQueueBrowser;

import java.util.Iterator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueBrowser;

/**
 * @author colincrist@hermesjms.com
 */
public class RegexBrowseDestinationTask extends BrowseDestinationTask
{
    private String regex;
    private String title ;
    /**
     * @param hermes
     * @param iter
     */
    public RegexBrowseDestinationTask(Hermes hermes, Iterator iter, String regex, String title)
    {
        super(hermes, iter);

        this.regex = regex;
        this.title = title ;
    }

    public String getTitle()
    {
       return "Searching " + title ;
    }
    
    /**
     * @param hermes
     * @param dConfig
     */
    public RegexBrowseDestinationTask(Hermes hermes, DestinationConfig dConfig, String regex, String title)
    {
        super(hermes, dConfig);

        this.regex = regex;
        this.title = title ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.browser.tasks.BrowseDestinationTask#createBrowser(javax.jms.Destination)
     */
    protected QueueBrowser createBrowser(Destination destination, DestinationConfig dConfig) throws JMSException
    {
        return new RegexQueueBrowser(super.createBrowser(destination, dConfig), regex, false, true);
    }
}
