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
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.BrowserAction;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.Action;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

/**
 * Save the selected messages in jms2xml.xsd format.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SaveMessagesAsXMLAction.java,v 1.4 2007/01/14 15:32:23 colincrist Exp $
 */

public class SaveMessagesAsXMLAction extends BrowseActionListenerAdapter
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 7718784305773773366L;
private static final Logger log = Logger.getLogger(SaveMessagesAsXMLAction.class) ;
   
   public SaveMessagesAsXMLAction()
   {
      putValue(Action.NAME, "Save as XML...") ;
      putValue(Action.SHORT_DESCRIPTION, "Save selected messages encoded a XML.") ;
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.messages.save.xml.many")) ;
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
               JFileChooser chooser;

               if (DirectoryCache.lastSaveAsDirectory == null)
               {
                  chooser = new JFileChooser(new File(HermesBrowser.getBrowser().getRepositoryManager().getDirectory()));
               }
               else
               {
                  chooser = new JFileChooser(DirectoryCache.lastSaveAsDirectory);
               }

               if (chooser.showDialog(HermesBrowser.getBrowser(), "Save Messages...") == JFileChooser.APPROVE_OPTION)
               {
                  DirectoryCache.lastSaveAsDirectory = chooser.getSelectedFile().getParentFile();

                  final FileOutputStream ostream = new FileOutputStream(chooser.getSelectedFile());

                  action.getHermes().toXML(action.getSelectedMessages(), ostream);
                  ostream.close();

                  if (action.getSelectedMessages().size() == 1)
                  {
                     Hermes.ui.getDefaultMessageSink().add("Message saved");
                  }
                  else
                  {
                     Hermes.ui.getDefaultMessageSink().add(new Integer(action.getSelectedMessages().size()) + " messages saved");
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
         HermesBrowser.getBrowser().showErrorDialog("Unable to save:", e) ;
      }

   }

}
