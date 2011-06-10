package hermes.browser.dialog.message;

import hermes.swing.PropertyRow;
import hermes.swing.PropertyTableModel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

@SuppressWarnings("serial")
public class GenericPropertyPanel extends JPanel {
	private JTable table;
	protected PropertyTableModel model = new PropertyTableModel();

	/**
	 * Create the panel.
	 * 
	 * @param editable
	 */
	public GenericPropertyPanel(boolean editable) {
		setLayout(new BorderLayout(0, 0));

		table = new GenericPropertyTable(model);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JPanel options = new JPanel(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("16dlu"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC, }));

		if (editable) {
			add(options, BorderLayout.WEST);
		}

		JButton btnAdd = new JButton();
		btnAdd.setBorderPainted(false);
		btnAdd.setIcon(new ImageIcon(GenericPropertyPanel.class.getResource("/hermes/browser/icons/recordAdd.png")));
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				model.addRow(new PropertyRow());
			}
		});
		options.add(btnAdd, "1, 2");

		JButton btnRemove = new JButton();
		btnRemove.setBorderPainted(false);
		btnRemove.setIcon(new ImageIcon(GenericPropertyPanel.class.getResource("/hermes/browser/icons/recordDelete.png")));
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int row = table.getSelectedRow();
				if (row >= 0) {
					model.removeRows(row);
					if (model.getRowCount() > row - 1) {
						table.getSelectionModel().setLeadSelectionIndex(row);
					} else if (table.getRowCount() > 0) {
						table.getSelectionModel().setLeadSelectionIndex(model.getRowCount() - 1);
					}
				}
			}
		});
		options.add(btnRemove, "1, 4");

		JButton btnUp = new JButton();
		btnUp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				if (row > 0 && model.getRowCount() > 1) {
					model.moveRow(row, row, row - 1);
					table.getSelectionModel().setLeadSelectionIndex(row - 1);

				}

			}
		});
		btnUp.setBorderPainted(false);
		btnUp.setIcon(new ImageIcon(GenericPropertyPanel.class.getResource("/hermes/browser/icons/moveUp.png")));
		options.add(btnUp, "1, 6");

		JButton btnDown = new JButton();
		btnDown.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				if (row >= 0 && model.getRowCount() > row + 1) {
					model.moveRow(row, row, row + 1);
					table.getSelectionModel().setLeadSelectionIndex(row + 1);
				}
			}
		});
		btnDown.setBorderPainted(false);
		btnDown.setIcon(new ImageIcon(GenericPropertyPanel.class.getResource("/hermes/browser/icons/moveDown.png")));
		options.add(btnDown, "1, 8");

		add(new JScrollPane(table), BorderLayout.CENTER);
	}
}
