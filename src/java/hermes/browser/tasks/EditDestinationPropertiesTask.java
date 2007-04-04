/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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
import hermes.browser.dialog.DestinationConfigDialog;
import hermes.config.DestinationConfig;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.jidesoft.swing.JideSwingUtilities;

/**
 * @author colincrist@hermesjms.com
 */
public class EditDestinationPropertiesTask extends TaskSupport
{
   private static final Logger log = Logger.getLogger(EditDestinationPropertiesTask.class);
   private Hermes hermes;
   private Destination destination;
   private DestinationConfig config ;
   private Runnable onOK ;

   /**
    * @param icon
    */
   public EditDestinationPropertiesTask(Hermes hermes, DestinationConfig config)
   {
      super(IconCache.getIcon("jms.unknown"));
      this.hermes = hermes;
      this.config = config;
   }
   
   public EditDestinationPropertiesTask(Hermes hermes, DestinationConfig config, Runnable onOK)
   {
      this(hermes, config) ;
      this.onOK = onOK  ;
   }
   

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.Task#invoke()
    */
   public void invoke() throws Exception
   {
      //
      // We may not have a destination name yet if we're adding a new
      // destination manually.

      if (config.getName() != null)
      {
         Hermes.ui.getDefaultMessageSink().add("Getting properties for " + config.getName() + "...");

         try
         {
            destination = hermes.getDestination(config.getName(), Domain.getDomain(config.getDomain()));
         }
         catch (JMSException ex)
         {
            Hermes.ui.getDefaultMessageSink().add("Unable to create a session to get destination bean properties. " + ex.getMessage());
         }
      }

      if (destination != null)
      {
         Hermes.ui.getDefaultMessageSink().add("Getting properties for " + config.getName() + "... done.");

         try
         {
            hermes.close();
         }
         catch (JMSException ex)
         {
            // Swallow it

            log.error(ex.getMessage(), ex);
         }
      }

      SwingUtilities.invokeAndWait(new Runnable()
      {
         public void run()
         {
            final DestinationConfigDialog dialog = new DestinationConfigDialog(HermesBrowser.getBrowser(), hermes.getId(), destination, config);

            dialog.pack();
            JideSwingUtilities.centerWindow(dialog);
            dialog.show();
            
            if (onOK != null)
            {
               onOK.run() ;
            }
         }
      });
   }
}