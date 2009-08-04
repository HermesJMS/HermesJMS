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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JNDIConnectionFactory.java,v 1.4 2004/05/23 13:45:52 colincrist
 *          Exp $
 */
public class JNDIConnectionFactory extends JNDIContextFactory implements ConnectionFactory
{
   private static final Logger log = Logger.getLogger(JNDIConnectionFactory.class);
   private String rmiAdaptorBinding;

   public ConnectionFactory _getConnectionFactory() throws NamingException, JMSException
   {
      return (ConnectionFactory) createContext().lookup(getBinding());
   }

   /**
    * @return Returns the rmiAdaptorBinding.
    */
   public String getRmiAdaptorBinding()
   {
      return rmiAdaptorBinding;
   }

   /**
    * @param rmiAdaptorBinding
    *           The rmiAdaptorBinding to set.
    */
   public void setRmiAdaptorBinding(String rmiAdaptorBinding)
   {
      this.rmiAdaptorBinding = rmiAdaptorBinding;
   }

   private final void checkBinding() throws JMSException
   {
      if (getBinding() == null)
      {
         throw new HermesException("The binding property to locate the ConnectionFactory in the Context is not set");
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jms.ConnectionFactory#createConnection()
    */
   public Connection createConnection() throws JMSException
   {
      checkBinding();

      ConnectionFactory factory;

      try
      {
         factory = (ConnectionFactory) createContext().lookup(getBinding());
      }
      catch (NamingException e)
      {
         log.error(e.getMessage(), e);
         throw new JMSException(e.getMessage());
      }

      return factory.createConnection();
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jms.ConnectionFactory#createConnection(java.lang.String,
    *      java.lang.String)
    */
   public Connection createConnection(String arg0, String arg1) throws JMSException
   {
      checkBinding();

      ConnectionFactory factory;

      try
      {
         factory = (ConnectionFactory) createContext().lookup(getBinding());
      }
      catch (NamingException e)
      {
         log.error(e.getMessage(), e);
         throw new JMSException(e.getMessage());
      }

      return factory.createConnection(arg0, arg1);
   }

}