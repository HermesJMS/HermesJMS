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

import hermes.BrowseInterruptedException;
import hermes.Hermes;
import hermes.HermesRepository;
import hermes.browser.IconCache;

import java.util.Collection;
import java.util.Iterator;

import javax.jms.Message;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public class BrowseRepositoryFileTask extends TaskSupport
{
    private static final Logger log = Logger.getLogger(BrowseRepositoryFileTask.class);
    private Hermes hermes;
    private HermesRepository repository;

    public BrowseRepositoryFileTask(Hermes hermes, HermesRepository repository)
    {
        super(IconCache.getIcon("jms.queue"));

        this.hermes = hermes;
        this.repository = repository;
    }
    
    public BrowseRepositoryFileTask(HermesRepository repository)
    {
       this(null, repository) ;
    }

    public String getTitle()
    {
       return "Browse" ;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see hermes.browser.tasks.Task#run()
     */
    public void invoke() throws Exception
    {
        int nmessages = 0;

        try
        {
            Collection messages = repository.getMessages(hermes);

            for (Iterator iter = messages.iterator(); iter.hasNext() && isRunning();)
            {
                Message m = (Message) iter.next();

                if ( m != null)
                {
                    nmessages++;
                    notifyMessage(m);
                }
            }
        }
        catch (BrowseInterruptedException ex)
        {
            log.error("browse stopped: " + ex.getMessage());
        }
        finally
        {
            log.debug("nmessages=" + nmessages);
        }

        notifyStatus("Read " + nmessages + " messages from " + repository.getId());

        if (hermes != null)
        {
           hermes.close();
        }
    }
}
