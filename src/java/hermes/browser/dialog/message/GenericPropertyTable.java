package hermes.browser.dialog.message;

import hermes.swing.PropertyRow;
import hermes.swing.PropertyTableModel;
import hermes.swing.PropertyType;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

public class GenericPropertyTable extends JTable {

	public GenericPropertyTable(final PropertyTableModel model) {
		setModel(model) ;
		
		TableColumn typeColumn = getColumnModel().getColumn(1) ;
		DefaultComboBoxModel typeComboModel = new DefaultComboBoxModel(PropertyType.values()) ;
		final JComboBox typeCombo = new JComboBox(typeComboModel) ;
		
		typeColumn.setCellEditor(new DefaultCellEditor(typeCombo)) ;
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				PropertyRow row = model.getRow(getSelectedRow()) ;
				typeCombo.setSelectedItem(row.type) ;
			}
		}) ;
	}
}
