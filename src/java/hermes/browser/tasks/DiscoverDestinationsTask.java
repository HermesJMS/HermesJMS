/* 
 * Copyright 2003,2004 Colin Crist
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
import hermes.browser.model.BrowserTreeModel;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.config.DestinationConfig;
import hermes.swing.SwingRunner;

import java.util.Collection;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * @author colincrist@yaho.com last changed by: $Author: colincrist $
 * @version $Id: DiscoverDestinationsTask,v 1.5 2004/11/02 22:01:40 colincrist
 *          Exp $
 */
public class DiscoverDestinationsTask extends TaskSupport
{
    private static final Logger log = Logger.getLogger(DiscoverDestinationsTask.class);
    private Hermes hermes;
    private HermesTreeNode hermesNode;
    private BrowserTreeModel treeModel;
    private String title ;

    /**
     * @param content
     * @param title
     * @param listener
     */
    public DiscoverDestinationsTask(BrowserTreeModel treeModel, HermesTreeNode hermesNode) throws JMSException
    {
        super(IconCache.getIcon("jms.unknown"));

        this.hermesNode = hermesNode;
        this.treeModel = treeModel;
        this.hermes = hermesNode.getHermes();
        this.title = "Discover from " + hermesNode.getHermes().getId();
    }

    public String getTitle()
    {
       return title ;
    }

    public void invoke() throws Exception
    {
        try
        {

            final Collection destinations = hermes.discoverDestinationConfigs();

            for (Iterator iter = destinations.iterator(); iter.hasNext();)
            {
                DestinationConfig dConfig = (DestinationConfig) iter.next();

                log.info("found name=" + dConfig.getName() + ", domain=" + Domain.getDomain(dConfig.getDomain()) + (dConfig.isDurable() ? " durableName=" + dConfig.getClientID() : ""));
            }

            if ( isRunning())
            {
                final StringBuffer msg = new StringBuffer();

                switch (destinations.size())
                {
                case 0:
                    msg.append("No destinations found.");
                    break;
                case 1:
                    msg.append("One destination found.");
                    break ;

                default:
                    msg.append("Discovered " + Integer.toString(destinations.size())).append(" destinations.");
                }

                if ( destinations.size() == 0)
                {
                    SwingRunner.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), msg.toString(), "Discover.", JOptionPane.OK_OPTION);
                        }
                    });
                }
                else
                {
                    msg.append("\nWould you like to replace the current set of destinations for ").append(hermes.getId()).append(".");

                    
                    SwingRunner.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            if (hermesNode.getChildCount() == 0 || JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), msg.toString(), "Discover.", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                            {
                                try
                                {
                                    HermesBrowser.getBrowser().replaceDestinationConfigs(hermes, destinations);
                                    HermesBrowser.getBrowser().saveConfig();
                                    treeModel.nodeStructureChanged(hermesNode);
                                    HermesBrowser.getBrowser().getBrowserTree().expandPath(new TreePath(hermesNode.getPath())) ;

                                    Hermes.ui.getDefaultMessageSink().add("Destinations updated for " + hermes.getId());
                                }
                                catch (Exception ex)
                                {
                                   
                                    HermesBrowser.getBrowser().showErrorDialog("Adding destinations to tree", ex) ;
                                }
                            }
                            else
                            {
                                Hermes.ui.getDefaultMessageSink().add("Update of destinations for " + hermes.getId() + " cancelled.");
                            }
                        }
                    });
                }
            }
            else
            {
                Hermes.ui.getDefaultMessageSink().add("Discover on " + hermes.getId() + " cancelled");
            }
        }
        finally
        {
            hermes.close();
        }
    }

}