/**
 * 
 */
package hermes.ext.seebeyond;

import hermes.HermesException;

import java.util.List;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * @author murali
 * 
 */
public abstract class SunSeeBeyondAbstractAdmin implements SunSeeBeyondAdminBase {

	protected MBeanServerConnection mConn;

	protected ObjectName seeBeyondIQManager;

	protected Object invoke(String operationName, Object[] params, String[] signatures) throws HermesException {
		try {
			return mConn.invoke(seeBeyondIQManager, operationName, params, signatures);
		} catch (Exception e) {
			throw new HermesException(e.getMessage(), e);
		}
	}

	
	public List getTopics() throws HermesException {

		return (List) invoke("getTopics", null, null);
	}

	public List getQueues() throws HermesException {

		return (List) invoke("getQueues", null, null);
	}

	public Properties getTopicMsgProperties(String topicName, String messageID) throws HermesException {

		return (Properties) invoke("getTopicMsgProperties", new String[] { topicName, messageID }, new String[] {
				"java.lang.String", "java.lang.String" });
	}

	public Properties getQueueMsgProperties(String queueName, String messageID) throws HermesException {

		return (Properties) invoke("getQueueMsgProperties", new String[] { queueName, messageID }, new String[] {
				"java.lang.String", "java.lang.String" });
	}
	
	public List getTopicMsgPropertiesList(String topicName, long startIndex, long numberOfMessages, int serverType) throws HermesException {
		return (List) invoke("getTopicMsgPropertiesList", new Object[] { topicName, new Long(startIndex), new Long(numberOfMessages), new Integer(serverType) }
		, new String[] { "java.lang.String","java.lang.Long","java.lang.Long","java.lang.Integer" });
	}

	public List getQueueMsgPropertiesList(String queueName, long startIndex, long numberOfMessages, int serverType) throws HermesException {
		return (List) invoke("getQueueMsgPropertiesList", new Object[] { queueName, new Long(startIndex), new Long(numberOfMessages), new Integer(serverType) }
		, new String[] { "java.lang.String","java.lang.Long","java.lang.Long","java.lang.Integer" });
	}
	

	public List getSubscribers(String topicName) throws HermesException {

		return (List) invoke("getSubscribers", new String[] { topicName }, new String[] { "java.lang.String" });
	}

	public List getReceivers(String queueName) throws HermesException {

		return (List) invoke("getReceivers", new String[] { queueName }, new String[] { "java.lang.String" });
	}

	public boolean changeTopicTextMessage(String topicName, String messageID, String body) throws HermesException {

		Boolean success = (Boolean) invoke("changeTopicTextMessage", new String[] { topicName, messageID, body },
				new String[] { "java.lang.String", "java.lang.String", "java.lang.String" });
		return success.booleanValue();
	}

	public boolean changeQueueTextMessage(String queueName, String messageID, String body) throws HermesException {
		Boolean success = (Boolean) invoke("changeQueueTextMessage", new String[] { queueName, messageID, body },
				new String[] { "java.lang.String", "java.lang.String", "java.lang.String" });
		return success.booleanValue();
	}

	public boolean deleteTopicMessage(String topicName, String messageID) throws HermesException {
		Boolean success = (Boolean) invoke("deleteTopicMessage", new String[] { topicName, messageID }, new String[] {
				"java.lang.String", "java.lang.String" });
		return success.booleanValue();
	}

	public boolean deleteQueueMessage(String queueName, String messageID) throws HermesException {
		Boolean success = (Boolean) invoke("deleteQueueMessage", new String[] { queueName, messageID }, new String[] {
				"java.lang.String", "java.lang.String" });
		return success.booleanValue();
	}

	public boolean suspendTopic(String topicName) throws HermesException {
		Boolean success = (Boolean) invoke("suspendTopic", new String[] { topicName },
				new String[] { "java.lang.String" });
		return success.booleanValue();
	}

	public boolean suspendQueue(String queueName) throws HermesException {
		Boolean success = (Boolean) invoke("suspendQueue", new String[] { queueName },
				new String[] { "java.lang.String" });
		return success.booleanValue();
	}

	public boolean resumeTopic(String topicName) throws HermesException {
		Boolean success = (Boolean) invoke("resumeTopic", new String[] { topicName },
				new String[] { "java.lang.String" });
		return success.booleanValue();
	}

	public boolean resumeQueue(String queueName) throws HermesException {
		Boolean success = (Boolean) invoke("resumeQueue", new String[] { queueName },
				new String[] { "java.lang.String" });
		return success.booleanValue();
	}

	public boolean createTopic(String topicName) throws HermesException {
		Boolean success = (Boolean) invoke("createTopic", new String[] { topicName },
				new String[] { "java.lang.String" });
		return success.booleanValue();
	}

	public boolean createQueue(String queueName) throws HermesException {
		Boolean success = (Boolean) invoke("createQueue", new String[] { queueName },
				new String[] { "java.lang.String" });
		return success.booleanValue();
	}

	public boolean deleteTopic(String topicName) throws HermesException {
		Boolean success = (Boolean) invoke("deleteTopic", new String[] { topicName },
				new String[] { "java.lang.String" });
		return success.booleanValue();
	}

	public boolean deleteQueue(String queueName) throws HermesException {
		Boolean success = (Boolean) invoke("deleteQueue", new String[] { queueName },
				new String[] { "java.lang.String" });
		return success.booleanValue();
	}

	public void submitNewMessage(String msgType, String destinationType, String destinationName, Object messageData,
			int messagePriority, int messageDeliveryMode, long messageTimeToLive) throws HermesException {
		invoke("submitNewMessage", new Object[] { msgType, destinationType, destinationName, messageData,
				new Integer(messagePriority), new Integer(messageDeliveryMode), new Long(messageTimeToLive) }, new String[] { "java.lang.String",
				"java.lang.String", "java.lang.String", "java.lang.Object"
				, "java.lang.Integer", "java.lang.Integer", "java.lang.Long" });

	}

	public boolean createTopicDurableSubscriber(String topicName, String subscriberName) throws HermesException {
		Boolean success = (Boolean) invoke("createTopicDurableSubscriber", new String[] { topicName, subscriberName },
				new String[] { "java.lang.String", "java.lang.String" });
		return success.booleanValue();
	}

	public boolean unsubscribeDurableSubscriber(String topicName, String subscriberName) throws HermesException {
		Boolean success = (Boolean) invoke("unsubscribeDurableSubscriber", new String[] { topicName, subscriberName },
				new String[] { "java.lang.String", "java.lang.String" });
		return success.booleanValue();
	}

	public Properties getServerStatus() throws HermesException {
		return (Properties) invoke("getServerStatus", null, null);
	}

	public Properties getTopicStatistics(String topicName) throws HermesException {
		return (Properties) invoke("getTopicStatistics", new String[] { topicName },
				new String[] { "java.lang.String" });
	}

	public Properties getQueueStatistics(String queueName) throws HermesException {
		return (Properties) invoke("getQueueStatistics", new String[] { queueName },
				new String[] { "java.lang.String" });
	}

	public boolean isServerReady() throws HermesException {
		Boolean isReady = (Boolean) invoke("getServerStatus", null, null);
		return isReady.booleanValue();
	}

	public boolean republishTopicMessage(String topicName, String messageID, String body) throws HermesException {
		Boolean success = (Boolean) invoke("republishTopicMessage", new String[] { topicName, messageID, body },
				new String[] { "java.lang.String", "java.lang.String", "java.lang.String" });
		return success.booleanValue();
	}

	public boolean resendQueueMessage(String queueName, String messageID, String body) throws HermesException {
		Boolean success = (Boolean) invoke("resendQueueMessage", new String[] { queueName, messageID, body },
				new String[] { "java.lang.String", "java.lang.String", "java.lang.String" });
		return success.booleanValue();
	}

}
