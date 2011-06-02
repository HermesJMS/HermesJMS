package hermes.browser.components;

import hermes.MessageFactory;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.NamingException;

public interface EditedMessageHandler extends MessageListener, MessageFactory{
	public Queue createQueue(String named) throws JMSException, NamingException ;
	public Topic createTopic(String named) throws JMSException, NamingException ;
}
