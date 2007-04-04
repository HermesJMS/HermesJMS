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

package hermes.ext.joram;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.User;



/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: JoramAdmin.java,v 1.9 2006/07/13 07:35:35 colincrist Exp $
 */
public class JoramAdmin extends HermesAdminSupport implements HermesAdmin
{
   private static final Logger log = Logger.getLogger(JoramAdmin.class);
   private JoramAdminFactory factory;
   private Hermes hermes;
   private JNDIConnectionFactory jndiCF;
   private boolean connected = false;
   private User user;

   /**
    * 
    */
   public JoramAdmin(JoramAdminFactory factory, Hermes hermes, JNDIConnectionFactory jndiCF)
   {
      super(hermes);
      this.factory = factory;
      this.jndiCF = jndiCF;
   }

   private synchronized void checkConnected() throws JMSException
   {
      if (!connected)
      {
         try
         {
            AdminModule.connect(factory.getHostname(), factory.getPort(), factory.getUsername(), factory.getPassword(), factory.getCnxTimer());
         }
         catch (ConnectException e)
         {
            throw new HermesException(e);
         }
         catch (UnknownHostException e)
         {
            throw new HermesException(e);
         }
         catch (AdminException e)
         {
            throw new HermesException(e);
         }

         connected = true;
      }
   }

   protected User getUser(String userName) throws JMSException
   {
      checkConnected();

      try
      {
         for (Iterator iter = AdminModule.getUsers().iterator(); iter.hasNext();)
         {
            User u = (User) iter.next();

            if (u.getName().equals(userName))
            {
               return u;
            }
         }
         
         throw new HermesException("No such user " + userName) ;
      }
      catch (ConnectException e)
      {
         throw new HermesException(e);
      }
      catch (AdminException e)
      {
         throw new HermesException(e);
      }
   }

   @Override
   protected Collection discoverDurableSubscriptions(String topicName, String jndiName) throws JMSException
   {
      final ArrayList rval = new ArrayList() ;
      
      checkConnected();
      
      try
      {
         for (Iterator iter = AdminModule.getUsers().iterator(); iter.hasNext();)
         {
            final User u = (User) iter.next();
            final Subscription[] subs = u.getSubscriptions() ;
            
            for (int i = 0 ; i < subs.length ; i++)
            {
               if (subs[i].getTopicId().equals(topicName))
               {
                  DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig() ;
                  
                  dConfig.setName(jndiName) ;
                  dConfig.setDurable(true) ;
                  dConfig.setClientID(subs[i].getName()) ;
                  dConfig.setDomain(Domain.TOPIC.getId()) ;
                  
                  rval.add(dConfig) ;
               }
            }            
         }
      }
      catch (ConnectException e)
      {
         throw new HermesException(e);
      }
      catch (AdminException e)
      {
         throw new HermesException(e);
      }

     return rval ;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesAdmin#getDepth(javax.jms.Destination)
    */
   public int getDepth(DestinationConfig dest) throws JMSException
   {
      checkConnected();

      try
      {
      if (dest.getDomain() == Domain.QUEUE.getId())
      {
         
            Queue queue = (Queue) getHermes().getDestination(dest.getName(), Domain.QUEUE);

            return queue.getPendingMessages();
         
      }
      else
      {
         Topic topic = (Topic) getHermes().getDestination(dest.getName(), Domain.TOPIC) ;
         
        
         throw new HermesException("Cannot get depth, " + dest.getName() + " is a topic");
      }
      
      }
      catch (ConnectException e)
      {
         throw new HermesException(e);
      }
      catch (AdminException e)
      {
         throw new HermesException(e);
      }
      catch (NamingException e)
      {
         throw new HermesException(e);
      }
   }

   /*
  
   public QueueBrowser createDurableSubscriptionBrowser(DestinationConfig dConfig) throws JMSException
   {
      checkConnected() ;
      
      try
      {
      final Topic topic = (Topic) getHermes().getDestination(dConfig.getName(), Domain.TOPIC) ;
      final User user = getUser(factory.getUsername()) ;      
      final String[] ids = user.getMessageIds(dConfig.getClientID()) ;
      
      return new QueueBrowser()
      {
         public void close() throws JMSException
         {
            // TODO Auto-generated method stub     
         }
      
         public Enumeration getEnumeration() throws JMSException
         {
          return new Enumeration()
         {
             int i = 0 ;
            public Object nextElement()
            {
              
            }
         
            public boolean hasMoreElements()
            {
              return i < ids.length ;
            }
         
         } ;
         }
      
         public String getMessageSelector() throws JMSException
         {
            // TODO Auto-generated method stub
            return null;
         }
      
         public javax.jms.Queue getQueue() throws JMSException
         {
            // TODO Auto-generated method stub
            return null;
         }
      
      } ;
      } 
      catch (NamingException e)
      {
         throw new HermesException(e) ;
      }
      catch (AdminException e)
      {
         throw new HermesException(e) ;
      }
      catch (ConnectException e)
      {
         throw new HermesException(e);
      }
      
   }
  */

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesAdmin#close()
    */
   public void close() throws JMSException
   {
      AdminModule.disconnect();
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesAdmin#getStatistics(javax.jms.Destination)
    */
   public Map getStatistics(DestinationConfig dConfig) throws JMSException
   {
      final Map rval = new LinkedHashMap();

      if (dConfig.getDomain() == Domain.QUEUE.getId())
      {
         Queue queue = (Queue) getHermes().getSession().createQueue(dConfig.getName());

         try
         {
            checkConnected();

            rval.put("Name", queue.getName());
            rval.put("AdminName", queue.getAdminName());
            rval.put("Threshold", Integer.toString(queue.getThreshold()));
         }
         catch (ConnectException e)
         {
            throw new HermesException(e);
         }
         catch (AdminException e)
         {
            throw new HermesException(e);
         }
      }
      else
      {
         throw new HermesException("Cannot get statistics, " + dConfig.getName() + " is a topic");
      }

      return rval;
   }

}