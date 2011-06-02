package hermes.swing;

import java.util.Arrays;

public class PropertyTableModel extends RowTableModel<PropertyRow> {

	public PropertyTableModel() {
		super(Arrays.asList("Name", "Type", "Value"));
	}

	@Override
	public Object getValueAt(int y, int x) {
		PropertyRow row = getRow(y);
		switch (x) {
		case 0:
			return row.name;
		case 1:
			return row.renderType();
		case 2:
			return row.value;
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		PropertyRow row = getRow(rowIndex) ;
		switch (columnIndex) {
		case 0:
			row.name = aValue.toString() ;
			break;
		case 1:
			row.type = PropertyType.fromString(aValue.toString()) ;
			break ;
		case 2:
			row.value = aValue ;
			break ;
		}
	}
}
