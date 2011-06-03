package hermes.browser.dialog.message;

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.components.EditedMessageHandler;
import hermes.util.JMSUtils;
import hermes.util.TextUtils;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.NamingException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
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

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.time.FastDateFormat;


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
	private JComboBox messageTypeCombo;
	private JCheckBox queueCheckBox;
	private JCheckBox topicCheckBox;

	private void syncFromMessage() throws JMSException {
		Calendar cal = Calendar.getInstance();
		FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSSZ") ;

		queueCheckBox.setSelected(message.getJMSDestination() instanceof Queue);
		topicCheckBox.setSelected(message.getJMSDestination() instanceof Topic);
		messageIdField.setText(message.getJMSMessageID());
		replyToField.setText(JMSUtils.getDestinationName(message.getJMSReplyTo()));
		destinationField.setText(JMSUtils.getDestinationName(message.getJMSDestination()));
		correlationIDField.setText(message.getJMSCorrelationID());
		typeField.setText(message.getJMSType());
		timestampField.setText(fmt.format(new Date(message.getJMSTimestamp())));
		expirationSpinner.setValue(message.getJMSExpiration());
		priroritySpinner.setValue(message.getJMSPriority());
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

	public Message createMessage(EditedMessageHandler handler) throws JMSException, NamingException {
		MessageType messageType = (MessageType) messageTypeCombo.getSelectedItem();
		Message message = null;
		switch (messageType) {
		case TextMessage:
			message = handler.createTextMessage();
			break;
		default:
			throw new JMSException("Unsupported message type " + messageType);
		}

		if (!TextUtils.isEmpty(replyToField.getText())) {
			message.setJMSReplyTo(queueCheckBox.isSelected() ? handler.createQueue(replyToField.getText()) : handler.createTopic(replyToField.getText()));
		}

		if (!TextUtils.isEmpty(typeField.getText())) {
			message.setJMSType(typeField.getText());
		}

		if (this.message != null) {
//			message.setJMSMessageID(this.message.getJMSMessageID());
//			message.setJMSTimestamp(this.message.getJMSTimestamp());
			message.setJMSDestination(this.message.getJMSDestination());
		}
		if (!TextUtils.isEmpty(correlationIDField.getText())) {
			message.setJMSCorrelationID(correlationIDField.getText());
		}

		message.setJMSPriority(((SpinnerNumberModel) priroritySpinner.getModel()).getNumber().intValue()) ;
		message.setJMSExpiration(((SpinnerNumberModel) expirationSpinner.getModel()).getNumber().longValue()) ;

		return message;
	}

	private void handleException(JMSException ex) {

	}

	/**
	 * Create the panel.
	 */
	public JMSHeaderPropertyPanel() {
		setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

		JLabel jmsMessageIDLabel = new JLabel("JMS MessageID:");
		jmsMessageIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(jmsMessageIDLabel, "2, 4");

		messageIdField = new JTextField();
		messageIdField.setEditable(false);
		add(messageIdField, "4, 4, fill, default");
		messageIdField.setColumns(10);

		JLabel jmsDestinationLabel = new JLabel("JMS Destination:");
		jmsDestinationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(jmsDestinationLabel, "2, 6, right, default");

		destinationField = new JTextField();
		destinationField.setEditable(false);
		add(destinationField, "4, 6, fill, default");
		destinationField.setColumns(10);

		JLabel jmsTimestampLabel = new JLabel("JMS Timestamp:");
		jmsTimestampLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(jmsTimestampLabel, "2, 8, right, default");

		timestampField = new JTextField();
		timestampField.setEditable(false);
		add(timestampField, "4, 8, fill, default");
		timestampField.setColumns(10);

		JLabel jmsReplyToLabel = new JLabel("JMS ReplyTo:");
		jmsReplyToLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(jmsReplyToLabel, "2, 10, right, default");

		replyToField = new JTextField();

		add(replyToField, "4, 10, fill, default");
		replyToField.setColumns(10);

		JPanel panel = new JPanel();
		add(panel, "4, 12, fill, fill");
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		queueCheckBox = new JCheckBox("Queue");
		queueCheckBox.setSelected(true);
		queueCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				topicCheckBox.setSelected(!queueCheckBox.isSelected());
			}
		});
		panel.add(queueCheckBox);

		topicCheckBox = new JCheckBox("Topic");
		topicCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				queueCheckBox.setSelected(!topicCheckBox.isSelected());
			}
		});
		panel.add(topicCheckBox);

		JLabel jmsTypeLabel = new JLabel("JMS Type:");
		add(jmsTypeLabel, "2, 14, right, default");

		typeField = new JTextField();
		add(typeField, "4, 14, fill, default");
		typeField.setColumns(10);

		JPanel cpanel = new JPanel();
		cpanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 5));

		JLabel jmsCorrelationIDLabel = new JLabel("JMS CorrelationID:");
		jmsCorrelationIDLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		add(jmsCorrelationIDLabel, "2, 16, right, default");

		correlationIDField = new JTextField();
		JButton generateCorrelationIdButton = new JButton("Generate") ;
		generateCorrelationIdButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				correlationIDField.setText(UUID.randomUUID().toString()) ;
			}
		}) ;
		cpanel.add(correlationIDField) ;
		cpanel.add(generateCorrelationIdButton) ;
		add(cpanel, "4, 16, fill, default");
		correlationIDField.setColumns(30);

//		JLabel jmsDeliveryModeLabel = new JLabel("JMS DeliveryMode:");
//		add(jmsDeliveryModeLabel, "2, 18, right, default");
//
//		deliveryModeCombo = new JComboBox();
//		deliveryModeCombo.setModel(new DefaultComboBoxModel(DeliveryMode.values()));
//		deliveryModeCombo.setEditable(false);
//		add(deliveryModeCombo, "4, 18, fill, default");

		JLabel jmsPriorityLabel = new JLabel("JMS Priority:");
		add(jmsPriorityLabel, "2, 18, right, default");

		priroritySpinner = new JSpinner();
		priroritySpinner.setModel(new SpinnerNumberModel(new Integer(4), new Integer(0), new Integer(9), new Integer(1)));
		add(priroritySpinner, "4, 18");

		JLabel jmsExpirationLabel = new JLabel("JMS Expiration:");
		add(jmsExpirationLabel, "2, 20, right, default");

		expirationSpinner = new JSpinner();

		expirationSpinner.setModel(new SpinnerNumberModel(new Long(0), new Long(0), null, new Long(1)));
		add(expirationSpinner, "4, 20");

		JLabel messageTypeLabel = new JLabel("Message Type:");
		add(messageTypeLabel, "2, 22, right, default");

		messageTypeCombo = new JComboBox();
		messageTypeCombo.setModel(new DefaultComboBoxModel(MessageType.values()));
		add(messageTypeCombo, "4, 22, fill, default");

	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) throws JMSException {
		this.message = message;
		syncFromMessage();
	}

	public void setMessage(Message message, String destinationName, Domain domain) throws JMSException {
		this.message = message;
		if (message != null) {
			syncFromMessage();
		}
		if (destinationName != null) {
			this.destinationField.setText(destinationName);
		}
	}

}
