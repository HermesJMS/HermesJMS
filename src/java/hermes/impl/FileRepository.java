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

package hermes.impl;

import hermes.Hermes;
import hermes.HermesException;
import hermes.HermesRepository;
import hermes.xml.Content;
import hermes.xml.MessageSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Logger;

/**
 * Stores messages on the filesystem
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FileRepository.java,v 1.4 2005/06/28 15:36:15 colincrist Exp $
 */

public class FileRepository implements HermesRepository
{
    private static final Logger log = Logger.getLogger(FileRepository.class);
    private static final String SEPARATOR = System.getProperty("file.separator");
    private File file;
    private Content xmlMessages;
    private DefaultXMLHelper xmlSupport = new DefaultXMLHelper();

    public FileRepository(File file) throws IOException
    {
        this.file = file;

    }

    public String getToolTipText()
    {
        return file.getPath();
    }

    private void read() throws Exception
    {
        if (!file.exists())
        {
            file.createNewFile();
        }
        else
        {
            try
            {
                xmlMessages = xmlSupport.readContent(new FileInputStream(file));
            }
            catch (Exception ex)
            {
                log.error(ex.getMessage(), ex);
            }
        }

        if (xmlMessages == null)
        {
            xmlMessages = xmlSupport.getFactory().createContent();
        }

    }

    private void save() throws Exception
    {
        if (xmlMessages != null)
        {
            xmlSupport.saveContent(xmlMessages, new FileOutputStream(file));
        }
    }

    public String getId()
    {
        return file.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepository#getMessages(java.lang.String)
     */
    public Collection getMessages(Hermes hermes) throws JMSException
    {
        if (hermes == null)
        {
            throw new HermesException("No Hermes available");
        }

        try
        {
            if (xmlMessages == null)
            {
                read();
            }

            return xmlSupport.fromMessageSet(hermes, xmlMessages);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            throw new HermesException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepository#addMessages(java.lang.String,
     *      java.util.Collection)
     */
    public void addMessages(Hermes hermes, Collection messages) throws JMSException
    {
        if (hermes == null)
        {
            throw new HermesException("No Hermes available");
        }

        try
        {
            if (xmlMessages == null)
            {
                read();
            }

            MessageSet newMessages = xmlSupport.toMessageSet(messages);

            xmlMessages.getEntry().addAll(newMessages.getEntry());

            save();
        }
        catch (JMSException e)
        {
            throw e; // Ugh.
        }
        catch (Exception e)
        {
            throw new HermesException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepository#addMessage(hermes.Hermes, javax.jms.Message)
     */
    public void addMessage(Hermes hermes, Message message) throws JMSException
    {
        List tmp = new ArrayList();
        tmp.add(message);

        addMessages(hermes, tmp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepository#delete()
     */
    public void delete()
    {
        if (file != null)
        {
            file.delete();
        }

        xmlMessages = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepository#size()
     */
    public int size()
    {
        if (xmlMessages == null)
        {
            return 0;
        }
        else
        {
            return xmlMessages.getEntry().size();
        }

    }

}