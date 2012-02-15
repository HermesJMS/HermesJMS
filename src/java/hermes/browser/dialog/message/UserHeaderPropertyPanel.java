package hermes.browser.dialog.message;

import hermes.browser.HermesBrowser;
import hermes.config.HermesConfig;
import hermes.swing.PropertyRow;
import hermes.swing.PropertyType;
import hermes.util.TextUtils;

import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Logger;

public class UserHeaderPropertyPanel extends GenericPropertyPanel {
	private static final Logger log = Logger.getLogger(UserHeaderPropertyPanel.class);

	public UserHeaderPropertyPanel(Message message) throws JMSException {
		super(true);

		if (message != null) {
			HermesConfig config = HermesBrowser.getBrowser().getConfig() ;
			for (Enumeration<String> e = message.getPropertyNames(); e.hasMoreElements();) {
				PropertyRow row = new PropertyRow();
				row.name = e.nextElement();
				row.value = message.getObjectProperty(row.name);
				row.type = PropertyType.fromObject(row.value);
				
				if (config.isCopyJMSProviderProperties() || !row.name.startsWith("JMS")) {
					model.addRow(row);
				}
			}
		}
	}

	public void setProperties(Message message) throws NumberFormatException, JMSException {
		for (int i = 0; i < model.getRowCount(); i++) {
			PropertyRow row = model.getRow(i);
			if (!TextUtils.isEmpty(row.name)) {
				switch (row.type) {
				case INT:
					message.setIntProperty(row.name, row.value == null ? null : Integer.decode(row.value.toString()));
					break;
				case DOUBLE:
					message.setDoubleProperty(row.name, row.value == null ? null : Double.parseDouble(row.value.toString()));
					break;
				case LONG:
					message.setLongProperty(row.name, row.value == null ? null : Long.decode(row.value.toString()));
					break;
				case BOOLEAN:
					message.setBooleanProperty(row.name, row.value == null ? null : Boolean.parseBoolean(row.value.toString()));
					break;
				case CHAR:
					message.setStringProperty (row.name, row.value == null ? null : row.value.toString());
					break;
				case STRING:
					message.setStringProperty(row.name, row.value == null ? null : row.value.toString());
					break;
				case BYTE:
					message.setByteProperty(row.name, row.value == null ? null : Byte.parseByte(row.value.toString()));
					break;
				}
			}
		}
	}
}
