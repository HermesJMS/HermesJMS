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

package hermes;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JNDIQueueConnectionFactory.java,v 1.1 2004/05/01 15:52:22
 *          colincrist Exp $
 */
public class JNDIQueueConnectionFactory extends JNDIConnectionFactory implements QueueConnectionFactory
{
	private static final Logger log = Logger.getLogger(JNDIQueueConnectionFactory.class);

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueConnectionFactory#createQueueConnection()
     */
    public synchronized QueueConnection createQueueConnection() throws JMSException
    {
        return getFactory().createQueueConnection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.QueueConnectionFactory#createQueueConnection(java.lang.String,
     *      java.lang.String)
     */
    public synchronized QueueConnection createQueueConnection(String arg0, String arg1) throws JMSException
    {
        return getFactory().createQueueConnection(arg0, arg1);
    }
    
    private QueueConnectionFactory getFactory() throws JMSException 
    {
        Context ctx = null ;
        QueueConnectionFactory factory ;
        
        try
        {
            ctx = createContext() ;
            factory = (QueueConnectionFactory) ctx.lookup(getBinding());
        }
        catch (NamingException e)
        {
			log.error(e.getMessage(), e);
            throw new JMSException(e.getMessage());
        }
        finally
        {
            if (ctx != null)
            {
                try
                {
                    ctx.close() ;
                }
                catch (NamingException e1)
                {
                    log.error("closing JNDI context: " + e1.getMessage(), e1) ;
                }
            }
        }
   
        return factory ;
    }
}