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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueSender;

import org.apache.log4j.Logger;

/**
 * An XML file provider.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FileMessageProducer.java,v 1.2 2004/05/08 15:15:48 colincrist
 *          Exp $
 */

public class FileMessageProducer implements QueueSender, Commitable
{
    private static final Logger log = Logger.getLogger(FileMessageProducer.class);
    private FileSession session;
    private FileQueue queue;
    private Collection messages = new ArrayList();

    public FileMessageProducer(FileSession session, FileQueue queue) throws JMSException
    {
        this.session = session;
        this.queue = queue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#close()
     */
    public void close() throws JMSException
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#getDeliveryMode()
     */
    public int getDeliveryMode() throws JMSException
    {
        return 1;

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#getDisableMessageID()
     */
    public boolean getDisableMessageID() throws JMSException
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#getDisableMessageTimestamp()
     */
    public boolean getDisableMessageTimestamp() throws JMSException
    {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#getPriority()
     */
    public int getPriority() throws JMSException
    {
        return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#getTimeToLive()
     */
    public long getTimeToLive() throws JMSException
    {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#setDeliveryMode(int)
     */
    public void setDeliveryMode(int arg0) throws JMSException
    {
        // NOP
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#setDisableMessageID(boolean)
     */
    public void setDisableMessageID(boolean arg0) throws JMSException
    {
        // NOP
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#setDisableMessageTimestamp(boolean)
     */
    public void setDisableMessageTimestamp(boolean arg0) throws JMSException
    {
        // NOP
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#setPriority(int)
     */
    public void setPriority(int arg0) throws JMSException
    {
        // NOP
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#setTimeToLive(long)
     */
    public void setTimeToLive(long arg0) throws JMSException
    {
        // NOP
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.Commitable#commit()
     */
    public void commit() throws JMSException
    {
        if (messages.size() > 0)
        {
            try
            {
                if (queue == null)
                {
                    for (Iterator iter = messages.iterator() ; iter.hasNext() ;)
                    {
                        Message m = (Message) iter.next() ;
                        
                        if (m.getJMSDestination() != null)
                        {
                            if (m.getJMSDestination() instanceof FileQueue)
                            {
                                FileQueue queue = (FileQueue) m.getJMSDestination() ;
                                Collection messages = new ArrayList() ;
                                messages.add(m) ;
                                
                                queue.addMessages(session, messages) ;
                            }
                            else
                            {
                                throw new HermesException("destination is not a FileQueue") ;
                            }
                        }
                        else
                        {
                            throw new HermesException("message has no destination") ;
                        }
                    }
                }
                else
                {
                    queue.addMessages(session, messages);
                }
            }
            catch (IOException e)
            {
                throw new HermesException(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.Commitable#rollback()
     */
    public void rollback() throws JMSException
    {
        messages.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSender#getQueue()
     */
    public Queue getQueue() throws JMSException
    {
        return queue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSender#send(javax.jms.Message, int, int, long)
     */
    public void send(Message arg0, int arg1, int arg2, long arg3) throws JMSException
    {
        messages.add(arg0);

        if (!session.getTransacted())
        {
            commit();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSender#send(javax.jms.Message)
     */
    public void send(Message arg0) throws JMSException
    {
        messages.add(arg0);

        if (!session.getTransacted())
        {
            commit();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSender#send(javax.jms.Queue, javax.jms.Message, int,
     *      int, long)
     */
    public void send(Queue arg0, Message arg1, int arg2, int arg3, long arg4) throws JMSException
    {
        arg1.setJMSDestination(arg0) ;
        
        messages.add(arg1);

        if (!session.getTransacted())
        {
            commit();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSender#send(javax.jms.Queue, javax.jms.Message)
     */
    public void send(Queue arg0, Message arg1) throws JMSException
    {
        arg1.setJMSDestination(arg0) ;
        
        messages.add(arg1);

        if (!session.getTransacted())
        {
            commit();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#getDestination()
     */
    public Destination getDestination() throws JMSException
    {
        return queue ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#send(javax.jms.Destination,
     *      javax.jms.Message, int, int, long)
     */
    public void send(Destination arg0, Message arg1, int arg2, int arg3, long arg4) throws JMSException
    {
        arg1.setJMSDestination(arg0) ;
        
        messages.add(arg1);

        if (!session.getTransacted())
        {
            commit();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.MessageProducer#send(javax.jms.Destination,
     *      javax.jms.Message)
     */
    public void send(Destination arg0, Message arg1) throws JMSException
    {
        arg1.setJMSDestination(arg0) ;
        
        messages.add(arg1);

        if (!session.getTransacted())
        {
            commit();
        }

    }
}