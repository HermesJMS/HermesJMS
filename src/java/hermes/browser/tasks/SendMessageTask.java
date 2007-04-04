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

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.swing.SwingRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JOptionPane;

import org.apache.log4j.Category;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: SendMessageTask.java,v 1.9 2006/07/13 07:35:31 colincrist Exp $
 */

public class SendMessageTask extends TaskSupport
{
   private static final Category cat = Category.getInstance(SendMessageTask.class);

   private Hermes hermes;
   private String destinationName;
   private String content;
   private List files;
   private Set messageDelimiters = new HashSet();
   private Domain domain;
   private boolean isXML = true;
   private int uploaded = 0;
   private int persistence = HermesBrowser.getBrowser().getSendPersistence() ;

   public SendMessageTask(Hermes hermes, String destinationName, Domain domain, String content)
   {
      super(IconCache.getIcon("hermes.messages.send"));

      this.hermes = hermes;
      this.destinationName = destinationName;
      this.content = content;
      this.domain = domain;
   }

   public SendMessageTask(Hermes hermes, String destinationName, Domain domain, File file, boolean isXML)
   {
      super(IconCache.getIcon("hermes.messages.send"));

      this.hermes = hermes;
      this.destinationName = destinationName;
      this.files = new ArrayList();
      this.isXML = isXML;
      this.domain = domain;
      files.add(file);
   }

   public String getTitle()
   {
      return "Send";
   }

   public SendMessageTask(Hermes hermes, String destinationName, Domain domain, List files, boolean isXML)
   {
      super(IconCache.getIcon("hermes.messages.send"));

      this.hermes = hermes;
      this.destinationName = destinationName;
      this.files = files;
      this.isXML = isXML;
      this.domain = domain ;
   }

   private void doUpload(Destination to, Iterator<Message> messages) throws JMSException
   {
      while (messages.hasNext())
      {
         Message m = (Message) messages.next();

         hermes.send(to, m);
         uploaded++;
      }
   }

   public void invoke() throws Exception
   {
      InputStream istream = null;

      try
      {
         Destination to = hermes.getDestination(destinationName, domain);

         if (files != null)
         {

            //
            // The messages are held in files....

            for (Iterator iter = files.iterator(); iter.hasNext();)
            {
               final File file = (File) iter.next();
               Collection<Message> messages = null;

               istream = new FileInputStream(file);

               if (isXML)
               {
                  messages = hermes.fromXML(istream);
               }
               else
               {
                  BufferedReader reader = new BufferedReader(new InputStreamReader(istream));
                  StringBuffer payload = new StringBuffer();
                  String line;

                  while ((line = reader.readLine()) != null)
                  {
                     payload.append(line).append('\n');
                  }

                  reader.close();

                  messages = new ArrayList<Message>();
                  messages.add(hermes.createTextMessage(payload.toString()));
               }

               doUpload(to, messages.iterator());

               istream.close();
            }
         }
         else if (content != null)
         {
            final Collection<Message> messages = hermes.fromXML(content);

            synchronized (this)
            {
               SwingRunner.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     if (messages.size() > 0)
                     {
                        if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), "Are you sure you wish to send the " + messages.size()
                              + ((messages.size() > 1) ? " messages" : " message") + " from the clipboard to " + destinationName + " ?", "Warning",
                              JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        {
                           stop();

                           notifyStatus("Send to" + destinationName + " cancelled");
                        }

                        synchronized (SendMessageTask.this)
                        {
                           SendMessageTask.this.notify();
                        }
                     }
                     else
                     {
                        JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), "No messages on the clipboard.", "Cannot Send", JOptionPane.ERROR_MESSAGE);
                     }
                  }
               });

               try
               {
                  this.wait();
               }
               catch (InterruptedException ex)
               {
                  // Nah...
               }
            }
            doUpload(to, messages.iterator()) ;
         }

         hermes.commit();

         if (uploaded == 1)
         {
            notifyStatus("One message uploaded.");
         }
         else
         {
            notifyStatus(new String(uploaded + " messages uploaded to " + destinationName));
         }

      }
      finally
      {
         if (istream != null)
         {
            try
            {
               istream.close();
            }
            catch (IOException e)
            {
               cat.error(e.getMessage(), e);
            }
         }

         hermes.close();
      }
   }
}