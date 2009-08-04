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
import hermes.config.DestinationConfig;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * Unsubscribe to durable subscriptions.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DurableUnsubscribeAction.java,v 1.5 2005/07/15 15:11:02
 *          colincrist Exp $
 */

public class DurableUnsubscribeAction extends ActionSupport
{
   private static final Logger log = Logger.getLogger(DurableUnsubscribeAction.class);

   public DurableUnsubscribeAction()
   {
      putValue(Action.NAME, "Unsubscribe...");
      putValue(Action.SHORT_DESCRIPTION, "Unsubscribe any durable subscription on this topic");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.unsubscribe"));

      setEnabled(false);

      getBrowserTree().getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
      {
         public void valueChanged(TreeSelectionEvent event)
         {
            if (event.getPath() != null && event.getPath().getLastPathComponent() instanceof DestinationConfigTreeNode)
            {
               final DestinationConfigTreeNode destinationNode = (DestinationConfigTreeNode) event.getPath().getLastPathComponent();

               setEnabled(destinationNode.getConfig().isDurable());
            }
            else
            {
               setEnabled(false);
            }
         }
      });
   }

   public void actionPerformed(ActionEvent e)
   {

      final TreePath curPath = HermesBrowser.getBrowser().getBrowserTree().getSelectionPath();

      if (curPath != null)
      {
         final TreeNode node = (TreeNode) curPath.getLastPathComponent();

         if (node instanceof DestinationConfigTreeNode)
         {
            try
            {
               final DestinationConfigTreeNode tNode = (DestinationConfigTreeNode) node;
               final HermesTreeNode hNode = (HermesTreeNode) tNode.getHermesTreeNode();
               final DestinationConfig dConfig = tNode.getConfig();

               if (dConfig.isDurable())
               {
                  if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), "Would you like to unsubscribe from durable subscription clientID="
                        + dConfig.getClientID() + " on " + tNode.getDestinationName(), "Please confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                  {
                     Hermes.ui.getThreadPool().invokeLater(new Runnable()
                     {
                        public void run()
                        {
                           try
                           {
                              hNode.getHermes().unsubscribe(dConfig.getClientID());
                              Hermes.ui.getDefaultMessageSink().add("Unsubscribed");
                           }
                           catch (Throwable e)
                           {
                              HermesBrowser.getBrowser().showErrorDialog("Unable to unsubscribe", e);
                           }
                           finally
                           {
                              try
                              {
                                 hNode.getHermes().close();
                              }
                              catch (Throwable t)
                              {
                                 log.error(t.getMessage(), t);
                              }
                           }
                        }
                     });
                  }
                  else
                  {
                     Hermes.ui.getDefaultMessageSink().add("Unsubscribe cancelled.");
                  }
               }
               else
               {
                  HermesBrowser.getBrowser().showErrorDialog("Topic " + dConfig.getName() + " is not configured as durable.") ;
               }
            }

            catch (Throwable t)
            {

               HermesBrowser.getBrowser().showErrorDialog("Unable to unsubscribe", t);
            }
         }
      }
   }
}
