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
import hermes.browser.dialog.EditNamingConfigDialog;
import hermes.browser.dialog.PreferencesDialog;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.model.tree.MessageStoreURLTreeNode;
import hermes.browser.model.tree.NamingConfigTreeNode;
import hermes.browser.tasks.EditDestinationPropertiesTask;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.jidesoft.swing.JideSwingUtilities;

/**
 * Send jms2xml.xsd format file to currently selected queue or topic.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: EditObjectAction.java,v 1.9 2005/12/14 08:11:24 colincrist Exp $
 */

public class EditObjectAction extends ActionSupport
{
   private static final Logger log = Logger.getLogger(EditObjectAction.class);

   public EditObjectAction()
   {
      putValue(Action.NAME, "Edit...");
      putValue(Action.SHORT_DESCRIPTION, "Edit queue, topic, session or context.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("toolbarButtonGraphics/general/Edit16.gif"));

      setEnabled(false);

      if (!HermesBrowser.getBrowser().isRestricted())
      {
         enableOnBrowserTreeSelection(new Class[] { MessageStoreURLTreeNode.class, DestinationConfigTreeNode.class, HermesTreeNode.class, NamingConfigTreeNode.class } , this, false) ;
      }
   }

   public void actionPerformed(ActionEvent arg0)
   {
      try
      {
         final TreePath treePath = HermesBrowser.getBrowser().getBrowserTree().getSelectionModel().getSelectionPath();

         if (treePath != null)
         {
            Object component = treePath.getLastPathComponent();

            if (component instanceof DestinationConfigTreeNode)
            {
               final DestinationConfigTreeNode node = (DestinationConfigTreeNode) component;
               final HermesTreeNode hermesNode = (HermesTreeNode) node.getHermesTreeNode();

               Hermes.ui.getThreadPool().invokeLater(new EditDestinationPropertiesTask(hermesNode.getHermes(), node.getConfig()));
            }
            else if (component instanceof HermesTreeNode)
            {
               final HermesTreeNode hermesNode = (HermesTreeNode) component;
               final PreferencesDialog dialog = new PreferencesDialog(HermesBrowser.getBrowser());

               dialog.init();
               dialog.refocus(hermesNode.getHermes().getId());
               JideSwingUtilities.centerWindow(dialog);
               dialog.show();
            }
            else if (component instanceof NamingConfigTreeNode)
            {
               final NamingConfigTreeNode namingNode = (NamingConfigTreeNode) component;
               final EditNamingConfigDialog dialog = new EditNamingConfigDialog(HermesBrowser.getBrowser(), namingNode.getId(), HermesBrowser.getBrowser()
                     .getConfig().getNaming());

               dialog.pack();
               JideSwingUtilities.centerWindow(dialog);
               dialog.show();
            }
            else if (component instanceof MessageStoreURLTreeNode)
            {
               MessageStoreURLTreeNode node = (MessageStoreURLTreeNode) component ;
               
               if (node.isConfigurable())
               {
                  ActionRegistry.getAction(CreateNewJDBCAction.class).actionPerformed(new ActionEvent(node.getConfig(), 0, null)) ;
               }
               else
               {
                  HermesBrowser.getBrowser().showErrorDialog("You cannot edit the default message store") ;
               }
            }
         }
      }
      catch (Exception ex)
      {
         HermesBrowser.getBrowser().showErrorDialog("Cannot edit: ", ex);
      }
   }
}
