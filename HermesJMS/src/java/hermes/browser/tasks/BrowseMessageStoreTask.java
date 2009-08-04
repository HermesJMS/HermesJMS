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
import hermes.Hermes;
import hermes.MessageSelector;
import hermes.MessageSelectorFactory;
import hermes.MessageSelectorFactoryFactory;
import hermes.browser.IconCache;
import hermes.store.MessageStore;
import hermes.util.JMSUtils;
import hermes.util.TextUtils;

import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public class BrowseMessageStoreTask extends TaskSupport
{
   private static final Logger log = Logger.getLogger(BrowseMessageStoreTask.class);
  
   private MessageSelectorFactory messageSelectorFactory ;
   private Hermes hermes;
   private MessageStore messageStore;
   private Destination destination;
   private MessageSelector messageSelector ;

   public BrowseMessageStoreTask(Hermes hermes, MessageStore messageStore, Destination destination, String selector) throws JMSException
   {
      super(IconCache.getIcon("hermes.store"));

      this.hermes = hermes;
      this.messageStore = messageStore;
      this.destination = destination;
     
      if (selector != null)
      {
         final MessageSelectorFactory messageSelectorFactory = MessageSelectorFactoryFactory.create() ;
         messageSelector = messageSelectorFactory.create(selector) ;
      }
   }

   public String getTitle()
   {
      return "Browsing " + (destination == null ? "" : JMSUtils.getDestinationName(destination) + " in ") + messageStore.getId();
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.Task#run()
    */
   public void invoke() throws Exception
   {
      int nmessages = 0;
      QueueBrowser browser = null;
      
      
      
      try
      {       
         if (destination == null)
         {
            if (hermes == null)
            {
               browser = messageStore.visit();
            }
            else
            {
               browser = messageStore.visit(hermes, MessageStore.HeaderPolicy.MESSAGEID_AND_DESTINATION);
            }
         }
         else
         {
            if (hermes == null)
            {
               browser = messageStore.visit(destination);
            }
            else
            {
               browser = messageStore.visit(hermes, destination, MessageStore.HeaderPolicy.MESSAGEID_AND_DESTINATION);
            }
         }

         
         for  (Enumeration iter = browser.getEnumeration() ; iter.hasMoreElements() && isRunning() ;)
         {
            final Message m = (Message) iter.nextElement();
         
            if (m != null)
            {
               if (messageSelector != null)
               {
                  if (!messageSelector.matches(m))
                  {
                     continue ;
                  }
               }
               
               nmessages++;
               notifyMessage(m);
            }
         }
      }
      catch (BrowseInterruptedException ex)
      {
         log.error("browse stopped: " + ex.getMessage());
      }
      finally
      {
         log.debug("nmessages=" + nmessages);
         
         JMSUtils.closeQuietly(browser) ;
      }

      notifyStatus("Read " + nmessages + " message" + TextUtils.plural(nmessages) + " from " + messageStore.getId());

      if (hermes != null)
      {
         hermes.close();
      }
   }
}
