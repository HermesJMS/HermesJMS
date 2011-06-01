package hermes.browser.dialog.message;

import hermes.util.JMSUtils;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class JMSHeaderPropertyPanel extends JPanel {
	private JTextField messageIdField;
	private JTextField replyToField;
	private JTextField correlationIDField;
	private JTextField destinationField;
	private JTextField typeField;
	private JTextField timestampField;
	private Message message;
	private JSpinner expirationSpinner;
	private JSpinner priroritySpinner;
	private JComboBox deliveryModeCombo;
	private JComboBox messageTypeCombo;
	private JCheckBox queueCheckBox;
	private JCheckBox topicCheckBox;

	private void syncFromMessage() throws JMSException {
		Calendar cal = Calendar.getInstance();
		DateFormat fmt = DateFormat.getDateInstance();

		queueCheckBox.setSelected(message.getJMSDestination() instanceof Queue) ;
		topicCheckBox.setSelected(message.getJMSDestination() instanceof Topic) ;
		messageIdField.setText(message.getJMSMessageID());
		replyToField.setText(JMSUtils.getDestinationName(message.getJMSReplyTo()));
		destinationField.setText(JMSUtils.getDestinationName(message.getJMSDestination()));
		correlationIDField.setText(message.getJMSCorrelationID());
		typeField.setText(message.getJMSType());
		timestampField.setText(fmt.format(new Date(message.getJMSTimestamp())));
		expirationSpinner.setValue(message.getJMSExpiration());
		priroritySpinner.setValue(message.getJMSPriority());
		deliveryModeCombo.setSelectedItem(message.getJMSDeliveryMode() == javax.jms.DeliveryMode.PERSISTENT ? DeliveryMode.PERSISTENT
				: DeliveryMode.NON_PERSISTENT);
		if (message instanceof TextMessage) {
			messageTypeCombo.setSelectedItem(MessageType.TextMessage);
		} else if (message instanceof BytesMessage) {
			messageTypeCombo.setSelectedItem(MessageType.BytesMessage);
		} else if (message instanceof ObjectMessage) {
			messageTypeCombo.setSelectedItem(MessageType.ObjectMessage);
		} else if (message instanceof MapMessage) {
			messageTypeCombo.setSelectedItem(MessageType.MapMessage);
		} else if (message instanceof StreamMessage) {
			messageTypeCombo.setSelectedItem(MessageType.StreamMessage);
		}
	}

	private void handleException(JMSException ex) {

	}

	/**
	 * Create the panel.
	 */
	public JMSHeaderPropertyPanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));

		JLabel jmsMessageIDLabel = new JLabel("JMS MessageID:");
		jmsMessageIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(jmsMessageIDLabel, "2, 4");

		messageIdField = new JTextField();
		messageIdField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					message.setJMSMessageID(messageIdField.getText());
				} catch (JMSException ex) {
					handleException(ex);
				}
			}
		});
		add(messageIdField, "4, 4, fill, default");
		messageIdField.setColumns(10);

		JLabel jmsDestinationLabel = new JLabel("JMS Destination:");
		jmsDestinationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(jmsDestinationLabel, "2, 6, right, default");

		destinationField = new JTextField();
		destinationField.setEditable(false);
		add(destinationField, "4, 6, fill, default");
		destinationField.setColumns(10);
		
		JPanel panel = new JPanel();
		add(panel, "4, 8, fill, fill");
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		queueCheckBox = new JCheckBox("Queue");
		panel.add(queueCheckBox);
		
		topicCheckBox = new JCheckBox("Topic");
		panel.add(topicCheckBox);

		JLabel jmsTimestampLabel = new JLabel("JMS Timestamp:");
		jmsTimestampLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(jmsTimestampLabel, "2, 10, right, default");

		timestampField = new JTextField();
		timestampField.setEditable(false);
		add(timestampField, "4, 10, fill, default");
		timestampField.setColumns(10);

		JLabel jmsReplyToLabel = new JLabel("JMS ReplyTo:");
		jmsReplyToLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(jmsReplyToLabel, "2, 12, right, default");

		replyToField = new JTextField();

		add(replyToField, "4, 12, fill, default");
		replyToField.setColumns(10);

		JLabel jmsTypeLabel = new JLabel("JMS Type:");
		add(jmsTypeLabel, "2, 14, right, default");

		typeField = new JTextField();
		typeField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					message.setJMSType(typeField.getText());
				} catch (JMSException ex) {
					handleException(ex);
				}
			}
		});
		add(typeField, "4, 14, fill, default");
		typeField.setColumns(10);

		JLabel jmsCorrelationIDLabel = new JLabel("JMS CorrelationID:");
		jmsCorrelationIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(jmsCorrelationIDLabel, "2, 16, right, default");

		correlationIDField = new JTextField();
		correlationIDField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					message.setJMSCorrelationID(correlationIDField.getText());
				} catch (JMSException ex) {
					handleException(ex);
				}
			}
		});
		add(correlationIDField, "4, 16, fill, default");
		correlationIDField.setColumns(10);

		JLabel jmsDeliveryModeLabel = new JLabel("JMS DeliveryMode:");
		add(jmsDeliveryModeLabel, "2, 18, right, default");

		deliveryModeCombo = new JComboBox();
		deliveryModeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (deliveryModeCombo.getSelectedItem().equals(DeliveryMode.NON_PERSISTENT)) {
						message.setJMSDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
					} else {
						message.setJMSDeliveryMode(javax.jms.DeliveryMode.PERSISTENT);
					}
				} catch (JMSException ex) {
					handleException(ex);
				}
			}
		});
		deliveryModeCombo.setModel(new DefaultComboBoxModel(DeliveryMode.values()));
		add(deliveryModeCombo, "4, 18, fill, default");

		JLabel jmsPriorityLabel = new JLabel("JMS Priority:");
		add(jmsPriorityLabel, "2, 20, right, default");

		priroritySpinner = new JSpinner();
		priroritySpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				try {
					message.setJMSPriority((Integer) priroritySpinner.getValue()) ;
				} catch (JMSException ex) {
					handleException(ex);
				}
			}
		});
		priroritySpinner.setModel(new SpinnerNumberModel(4, 0, 9, 1));
		add(priroritySpinner, "4, 20");

		JLabel jmsExpirationLabel = new JLabel("JMS Expiration:");
		add(jmsExpirationLabel, "2, 22, right, default");

		expirationSpinner = new JSpinner();
		expirationSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				try {
					message.setJMSExpiration((Long) expirationSpinner.getValue()) ;
				} catch (JMSException ex) {
					handleException(ex);
				}
			}
		});
		
		expirationSpinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		add(expirationSpinner, "4, 22");

		JLabel messageTypeLabel = new JLabel("Message Type:");
		add(messageTypeLabel, "2, 24, right, default");

		messageTypeCombo = new JComboBox();
		messageTypeCombo.setModel(new DefaultComboBoxModel(MessageType.values()));
		add(messageTypeCombo, "4, 24, fill, default");

	}

	public Message getMessage() {
		return message ;
	}
	public void setMessage(Message message) throws JMSException {
		this.message = message;
		syncFromMessage();
	}

}
