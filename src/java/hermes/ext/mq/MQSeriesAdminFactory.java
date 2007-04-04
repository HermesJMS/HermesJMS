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

package hermes.ext.mq;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;

import java.util.StringTokenizer;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.ibm.mq.MQException;
import com.ibm.mq.jms.MQConnectionFactory;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MQSeriesAdminFactory.java,v 1.1 2004/07/30 17:25:15 colincrist
 *          Exp $
 */
public class MQSeriesAdminFactory implements HermesAdminFactory
{
    private static final Logger log = Logger.getLogger(MQSeriesAdminFactory.class);
    private String logExclude ;
    
    public MQSeriesAdminFactory()
    {
        // NOP
    }
 
    public void setLogExclude(String logExclude)
    {
       this.logExclude = logExclude ;
       
       for (final StringTokenizer tokens = new StringTokenizer(logExclude, ",") ; tokens.hasMoreTokens() ;)
       {
          final String code = tokens.nextToken() ;
          
          try
          {
          
             MQException.logExclude(Integer.decode(code)) ;
             
             log.debug("set MQException.logExclude(" + code + ")") ;
          }
          catch (Throwable t)
          {
             log.error("cannot set MQException.logExclude, probably pre WMQ 6.0:" + t.getMessage()) ;
          }
       }
    }
    
    public String getLogExclude()
    {
       return logExclude ;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see hermes.ProviderExtension#createSession(javax.jms.ConnectionFactory)
     */
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException
    {
        if (connectionFactory instanceof MQConnectionFactory)
        {
            MQConnectionFactory mqCF = (MQConnectionFactory) connectionFactory;
            return new MQSeriesAdmin(hermes, mqCF);
        }
        else if (connectionFactory instanceof JNDIConnectionFactory)
        {
            //
            // Pull it out the real factory and try again.
            
            JNDIConnectionFactory jndiCF = (JNDIConnectionFactory) connectionFactory ;
            
            return createSession(hermes, jndiCF._getConnectionFactory()) ;
        }
        else
        {
            throw new HermesException("JMS provider " + connectionFactory.getClass().getName() + " is not MQSeries");
        }
    }
}