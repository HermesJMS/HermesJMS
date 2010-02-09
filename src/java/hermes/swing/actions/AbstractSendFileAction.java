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
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.tasks.SendMessageTask;

import javax.swing.JFileChooser;

/**
 * Helper for actions that send files to queues or topics.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: AbstractSendFileAction.java,v 1.2 2005/05/24 12:58:36
 *          colincrist Exp $
 */

public abstract class AbstractSendFileAction extends ActionSupport
{
   public AbstractSendFileAction()
   {
      setEnabled(false);

      if (!HermesBrowser.getBrowser().isRestricted())
      {
         enableOnBrowserTreeSelection(new Class[] { DestinationConfigTreeNode.class }, this, true);
      }
   }

   public void doSendAFile(int isXML, boolean preserveDestination)
   {
      final DestinationConfigTreeNode destinationNode = (DestinationConfigTreeNode) HermesBrowser.getBrowser().getBrowserTree().getLastSelectedPathComponent();
      final HermesTreeNode hermesNode = (HermesTreeNode) destinationNode.getHermesTreeNode();
      final String text = ((isXML == SendMessageTask.IS_XML) ? "Send XML to " : "Send file to ") + destinationNode.getDestinationName();

      JFileChooser chooser = null;

      if (DirectoryCache.lastUploadDirectory == null)
      {
         chooser = new JFileChooser(System.getProperty("user.dir"));
      }
      else
      {
         chooser = new JFileChooser(DirectoryCache.lastUploadDirectory);
      }

      if (chooser.showDialog(HermesBrowser.getBrowser(), text) == JFileChooser.APPROVE_OPTION)
      {
         DirectoryCache.lastUploadDirectory = chooser.getSelectedFile().getParentFile();
         HermesBrowser.getBrowser().getActionFactory().createSimpleSendMessageAction(hermesNode.getHermes(), destinationNode.getDestinationName(), destinationNode.getDomain(),
               chooser.getSelectedFile(), isXML, preserveDestination);
      }
      else
      {
         Hermes.ui.getDefaultMessageSink().add("File upload cancelled");
      }
   }
}
