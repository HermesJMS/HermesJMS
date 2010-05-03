/* 
 * Copyright 2003,2004,2005,2006,2007 Colin Crist
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
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.tasks.BrowseDestinationTask;
import hermes.browser.tasks.MessageTaskListener;
import hermes.browser.tasks.Task;
import hermes.config.DestinationConfig;
import hermes.util.TextUtils;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Message;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.ProgressMonitor;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.apache.log4j.Logger;

/**
 * Save all messages on the selected queue into a file in jms2xml.xsd format.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class SaveAllMessagesAsXMLAction extends BrowseActionListenerAdapter
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 2695865313851844473L;
private static final Logger log = Logger.getLogger(SaveAllMessagesAsXMLAction.class);

   public SaveAllMessagesAsXMLAction()
   {
      putValue(Action.NAME, "Save all as XML...");
      putValue(Action.SHORT_DESCRIPTION, "Save all messages encoded a XML.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.messages.save.xml.many"));
      setEnabled(false);

      HermesBrowser.getBrowser().getBrowserTree().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
      {
         public void valueChanged(TreeSelectionEvent e)
         {
            if (e.getNewLeadSelectionPath() != null)
            {
               setEnabled(e.getNewLeadSelectionPath().getLastPathComponent() instanceof DestinationConfigTreeNode);
            }
            else
            {
               setEnabled(false) ;
            }
         }
      });
   }

   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
         final DestinationConfigTreeNode destinationNode = HermesBrowser.getBrowser().getBrowserTree().getSelectedDestinationNodes().get(0);
         final DestinationConfig destinationConfig = destinationNode.getConfig();
         final Hermes hermes = ((HermesTreeNode) destinationNode.getParent()).getHermes();

         JFileChooser chooser;

         if (DirectoryCache.lastSaveAsDirectory == null)
         {
            chooser = new JFileChooser(new File(HermesBrowser.getBrowser().getRepositoryManager().getDirectory()));
         }
         else
         {
            chooser = new JFileChooser(DirectoryCache.lastSaveAsDirectory);
         }

         if (chooser.showDialog(HermesBrowser.getBrowser(), "Save All Messages...") == JFileChooser.APPROVE_OPTION)
         {
            DirectoryCache.lastSaveAsDirectory = chooser.getCurrentDirectory();

            final File file = chooser.getSelectedFile();
            final List<Message> messages = new ArrayList<Message>();
            final BrowseDestinationTask task = new BrowseDestinationTask(hermes, destinationConfig);
            final ProgressMonitor progress = new ProgressMonitor(HermesBrowser.getBrowser(), "Saving from " + destinationConfig.getName() + " to " + file.getName(), "Browsing...", 0, 3) ;
            progress.setMillisToPopup(0) ;
            progress.setMillisToDecideToPopup(0) ;
            
            task.addTaskListener(new MessageTaskListener()
            {

               public void onMessage(Task task, Message message)
               {
                  if (progress.isCanceled())
                  {
                     task.stop() ;
                  }
                  
                  messages.add(message);
                  progress.setNote(messages.size() + " message" + TextUtils.plural(messages.size()) + " read") ;
               }

               public void onThrowable(Task task, Throwable t)
               {
                  HermesBrowser.getBrowser().showErrorDialog(t);

               }

               public void onStopped(Task task)
               {
                  if (!progress.isCanceled())
                  {
                     doSave();
                  }
                  else
                  {
                     Hermes.ui.getDefaultMessageSink().add("Cancelled") ;
                  }
               }

               public void onStatus(Task task, String status)
               {
                  // NOP

               }

               public void onStarted(Task task)
               {
                  Hermes.ui.getDefaultMessageSink().add("Browsing " + destinationConfig.getName() + "...") ;
                  progress.setProgress(1) ;
               }

               private void doSave()
               {
                  FileOutputStream ostream = null;

                  try
                  {
                     progress.setProgress(2) ;
                     progress.setNote("Saving to " + file.getName()) ;
                     Hermes.ui.getDefaultMessageSink().add("Saving to " + file.getName()) ;
                     ostream = new FileOutputStream(file);
                     hermes.toXML(messages, ostream);
                     ostream.close();
                     Hermes.ui.getDefaultMessageSink().add(
                            messages.size() + " message" + TextUtils.plural(messages.size()) + " saved to " + file.getName());

                  }
                  catch (Exception ex)
                  {
                     HermesBrowser.getBrowser().showErrorDialog(ex);

                  }
                  finally
                  {
                     progress.setProgress(3) ;
                     progress.close() ;
                     
                     if (ostream != null)
                     {
                        try
                        {                       
                           ostream.close();
                        }
                        catch (Exception ex)
                        {
                           log.error(ex.getMessage(), ex);
                        }
                     }
                  }
               }
            });

            task.start();
         }
      }
      catch (Exception e)
      {
         HermesBrowser.getBrowser().showErrorDialog("Unable to save:", e);
      }
   }
}
