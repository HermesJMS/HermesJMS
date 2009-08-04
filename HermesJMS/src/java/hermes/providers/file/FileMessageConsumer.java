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

import java.io.IOException;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;

import org.apache.log4j.Logger;

/**
 * An XML file provider.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FileMessageConsumer.java,v 1.1 2004/05/01 15:52:35 colincrist
 *          Exp $
 */

public class FileMessageConsumer implements QueueReceiver, Commitable
{
    private static final Logger log = Logger.getLogger(FileMessageConsumer.class);

    private FileSession session;
    private MessageListener listener;
    private FileQueue queue;
    private String selector;
    private Iterator messages;

    public FileMessageConsumer(FileSession session, FileQueue queue, String selector) throws JMSException, IOException
    {
        this.session = session;
        this.queue = queue;
        this.selector = selector;

        messages = queue.getMessages(session, selector).iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageConsumer#close()
     */
    public void close() throws JMSException
    {
        log.debug("close()");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageConsumer#getMessageListener()
     */
    public MessageListener getMessageListener() throws JMSException
    {
        return listener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageConsumer#getMessageSelector()
     */
    public String getMessageSelector() throws JMSException
    {
        return selector;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageConsumer#receive()
     */
    public Message receive() throws JMSException
    {
        return (Message) messages.next();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageConsumer#receive(long)
     */
    public Message receive(long arg0) throws JMSException
    {
        if (messages.hasNext())
        {
            return (Message) messages.next();
        }
        else
        {
            try
            {
                Thread.sleep(arg0);
            }
            catch (InterruptedException e)
            {
                log.error(e.getMessage(), e);
            }

            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageConsumer#receiveNoWait()
     */
    public Message receiveNoWait() throws JMSException
    {
        if (messages.hasNext())
        {
            return (Message) messages.next();
        }
        else
        {
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageConsumer#setMessageListener(javax.jms.MessageListener)
     */
    public void setMessageListener(final MessageListener listener) throws JMSException
    {
        this.listener = listener;

        //
        // Deliver any messages onto the listener, the task will be stacked up
        // until Connection.start() is called.

        Runnable r = new Runnable()
        {
            public void run()
            {
                Message m = null;

                try
                {
                    while ((m = receiveNoWait()) != null)
                    {
                        listener.onMessage(m);
                    }
                }
                catch (JMSException e)
                {
                    log.error(e.getMessage(), e);
                }
            }
        };

        session.getConnection().doOnStart(r);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueReceiver#getQueue()
     */
    public Queue getQueue() throws JMSException
    {
        return queue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.Commitable#commit()
     */
    public void commit() throws JMSException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.Commitable#rollback()
     */
    public void rollback() throws JMSException
    {
        // TODO Auto-generated method stub

    }

}