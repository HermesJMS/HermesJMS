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

package hermes.swing.actions;

import hermes.Hermes;
import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.BrowserAction;
import hermes.util.JMSUtils;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.Action;

import org.apache.log4j.Logger;

import com.jidesoft.swing.FolderChooser;

/**
 * Save the selected messages a Text. Only JMS TextMessages are saved.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SaveMessagesAsTextAction.java,v 1.8 2007/01/14 15:32:22 colincrist Exp $
 */

public class SaveMessagesAsTextAction extends BrowseActionListenerAdapter
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -5949023455568660495L;
private static final Logger log = Logger.getLogger(SaveMessagesAsTextAction.class) ;

   public SaveMessagesAsTextAction()
   {
      putValue(Action.NAME, "Save individually as Text...") ;
      putValue(Action.SHORT_DESCRIPTION, "Save selected messages as Text in separate files.") ;
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.messages.save.text")) ;
      
      setEnabled(false) ;
   }
   
   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
         if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof BrowserAction)
         {
            final BrowserAction action = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();

            if (action.getSelectedMessages().size() > 0)
            {
               FolderChooser chooser;

               if (DirectoryCache.lastSaveAsDirectory == null)
               {
                  chooser = new FolderChooser(new File(HermesBrowser.getBrowser().getRepositoryManager().getDirectory()));
               }
               else
               {
                  chooser = new FolderChooser(DirectoryCache.lastSaveAsDirectory);
               }


               if (chooser.showDialog(HermesBrowser.getBrowser(), "Select directory for messages") == FolderChooser.APPROVE_OPTION)
               {
                  DirectoryCache.lastSaveAsDirectory = chooser.getSelectedFile();

                  for (final Message message : action.getSelectedMessages())
                  {
                     if (message instanceof TextMessage)
                     {
                        String filename = JMSUtils.getFilenameFromMessageID(message.getJMSMessageID()) ;
                        filename = DirectoryCache.lastSaveAsDirectory.getAbsolutePath() + File.separator + filename + ".txt";

                        final TextMessage textMessage = (TextMessage) message;
                        final File file = new File(filename);
                        final FileWriter writer = new FileWriter(file);

                        writer.write(textMessage.getText());
                        writer.flush();
                        writer.close();

                        log.debug("written TextMessage payload to " + file.getAbsolutePath());
                     }
                     else
                     {
                        throw new HermesException("Can only write TextMessages as text files, other messages must be written as XML");
                     }
                  }

                  if (action.getSelectedMessages().size() == 1)
                  {
                     Hermes.ui.getDefaultMessageSink().add("Message saved");
                  }
                  else
                  {
                     Hermes.ui.getDefaultMessageSink().add(action.getSelectedMessages() + " messages saved");
                  }
               }
               else
               {
                  Hermes.ui.getDefaultMessageSink().add("Save messages cancelled");
               }
            }
         }
      }
      catch (Exception e)
      {
         HermesBrowser.getBrowser().showErrorDialog("Unable to save: ", e);
      }
   }

   
}
