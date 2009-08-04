/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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

package hermes.ant;

import hermes.Domain;
import hermes.Hermes;
import hermes.config.DestinationConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.jms.Message;
import javax.jms.QueueBrowser;

import org.apache.tools.ant.BuildException;

/**
 * Ant task to browse a queue and write the messages out as XML to a file.
 * 
 * @author colincrist@hermesjms.com
 */
public class BrowseQueueTask extends AbstractTask
{
    private String queue;
    private String file;

    public void execute() throws BuildException
    {
        if ( queue == null)
        {
            throw new BuildException("queue property not set");
        }

        if ( file == null)
        {
            throw new BuildException("file property for the message XML file not set");
        }

        try
        {
           
            final Hermes myHermes = HermesFactory.createHermes(getConfig(), getHermes()) ;
            final DestinationConfig destination = myHermes.getDestinationConfig(getQueue(), Domain.QUEUE) ;
            final Collection<Message> messages = new ArrayList<Message>() ;
            final FileOutputStream ostream = new FileOutputStream(new File(getFile())) ;         
            final QueueBrowser browser = myHermes.createBrowser(destination) ;
            
            for (Enumeration iter = browser.getEnumeration() ; iter.hasMoreElements() ; )
            {
                messages.add((Message) iter.nextElement()) ;
            }
            
            myHermes.toXML(messages, ostream) ;
            
            log(messages.size() + " message(s) written to file " + getFile() + " from " + getQueue() + " on " + getHermes()) ;
            
            browser.close() ;
            ostream.close() ;
            myHermes.close() ;
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public String getQueue()
    {
        return queue;
    }

    public void setQueue(String queue)
    {
        this.queue = queue;
    }

}
