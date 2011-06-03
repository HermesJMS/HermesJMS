package hermes.browser.dialog.message;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JPanel;

public abstract class MessageWriter extends JPanel{
	abstract void onMessage(Message message) throws JMSException ;
	abstract boolean supports(MessageType type) ;
}
