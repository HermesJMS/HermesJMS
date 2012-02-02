package hermes.browser.dialog.message;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.swing.JLabel;

public class CannotEditMessageWriter extends MessageWriter {
	private Message message;

	public CannotEditMessageWriter() {
		this(null) ;
	}
	public CannotEditMessageWriter(Message message) {
		this.message = message ;
		add(new JLabel("Payload not editable")) ;
	}
	@Override
	void onMessage(Message newMessage) throws JMSException {		
		if (message != null && message instanceof ObjectMessage && newMessage instanceof ObjectMessage) {
			((ObjectMessage) newMessage).setObject(((ObjectMessage) message).getObject()) ;
		}
	}

	@Override
	boolean supports(MessageType type) {
		return type == MessageType.ObjectMessage || type == MessageType.StreamMessage || type == MessageType.Message ;
	}
}
