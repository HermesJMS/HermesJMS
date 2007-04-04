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

package hermes.ext;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdminListener;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.browser.MessageRenderer;
import hermes.config.DestinationConfig;
import hermes.impl.TopicBrowser;
import hermes.util.JMSUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: HermesAdminSupport.java,v 1.1 2004/07/21 19:46:16 colincrist
 *          Exp $
 */
public abstract class HermesAdminSupport
{
   private static final Logger log = Logger.getLogger(HermesAdminSupport.class);
   private static final Map<String, Object> defaultStatistics = new HashMap<String, Object>();
   private final Set<HermesAdminListener> listeners = new HashSet<HermesAdminListener>();
   private final List statisticsHeader = new ArrayList();
   private final Map destinationConfigs = new HashMap();
   private final Hermes hermes;
   private boolean isNameInNamespaceCheck = true ;

   static
   {
      defaultStatistics.put("Unavailable", "No statistics currently available.");
   }

   public static Map getDefaultStatistics()
   {
      return defaultStatistics;
   }

   /**
    *  
    */
   public HermesAdminSupport(Hermes hermes)
   {
      super();

      this.hermes = hermes;
   }

   public void addListener(HermesAdminListener listener)
   {
      listeners.add(listener);
   }

   public void removeListener(HermesAdminListener listener)
   {
      listeners.remove(listener);
   }

   protected void notifyDestinationAdded(DestinationConfig config)
   {
      for (final HermesAdminListener listener : listeners)
      {
         listener.onDestinationAdded(config);
      }
   }

   protected void notifyDestinationRemoved(DestinationConfig config)
   {
      for (final HermesAdminListener listener : listeners)
      {
         listener.onDestinationRemoved(config);
      }
   }

   public long getAge(DestinationConfig dest) throws JMSException
   {
      long rval = 0;

      QueueBrowser browser = hermes.createBrowser(dest);
      Enumeration iter = browser.getEnumeration();

      if (iter.hasMoreElements())
      {
         Message topMessage = (Message) iter.nextElement();

         if (topMessage != null)
         {
            rval = topMessage.getJMSTimestamp();
         }
      }

      browser.close();

      return rval;
   }

   public abstract int getDepth(DestinationConfig destination) throws JMSException;

   public Hermes getHermes()
   {
      return hermes;
   }

   public int truncate(DestinationConfig dConfig) throws JMSException
   {
      boolean keepRunning = true;
      
      StringBuffer message = new StringBuffer();
      int numToDelete = 0;
      ProgressMonitor monitor = new ProgressMonitor(HermesBrowser.getBrowser(), "Deleting from " + dConfig.getName(), "Discovering size of " + dConfig.getName(),
            0, 102);

      monitor.setMillisToDecideToPopup(100);
      monitor.setMillisToPopup(400);

      try
      {

         QueueBrowser browser = getHermes().createBrowser(dConfig);
         Enumeration iter = browser.getEnumeration();

         monitor.setProgress(1);

         Hermes.ui.getDefaultMessageSink().add("Discovering size of " + dConfig.getName());

         while (iter.hasMoreElements() && keepRunning && !monitor.isCanceled())
         {
            iter.nextElement();
            numToDelete++;
         }

         browser.close();
         getHermes().rollback();

         if (monitor.isCanceled())
         {
            keepRunning = false;
         }

         String message2 = dConfig.getName() + " is already empty.";

         if (numToDelete == 1)
         {
            message2 = "Deleting one message from " + dConfig.getName();
         }
         else
         {
            message2 = "Deleting " + numToDelete + " messages from " + dConfig.getName();
         }

         Hermes.ui.getDefaultMessageSink().add(message2);

         monitor.setProgress(2);

         final Destination destination = getHermes().getDestination(dConfig.getName(), Domain.getDomain(dConfig.getDomain())) ;
         
         for (int i = 0; i < numToDelete && keepRunning && !monitor.isCanceled(); i++)
         {
            Message m = null;

            while (m == null && keepRunning)
            {
               m = getHermes().receive(destination, 10000);
            }

            monitor.setNote(new Long(i) + " messages deleted");

            float l_i = i;
            float l_numToDelete = numToDelete;
            int progress = Math.round(2 + ((l_i / l_numToDelete) * 100));

            monitor.setProgress(progress);
         }

         if (monitor.isCanceled())
         {
            keepRunning = false;
         }

         if (numToDelete > 0)
         {
            if (keepRunning)
            {
               getHermes().commit();
               message.append("Committed. ").append(numToDelete).append(" messages deleted from " + dConfig.getName());
            }
            else
            {
               getHermes().rollback();
               message.append("Trucate of ").append(dConfig.getName()).append(" rolled back");
            }
         }
      }
      catch (Exception ex)
      {
         message.append("During truncate of ").append(dConfig.getName()).append(": ").append(ex.getMessage());
         log.error(ex);
      }
      finally
      {
         if (monitor != null)
         {
            monitor.close();
         }
      }

      Hermes.ui.getDefaultMessageSink().add(message);

      return numToDelete;
   }

   public Map getStatistics(DestinationConfig destination) throws JMSException
   {
      return defaultStatistics;
   }

   public Collection getStatistics(Collection destinations) throws JMSException
   {
      List rval = new ArrayList();

      for (Iterator iter = destinations.iterator(); iter.hasNext();)
      {
         DestinationConfig d = (DestinationConfig) iter.next();
         rval.add(getStatistics(d));
      }

      return rval;
   }

   public Enumeration createBrowserProxy(Enumeration iter) throws JMSException
   {
      return iter;
   }

   protected Collection discoverDurableSubscriptions(String topicName, String jndiName) throws JMSException
   {
      return new ArrayList();
   }

   private void discoverDestinationConfigsFromContext(String path, Context ctx, Collection<DestinationConfig> rval, Set<String> visited) throws JMSException
   {
      try
      {
         if (isNameInNamespaceCheck)
         {
            try
            {
               if (visited.contains(ctx.getNameInNamespace()))
               {
                  return ;
               }
               
               visited.add(ctx.getNameInNamespace());
            }
            catch (OperationNotSupportedException ex)
            {
               isNameInNamespaceCheck = false ;
            }
         }

         for (final NamingEnumeration iter = ctx.list(""); iter.hasMore();)
         {
            final NameClassPair entry = (NameClassPair) iter.next();

            try
            {
               final Object object = ctx.lookup(entry.getName());
               DestinationConfig config = null;

               if (object instanceof Destination)
               {
                  final Domain domain = Domain.getDomain((Destination) object);

                  if (domain == Domain.QUEUE)
                  {
                     config = HermesBrowser.getConfigDAO().createDestinationConfig();
                     config.setDomain(Domain.QUEUE.getId());
                  }
                  else if (domain == Domain.TOPIC)
                  {
                     config = HermesBrowser.getConfigDAO().createDestinationConfig();
                     config.setDomain(Domain.TOPIC.getId());

                     rval.addAll(discoverDurableSubscriptions(JMSUtils.getDestinationName((Topic) object), path == null ? entry.getName() : path + "/"
                           + entry.getName()));

                  }
                  else
                  {
                     config = HermesBrowser.getConfigDAO().createDestinationConfig();
                     config.setDomain(Domain.UNKNOWN.getId());
                  }
               }
               else if (object instanceof Context)
               {
                  String ctxPath;

                  if (path == null)
                  {
                     ctxPath = entry.getName();
                  }
                  else
                  {
                     ctxPath = path + "/" + entry.getName();
                  }

                  discoverDestinationConfigsFromContext(ctxPath, (Context) object, rval, visited);
               }

               if (config != null)
               {
                  String binding = null;

                  if (path == null)
                  {
                     binding = entry.getName();
                  }
                  else
                  {
                     binding = path + "/" + entry.getName();
                  }

                  config.setName(binding);
                  rval.add(config);
               }
            }
            catch (Throwable ex)
            {
               log.error("discoverDestinationConfig binding=" + entry.getName() + ": " + ex.getMessage(), ex);
            }
         }

      }
      catch (NamingException e)
      {
         log.error(e.getMessage(), e) ;
         throw new HermesException(e);
      }
   }

   public Collection<DestinationConfig> discoverDestinationConfigs() throws JMSException
   {
      if (hermes.getConnectionFactory() instanceof JNDIConnectionFactory)
      {
         final JNDIConnectionFactory cf = (JNDIConnectionFactory) hermes.getConnectionFactory();
         final Collection<DestinationConfig> rval = new ArrayList<DestinationConfig>();
         final Context ctx = cf.createContext();

         discoverDestinationConfigsFromContext(null, ctx, rval, new HashSet<String>());

         return rval;
      }
      else
      {
         throw new HermesException("The default provider extension cannot discover queues or topics on " + hermes.getConnectionFactory().getClass().getName());
      }
   }

   public MessageRenderer getMessageRenderer() throws JMSException
   {
      return null;
   }

   public QueueBrowser createDurableSubscriptionBrowser(DestinationConfig dConfig) throws JMSException
   {
      return new TopicBrowser(getHermes().getSession(), getHermes().getDestinationManager(), dConfig);
   }
   
   public String getRealDestinationName(DestinationConfig dConfig) throws JMSException
   {
      try
      {
         return JMSUtils.getDestinationName(getHermes().getDestination(dConfig.getName(), Domain.getDomain(dConfig.getDomain()))) ;
      }
      catch (NamingException ex)
      {
         throw new HermesException(ex) ;
      }
   }
}