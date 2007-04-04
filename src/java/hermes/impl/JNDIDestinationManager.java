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

package hermes.impl;

import hermes.Domain;
import hermes.HermesException;
import hermes.impl.jms.SimpleDestinationManager;
import hermes.util.JMSUtils;

import java.util.Properties;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * Handler for destinations that are bound into JNDI. Lets you mix JNDI binding
 * names and ordinary destinations names, if the name is not found in the
 * context then if createIfNotBound is set just go ahead and delegate back to
 * the javax.jms.Session.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: JNDIDestinationManager.java,v 1.3 2004/07/21 20:25:41
 *          colincrist Exp $
 */
public class JNDIDestinationManager extends SimpleDestinationManager
{
   private static final Logger log = Logger.getLogger(JNDIDestinationManager.class);
   private Properties properties;
   private boolean createIfNotBound;
   private Context context;

   public JNDIDestinationManager(Properties properties, boolean createIfNotBound)
   {
      this.properties = properties;
      this.createIfNotBound = createIfNotBound;
   }

   /**
    * Lookup a destination from JNDI - however if its not bound in JNDI and
    * createIfNotBound is set then just default back to the session. Means u can
    * mix JNDI and non-JNDI named destinations on the same Hermes.
    */
   protected Destination createDesintaion(Session session, String named, Domain domain) throws JMSException
   {
      Destination rval = null;

      try
      {
         rval = (Destination) getContext().lookup(named);
      }
      catch (NameNotFoundException e)
      {
         if (!createIfNotBound)
         {
            throw new HermesException(e);
         }
      }
      catch (NamingException e)
      {
         // force a recreation of the context...

         synchronized (this)
         {
            try
            {
               context.close();
            }
            catch (NamingException e1)
            {
               log.error("closing context: " + e1.getMessage(), e1);
            }

            context = null;
         }

         throw new HermesException(e);
      }

      if (rval == null)
      {
         log.warn("failed to find " + named + " in JNDI context, trying a creation by name");

         return super.createDesintaion(session, named, domain);
      }
      else
      {
         log.debug("got destination " + JMSUtils.getDestinationName(rval) + " bound at " + named);

         return rval;
      }
   }

   public Context getContext() throws NamingException
   {
      synchronized (this)
      {
         if (context == null)
         {
            context = new InitialContext(properties);
         }
      }
      return context;
   }

}