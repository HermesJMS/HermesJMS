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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.log4j.Logger;

/**
 * An XML file provider.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FileConnection.java,v 1.6 2004/11/07 11:24:49 colincrist Exp $
 */
public class FileConnection implements QueueConnection
{
    private static final Logger log = Logger.getLogger(FileConnection.class);

    private String clientID;
    private ExceptionListener exceptionListener;
    private FileConnectionFactory factory;
    private Collection tasksOnStart = new ArrayList();
    private boolean started = false;

    public FileConnection(FileConnectionFactory factory)
    {
        this.factory = factory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#close()
     */
    public void close() throws JMSException
    {
        log.debug("close()");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#getClientID()
     */
    public String getClientID() throws JMSException
    {
        return clientID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#getExceptionListener()
     */
    public ExceptionListener getExceptionListener() throws JMSException
    {
        return exceptionListener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#getMetaData()
     */
    public ConnectionMetaData getMetaData() throws JMSException
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#setClientID(java.lang.String)
     */
    public void setClientID(String clientID) throws JMSException
    {
        this.clientID = clientID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#setExceptionListener(javax.jms.ExceptionListener)
     */
    public void setExceptionListener(ExceptionListener arg0) throws JMSException
    {
        exceptionListener = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#start()
     */
    public synchronized void start() throws JMSException
    {
        if (!started)
        {
            started = true;

            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    for (Iterator iter = tasksOnStart.iterator(); iter.hasNext();)
                    {
                        Runnable r = (Runnable) iter.next();

                        r.run();
                    }
                }
            });

            t.start();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#stop()
     */
    public void stop() throws JMSException
    {

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueConnection#createConnectionConsumer(javax.jms.Queue,
     *      java.lang.String, javax.jms.ServerSessionPool, int)
     */
    public ConnectionConsumer createConnectionConsumer(Queue arg0, String arg1, ServerSessionPool arg2, int arg3) throws JMSException
    {
        throw new HermesException("Not available with File provider");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueConnection#createQueueSession(boolean, int)
     */
    public QueueSession createQueueSession(boolean arg0, int arg1) throws JMSException
    {
        return new FileSession(this, arg0, arg1);
    }

    /**
     * @return Returns the factory.
     */
    public FileConnectionFactory getFactory()
    {
        return factory;
    }

    public synchronized void doOnStart(final Runnable r)
    {
        if (started)
        {
            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    r.run();
                }
            });
        }
        else
        {
            tasksOnStart.add(r);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#createConnectionConsumer(javax.jms.Destination,
     *      java.lang.String, javax.jms.ServerSessionPool, int)
     */
    public ConnectionConsumer createConnectionConsumer(Destination arg0, String arg1, ServerSessionPool arg2, int arg3) throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#createDurableConnectionConsumer(javax.jms.Topic,
     *      java.lang.String, java.lang.String, javax.jms.ServerSessionPool,
     *      int)
     */
    public ConnectionConsumer createDurableConnectionConsumer(Topic arg0, String arg1, String arg2, ServerSessionPool arg3, int arg4) throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Connection#createSession(boolean, int)
     */
    public Session createSession(boolean arg0, int arg1) throws JMSException
    {
        return new FileSession(this, arg0, arg1);
    }
}