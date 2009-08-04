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

package hermes.browser;

import hermes.Hermes;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.log4j.Category;

/**
 * Reads the entire file and creates a single javax.jms.TextMessage containing
 * the file.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SimpleMessageFileIterator.java,v 1.1 2004/05/01 15:52:35
 *          colincrist Exp $
 */

public class SimpleMessageFileIterator extends AbstractMessageFileIterator
{
    private static final Category cat = Category.getInstance(SimpleMessageFileIterator.class);
    private boolean fileRead = false;

    public SimpleMessageFileIterator(Hermes hermes, File file)
    {
        super(hermes, file);
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext()
    {
        return !fileRead;
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        Message message = null;

        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuffer buffer = new StringBuffer();
            boolean inHeader = true;

            message = hermes.createTextMessage();

            try
            {
                String line;

                while (inHeader && (line = reader.readLine()) != null)
                {
                    if (line.startsWith("# ObjectMessage"))
                    {
                        message = hermes.createObjectMessage();
                    }
                    else if (line.startsWith("# TextMessage"))
                    {
                        // NOP
                    }
                    else if (line.startsWith("# Properties"))
                    {
                        inHeader = true;
                    }
                    else if (line.startsWith("# Message"))
                    {
                        inHeader = false;

                    }
                    else
                    {
                        if (inHeader)
                        {
                            String[] property = line.split("=");

                            message.setStringProperty(property[0], property[1]);
                        }
                    }
                }

                if (message instanceof TextMessage)
                {
                    while ((line = reader.readLine()) != null)
                    {
                        buffer.append(line).append("\n");
                    }

                    ((TextMessage) message).setText(buffer.toString());
                }

                if (message instanceof ObjectMessage)
                {
                    while ((line = reader.readLine()) != null)
                    {
                        buffer.append(line);
                    }

                    ByteArrayInputStream bistream = new ByteArrayInputStream(buffer.toString().getBytes());
                    ObjectInputStream oistream = new ObjectInputStream(bistream);
                    Object o = oistream.readObject();

                    ((ObjectMessage) message).setObject((Serializable) o);
                }

            }
            catch (Exception ex)
            {
                cat.error(ex.getMessage(), ex);
            }
        }
        catch (Exception ex)
        {
            cat.error(ex.getMessage(), ex);
        }

        fileRead = true;

        return message;
    }
}