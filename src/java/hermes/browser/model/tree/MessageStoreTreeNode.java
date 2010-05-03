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

package hermes.browser.model.tree;

import hermes.browser.IconCache;
import hermes.browser.model.BrowserTreeModel;
import hermes.store.MessageStore;
import hermes.store.MessageStoreListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.swing.SwingUtilities;
import javax.swing.tree.MutableTreeNode;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageStoreTreeNode.java,v 1.2 2005/07/15 15:11:01 colincrist
 *          Exp $
 */

public class MessageStoreTreeNode extends AbstractTreeNode implements MessageStoreListener
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -2283132470316933038L;
private static final Logger log = Logger.getLogger(MessageStoreTreeNode.class);
   private static final Timer timer = new Timer();
   private TimerTask timerTask;
   private BrowserTreeModel model;
   private Map<String, MessageStoreTopicTreeNode> topics = new HashMap<String, MessageStoreTopicTreeNode>();
   private Map<String, MessageStoreQueueTreeNode> queues = new HashMap<String, MessageStoreQueueTreeNode>();

   public MessageStoreTreeNode(BrowserTreeModel model, String id, MessageStore messageStore)
   {
      super(id, messageStore);

      this.model = model;

      setIcon(IconCache.getIcon("hermes.store"));

      messageStore.addMessageListener(this);

      timerTask = new TimerTask()
      {
         @Override
         public void run()
         {
            updateDestinations();
         }
      };

      timer.schedule(timerTask, 0);
   }

   @Override
   public void remove(MutableTreeNode aChild)
   {
      // TODO Auto-generated method stub
      super.remove(aChild);
   }

   public MessageStore getMessageStore()
   {
      return (MessageStore) getBean();
   }

   public void close()
   {
      if (timerTask != null)
      {
         timerTask.cancel();
      }

      getMessageStore().removeMessageListener(this);
   }

   private void addQueue(Queue queue) throws JMSException
   {
      if (!queues.containsKey(queue.getQueueName()))
      {
         final MessageStoreQueueTreeNode node = new MessageStoreQueueTreeNode(getMessageStore(), queue.getQueueName(), queue);

         queues.put(queue.getQueueName(), node);

         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               add(node);
               model.nodeStructureChanged(MessageStoreTreeNode.this);
            }
         });

      }
   }

   private void addTopic(Topic topic) throws JMSException
   {
      if (!topics.containsKey(topic.getTopicName()))
      {
         final MessageStoreTopicTreeNode node = new MessageStoreTopicTreeNode(getMessageStore(), topic.getTopicName(), topic);

         topics.put(topic.getTopicName(), node);

         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               add(node);
               model.nodesWereInserted(MessageStoreTreeNode.this, new int[] { getChildCount() - 1 } );
            }
         });
      }
   }

   private void removeQueue(String queueName) throws JMSException
   {
      final MessageStoreQueueTreeNode node = queues.get(queueName);
      final int oldIndex = getIndex(node) ;
      queues.remove(queueName);

      if (node != null)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               remove(node);
               model.nodesWereRemoved(MessageStoreTreeNode.this, new int[] { oldIndex }, new Object[] { node } );
            }
         });
      }
   }

   private void removeTopic(String topicName) throws JMSException
   {
      final MessageStoreTopicTreeNode node = topics.get(topicName);
      final int oldIndex = getIndex(node) ;
      
      topics.remove(topicName);

      if (node != null)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               remove(node);
               model.nodesWereRemoved(MessageStoreTreeNode.this, new int[] { oldIndex }, new Object[] { node } );
            }
         });
      }
   }

   private void updateDestinations()
   {
      try
      {
         final Collection<Destination> destinations = getMessageStore().getDestinations();
         final Set<String> currentQueues = new HashSet<String>();
         final Set<String> currentTopics = new HashSet<String>();

         //
         // Check for new topics and queues...

         for (Destination destination : destinations)
         {
            if (destination instanceof Queue)
            {
               final Queue queue = (Queue) destination;

               currentQueues.add(queue.getQueueName());
               addQueue(queue);

            }
            else
            {
               final Topic topic = (Topic) destination;

               currentTopics.add(topic.getTopicName());
               addTopic(topic);
            }
         }

         //
         // Remove non-existent topics and queues....

         for (final String queueName : queues.keySet())
         {
            if (!currentQueues.contains(queueName))
            {
               removeQueue(queueName);
               break;
            }
         }

         for (final String topicName : topics.keySet())
         {
            if (!currentTopics.contains(topicName))
            {
               removeTopic(topicName);
               break;
            }
         }
      }
      catch (JMSException e)
      {
         log.error(e);
      }

      timerTask = new TimerTask()
      {
         @Override
         public void run()
         {
            // updateDestinations();
         }
      };

      timer.schedule(timerTask, 1000);
   }

   public void onDestination(Destination d)
   {
      try
      {
         if (d instanceof Queue)
         {
            addQueue((Queue) d);
         }
         else
         {
            addTopic((Topic) d);
         }
      }
      catch (JMSException e)
      {
         log.error(e.getMessage(), e);
      }
   }

   public void onDestinationDeleted(Destination d)
   {
      try
      {
         if (d instanceof Queue)
         {
            removeQueue(((Queue) d).getQueueName());
         }
         else
         {
            removeTopic(((Topic) d).getTopicName());
         }
      }
      catch (JMSException e)
      {
         log.error(e.getMessage(), e);
      }
   }

   public void onException(Exception e)
   {
      // TODO Auto-generated method stub
   }

   public final void onMessageDeleted(Message m)
   {
      // NOP
   }

   public final void onMessage(Message arg0)
   {
      // NOP
   }

}
