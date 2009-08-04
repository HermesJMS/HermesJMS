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

package hermes.browser.tasks;

import hermes.BrowseInterruptedException;
import hermes.Domain;
import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.config.DestinationConfig;
import hermes.impl.QueueBrowserWithConsumer;

import java.util.Enumeration;
import java.util.Iterator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public class BrowseDestinationTask extends TaskSupport implements Task
{
   private static final Logger log = Logger.getLogger(BrowseDestinationTask.class);
   private Hermes hermes;
   private Iterator iter;
   private QueueBrowser browser;
   private String title = "Browse";
   private boolean useMessageConsumer = false ;

   public BrowseDestinationTask(Hermes hermes, final DestinationConfig dConfig)
   {
      super(Domain.getDomain(dConfig.getDomain()).getIcon());

      this.hermes = hermes;
      this.title = "Browsing " + dConfig.getName() + " on " + hermes.getId();

      if (dConfig.isDurable())
      {
         title = title + " (durableName=" + dConfig.getClientID() + ")";
      }

      this.iter = new Iterator()
      {
         private boolean first = true;

         public void remove()
         {
            // TODO Auto-generated method stub
         }

         public boolean hasNext()
         {
            return first;
         }

         public Object next()
         {
            first = false;
            return dConfig;
         }
      };
   }

   public BrowseDestinationTask(Hermes hermes, Iterator iter)
   {
      super(IconCache.getIcon("jms.unknown"));

      this.hermes = hermes;
      this.iter = iter;
   }

   public String getTitle()
   {
      return title;
   }

   protected QueueBrowser createBrowser(Destination destination, DestinationConfig dConfig) throws JMSException
   {
      if (dConfig.getDomain() == Domain.QUEUE.getId() && hermes.getSessionConfig().isUseConsumerForQueueBrowse())
      {
         log.debug("using a MessageConsumer for the QueueBrowse") ;
         
         
         return new QueueBrowserWithConsumer(hermes, (Queue) destination,  dConfig.getSelector(), HermesBrowser.getBrowser().getQueueBrowseConsumerTimeout()) ;
      }
      else
      {
         return hermes.createBrowser(dConfig) ;
      }
   }

   public void stop()
   {
      super.stop();

      try
      {
         if (browser != null)
         {
            browser.close();
            browser = null;
         }
      }
      catch (JMSException e)
      {
         log.error(e.getMessage(), e);
      }
   }

   public void invoke() throws Exception
   {
      while (iter.hasNext())
      {
         final DestinationConfig dConfig = (DestinationConfig) iter.next();
         int nmessages = 0;

                  
         try
         {
            final Destination destination = hermes.getDestination(dConfig.getName(), Domain.getDomain(dConfig.getDomain()));
            browser = createBrowser(destination, dConfig);

            notifyStatus("Running...");

            for (final Enumeration messageIter = browser.getEnumeration() ; messageIter.hasMoreElements() && isRunning();)
            {
               final Message message = (Message) messageIter.nextElement();

               if (message != null)
               {
                  notifyMessage(message);
                  nmessages++;
               }
               else
               {
                  // @TODO May be a bug here, keep an eye out for this message recurring!
                  
                  log.error("Got a null message!") ;
                  Thread.sleep(500) ;
               }
            }

            if (!isRunning())
            {
               log.debug("user requested stop browse of " + dConfig.getName());
            }
         }
         catch (BrowseInterruptedException ex)
         {
            log.info("browse of " + getTitle() + " interrupted after " + nmessages);
         }
         catch (InterruptedException ex)
         {
            log.info(ex.getMessage(), ex) ;
         }
         finally
         {
            log.debug("browse complete nmessages=" + nmessages);
            
            notifyStatus("Done.");

            if (browser != null)
            {
               browser.close();
               browser = null;
            }

            hermes.close();
         }
      }
   }

   public boolean isUseMessageConsumer()
   {
      return useMessageConsumer;
   }

   public void setUseMessageConsumer(boolean useMessageConsumer)
   {
      this.useMessageConsumer = useMessageConsumer;
   }
}
