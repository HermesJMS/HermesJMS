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

import hermes.HermesConstants;
import hermes.browser.HermesBrowser;
import hermes.swing.SwingRunner;

import javax.jms.JMSException;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public class HermesBrowserTaskListener implements TaskListener
{
   private static final Logger log = Logger.getLogger(HermesBrowserTaskListener.class);
   private HermesBrowser hermesBrowser;

   public HermesBrowserTaskListener(HermesBrowser hermesBrowser)
   {
      this.hermesBrowser = hermesBrowser;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.TaskListener#onStarted(hermes.browser.tasks.Task)
    */
   public void onStarted(Task task)
   {
      // NOP
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.TaskListener#onStopped(hermes.browser.tasks.Task)
    */
   public void onStopped(Task task)
   {
      // NOP
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.TaskListener#onStatus(hermes.browser.tasks.Task,
    *      java.lang.String)
    */
   public void onStatus(Task task, String status)
   {
      if (task.getTitle() != null)
      {
         hermesBrowser.getDefaultMessageSink().add(task.getTitle() + ": " + status);
      }
      else
      {
         hermesBrowser.getDefaultMessageSink().add(status);
      }

   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.TaskListener#onThrowable(hermes.browser.tasks.Task,
    *      java.lang.Throwable)
    */
   public void onThrowable(Task task, Throwable t)
   {
      log.error(t.getMessage(), t);

      if (task.getTitle() != null)
      {
         hermesBrowser.getDefaultMessageSink().add(task.getTitle() + ": " + t.getMessage());
      }
      else
      {
         hermesBrowser.getDefaultMessageSink().add(t.getMessage());
      }

      final StringBuffer stringBuffer = new StringBuffer();

      stringBuffer.append(t.getClass().getName()).append(HermesConstants.CR);

      if (t.getMessage() == null)
      {
         stringBuffer.append("Provider gave no error message, check the logfile for errors.").append(HermesConstants.CR);
      }
      else
      {
         stringBuffer.append(t.getMessage()).append(HermesConstants.CR);
      }

      Exception linked = null ;
      
      if (t instanceof JMSException)
      {
         JMSException e = (JMSException) t;

         if (e.getLinkedException() != null)
         {
            if (e.getLinkedException().getMessage() == null)
            {
               // NOP
            }
            else if (e.getLinkedException().getMessage().equals(e.getMessage()))
            {
               // Dupe message.
            }
            else
            {
               stringBuffer.append(e.getLinkedException().getMessage()).append(HermesConstants.CR);
            }
            
            linked = e.getLinkedException() ;
         }
      }

      HermesBrowser.getBrowser().showErrorDialog(stringBuffer.toString(), linked == null ? t : linked) ;

   }

}
