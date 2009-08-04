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

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.store.MessageStore;

import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: RecordDestinationTask.java,v 1.4 2005/08/07 09:02:51 colincrist Exp $
 */

public class RecordDestinationTask extends TaskSupport
{
   private Hermes hermes;
   private DestinationConfigTreeNode node;;
   private MessageStore messageStore;

   public RecordDestinationTask(Hermes hermes, DestinationConfigTreeNode node, MessageStore messageStore)
   {
      super(node.getDomain() == Domain.QUEUE ? IconCache.getIcon("hermes.queue.record") : IconCache.getIcon("hermes.topic.record"));

      this.hermes = hermes;
      this.node = node;
      this.messageStore = messageStore;
   }

   @Override
   public String getTitle()
   {
     return "Recording " + node.getDestinationName() + " into " + messageStore.getId() ;
   }

   @Override
   public void invoke() throws Exception
   {
      try
      {
         SwingUtilities.invokeLater(new Runnable()
         {         
            public void run()
            {
               JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), getTitle() + ", click on the Tools tab to see running tasks.", "Running", JOptionPane.INFORMATION_MESSAGE) ;
            }
         }) ;
         
         Hermes.ui.getDefaultMessageSink().add(getTitle()) ;
         
         final Destination d = hermes.getDestination(node.getDestinationName(), node.getDomain());
         final QueueBrowser browser = hermes.createBrowser(node.getConfig());

         for (Enumeration iter = browser.getEnumeration(); iter.hasMoreElements() && isRunning();)
         {
            Message m = (Message) iter.nextElement();

            if (m != null)
            {
               messageStore.store(m);
               messageStore.checkpoint();
            }
         }

         messageStore.checkpoint();
         browser.close();
         hermes.close();
      }
      finally
      {
         Hermes.ui.getDefaultMessageSink().add("Finished recording " + node.getDestinationName() + " into " + messageStore.getId()) ;
         hermes.close();
      }
   }

}
