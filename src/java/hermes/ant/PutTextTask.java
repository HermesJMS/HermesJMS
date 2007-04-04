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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.jms.Destination;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;

/**
 * Hermes task to place a file onto a queue/topic as a TextMessage.
 * 
 * @author colincrist@hermesjms.com
 */
public class PutTextTask extends AbstractTask
{
    private static final Logger log = Logger.getLogger(PutTextTask.class) ;
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
            final BufferedReader reader = new BufferedReader(new FileReader(new File(getFile()))) ;
            final TextMessage textMessage = myHermes.createTextMessage() ;
            final StringBuffer buffer = new StringBuffer() ;
            
            String line ;

            while ((line = reader.readLine()) != null)
            {
                buffer.append(line) ;
            }
            
            textMessage.setText(buffer.toString()) ;
            myHermes.send(destination, textMessage) ;
            
            if (myHermes.getTransacted())
            {
                myHermes.commit() ;
            }
            
            log(" message written to " + getDestination() + " on " + getHermes()) ; ;
            
            myHermes.close() ;
        }
        catch (Throwable ex)
        {
            log.error(ex.getMessage(), ex) ;
            
            throw new BuildException(ex) ;
        }
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public String getFile()
    {
        return file;
    }

    public void setFile(String file)
    {
        this.file = file;
    }
}
