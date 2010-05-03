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

package hermes.browser.components;

import hermes.Hermes;
import hermes.HermesException;
import hermes.HermesWatchListener;
import hermes.HermesWatchManager;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.model.QueueWatchTableModel;
import hermes.browser.model.WatchInfo;
import hermes.config.DestinationConfig;
import hermes.config.WatchConfig;
import hermes.impl.DestinationConfigKeyWrapper;
import hermes.swing.SwingRunner;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.event.DockableFrameAdapter;
import com.jidesoft.docking.event.DockableFrameEvent;
import com.jidesoft.swing.JidePopupMenu;
import com.jidesoft.swing.JideScrollPane;

/**
 * A sortable table that shows interesting statistics about a queue/topics.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: WatchDockableFrame.java,v 1.9 2005/08/07 09:02:51 colincrist
 *          Exp $
 */

public class WatchDockableFrame extends DockableFrame implements HermesWatchListener
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -2892316512453404493L;
private static final Timer flashingTimer = new Timer();
   private static final Logger log = Logger.getLogger(WatchDockableFrame.class);
   private static final long DEFAULT_AGE_ALERT = 0;
   private static final int DEFAULT_DEPTH_ALERT = 0;

   private QueueWatchTableModel model;
   private WatchConfig config;
   private JPopupMenu popupMenu;
   private JMenuItem stopItem;
   private JMenuItem stopAllItem;
   private JMenuItem saveItem;
   private JMenuItem browseItem;
   private JMenuItem updateNow;
   private JMenuItem truncateItem;
   private JMenuItem expandAll;
   private JMenuItem collapseAll;
   private MouseAdapter mouseListener;
   private WatchTable table;
   private JideScrollPane tableScrollPane = new JideScrollPane();
   private boolean keepRunning = true;
   private boolean renaming = false;
   private TimerTask flashingTimerTask;
   private HermesWatchManager watchManager = new HermesWatchManager();

   public WatchDockableFrame(WatchConfig config)
   {
      super(config.getId(), IconCache.getIcon("hermes.watch"));

      this.config = config;

      setAvailableButtons(DockableFrame.BUTTON_CLOSE | DockableFrame.BUTTON_MAXIMIZE | DockableFrame.BUTTON_AUTOHIDE | DockableFrame.BUTTON_FLOATING);
      getContext().setInitMode(DockContext.STATE_FRAMEDOCKED);
      getContext().setInitSide(DockContext.DOCK_SIDE_EAST);
      
      flashingTimerTask = new TimerTask()
      {
         public void run()
         {
            maybeDoFlash();
         }
      };

      flashingTimer.schedule(flashingTimerTask, 1000, 1000);

      init();
   }

   public void close()
   {
      watchManager.close();
      model.removeAll();
      flashingTimerTask.cancel();
   }

   public void updateNow()
   {
      watchManager.updateNow();
   }

   /**
    * Called to update the configuration into the model.
    */
   public void updateConfig()
   {
      if (config.getUpdateFrequency() < 1000)
      {
         config.setUpdateFrequency(1000L);

         Hermes.ui.getDefaultMessageSink().add("Minimum watch frequency is 1000ms");
      }

      config.getDestination().clear();
      config.getDestination().addAll(model.getDestinationWatchConfigs());
   }

   /**
    * Add a watch on all the destinations configured on a Hermes.
    * 
    * @param hermes
    */
   public void addWatch(final Hermes hermes)
   {
      for (Iterator iter = hermes.getDestinations(); iter.hasNext();)
      {
         DestinationConfig dConfig = (DestinationConfig) iter.next();

         addWatch(hermes.getId(), dConfig);
      }
   }

   public void removeWatch(String hermesId, String destinationName)
   {
      final WatchInfo info = model.findWatchInfo(hermesId, destinationName);

      if (info != null)
      {
         log.debug("removing hermesId=" + hermesId + " destinationName=" + destinationName);

         if (!SwingUtilities.isEventDispatchThread())
         {
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  model.removeRow(info);
               }
            });
         }
         else
         {
            model.removeRow(info);
         }
      }
      else
      {
         log.error("cannot remove hermesId=" + hermesId + " destinationName=" + destinationName);
      }
   }

   /**
    * Add a watch for the hermes/destination, alerting after depthAlert messages
    * are on the queue/topic or the top message is older than ageAlert ms.
    * 
    * @param hermes
    * @param destinationName
    * @param depthAlert
    * @param ageAlert
    */
   private void addWatch(String hermesId, DestinationConfig dConfig, int depthAlert, long ageAlert)
   {
      try
      {
         final WatchInfo info = new WatchInfo(hermesId, dConfig);
         final Hermes hermes = (Hermes) HermesBrowser.getBrowser().getContext().lookup(hermesId);

         info.setAgeAlert(ageAlert);
         info.setDepthAlert(depthAlert);

         watchManager.addWatch(hermes, dConfig, this);

         SwingRunner.invokeLater(new Runnable()
         {
            public void run()
            {
               model.addRow(info);
            }
         });

      }
      catch (Exception ex)
      {
         log.error("in AddWatch(): " + ex.getMessage(), ex);
      }
   }

   public void addWatch(String hermesId, DestinationConfig dConfig)
   {
      addWatch(hermesId, dConfig, HermesWatchManager.DEFAULT_DEPTH_ALERT, HermesWatchManager.DEFAULT_AGE_ALERT);
   }

   private void maybeDoFlash()
   {

      Runnable run = null;

      if (model != null && !model.hasAlert())
      {
         run = new Runnable()
         {
            public void run()
            {
               if (config != null)
               {
                  getDockingManager().denotifyFrame(config.getId());
               }
            }
         };
      }
      else
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               getDockingManager().notifyFrame(config.getId());
            }
         });
      }

      if (run != null)
      {
         SwingUtilities.invokeLater(run);
      }
   }

   /**
    * Initialize.
    */
   private void init()
   {
      model = new QueueWatchTableModel();

      model.addColumn("Session");
      model.addColumn("Destination");
      model.addColumn("Durable") ;
      model.addColumn("Depth");
      model.addColumn("Oldest");
      
      model.addTableModelListener(new TableModelListener()
      {
         public void tableChanged(TableModelEvent e)
         {
            table.resort();
         }
      });

      table = new WatchTable(model, config.isShowAge());
      table.setComponentFactory(model);
      table.setSortable(true);
      table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

      mouseListener = new MouseAdapter()
      {
         public void mousePressed(MouseEvent e)
         {
            if (SwingUtilities.isRightMouseButton(e))
            {
               //
               // Make a right-click also change the selection path

               maybeDoPopup(e);
            }
         }

         public void mouseClicked(MouseEvent e)
         {
            maybeDoBrowse(e);
         }
      };

      getPopupMenu();

      table.addMouseListener(mouseListener);

      tableScrollPane.setViewportView(table);
      tableScrollPane.addMouseListener(mouseListener);

      //
      // Its a list of session/destination pairs.

      for (Iterator iter = config.getDestination().iterator(); iter.hasNext();)
      {
         try
         {
            final DestinationConfig dConfig = (DestinationConfig) iter.next();

            if (dConfig.getMyHermes() != null)
            {
               addWatch(dConfig.getMyHermes(), dConfig, config.getDefaultDepthAlertThreshold(), config.getDefaultAgeAlertThreshold());
            }
            else
            {
               log.error("Discarded watch with a null Hermes") ;
            }
         }
         catch (Exception e1)
         {
            log.error("could not start watch: " + e1.getMessage(), e1);
         }
      }

      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(tableScrollPane);

      addDockableFrameListener(new DockableFrameAdapter()
      {
         public void dockableFrameShown(DockableFrameEvent arg0)
         {
            log.debug("frame " + config.getId() + " visible, updating");
            watchManager.updateNow();
         }
      });

   }

   public boolean doBrowse()
   {
      try
      {
         WatchInfo info = model.getRow(table.getActualRowAt(table.getSelectedRows()[0]));

         if (info != null)
         {
            if (info.getE() == null)
            {
               Hermes hermes = null;

               try
               {
                  hermes = (Hermes) HermesBrowser.getBrowser().getContext().lookup(info.getHermesId());
               }
               catch (NamingException e)
               {
                  log.error(e.getMessage(), e);

                  HermesBrowser.getBrowser().showErrorDialog("Cannot browse: ", e);
               }

               HermesBrowser.getBrowser().getActionFactory().createQueueBrowseAction(hermes, info.getConfig());
               return true;
            }
            else
            {
               JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), "Cannot watch: " + info.getE().getClass().getName() + "\n" + info.getE().getMessage(),
                     "Error", JOptionPane.ERROR_MESSAGE);
               return false;
            }
         }
      }
      catch (JMSException e1)
      {
         JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), "Cannot browse: " + e1.getClass().getName() + "\n" + e1.getMessage(), "Error",
               JOptionPane.ERROR_MESSAGE);
         log.error(e1.getMessage(), e1);
      }

      return false;
   }

   /**
    * See if someone has double-clicked a row in the browser. If possible browse
    * that queue/topic but if there is an exception cached there than show it.
    * 
    * @param e
    * @return
    */
   private boolean maybeDoBrowse(MouseEvent e)
   {
      if (e.getClickCount() == 2)
      {
         if (table.getSelectedRows().length == 1)
         {
            return doBrowse();
         }
      }

      return false;
   }

   /**
    * See if we want to show a popup of options.
    * 
    * @param e
    * @return
    */
   private boolean maybeDoPopup(MouseEvent e)
   {
      if (model.getRowCount() > 0)
      {
         if (table.getSelectedRows().length > 0)
         {
            stopItem.setEnabled(true);
            stopAllItem.setEnabled(true);
            browseItem.setEnabled(true);
            truncateItem.setEnabled(true);
         }
         else
         {
            stopItem.setEnabled(false);
            stopAllItem.setEnabled(false);
            browseItem.setEnabled(false);
            truncateItem.setEnabled(false);
         }

         getPopupMenu().show(e.getComponent(), e.getX(), e.getY());

         return true;
      }

      return false;
   }

   public void doTruncate()
   {
      if (table.getSelectedRows().length > 0)
      {
         for (int i = 0; i < table.getSelectedRows().length; i++)
         {
            int row = table.getSelectedRows()[i];
            WatchInfo info = (WatchInfo) model.getRow(table.getActualRowAt(row));

            try
            {
               Hermes hermes = (Hermes) HermesBrowser.getBrowser().getContext().lookup(info.getHermesId());

               HermesBrowser.getBrowser().getActionFactory().createTruncateAction(hermes, info.getConfig());
            }
            catch (Throwable t)
            {
               log.error(t.getMessage(), t);

               HermesBrowser.getBrowser().showErrorDialog("Truncating " + info.getConfig().getName(), t);
            }
         }
      }
   }

   public void doStop()
   {
      int[] rows = table.getSelectedRows();

      for (int i = 0; i < rows.length; i++)
      {
         rows[i] = table.getActualRowAt(rows[i]);
      }

      for (int i = 0; i < rows.length; i++)
      {
         final WatchInfo wInfo = model.getRow(rows[i]);

         model.removeRow(rows[i]);

         try
         {
            final Hermes hermes = (Hermes) HermesBrowser.getBrowser().getContext().lookup(wInfo.getHermesId());

            log.debug("removing watch entry hermes=" + hermes.getId() + ", destination=" + wInfo.getConfig().getName());

            watchManager.removeWatch(hermes, wInfo.getConfig().getName(), this);
         }
         catch (Exception e)
         {
            log.error(e.getMessage(), e);
         }
      }
   }

   public void doStopAll()
   {
      model.removeAll();

      watchManager.clear();
   }

   public void doSave()
   {
      if (keepRunning)
      {
         try
         {
            updateConfig();

            HermesBrowser.getBrowser().saveConfig();

            Hermes.ui.getDefaultMessageSink().add("Watch config " + config.getId() + " saved.");
         }
         catch (HermesException e)
         {
            log.error(e.getMessage(), e);

            JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), "Cannot save configuration: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
         }
      }
      else
      {
         log.error("cannot save, keepRunning=false");
      }
   }

   public JPopupMenu getPopupMenu()
   {
      if (popupMenu == null)
      {
         popupMenu = new JidePopupMenu();
         stopItem = new JMenuItem("Remove");
         stopAllItem = new JMenuItem("Remove all");
         browseItem = new JMenuItem("Browse", IconCache.getIcon("hermes.browse"));
         truncateItem = new JMenuItem("Truncate", IconCache.getIcon("hermes.queue.truncate"));
         saveItem = new JMenuItem("Save", IconCache.getIcon("hermes.save"));
         updateNow = new JMenuItem("Update", IconCache.getIcon("hermes.update"));
         expandAll = new JMenuItem("Expand all", IconCache.getIcon("hermes.expand.all"));
         collapseAll = new JMenuItem("Collapse All", IconCache.getIcon("hermes.collapse.all"));

         popupMenu.add(stopItem);
         popupMenu.add(stopAllItem);
         popupMenu.add(saveItem);
         popupMenu.addSeparator();
         popupMenu.add(browseItem);
         popupMenu.add(updateNow);
         popupMenu.add(truncateItem);
         popupMenu.add(expandAll);
         popupMenu.add(collapseAll);

         truncateItem.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent arg0)
            {
               doTruncate();
            }
         });

         updateNow.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               watchManager.updateNow();
            }
         });

         stopItem.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               doStop();
            }
         });

         stopAllItem.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               doStopAll();
            }
         });

         saveItem.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               doSave();
            }
         });

         browseItem.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               doBrowse();
            }
         });

         collapseAll.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent arg0)
            {
               table.collapseAllRows();
            }
         });

         expandAll.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent arg0)
            {
               table.expandAllRows();
            }
         });

      }

      return popupMenu;
   }

   /**
    * @return Returns the mouseListener.
    */
   public MouseAdapter getMouseListener()
   {
      return mouseListener;
   }

   /**
    * @return Returns the config.
    */
   public WatchConfig getConfig()
   {
      return config;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.jidesoft.document.DocumentComponent#getDisplayTitle()
    */
   public String getHighlighedTitle()
   {
      return "<html><b><font color=\"red\">" + config.getId() + "</font></b></html>";
   }

   public String getPlainTitle()
   {
      return config.getId();
   }

   private WatchInfo getWatchInfo(Hermes hermes, DestinationConfig dConfig) throws JMSException
   {
      return model.getRowByKey(new DestinationConfigKeyWrapper(dConfig));
   }

   public void onDepthChange(Hermes hermes, DestinationConfig dConfig, long depth)
   {
      try
      {
         WatchInfo info = getWatchInfo(hermes, dConfig);

         info.setDepth((int) depth);

      }
      catch (JMSException e)
      {
         log.error(e.getMessage(), e);
      }
   }

   public void onException(Hermes hermes, DestinationConfig dConfig, Exception e)
   {
      try
      {
         WatchInfo info = getWatchInfo(hermes, dConfig);

         info.setE(e);

         setTabTitle(getTitle());
         repaint();

      }
      catch (JMSException ex)
      {
         log.error(ex.getMessage(), ex);
      }

   }

   public void onOldestMessageChange(Hermes hermes, DestinationConfig dConfig, Date oldest)
   {
      try
      {
         WatchInfo info = getWatchInfo(hermes, dConfig);

         if (oldest != null)
         {
            info.setOldest(oldest.getTime());
         }
         else
         {
            info.setOldest(0);
         }

      }
      catch (JMSException ex)
      {
         log.error(ex.getMessage(), ex);
      }
   }

   public void onPropertyChange(Hermes hermes, DestinationConfig dConfig, Map properties)
   {
      try
      {
         WatchInfo info = getWatchInfo(hermes, dConfig);

         info.setStatistics(properties);

         repaint();

      }
      catch (JMSException ex)
      {
         log.error(ex.getMessage(), ex);
      }
   }

}