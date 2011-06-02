package hermes.browser.actions;

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.components.EditedMessageHandler;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.NamingException;

public abstract class AbstractEditedMessageHandler implements EditedMessageHandler {
	private Hermes hermes;

	public AbstractEditedMessageHandler(Hermes hermes) {
		this.hermes = hermes;
	}

	public Hermes getHermes() {
		return hermes ;
	}
	@Override
	public abstract void onMessage(Message message) ;

	@Override
	public BytesMessage createBytesMessage() throws JMSException {
		return getHermes().createBytesMessage();
	}

	@Override
	public MapMessage createMapMessage() throws JMSException {
		return getHermes().createMapMessage();
	}

	@Override
	public ObjectMessage createObjectMessage() throws JMSException {
		return getHermes().createObjectMessage();
	}

	@Override
	public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
		return getHermes().createObjectMessage(object);
	}

	@Override
	public StreamMessage createStreamMessage() throws JMSException {
		return getHermes().createStreamMessage();
	}

	@Override
	public TextMessage createTextMessage() throws JMSException {
		return getHermes().createTextMessage();
	}

	@Override
	public TextMessage createTextMessage(String text) throws JMSException {
		return getHermes().createTextMessage(text);
	}

	@Override
	public Message createMessage() throws JMSException {
		return getHermes().createMessage();
	}

	@Override
	public Destination getDestination(String name, Domain domain) throws JMSException, NamingException {
		return getHermes().getDestination(name, domain);
	}

	@Override
	public String getDestinationName(Destination to) throws JMSException {
		return getHermes().getDestinationName(to);
	}

	@Override
	public Queue createQueue(String named) throws JMSException, NamingException {
		return getHermes().createQueue(named);
	}

	@Override
	public Topic createTopic(String named) throws JMSException, NamingException {
		return getHermes().createTopic(named);
	}
}
