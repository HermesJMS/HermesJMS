package hermes.browser.dialog.message;

import hermes.swing.PropertyRow;
import hermes.swing.PropertyType;
import hermes.util.TextUtils;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;

public class UserHeaderPropertyPanel extends GenericPropertyPanel {

	public UserHeaderPropertyPanel(Message message) throws JMSException {
		super();

		if (message != null) {
			for (Enumeration<String> e = message.getPropertyNames(); e.hasMoreElements();) {
				PropertyRow row = new PropertyRow();
				row.name = e.nextElement();
				row.value = message.getObjectProperty(row.name);
				row.type = PropertyType.fromObject(row.value);
				model.addRow(row);
			}
		}
	}

	public void setProperties(Message message) throws NumberFormatException, JMSException {
		for (int i = 0; i < model.getRowCount(); i++) {
			PropertyRow row = model.getRow(i);
			if (!TextUtils.isEmpty(row.name)) {
				switch (row.type) {
				case INT:
					message.setIntProperty(row.name, Integer.decode(row.value.toString()));
					break;
				case DOUBLE:
					message.setDoubleProperty(row.name, Double.parseDouble(row.value.toString()));
					break;
				case LONG:
					message.setLongProperty(row.name, Long.decode(row.value.toString()));
					break;
				case BOOLEAN:
					message.setBooleanProperty(row.name, Boolean.parseBoolean(row.value.toString()));
					break;
				case STRING:
					message.setStringProperty(row.name, row.value.toString());
					break;
				case BYTE:
					message.setByteProperty(row.name, Byte.parseByte(row.value.toString()));
					break;
				}
			}
		}
	}
}
