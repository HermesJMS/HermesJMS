package hermes.browser.dialog.message;

import hermes.swing.PropertyRow;
import hermes.swing.PropertyType;
import hermes.util.TextUtils;

import java.awt.BorderLayout;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.swing.JLabel;

import com.jgoodies.forms.factories.Borders;

public class MapMessagePayloadPanel extends MessageWriter {

	private PanelImpl panel;

	public MapMessagePayloadPanel() throws JMSException {
		this(null, true);
	}

	public MapMessagePayloadPanel(MapMessage message, boolean editable) throws JMSException {
		panel = new PanelImpl(message, editable);
		setLayout(new BorderLayout());
		if (false) {
			JLabel label = new JLabel("Edit the MapMessage");
			label.setBorder(Borders.EMPTY_BORDER);
			add(label, BorderLayout.NORTH);

		}
		add(panel, BorderLayout.CENTER);
	}

	class PanelImpl extends GenericPropertyPanel {
		public PanelImpl(MapMessage message, boolean editable) throws JMSException {
			super(editable);

			if (message != null) {
				for (Enumeration<String> e = message.getMapNames(); e.hasMoreElements();) {
					PropertyRow row = new PropertyRow();
					row.name = e.nextElement();
					row.value = message.getObject(row.name);
					row.type = PropertyType.fromObject(row.value);
					model.addRow(row);
				}
			}
		}

		public void setProperties(MapMessage message) throws NumberFormatException, JMSException {
			for (int i = 0; i < model.getRowCount(); i++) {
				PropertyRow row = model.getRow(i);
				if (!TextUtils.isEmpty(row.name)) {
					switch (row.type) {
					case INT:
						message.setInt(row.name, Integer.decode(row.value.toString()));
						break;
					case DOUBLE:
						message.setDouble(row.name, Double.parseDouble(row.value.toString()));
						break;
					case LONG:
						message.setLong(row.name, Long.decode(row.value.toString()));
						break;
					case BOOLEAN:
						message.setBoolean(row.name, Boolean.parseBoolean(row.value.toString()));
						break;
					case STRING:
						message.setString(row.name, row.value.toString());
						break;
					case CHAR:
						message.setChar(row.name, row.value.toString().charAt(0));
						break;
					case BYTE:
						message.setByte(row.name, Byte.parseByte(row.value.toString()));
						break;
					}
				}
			}
		}
	}

	@Override
	void onMessage(Message message) throws JMSException {
		panel.setProperties((MapMessage) message);
	}

	@Override
	boolean supports(MessageType type) {
		return type == MessageType.MapMessage;
	}
}
