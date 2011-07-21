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

import hermes.fix.FIXMessageTable;
import hermes.fix.FIXMessageTableModel;
import hermes.fix.SessionKey;
import hermes.fix.quickfix.QuickFIXMessageCache;
import hermes.swing.ProxyListSelectionModel;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TimerTask;

import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import com.codestreet.selector.parser.InvalidSelectorException;
import com.jidesoft.document.DocumentComponentListener;
import com.jidesoft.grid.ListSelectionModelGroup;

/**
 * HermesAction to perform the browse of a queue or topic.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FIXBrowserDocumentComponent.java,v 1.1 2006/04/30 12:55:05
 *          colincrist Exp $
 */

public class FIXSessionBrowserDocumentComponent extends AbstractFIXBrowserDocumentComponent implements FilterableAction, ListSelectionListener {
	private static final Logger log = Logger.getLogger(FIXSessionBrowserDocumentComponent.class);
	private boolean firstMessage = true;
	private ListSelectionEvent lastSelected;
	private final ListSelectionModelGroup listSelectionGroup = new ListSelectionModelGroup();

	private FIXMessageTable messageTable;
	private FIXMessageTableModel messageTableModel;
	private ListSelectionListener messageTableListSelectionListener;
	private TableModelListener messageTableModelListener;
	private JPopupMenu popup;
	private SessionKey selectedSessionKey;
	private ProxyListSelectionModel proxySelectionModel = new ProxyListSelectionModel();
	private QuickFIXMessageCache messageCache;

	public FIXSessionBrowserDocumentComponent(QuickFIXMessageCache messageCache, SessionKey sessionKey) {
		super(sessionKey.toString());
		this.messageCache = messageCache;
		messageTableModel = new FIXMessageTableModel(sessionKey);
		messageTable = new FIXMessageTable(sessionKey, messageTableModel);

		messageTable.getSelectionModel().addListSelectionListener(this);

		init();
	}

	public ListSelectionModel getListSelectionModel() {
		return proxySelectionModel;
	}

	public boolean isNavigableForward() {
		return messageTable.getSelectedRow() < messageTable.getRowCount() - 1;
	}

	public boolean isNavigableBackward() {
		return messageTable.getSelectedRow() > 0 && messageTable.getRowCount() > 1;
	}

	public void navigateBackward() {
		int currentRow = messageTable.getSelectedRow();
		messageTable.getSelectionModel().setSelectionInterval(currentRow - 1, currentRow - 1);
	}

	public void navigateForward() {
		final int currentRow = messageTable.getSelectedRow();
		messageTable.getSelectionModel().setSelectionInterval(currentRow + 1, currentRow + 1);
	}

	public Collection<Object> getSelectedMessages() {
		return messageTable.getSelectedMessages();
	}

	@Override
	protected void doClose() {
		super.doClose();

		messageCache.close();

		for (DocumentComponentListener l : getDocumentComponentListeners()) {
			removeDocumentComponentListener(l);
		}
		messageTable.getSelectionModel().removeListSelectionListener(this);
		messageTableModel.clear();
	}

	public void decrementSelection() {
		int currentRow = messageTable.getSelectedRow();

		messageTable.getSelectionModel().setSelectionInterval(currentRow - 1, currentRow - 1);
	}

	public void incrementSelection() {
		final int currentRow = messageTable.getSelectedRow();

		messageTable.getSelectionModel().setSelectionInterval(currentRow + 1, currentRow + 1);
	}

	public boolean hasSelection() {
		return messageTable.getSelectedRowCount() > 0;
	}

	public void setSelector(String selector) throws InvalidSelectorException {
		messageTable.setSelector(selector);
	}

	protected void init() {
		super.init();

	}

	/**
	 * Called by the timer, it will update the UI with the new rows of consumes
	 * messages, updating the status panels accordingly. Returns true if the
	 * action is still running, otherwise false, allowing the timer to switch
	 * itself off.
	 */

	protected void updateTableRows(final boolean reschedule) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				synchronized (getCachedRows()) {
					if (messageTable != null) {
						messageTable.addMessages(getCachedRows());

						if (firstMessage) {
							messageTable.getSelectionModel().setSelectionInterval(0, 0);
							firstMessage = false;
						}

						setCachedRows(new ArrayList());
					}
				}

				StringBuffer buffer = new StringBuffer();

				if (!reschedule || isTaskStopped()) {
					buffer.append("Finished. ");
				} else {

					switch (messageTableModel.getRowCount()) {
					case 0:
						buffer.append("No messages read.");
						break;
					case 1:
						buffer.append("1 message found.");
						break;

					default:
						buffer.append(messageTableModel.getRowCount()).append(" messages read.");
					}
				}

				if (reschedule) {
					setStatusText(buffer.toString());
				} else {
					setStatusText("Finished. " + buffer.toString());
				}

				if (reschedule) {
					TimerTask timerTask = new TimerTask() {
						@Override
						public void run() {
							updateTableRows(true);
						}
					};

					timer.schedule(timerTask, getScreenUpdateTimeout());
				}
			}
		});

	}

	public QuickFIXMessageCache getMessageCache() {
		return messageCache;
	}

	@Override
	protected Component getHeaderComponent() {
		return messageTable;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		super.doSelectionChanged(messageTable, e);
	}
}