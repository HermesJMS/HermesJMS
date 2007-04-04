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
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JNDITopicConnectionFactory.java,v 1.2 2004/05/08 15:15:48
 *          colincrist Exp $
 */
public class JNDITopicConnectionFactory extends JNDIConnectionFactory implements TopicConnectionFactory
{
	private static final Logger log = Logger.getLogger(JNDITopicConnectionFactory.class);

    private TopicConnectionFactory getFactory() throws JMSException 
    {
        Context ctx = null ;
        TopicConnectionFactory factory ;
        
        try
        {
            ctx = createContext() ;
            factory = (TopicConnectionFactory) ctx.lookup(getBinding());
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
    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.TopicConnectionFactory#createTopicConnection()
     */
    public TopicConnection createTopicConnection() throws JMSException
    {
       return getFactory().createTopicConnection() ;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.TopicConnectionFactory#createTopicConnection(java.lang.String,
     *      java.lang.String)
     */
    public TopicConnection createTopicConnection(String arg0, String arg1) throws JMSException
    {
        return getFactory().createTopicConnection(arg0, arg1);
    }

}