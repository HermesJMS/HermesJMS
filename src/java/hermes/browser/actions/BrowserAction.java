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

package hermes.browser.actions;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.components.HierarchicalMessageHeaderTable;
import hermes.browser.components.MessageHeaderTable;
import hermes.browser.components.MessagePayloadPanel;
import hermes.browser.components.MessagesDeleteable;
import hermes.browser.components.NavigableComponent;
import hermes.browser.components.PopupMenuFactory;
import hermes.browser.model.MessageHeaderTableModel;
import hermes.browser.tasks.MessageTaskListener;
import hermes.browser.tasks.Task;
import hermes.config.DestinationConfig;
import hermes.swing.FilterablePanel;
import hermes.swing.SQL92FilterableTableModel;
import hermes.swing.SwingRunner;
import hermes.swing.SwingUtils;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import com.codestreet.selector.parser.InvalidSelectorException;
import com.jidesoft.document.DocumentComponentEvent;
import com.jidesoft.document.DocumentComponentListener;
import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.swing.JideScrollPane;

/**
 * HermesAction to perform the browse of a queue or topic.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: BrowserAction.java,v 1.50 2007/01/26 13:26:22 colincrist Exp $
 */

public abstract class BrowserAction extends AbstractDocumentComponent implements ListSelectionListener, DocumentComponentListener, MessageTaskListener,
      FilterableAction, NavigableComponent, MessagesDeleteable
{
   private static final Logger log = Logger.getLogger(BrowserAction.class);
   private static final Icon queueIcon = IconCache.getIcon(IconCache.QUEUE);
   private static final Timer timer = new Timer();
   private static final Icon topicIcon = IconCache.getIcon(IconCache.TOPIC);

   /**
    * Get the name to display in the title bar.
    */
   public static String getDisplayName(Hermes hermes, DestinationConfig config, String postfix)
   {
      String rval = null ;
      
      if (hermes != null)
      {
          rval =  hermes.getId() + " " + config.getName() + " " + (config.getSelector() != null ? config.getSelector() : "")
               + (config.isDurable() ? " name=" + config.getClientID() : "");
      }
      else
      {
          rval =  config.getName() + " " + (config.getSelector() != null ? config.getSelector() : "") + (config.isDurable() ? " name=" + config.getClientID() : "");
      }
      
      return postfix == null ? rval : rval + " for " + postfix ;
   }

   private final List<Message> cachedRows = new ArrayList<Message>();
   private final JLabel statusMessage = new JLabel();
   private boolean firstMessage = true;
   private Hermes hermes;
   private long lastMessagePerSecond = 0;
   private long lastMessagesRead = 0;
   private ListSelectionEvent lastSelected;
   private int maxMessages = 1000;
   private long maxMessagesPerSecond = 0;
   private final MessageHeaderTable messageHeaderTable;
   private final MessageHeaderTableModel messageHeaderTableModel;
   private SQL92FilterableTableModel filterModel;
   private final MessagePayloadPanel messagePayloadPanel;
   private JPopupMenu popup;
   private JideScrollPane headerScrollPane;
   private TimerTask rateTask;
   private int readMessages = 0;
   private int screenUpdateTimeout = 100;
   private JPanel statusPanel;
   private Task task;
   private JPanel topPanel;
   private long totalMessagesRead = 0;
   private TimerTask uiUpdateTimer;
   private TimerTask autoBrowseTimer;
   private boolean autoBrowse = false;
   private int selectedRow = -1;
   private boolean taskStopped = false;
   private DestinationConfig dConfig;
   private String postfix ;

   public BrowserAction(Hermes hermes, DestinationConfig dConfig, int maxMessages, String postfix) throws JMSException
   {
      super(new JPanel(), getDisplayName(hermes, dConfig, postfix));

      this.dConfig = dConfig;
      this.messageHeaderTableModel = new MessageHeaderTableModel(hermes, dConfig.getName());
      this.messageHeaderTable = new MessageHeaderTable(hermes, this, messageHeaderTableModel);
      this.hermes = hermes;
      this.maxMessages = maxMessages;
      this.messagePayloadPanel = new MessagePayloadPanel(dConfig.getName());
      this.postfix = postfix ;
      
      topPanel = (JPanel) getComponent();
   }

   public String getName()
   {
      if (dConfig.getDomain() == Domain.QUEUE.getId())
      {
         return "Q " + super.getName();
      }
      else
      {
         return "T " + super.getName();
      }
   }

   public ListSelectionModel getListSelectionModel()
   {
      return messageHeaderTable.getSelectionModel();
   }

   public boolean isNavigableForward()
   {
      return getMessageHeaderTable().getSelectedRow() < getMessageHeaderTable().getRowCount() - 1;
   }

   public boolean isNavigableBackward()
   {
      return getMessageHeaderTable().getSelectedRow() > 0 && getMessageHeaderTable().getRowCount() > 1;
   }

   public void navigateBackward()
   {
      decrementSelection();
   }

   public void navigateForward()
   {
      incrementSelection();
   }

   @Override
   public String getTitle()
   {
      return getDisplayName(hermes, dConfig, postfix);
   }

   public DestinationConfig getConfig()
   {
      return dConfig;
   }

   public String getSelector()
   {
      return dConfig.getSelector();
   }

   public Domain getDomain()
   {
      return Domain.getDomain(dConfig.getDomain());
   }

   public boolean isRunning()
   {
      return !taskStopped;
   }

   protected void addMessage(Message message) throws javax.jms.JMSException
   {
      synchronized (cachedRows)
      {
         cachedRows.add(message);

         totalMessagesRead++;
      }
   }

   protected abstract Task createTask() throws Exception;

   public void documentComponentMoved(DocumentComponentEvent arg0)
   {
      // NOP

   }

   public void documentComponentMoving(DocumentComponentEvent arg0)
   {
      // NOP
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.jidesoft.document.DocumentComponentListener#documentComponentActivated(com.jidesoft.document.DocumentComponentEvent)
    */
   public void documentComponentActivated(DocumentComponentEvent arg0)
   {
      // NOP
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.jidesoft.document.DocumentComponentListener#documentComponentClosed(com.jidesoft.document.DocumentComponentEvent)
    */
   public void documentComponentClosed(DocumentComponentEvent arg0)
   {
      log.debug("documentClosed " + getName());

      messageHeaderTableModel.clear();

      if (task != null)
      {
         task.stop();
      }

      if (autoBrowseTimer != null)
      {
         autoBrowseTimer.cancel();
      }

      autoBrowse = false;
   }

   public void documentComponentClosing(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentDeactivated(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentOpened(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void decrementSelection()
   {
      int currentRow = getMessageHeaderTable().getSelectedRow();

      getMessageHeaderTable().getSelectionModel().setSelectionInterval(currentRow - 1, currentRow - 1);
   }

   public void incrementSelection()
   {
      final int currentRow = getMessageHeaderTable().getSelectedRow();

      getMessageHeaderTable().getSelectionModel().setSelectionInterval(currentRow + 1, currentRow + 1);
   }

   public void enrichPopup(JPopupMenu popupMenu)
   {

   }

   /**
    * Create and display popup for deleting or saving selected messages
    * 
    * @param e
    */
   public void doPopup(MouseEvent e)
   {
      if (popup == null)
      {
         popup = PopupMenuFactory.createBrowseActionPopup();

         enrichPopup(popup);
      }

      popup.show(messageHeaderTable, e.getX(), e.getY());
   }

   public String getDestination()
   {
      return dConfig.getName();
   }

   public Hermes getHermes()
   {
      return hermes;
   }

   public Icon getIcon()
   {
      if (dConfig.getDomain() == Domain.QUEUE.getId())
      {
         return queueIcon;
      }
      else
      {
         if (dConfig.isDurable())
         {
            return IconCache.getIcon("jms.durableTopic");
         }
         else
         {
            return topicIcon;
         }
      }
   }

   public MessageHeaderTable getMessageHeaderTable()
   {
      return messageHeaderTable;
   }

   /**
    * Get the set of JMS message IDS in the selection (if any)
    */
   public Set<String> getSelectedMessageIDs() throws JMSException
   {
      Set<String> ids = new HashSet<String>();

      int[] rows = messageHeaderTable.getSelectedRows();

      for (int i = 0; i < rows.length; i++)
      {
         Message m = messageHeaderTableModel.getMessageAt(filterModel.getActualRowAt(rows[i]));

         if (m.getJMSMessageID() == null)
         {
            throw new HermesException("One or more of the messages has a null JMSMessageID");
         }

         try
         {

            ids.add(m.getJMSMessageID());
         }
         catch (Exception ex)
         {
            log.error("calling getJMSMessageID() on message " + m + ": " + ex.getMessage(), ex);
         }
      }

      return ids;
   }

   public boolean hasSelection()
   {
      return messageHeaderTable.getSelectedRowCount() > 0;
   }

   /**
    * Get the set of JMS messages in the selection (if any)
    */
   public Collection<Message> getSelectedMessages()
   {
      final ArrayList<Message> ids = new ArrayList<Message>();
      final int[] rows = messageHeaderTable.getSelectedRows();

      for (int i = 0; i < rows.length; i++)
      {
         final Message m = messageHeaderTableModel.getMessageAt(filterModel.getActualRowAt(rows[i]));

         try
         {
            ids.add(m);
         }
         catch (Exception ex)
         {
            log.error("calling getJMSMessageID() on message " + m + ": " + ex.getMessage(), ex);
         }
      }

      return ids;
   }

   private JPanel getStatusPanel()
   {
      if (statusPanel == null)
      {
         statusPanel = new JPanel();
         statusPanel.setLayout(new java.awt.BorderLayout());
         statusPanel.setAlignmentY(java.awt.Component.BOTTOM_ALIGNMENT);

         statusMessage.setText("Connecting...");
         statusMessage.setBorder(new EtchedBorder());

         statusPanel.add(statusMessage);
      }

      return statusPanel;
   }

   public String getTooltip()
   {
      return getTitle();
   }

   public void init() throws JMSException
   {
      initUI();

      refresh();
   }

   private void initUI() throws JMSException
   {

      headerScrollPane = new JideScrollPane();

      filterModel = new SQL92FilterableTableModel(messageHeaderTableModel);
      filterModel.setRowValueProvider(messageHeaderTableModel);
      messageHeaderTable.setModel(filterModel);

      topPanel.setLayout(new BorderLayout());

      if (HermesBrowser.getBrowser().getConfig().isEmbeddedMessageInBrowsePane())
      {
         topPanel.add(headerScrollPane, "Center");

         final HierarchicalTable hTable = new HierarchicalMessageHeaderTable(this, messageHeaderTableModel);
         headerScrollPane.setViewportView(hTable);

         hTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         hTable.setExpandableColumn(-1);
         hTable.setSingleExpansion(false);
      }
      else
      {
         //
         // The window is split with the messages read at the top and the
         // payload and stats at the bottom

         headerScrollPane.setViewportView(messageHeaderTable);

         JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

         splitPane.setDividerLocation(200);
         splitPane.setOneTouchExpandable(false);
         splitPane.setContinuousLayout(true);

         splitPane.add(headerScrollPane, "top");
         splitPane.add(messagePayloadPanel, "bottom");

         topPanel.add(splitPane, "Center");

         // Add to the desktop pane and register it for the Windows
         // menu...

         messageHeaderTable.getSelectionModel().addListSelectionListener(this);
      }

      JPanel bottomPanel = new JPanel(new BorderLayout());
      bottomPanel.add(new FilterablePanel(), BorderLayout.NORTH);
      bottomPanel.add(getStatusPanel(), BorderLayout.SOUTH);

      topPanel.add(bottomPanel, BorderLayout.SOUTH);

      HermesBrowser.getBrowser().addDocumentComponent(this);
      addDocumentComponentListener(this);

   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
    */
   public void onMessage(Task task, Message message)
   {
      synchronized (cachedRows)
      {
         cachedRows.add(message);
         totalMessagesRead++;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.TaskListener#onStarted()
    */
   public void onStarted(Task task)
   {
      // NOP
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.TaskListener#onStatus(java.lang.String)
    */
   public void onStatus(Task task, final String status)
   {
      SwingRunner.invokeLater(new Runnable()
      {
         public void run()
         {
            statusMessage.setText(status);
         }
      });
   }

   public abstract boolean isRefreshable();

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.TaskListener#onStopped()
    */
   public void onStopped(Task task)
   {
      try
      {
         updateTableRows();
         taskStopped = true;

         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               messageHeaderTableModel.setFinalMessageIndex(readMessages - 1);

               if (selectedRow >= 0)
               {
                  messageHeaderTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
                  valueChanged(null);
               }
            }
         });

         if (autoBrowse && isRefreshable())
         {
            autoBrowseTimer = new TimerTask()
            {
               @Override
               public void run()
               {
                  refresh();
               }
            };

            log.debug("scheduling browser refresh for " + getDestination() + " on session " + getHermes().getId() + " in "
                  + HermesBrowser.getBrowser().getConfig().getAutoBrowseRefreshRate() + "s");

            timer.schedule(autoBrowseTimer, HermesBrowser.getBrowser().getConfig().getAutoBrowseRefreshRate() * 1000);
         }
      }
      catch (HermesException e)
      {
         log.error(e.getMessage(), e);
      }
   }

   public TableModel getTableModel()
   {
      return getMessageHeaderTable().getModel();
   }

   public boolean isDeleteable()
   {
      return hasSelection();
   }

   public boolean isAutoBrowse()
   {
      return autoBrowse;
   }

   public void setAutoBrowse(boolean autoBrowse)
   {
      this.autoBrowse = autoBrowse;

      if (autoBrowse)
      {
         if (taskStopped)
         {
            refresh();
         }
      }
      else
      {
         if (autoBrowseTimer != null)
         {
            autoBrowseTimer.cancel();
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.TaskListener#onThrowable(java.lang.Throwable)
    */
   public void onThrowable(Task task, final Throwable t)
   {
      SwingRunner.invokeLater(new Runnable()
      {
         public void run()
         {
            statusMessage.setText(t.getMessage());
         }
      });
   }

   public void refresh()
   {
      readMessages = 0;
      maxMessagesPerSecond = 0;
      lastMessagePerSecond = 0;
      lastMessagesRead = 0;
      cachedRows.clear();

      if (task != null)
      {
         task.stop();
      }

      if (rateTask != null)
      {
         rateTask.cancel();
      }

      if (uiUpdateTimer != null)
      {
         uiUpdateTimer.cancel();
      }

      //
      // This timer will ensure that the update events to the table
      // caused by new messages are only applied at most every 70ms
      // so helping to keep the UI live.

      uiUpdateTimer = new TimerTask()
      {
         public void run()
         {
            BrowserAction.this.updateTableRows();

            if (task != null && !task.isRunning())
            {
               cancel();
            }
         }
      };

      timer.schedule(uiUpdateTimer, screenUpdateTimeout, screenUpdateTimeout);

      // 
      // This timer calculates the number of messages per second.

      rateTask = new TimerTask()
      {
         public void run()
         {
            synchronized (cachedRows)
            {
               lastMessagePerSecond = totalMessagesRead - lastMessagesRead;

               if (lastMessagePerSecond > maxMessagesPerSecond)
               {
                  maxMessagesPerSecond = lastMessagePerSecond;
               }

               lastMessagesRead = totalMessagesRead;
            }

            if (task != null && task.isRunning())
            {
               cancel();
            }
         }

      };

      timer.schedule(rateTask, 1000, 1000);

      //
      // Finally create and start the task.

      try
      {
         taskStopped = false;
         task = createTask();
         task.addTaskListener(this);
         task.start();
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
      }
   }

   public void setDestinationName(String destinationName)
   {
      messageHeaderTableModel.setDestinationName(destinationName);
   }

   public void setStatusText(String text)
   {
      statusMessage.setText(text);
   }

   public String toShortString() throws JMSException
   {
      return hermes.getMetaData().getShortName() + ": " + getDestination();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.jidesoft.document.DocumentComponentListener#documentComponentClosing(com.jidesoft.document.DocumentComponentEvent)
    */

   public String toString()
   {
      try
      {
         return "browse " + toShortString();
      }
      catch (JMSException ex)
      {
         return super.toString();
      }
   }

   /**
    * Called buy the timer, it will update the UI with the new rows of consumes
    * messages, updating the status panels accordingly. Returns true if the
    * action is still running, otherwise false, allowing the timer to switch
    * itself off.
    */

   public void updateTableRows()
   {
      try
      {
         SwingUtilities.invokeAndWait(new Runnable()
         {
            public void run()
            {
               synchronized (cachedRows)
               {
                  for (Iterator<Message> iter = cachedRows.iterator(); iter.hasNext();)
                  {
                     try
                     {
                        messageHeaderTableModel.addMessage(readMessages++, iter.next());

                        if (firstMessage)
                        {
                           messageHeaderTable.getSelectionModel().setSelectionInterval(0, 0);
                           firstMessage = false;
                        }
                     }
                     catch (JMSException e)
                     {
                        log.error(e.getMessage(), e);
                     }
                  }

                  cachedRows.clear();

                  while (maxMessages > 0 && messageHeaderTableModel.getRowCount() > maxMessages)
                  {
                     messageHeaderTableModel.removeFirstRow();
                  }

                  try
                  {
                     if (HermesBrowser.getBrowser().getConfig().isScrollMessagesDuringBrowse())
                     {
                        SwingUtils.scrollVertically(messageHeaderTable, SwingUtils.getRowBounds(messageHeaderTable, messageHeaderTableModel.getRowCount(),
                              messageHeaderTableModel.getRowCount()));
                     }
                  }
                  catch (HermesException ex)
                  {
                     log.error(ex.getMessage(), ex);
                  }
               }

               StringBuffer buffer = new StringBuffer();

               if (task != null && !task.isRunning())
               {
                  buffer.append("Finished. ");
               }

               switch (messageHeaderTableModel.getRowCount())
               {
                  case 0:
                     buffer.append("No messages read.");
                     break;
                  case 1:
                     buffer.append("1 message read.");
                     break;

                  default:
                     buffer.append(readMessages).append(" messages read.");
               }

               if (readMessages > maxMessages)
               {
                  // buffer.append(" (only keeping last " + maxMessages + "
                  // messages in table)");
               }

               if ((task != null && !task.isRunning()) || messageHeaderTableModel.getRowCount() > 0)
               {
                  statusMessage.setText(buffer.toString());
               }
            }
         });
      }
      catch (InterruptedException e)
      {
         log.error(e.getMessage(), e);
      }
      catch (InvocationTargetException e)
      {
         log.error(e.getMessage(), e);
      }
   }

   /**
    * When a user selects a row, bring up the messages in the below panel. Uses
    * the MessengerRenderer's that have been configured with the first one
    * returning a panel being the one used.
    */
   public void valueChanged(ListSelectionEvent e)
   {
      selectedRow = filterModel.getActualRowAt(messageHeaderTable.getSelectedRow());

      if (messageHeaderTableModel.getRowCount() > selectedRow && selectedRow >= 0)
      {
         final int row = selectedRow;
         final Message m = messageHeaderTableModel.getMessageAt(row);

         //
         // Keep the selected row visible.

         messageHeaderTable.scrollRectToVisible(messageHeaderTable.getCellRect(selectedRow, 0, true));

         if (m != null)
         {
            messagePayloadPanel.setMessage(hermes, m);
         }
      }
   }

   public void delete()
   {
      try
      {
         HermesBrowser.getBrowser().getActionFactory().createTruncateAction(hermes, getConfig(), getSelectedMessageIDs(), true);
      }
      catch (JMSException ex)
      {
         HermesBrowser.getBrowser().showErrorDialog(ex);
      }
   }

   public void setSelector(String selector) throws InvalidSelectorException
   {
      filterModel.setSelector(selector);
   }
}