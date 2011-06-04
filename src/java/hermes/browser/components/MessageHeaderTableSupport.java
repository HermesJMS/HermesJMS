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

import hermes.browser.actions.BrowserAction;
import hermes.browser.transferable.MessageHeaderTransferHandler;

import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

/**
 * Helper for the different implementations of the MessageHeaderTable.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: MessageHeaderTableSupport.java,v 1.1 2004/07/26 19:03:37
 *          colincrist Exp $
 */
class MessageHeaderTableSupport {
	private static final Logger log = Logger.getLogger(MessageHeaderTableSupport.class);

	static void init(final BrowserAction action, final MessageHeaderTable table, DataFlavor[] myFlavours) {
		table.setDragEnabled(true);
		table.setTransferHandler(new MessageHeaderTransferHandler(action));

		final MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isMiddleMouseButton(e)) {
					table.clearSelection();
				} else if (SwingUtilities.isLeftMouseButton(e)) {

					if (e.getClickCount() == 2) {
						table.onDoubleClick();
					} else {

						final JComponent c = (JComponent) e.getSource();
						final TransferHandler th = c.getTransferHandler();

						th.exportAsDrag(c, e, TransferHandler.COPY);
					}
				} else if (SwingUtilities.isRightMouseButton(e)) {
					action.doPopup(e);
				}
			}
		};

		table.addMouseListener(ml);

		final DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1967620759224151946L;

			protected void setValue(Object value) {
				super.setText((value == null) ? "" : value.toString());
			}
		};

		table.setDefaultRenderer(Date.class, dateRenderer);
	}

	static Component prepareRenderer(Component c, JTable table, TableCellRenderer renderer, int row, int column) {
		if (!table.isCellSelected(row, column)) {
			if (row % 2 == 1) {
				c.setBackground(Color.LIGHT_GRAY);
			} else {
				c.setBackground(table.getBackground());
			}
		}
		return c;
	}
}