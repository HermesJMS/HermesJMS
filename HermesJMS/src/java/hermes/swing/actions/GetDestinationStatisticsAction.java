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
import hermes.browser.dialog.MapPropertyDialog;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;

import java.awt.event.ActionEvent;
import java.util.Map;

import javax.jms.JMSException;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import com.jidesoft.swing.JideSwingUtilities;

/**
 * Get properties from JMX or whatever for this queue/topic/subscription.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: GetDestinationStatisticsAction.java,v 1.10 2006/07/11 06:26:40 colincrist Exp $
 */

public class GetDestinationStatisticsAction extends ActionSupport
{
   public GetDestinationStatisticsAction()
   {
      putValue(Action.NAME, "Properties...");
      putValue(Action.SHORT_DESCRIPTION, "Get properties and statistics from provider.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.statistics")) ;

      setEnabled(false);

      enableOnBrowserTreeSelection(new Class[] { DestinationConfigTreeNode.class } , this, true) ;    
   }

   public void actionPerformed(ActionEvent arg0)
   {
      final TreePath curPath = HermesBrowser.getBrowser().getBrowserTree().getSelectionModel().getSelectionPath();

      if ( curPath != null)
      {
          if (curPath.getLastPathComponent() instanceof DestinationConfigTreeNode)
          {
              final DestinationConfigTreeNode dNode = (DestinationConfigTreeNode) curPath.getLastPathComponent();
              final HermesTreeNode hNode = (HermesTreeNode) dNode.getHermesTreeNode();

              Hermes.ui.getDefaultMessageSink().add("Getting statistics for " + dNode.getDestinationName());

              Hermes.ui.getThreadPool().invokeLater(new Runnable()
              {
                  public void run()
                  {
                      Hermes hermes = null;

                      try
                      {
                          hermes = hNode.getHermes();
                          
                          final Map statistics = hermes.getStatistics(dNode.getConfig());

                          SwingUtilities.invokeLater(new Runnable()
                          {
                              public void run()
                              {
                                  MapPropertyDialog dialog = new MapPropertyDialog(HermesBrowser.getBrowser(),
                                          "Properties for " + dNode.getDestinationName(), null, statistics, false);
                                  dialog.pack();
                                  JideSwingUtilities.centerWindow(dialog);
                                  dialog.show();
                              }
                          });
                      }
                      catch (Throwable e)
                      {
                          HermesBrowser.getBrowser().showErrorDialog("Can't get properties for " + dNode.getDestinationName(), e);
                      }
                      finally
                      {
                          if ( hermes != null)
                          {
                              try
                              {
                                  hermes.close();
                              }
                              catch (JMSException e1)
                              {
                                  // NOP
                              }

                              Hermes.ui.getDefaultMessageSink().add("Ready.");
                          }
                      }
                  }
              });
          }
      }
   }
}
