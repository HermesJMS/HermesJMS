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
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import org.apache.log4j.Logger;

/**
 * An XML file provider.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FileSession.java,v 1.7 2005/07/08 19:42:54 colincrist Exp $
 */

public class FileSession extends FileMessageFactory implements QueueSession
{
    private static final Logger log = Logger.getLogger(FileSession.class);
    private static final String SEPARATOR = System.getProperty("file.separator");
    private FileConnection connection;
    private MessageListener listener;
    private boolean transacted;
    private int ackMode;
    private Collection onCommit = new ArrayList();

    public FileSession(FileConnection connection, boolean transacted, int ackMode)
    {
        this.connection = connection;
        this.transacted = transacted;
        this.ackMode = ackMode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#close()
     */
    public void close() throws JMSException
    {
        onCommit.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#commit()
     */
    public void commit() throws JMSException
    {
        for (Iterator iter = onCommit.iterator(); iter.hasNext();)
        {
            Commitable c = (Commitable) iter.next();

            c.commit();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#getMessageListener()
     */
    public MessageListener getMessageListener() throws JMSException
    {
        return listener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#getTransacted()
     */
    public boolean getTransacted() throws JMSException
    {
        return transacted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#recover()
     */
    public void recover() throws JMSException
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#rollback()
     */
    public void rollback() throws JMSException
    {
        for (Iterator iter = onCommit.iterator(); iter.hasNext();)
        {
            Commitable c = (Commitable) iter.next();

            c.rollback();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run()
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#setMessageListener(javax.jms.MessageListener)
     */
    public void setMessageListener(MessageListener listener) throws JMSException
    {
        this.listener = listener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSession#createBrowser(javax.jms.Queue,
     *      java.lang.String)
     */
    public QueueBrowser createBrowser(Queue arg0, String arg1) throws JMSException
    {
        return new FileQueueBrowser(this, (FileQueue) arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSession#createBrowser(javax.jms.Queue)
     */
    public QueueBrowser createBrowser(Queue arg0) throws JMSException
    {
        return new FileQueueBrowser(this, (FileQueue) arg0);
    }

   

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSession#createReceiver(javax.jms.Queue,
     *      java.lang.String)
     */
    public QueueReceiver createReceiver(Queue arg0, String arg1) throws JMSException
    {
        try
        {
            FileMessageConsumer rval = new FileMessageConsumer(this, (FileQueue) arg0, arg1);
            onCommit.add(rval);
            return rval;
        }
        catch (IOException e)
        {
            throw new HermesException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSession#createReceiver(javax.jms.Queue)
     */
    public QueueReceiver createReceiver(Queue arg0) throws JMSException
    {
        try
        {
            FileMessageConsumer rval = new FileMessageConsumer(this, (FileQueue) arg0, null);
            onCommit.add(rval);
            return rval;
        }
        catch (IOException e)
        {
            throw new HermesException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSession#createSender(javax.jms.Queue)
     */
    public QueueSender createSender(Queue arg0) throws JMSException
    {
        FileMessageProducer rval = new FileMessageProducer(this, (FileQueue) arg0);
        onCommit.add(rval);
        return rval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueSession#createTemporaryQueue()
     */
    public TemporaryQueue createTemporaryQueue() throws JMSException
    {
        try
        {
            return new FileQueue();
        }
        catch (IOException e)
        {
            throw new HermesException(e);
        }
    }

    /**
     * @return Returns the connection.
     */
    public FileConnection getConnection()
    {
        return connection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#createConsumer(javax.jms.Destination,
     *      java.lang.String, boolean)
     */
    public MessageConsumer createConsumer(Destination arg0, String arg1, boolean arg2) throws JMSException
    {
        try
        {
            FileMessageConsumer rval = new FileMessageConsumer(this, (FileQueue) arg0, null);
            onCommit.add(rval);
            return rval;
        }
        catch (IOException e)
        {
            throw new HermesException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#createConsumer(javax.jms.Destination,
     *      java.lang.String)
     */
    public MessageConsumer createConsumer(Destination arg0, String arg1) throws JMSException
    {
        try
        {
            FileMessageConsumer rval = new FileMessageConsumer(this, (FileQueue) arg0, null);
            onCommit.add(rval);
            return rval;
        }
        catch (IOException e)
        {
            throw new HermesException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#createConsumer(javax.jms.Destination)
     */
    public MessageConsumer createConsumer(Destination arg0) throws JMSException
    {
        try
        {
            FileMessageConsumer rval = new FileMessageConsumer(this, (FileQueue) arg0, null);
            onCommit.add(rval);
            return rval;
        }
        catch (IOException e)
        {
            throw new HermesException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#createDurableSubscriber(javax.jms.Topic,
     *      java.lang.String, java.lang.String, boolean)
     */
    public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1, String arg2, boolean arg3) throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#createDurableSubscriber(javax.jms.Topic,
     *      java.lang.String)
     */
    public TopicSubscriber createDurableSubscriber(Topic arg0, String arg1) throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#createProducer(javax.jms.Destination)
     */
    public MessageProducer createProducer(Destination arg0) throws JMSException
    {
        FileMessageProducer rval = new FileMessageProducer(this, (FileQueue) arg0);
        onCommit.add(rval);
        return rval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#createTemporaryTopic()
     */
    public TemporaryTopic createTemporaryTopic() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

   

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#getAcknowledgeMode()
     */
    public int getAcknowledgeMode() throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Session#unsubscribe(java.lang.String)
     */
    public void unsubscribe(String arg0) throws JMSException
    {
        // TODO Auto-generated method stub

    }
}