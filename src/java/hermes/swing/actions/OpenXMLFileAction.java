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
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.impl.FileRepository;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

/**
 * Open and browse a file in jms2xml format.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: OpenXMLFileAction.java,v 1.7 2007/01/14 15:32:23 colincrist Exp $
 */

public class OpenXMLFileAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -5218866220981487463L;

public OpenXMLFileAction()
   {
      putValue(Action.NAME, "Open Messages...");
      putValue(Action.SHORT_DESCRIPTION, "Open an XML messages file");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.file.xml.open"));
      putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false)) ; 
      
      setEnabled(false);

      enableOnBrowserTreeSelection(new Class[] { DestinationConfigTreeNode.class, HermesTreeNode.class } , this, true) ;    

   }

   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
         JFileChooser chooser = null;
         HermesTreeNode hermesNode = null;

         if (HermesBrowser.getBrowser().getBrowserTree().getSelectionPath().getLastPathComponent() instanceof HermesTreeNode)
         {
            hermesNode = (HermesTreeNode) HermesBrowser.getBrowser().getBrowserTree().getSelectionPath().getLastPathComponent();
         }
         else if (HermesBrowser.getBrowser().getBrowserTree().getSelectionPath().getLastPathComponent() instanceof DestinationConfigTreeNode)
         {
            hermesNode = (HermesTreeNode) ((DestinationConfigTreeNode) HermesBrowser.getBrowser().getBrowserTree().getSelectionPath().getLastPathComponent())
                  .getHermesTreeNode();

         }
         else
         {
            return;
         }

         if (DirectoryCache.lastUploadDirectory == null)
         {
            chooser = new JFileChooser(System.getProperty("user.dir"));
         }
         else
         {
            chooser = new JFileChooser(DirectoryCache.lastUploadDirectory);
         }
         
         if (chooser.showDialog(HermesBrowser.getBrowser(), "Open") == JFileChooser.APPROVE_OPTION)
         {
            DirectoryCache.lastUploadDirectory = chooser.getSelectedFile().getParentFile();

            HermesBrowser.getBrowser().getActionFactory().createRepositoryBrowseAction(new FileRepository(chooser.getSelectedFile()), hermesNode.getHermes());
         }
         else
         {
            Hermes.ui.getDefaultMessageSink().add("File open cancelled");
         }
      }
      catch (Exception ex)
      {
         HermesBrowser.getBrowser().showErrorDialog("Unable to open: ", ex);
      }

   }
}
