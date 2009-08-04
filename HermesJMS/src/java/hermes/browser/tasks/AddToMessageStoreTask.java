/* 
 * Copyright 2003,2004,2005 Colin Crist
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

import hermes.Hermes;
import hermes.MessageFactory;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.impl.DefaultXMLHelper;
import hermes.impl.XMLHelper;
import hermes.store.MessageStore;
import hermes.util.TextUtils;

import java.util.Collection;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: AddToMessageStoreTask.java,v 1.2 2005/07/16 12:57:51 colincrist
 *          Exp $
 */

public class AddToMessageStoreTask extends TaskSupport
{
   private static final Logger log = Logger.getLogger(AddToMessageStoreTask.class) ;
   private static XMLHelper xmlHelper = new DefaultXMLHelper();
   private MessageStore store;
   private Collection<Message> messages;

   public AddToMessageStoreTask(MessageStore store, Collection<Message> messages)
   {
      super(IconCache.getIcon("hermes.record"));
      this.store = store;
      this.messages = messages;
   }

   public AddToMessageStoreTask(MessageFactory factory, MessageStore store, String messagesAsXML) throws JMSException
   {
      this(store, xmlHelper.fromXML(factory, messagesAsXML));
   }

   public String getTitle()
   {
      return "Writing " + messages.size() + " message" + TextUtils.plural(messages.size()) + " to " + store.getId();
   }

   public void invoke()
   {
      try
      {
         int numWritten = 0;

         ProgressMonitor monitor = new ProgressMonitor(HermesBrowser.getBrowser(), "Writing " + messages.size()
               + ((messages.size() == 1) ? " message" : " messages") + " to " + store.getId(), "Connecting...", 0, messages.size() + 1);

         monitor.setMillisToDecideToPopup(50);
         monitor.setMillisToPopup(50);

         for (Message message : messages)
         {
            store.store(message);

            monitor.setNote(++numWritten + " messages written.");
            monitor.setProgress(numWritten);

            if (monitor.isCanceled() || !isRunning())
            {
               break;
            }
         }

         if (monitor.isCanceled() || !isRunning())
         {
            Hermes.ui.getDefaultMessageSink().add("Storing messages in " + store.getId() + " cancelled");
            store.rollback();
            monitor.close() ;
         }
         else
         {
            monitor.setNote("Checkpointing...");
            monitor.setProgress(++numWritten);

            log.debug("Checkpointing...") ;
            
            store.checkpoint();
            monitor.close() ;

            Hermes.ui.getDefaultMessageSink().add(messages.size() + " message" + TextUtils.plural(messages.size()) + " stored in " + store.getId());
         }

      }
      catch (Throwable e)
      {
         HermesBrowser.getBrowser().showErrorDialog(e);
      }
   }

}
