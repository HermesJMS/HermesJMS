package hermes.browser.dialog.message;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTable;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;

public class UserHeaderPropertyPanel extends JPanel {
	private JTable table;

	/**
	 * Create the panel.
	 */
	public UserHeaderPropertyPanel() {
		setLayout(new BorderLayout(0, 0));
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null},
			},
			new String[] {
				"Name", "Type", "Value"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class, Object.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		add(table, BorderLayout.CENTER);

	}

}
