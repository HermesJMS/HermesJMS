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

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

/**
 * An XML file provider.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FileConnectionFactory.java,v 1.2 2004/05/08 15:15:48 colincrist
 *          Exp $
 */

public class FileConnectionFactory implements QueueConnectionFactory
{
    private String baseDirectory;

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueConnectionFactory#createQueueConnection()
     */
    public QueueConnection createQueueConnection() throws JMSException
    {
        return new FileConnection(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueConnectionFactory#createQueueConnection(java.lang.String,
     *      java.lang.String)
     */
    public QueueConnection createQueueConnection(String arg0, String arg1) throws JMSException
    {
        return new FileConnection(this);
    }

    /**
     * @return Returns the baseDirectory.
     */
    public String getBaseDirectory()
    {
        return baseDirectory;
    }

    /**
     * @param baseDirectory
     *            The baseDirectory to set.
     */
    public void setBaseDirectory(String baseDirectory)
    {
        this.baseDirectory = baseDirectory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.ConnectionFactory#createConnection()
     */
    public Connection createConnection() throws JMSException
    {
        return new FileConnection(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.ConnectionFactory#createConnection(java.lang.String,
     *      java.lang.String)
     */
    public Connection createConnection(String arg0, String arg1) throws JMSException
    {
        return new FileConnection(this);
    }
}