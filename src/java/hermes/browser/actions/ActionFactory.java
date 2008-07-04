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

package hermes.browser.actions;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesRepository;
import hermes.browser.HermesBrowser;
import hermes.browser.model.BrowserTreeModel;
import hermes.browser.model.QueueWatchTableModel;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.tasks.CopyOrMoveMessagesTask;
import hermes.browser.tasks.DeleteMessagesFromStoreTask;
import hermes.browser.tasks.DestinationWatchAction;
import hermes.browser.tasks.DiscoverDestinationsTask;
import hermes.browser.tasks.SendMessageTask;
import hermes.browser.tasks.TruncateQueueTask;
import hermes.config.DestinationConfig;
import hermes.config.NamingConfig;
import hermes.store.MessageStore;

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * Factory for all things action related.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ActionFactory.java,v 1.15 2005/09/01 20:42:27 colincrist Exp $
 */
public class ActionFactory
{
   private HermesBrowser hermesBrowser;

   public ActionFactory(HermesBrowser hermesBrowser)
   {
      this.hermesBrowser = hermesBrowser;
   }

   public DestinationWatchAction createDestinationWatchAction(String id, JComponent forRepaint, QueueWatchTableModel watchModel) throws JMSException
   {
      DestinationWatchAction rval = new DestinationWatchAction(id, forRepaint, watchModel);

      rval.start();

      return rval;
   }

   public BrowseContextAction createBrowseContextAction(NamingConfig namingConfig)
   {
      return new BrowseContextAction(namingConfig);
   }

   public DiscoverDestinationsTask createDiscoverDestinationAction(BrowserTreeModel treeModel, HermesTreeNode hermeNode) throws JMSException
   {
      DiscoverDestinationsTask rval = new DiscoverDestinationsTask(treeModel, hermeNode);

      rval.start();

      return rval;
   }

   public BrowserAction createMessageStoreBrowseAction(MessageStore messageStore, Hermes hermes, Destination destination, String selector) throws JMSException
   {
      BrowserAction rval = new MessageStoreBrowserAction(hermes, messageStore, destination, selector);
      rval.init();
      return rval;
   }

   public BrowserAction createMessageStoreBrowseAction(MessageStore messageStore, Hermes hermes, String selector) throws JMSException
   {
      BrowserAction rval = new MessageStoreBrowserAction(hermes, messageStore, null, selector);
      rval.init();
      return rval;
   }

   /**
    * Create action to browse a file
    */
   public BrowserAction createRepositoryBrowseAction(HermesRepository repository, Hermes hermes) throws JMSException
   {
      int maxCachedMessages = hermesBrowser.getMaxMessagesInBrowserPane();

      BrowserAction rval = new RepositoryFileBrowserAction(hermes, repository, maxCachedMessages);

      rval.init();

      return rval;
   }

   public BrowserAction createRepositoryBrowseAction(HermesRepository repository) throws JMSException
   {
      int maxCachedMessages = hermesBrowser.getMaxMessagesInBrowserPane();

      BrowserAction rval = new RepositoryFileBrowserAction(repository, maxCachedMessages);

      rval.init();

      return rval;
   }

   /**
    * Create action to browse a queue/topic
    */
   public BrowserAction createQueueBrowseAction(Hermes hermes, DestinationConfig dConfig) throws javax.jms.JMSException
   {
      int maxCachedMessages = hermesBrowser.getMaxMessagesInBrowserPane();
      BrowserAction rval = new QueueBrowseAction(hermes, dConfig, maxCachedMessages, null);
      rval.init();
      return rval;
   }

   public BrowserAction createRegexQueueBrowseAction(Hermes hermes, DestinationConfig config, String regex) throws javax.jms.JMSException
   {
      int maxCachedMessages = hermesBrowser.getMaxMessagesInBrowserPane();
      BrowserAction rval = null;

      if (config != null)
      {
         rval = new RegexQueueBrowseAction(hermes, config, regex, maxCachedMessages);
      }
      else
      {
         rval = new RegexQueueBrowseAction(hermes, regex, maxCachedMessages);
      }

      rval.init();
      return rval;
   }

   public BrowserAction createStringSeachQueueBrowseAction(Hermes hermes, DestinationConfig config, String regex, boolean seachUserHeader)
         throws JMSException
   {
      int maxCachedMessages = hermesBrowser.getMaxMessagesInBrowserPane();
      BrowserAction rval = null;

      if (config != null)
      {
         rval = new StringSearchQueueBrowseAction(hermes, config, regex, seachUserHeader, maxCachedMessages);
      }
      else
      {
         rval = new StringSearchQueueBrowseAction(hermes, regex, seachUserHeader, maxCachedMessages);
      }

      rval.init();
      return rval;
   }

   /**
    * Create an action to copy a set of messages to a queue/topic
    */
   public CopyOrMoveMessagesTask createMessageCopyAction(Hermes hermes, String d, Domain domain, Collection set)
   {
      CopyOrMoveMessagesTask rval = new CopyOrMoveMessagesTask(hermes, d, domain, set, TransferHandler.COPY);

      rval.start();

      return rval;
   }

   public CopyOrMoveMessagesTask createMessageMoveAction(Hermes hermes, String d, Domain domain, Collection set)
   {
      CopyOrMoveMessagesTask rval = new CopyOrMoveMessagesTask(hermes, d, domain, set, TransferHandler.MOVE);

      rval.start();

      return rval;
   }

   /**
    * Create an action to send the contents of a file to the queue/topic
    */
   public SendMessageTask createSimpleSendMessageAction(Hermes hermes, String d, Domain domain, File file, boolean isXML)
   {
      SendMessageTask rval = new SendMessageTask(hermes, d, domain, file, isXML);

      rval.start();

      return rval;
   }

   public SendMessageTask createSimpleSendMessageAction(Hermes hermes, String d, Domain domain, String xml)
   {
      SendMessageTask rval = new SendMessageTask(hermes, d, domain, xml);

      rval.start();

      return rval;
   }

   public SendMessageTask createSimpleSendMessageAction(Hermes hermes, String d, Domain domain, List files, boolean isXML)
   {
      SendMessageTask rval = new SendMessageTask(hermes, d, domain, files, isXML);

      rval.start();

      return rval;
   }

   /**
    * Create an action to truncate all messages on a queue.
    */
   public TruncateQueueTask createTruncateAction(Hermes hermes, DestinationConfig destination) throws JMSException
   {
      TruncateQueueTask rval = new TruncateQueueTask(hermes, destination, true);

      rval.start();

      return rval;
   }

   public DeleteMessagesFromStoreTask createDeleteFromMessageStoreAction(MessageStore store, Collection<Message> messages, boolean warning) throws JMSException
   {
      DeleteMessagesFromStoreTask rval = new DeleteMessagesFromStoreTask(store, messages, warning);
      rval.start();
      return rval;
   }

   /**
    * Create action to truncate a set of messages from a queue
    */
   public TruncateQueueTask createTruncateAction(Hermes hermes, DestinationConfig destination, Collection messageIds, boolean warning) throws JMSException
   {
      TruncateQueueTask rval = new TruncateQueueTask(hermes, destination, messageIds, warning);

      rval.start();

      return rval;
   }

}