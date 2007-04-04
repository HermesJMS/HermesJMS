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

package hermes.providers.file;

import hermes.HermesException;
import hermes.MessageFactory;

import java.io.IOException;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;

import org.apache.commons.collections.iterators.IteratorEnumeration;

/**
 * An XML file provider.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FileQueueBrowser.java,v 1.5 2007/02/28 10:47:29 colincrist Exp $
 */

public class FileQueueBrowser implements QueueBrowser
{
    private FileQueue queue;
    private String selector;
    private MessageFactory factory;
 
    /**
     *  
     */
    public FileQueueBrowser(MessageFactory factory, FileQueue queue, String selector)
    {
        super();

        this.factory = factory;
        this.queue = queue;
        this.selector = selector;
        
      
    }

    public FileQueueBrowser(MessageFactory factory, FileQueue queue)
    {
        this(factory, queue, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueBrowser#close()
     */
    public void close() throws JMSException
    {
        // NOP
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueBrowser#getEnumeration()
     */
    public Enumeration getEnumeration() throws JMSException
    {
        try
        {
            return new IteratorEnumeration(queue.getMessages(factory, selector).iterator());
        }
        catch (IOException e)
        {
            throw new HermesException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueBrowser#getMessageSelector()
     */
    public String getMessageSelector() throws JMSException
    {
        return selector;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueBrowser#getQueue()
     */
    public Queue getQueue() throws JMSException
    {
        return queue;
    }

}