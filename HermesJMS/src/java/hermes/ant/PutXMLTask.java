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

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.jms.Destination;
import javax.jms.Message;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;

/**
 * Hermes task to place a number of messages written in jms2xml.xsd format onto a queue/topic.
 * 
 * @author colincrist@hermesjms.com
 */

public class PutXMLTask extends AbstractTask
{
    private static final Logger log = Logger.getLogger(PutXMLTask.class) ;
    private String destination;
    private String file;

    public void execute() throws BuildException
    {
        if ( destination == null)
        {
            throw new BuildException("destination property not set");
        }

        if ( file == null)
        {
            throw new BuildException("file property for the message XML file not set");
        }

        try
        {
            final Hermes myHermes = HermesFactory.createHermes(getConfig(), getHermes());
            final Destination destination = myHermes.getDestination(getDestination(), Domain.UNKNOWN);
            final FileInputStream istream = new FileInputStream(new File(getFile())) ;
            final Collection messages =  myHermes.fromXML(istream) ;
            
            istream.close() ;
            
            for (Iterator iter = messages.iterator() ; iter.hasNext() ; )
            {
                myHermes.send(destination, (Message) iter.next()) ;
            }
            
            if (myHermes.getTransacted())
            {
                myHermes.commit() ;
            }
            
            log(messages.size() + " message(s) written to " + getDestination() + " on " + getHermes()) ; ;
            
            myHermes.close() ;
        }
        catch (Throwable ex)
        {
            log.error(ex.getMessage(), ex) ;
            
            throw new BuildException(ex) ;
        }
    }

    /**
     * Gets the destination.
     * 
     * @return the name of the target queue/topic
     */
    public String getDestination()
    {
        return destination;
    }

    /**
     * Sets the destination to put the messages to.
     * 
     * @param destination
     */
    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    /**
     * Gets the name of the file to use as the source of the XML.
     * 
     * @return the file name as a string.
     */
    public String getFile()
    {
        return file;
    }

    /**
     * Sets the name of the file to use as the source of the XML
     * 
     * @param file
     */
    public void setFile(String file)
    {
        this.file = file;
    }
}
