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

package hermes.browser.actions;

import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.tasks.Task;
import hermes.browser.tasks.TaskListener;
import hermes.fix.FIXMessage;
import hermes.fix.FIXMessageTable;
import hermes.fix.FIXUtils;
import hermes.swing.FilterablePanel;
import hermes.swing.SwingRunner;
import hermes.swing.SwingUtils;
import hermes.util.DumpUtils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

import com.jidesoft.document.DocumentComponentEvent;
import com.jidesoft.document.DocumentComponentListener;
import com.jidesoft.swing.JideScrollPane;
import com.jidesoft.swing.JideTabbedPane;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: AbstractFIXBrowserDocumentComponent.java,v 1.4 2006/07/17
 *          21:20:54 colincrist Exp $
 */

public abstract class AbstractFIXBrowserDocumentComponent extends AbstractDocumentComponent implements DocumentComponentListener, TaskListener {
	private static final Logger log = Logger.getLogger(AbstractFIXBrowserDocumentComponent.class);
	private final JLabel statusMessage = new JLabel();
	protected static final Timer timer = new Timer();
	private int screenUpdateTimeout = 100;
	private JPanel statusPanel;
	private JPanel topPanel;
	private boolean taskStopped = false;
	private String title;
	private Task task;
	private List<FIXMessage> cachedRows = new ArrayList<FIXMessage>();
	private JideTabbedPane messageTabbedPane = new JideTabbedPane();
	private JideScrollPane messagePayloadPanel = new JideScrollPane();
	private JideScrollPane headerScrollPane = new JideScrollPane();
	private final JPanel bottomPanel = new JPanel(new BorderLayout());
	private final Map<FIXMessage, JComponent> renderedLRUMap = new LRUMap(100);

	public abstract Collection<Object> getSelectedMessages();

	public AbstractFIXBrowserDocumentComponent(String title) {
		super(new JPanel(), title);

		this.title = title;
		this.topPanel = (JPanel) getComponent();
	}

	protected void setCachedRows(List<FIXMessage> cachedRows) {
		this.cachedRows = cachedRows;

	}

	protected Collection<FIXMessage> getCachedRows() {
		return cachedRows;
	}

	protected abstract Component getHeaderComponent();

	protected Component getBottomComponent() {
		return bottomPanel;
	}

	protected void updateMessageTabbedPane(FIXMessage m) {
		final int index = getMessageTabbedPane().getSelectedIndex();

		getMessageTabbedPane().removeAll();
		getMessageTabbedPane().add("Parsed", getMessagePayloadPanel());
		getMessageTabbedPane().add("Hex", createHexPanel(m));
		getMessageTabbedPane().add("Text", createPrettyPrintPanel(m));

		if (index >= 0) {
			getMessageTabbedPane().setSelectedIndex(index);
		}
	}

	public void addMessage(FIXMessage message) {
		synchronized (cachedRows) {
			try {
				cachedRows.add(message);
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	protected void init() {
		headerScrollPane.setViewportView(getHeaderComponent());

		bottomPanel.add(new FilterablePanel(), BorderLayout.NORTH);
		bottomPanel.add(getStatusPanel(), BorderLayout.SOUTH);

		getTopPanel().setLayout(new BorderLayout());

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		splitPane.setDividerLocation(200);
		splitPane.setOneTouchExpandable(false);
		splitPane.setContinuousLayout(false);
		splitPane.add(headerScrollPane, "top");
		splitPane.add(messageTabbedPane, "bottom");

		messageTabbedPane.setTabPlacement(JTabbedPane.BOTTOM);

		getTopPanel().add(splitPane, BorderLayout.CENTER);
		getTopPanel().add(getBottomComponent(), BorderLayout.SOUTH);

		HermesBrowser.getBrowser().addDocumentComponent(this);

		addDocumentComponentListener(this);

		updateTableRows(true);
	}

	/**
	 * When a user selects a row, bring up the messages in the below panel. Uses
	 * the MessengerRenderer's that have been configured with the first one
	 * returning a panel being the one used.
	 */
	public void doSelectionChanged(FIXMessageTable table, ListSelectionEvent e) {
		int selectedRow = table.getSelectedRow();

		if (table.getRowCount() > selectedRow && selectedRow >= 0) {
			final int row = selectedRow;

			final FIXMessage m = table.getMessageAt(row);

//			//
//			// Keep the selected row visible.
//
//			table.scrollRectToVisible(table.getCellRect(selectedRow, 0, true));

			try {
				if (!renderedLRUMap.containsKey(m)) {
					JComponent rendered = FIXUtils.createView(m, true, true);
					renderedLRUMap.put(m, rendered);
				}

				getMessagePayloadPanel().setViewportView(renderedLRUMap.get(m));
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);

				JTextArea text = new JTextArea(e1.getMessage());
				text.setEditable(false);
				getMessagePayloadPanel().setViewportView(text);
			}

			updateMessageTabbedPane(m);
		}
	}

	public JPanel getTopPanel() {
		return topPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.browser.tasks.TaskListener#onStarted()
	 */
	public void onStarted(Task task) {
		// NOP
	}

	public void onThrowable(Task task, Throwable t) {
		// NOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.browser.tasks.TaskListener#onStatus(java.lang.String)
	 */
	public void onStatus(Task task, final String status) {
		SwingRunner.invokeLater(new Runnable() {
			public void run() {
				statusMessage.setText(status);
			}
		});
	}

	public boolean isTaskStopped() {
		return taskStopped;
	}

	protected abstract void updateTableRows(final boolean reschedule);

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.browser.tasks.TaskListener#onStopped()
	 */
	public void onStopped(Task task) {
		updateTableRows(false);
		taskStopped = true;
	}

	public void setStatusText(String text) {
		statusMessage.setText(text);
	}

	protected void doClose() {
		if (task != null) {
			task.stop();
		}

		renderedLRUMap.clear();
	}

	public void documentComponentDocked(DocumentComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void documentComponentFloated(DocumentComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void documentComponentMoved(DocumentComponentEvent arg0) {
		// NOP

	}

	public void documentComponentMoving(DocumentComponentEvent arg0) {
		// NOP
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jidesoft.document.DocumentComponentListener#documentComponentActivated
	 * (com.jidesoft.document.DocumentComponentEvent)
	 */
	public void documentComponentActivated(DocumentComponentEvent arg0) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jidesoft.document.DocumentComponentListener#documentComponentClosed
	 * (com.jidesoft.document.DocumentComponentEvent)
	 */
	public void documentComponentClosed(DocumentComponentEvent arg0) {
		doClose();
	}

	public void documentComponentClosing(DocumentComponentEvent arg0) {
		// NOP
	}

	public void documentComponentDeactivated(DocumentComponentEvent arg0) {
		// NOP
	}

	public void documentComponentOpened(DocumentComponentEvent arg0) {
		// NOP
	}

	protected JPanel getStatusPanel() {
		if (statusPanel == null) {
			statusPanel = new JPanel();
			statusPanel.setLayout(new java.awt.BorderLayout());
			statusPanel.setAlignmentY(java.awt.Component.BOTTOM_ALIGNMENT);

			statusMessage.setText("Reading...");
			statusMessage.setBorder(new EtchedBorder());

			statusPanel.add(statusMessage);
		}

		return statusPanel;
	}

	public String getTooltip() {
		return getTitle();
	}

	public Icon getIcon() {
		return IconCache.getIcon("hermes.file.fix");
	}

	public void setTask(Task task) {
		this.task = task;
		task.addTaskListener(this);
	}

	@Override
	public String getTitle() {
		return title;
	}

	public boolean isRunning() {
		return !taskStopped;
	}

	protected Component createPrettyPrintPanel(FIXMessage m) {
		final JTextArea textArea = new JTextArea();

		textArea.setEditable(false);
		textArea.setFont(Font.decode("Monospaced-PLAIN-12"));

		byte[] bytes = null;

		try {
			textArea.setText(FIXUtils.prettyPrint(m));
		} catch (Throwable e) {
			textArea.setText(e.getMessage());

			log.error("exception converting message to byte[]: ", e);
		}

		textArea.setCaretPosition(0);

		return SwingUtils.createJScrollPane(textArea);
	}

	protected Component createHexPanel(FIXMessage m) {
		final JTextArea textArea = new JTextArea();

		textArea.setEditable(false);
		textArea.setFont(Font.decode("Monospaced-PLAIN-12"));

		byte[] bytes = null;

		try {
			textArea.setText(DumpUtils.dumpBinary(m.getBytes(), DumpUtils.DUMP_AS_HEX_AND_ALPHA));
		} catch (Throwable e) {
			textArea.setText(e.getMessage());

			log.error("exception converting message to byte[]: ", e);
		}

		textArea.setCaretPosition(0);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(textArea);

		return scrollPane;
	}

	public int getScreenUpdateTimeout() {
		return screenUpdateTimeout;
	}

	protected JideScrollPane getMessagePayloadPanel() {
		return messagePayloadPanel;
	}

	protected JideTabbedPane getMessageTabbedPane() {
		return messageTabbedPane;
	}

}
