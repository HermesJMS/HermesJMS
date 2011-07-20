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

package hermes.fix;

import hermes.browser.IconCache;
import hermes.browser.components.PopupMenuFactory;
import hermes.browser.transferable.MessagesTransferHandler;
import hermes.swing.Colours;
import hermes.swing.SQL92FilterableTableModel;
import hermes.swing.SwingUtils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import com.codestreet.selector.parser.InvalidSelectorException;
import com.jidesoft.grid.SortableTable;
import com.jidesoft.swing.JidePopupMenu;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXMessageTable.java,v 1.15 2007/02/18 16:13:42 colincrist Exp
 *          $
 */

public class FIXMessageTable extends SortableTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2044612567082104913L;
	private static final Logger log = Logger.getLogger(FIXMessageTable.class);
	private SQL92FilterableTableModel selectorModel;
	private FIXMessageTableModel model;
	private JidePopupMenu popup;
	private SessionKey sessionKey;
	private boolean autoScroll = false;

	public boolean isAutoScroll() {
		return autoScroll;
	}

	public void setAutoScroll(boolean autoScroll) {
		this.autoScroll = autoScroll;
	}

	public FIXMessageTable(final SessionKey sessionKey, final FIXMessageTableModel model) {
		super();

		this.model = model;
		this.selectorModel = new SQL92FilterableTableModel(model, new FIXIdentifierExtension());
		this.selectorModel.setRowValueProvider(model);
		this.sessionKey = sessionKey;

		setSortable(true);

		setModel(selectorModel);
		setDragEnabled(true);
		setTransferHandler(new MessagesTransferHandler(this));
		getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		getColumn(FIXMessageTableModel.DIRECTION).setMaxWidth(IconCache.getIcon("hermes.back").getIconWidth() + 4);
		getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
			/**
		 * 
		 */
			private static final long serialVersionUID = -2075897579194001018L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				FIXMessage message = model.getMessageAt(selectorModel.getActualRowAt(row));

				if (model.getRole(message) == SessionRole.ACCEPTOR) {
					setIcon(IconCache.getIcon("hermes.back"));
				} else {
					setIcon(IconCache.getIcon("hermes.forward"));
				}

				return this;
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					// final JComponent c = (JComponent) e.getSource();
					// final TransferHandler th = c.getTransferHandler();

					getTransferHandler().exportAsDrag(FIXMessageTable.this, e, TransferHandler.COPY);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					doPopup(e);
				}
			}
		});

		model.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				if (isAutoScroll()) {
					// if (e.getType() == TableModelEvent.INSERT) {
					// getSelectionModel().setSelectionInterval(model.getRowCount()
					// - 1, model.getRowCount() - 1);
					// scrollRectToVisible(getCellRect(model.getRowCount() - 1,
					// 0, true));
					// }
				}
			}
		});
	}

	public SessionKey getSessionKey() {
		return sessionKey;
	}

	private void doPopup(MouseEvent e) {
		if (popup == null) {
			popup = PopupMenuFactory.createFIXMessageTablePopup(this);
		}

		popup.show(this, e.getX(), e.getY());
	}

	public Collection<Object> getSelectedMessages() {
		Collection<Object> rval = new ArrayList<Object>();
		int[] selected = getSelectedRows();

		for (int i = 0; i < selected.length; i++) {
			rval.add(getMessageAt(selected[i]));
		}

		return rval;
	}

	public FIXMessage getMessageAt(int row) {
		return model.getMessageAt(selectorModel.getActualRowAt(getActualRowAt(row)));
	}

	public void addMessages(Collection<FIXMessage> messages) {
		if (messages.size() > 0) {
			model.addMessages(messages);
			SwingUtils.scrollVertically((JComponent) this.getParent(), SwingUtils.getRowBounds(this, model.getRowCount(), model.getRowCount()));
		}
	}

	public void setSelector(String selector) throws InvalidSelectorException {
		selectorModel.setSelector(selector);
	}

	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component c = super.prepareRenderer(renderer, row, column);

		if (row < getRowCount()) {
			FIXMessage message = getMessageAt(row);

			if (message != null) {
				if (!isCellSelected(row, column)) {
					if (model.getRole(message) == SessionRole.ACCEPTOR) {
						c.setBackground(Colours.LIGHT_SEA_GREEN);
					} else {
						c.setBackground(Colours.LIGHT_SKY_BLUE);
					}
				}
			}
		}
		return c;
	}

	// @Override
	// public void scrollRectToVisible(Rectangle aRect) {
	// Container parent;
	// int dx = getX(), dy = getY();
	//
	// for (parent = getParent(); !(parent == null) && (!(parent instanceof
	// JViewport) || (((JViewport)
	// parent).getClientProperty("HierarchicalTable.mainViewport") == null));
	// parent = parent
	// .getParent()) {
	// Rectangle bounds = parent.getBounds();
	//
	// dx += bounds.x;
	// dy += bounds.y;
	// }
	//
	// if (!(parent == null) && !(parent instanceof CellRendererPane)) {
	// aRect.x += dx;
	// aRect.y += dy;
	//
	// ((JComponent) parent).scrollRectToVisible(aRect);
	// aRect.x -= dx;
	// aRect.y -= dy;
	// }
	// }
}
