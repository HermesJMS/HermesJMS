package hermes.swing.actions;

import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.BrowserAction;
import hermes.util.JMSUtils;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;

import javax.jms.Message;
import javax.swing.Action;

import org.apache.log4j.Logger;

import com.jidesoft.swing.FolderChooser;

/**
 * Save the selected messages in jms2xml.xsd format in separate files.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SaveMessagesIndividuallyAsXMLAction.java,v 1.6 2007/01/14 15:32:22 colincrist Exp $
 */

public class SaveMessagesIndividuallyAsXMLAction extends BrowseActionListenerAdapter
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -6774035003413105583L;
private static final Logger log = Logger.getLogger(SaveMessagesIndividuallyAsXMLAction.class) ;
   
   public SaveMessagesIndividuallyAsXMLAction()
   {
      putValue(Action.NAME, "Save individually as XML...") ;
      putValue(Action.SHORT_DESCRIPTION, "Save selected messages encoded a XML in separate files.") ;
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.messages.save.xml.one")) ;
      
      setEnabled(false) ;
      
     
   }
   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
         if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof BrowserAction)
         {
            BrowserAction action = (BrowserAction) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();

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

               //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

               if (chooser.showDialog(HermesBrowser.getBrowser(), "Select directory for messages") == FolderChooser.APPROVE_OPTION)
               {
                  DirectoryCache.lastSaveAsDirectory = chooser.getSelectedFile();

                  for (final Message m : action.getSelectedMessages())
                  {
                     final String filename = DirectoryCache.lastSaveAsDirectory.getAbsolutePath() + File.separator + JMSUtils.getFilenameFromMessageID(m.getJMSMessageID()) + ".xml";
                     final File file = new File(filename);

                     final FileOutputStream ostream = new FileOutputStream(file);

                     log.debug("saving message as " + filename);

                     action.getHermes().toXML(m, ostream);
                     ostream.close();
                  }

                  if (action.getSelectedMessages().size() == 1)
                  {
                     Hermes.ui.getDefaultMessageSink().add("Message saved");
                  }
                  else
                  {
                     Hermes.ui.getDefaultMessageSink().add(action.getSelectedMessages().size() + " messages saved");
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
