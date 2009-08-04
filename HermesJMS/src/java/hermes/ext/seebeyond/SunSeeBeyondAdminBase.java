/**
 * 
 */
package hermes.ext.seebeyond;

import hermes.HermesException;

import java.util.List;
import java.util.Properties;

import javax.management.MBeanException;

/**
 * @author murali
 *
 */
public interface SunSeeBeyondAdminBase {
	
	public static final int SERVER_TYPE = 0; //Zero implies not to deal with journalled messages.
	public static final String MESSAGE_COUNT_KEY = "MESSAGE_COUNT";
	public static final String MESSAGE_ID_KEY = "Message.JMSProperty.MI";
	
    public List getTopics()
        throws HermesException;

    public List getQueues()
        throws HermesException;

    public Properties getTopicMsgProperties(String topicName, String messageID)
        throws HermesException;

    public Properties getQueueMsgProperties(String queueName, String messageID)
        throws HermesException;
    
    public List getTopicMsgPropertiesList(String topicName, long startIndex, long numberOfMessages, int serverType)
    	throws HermesException;

    public List getQueueMsgPropertiesList(String queueName, long startIndex, long numberOfMessages, int serverType)
		throws HermesException;
    
    public List getSubscribers(String topicName)
        throws HermesException;

    public List getReceivers(String queueName)
        throws HermesException;

    public boolean changeTopicTextMessage(String topicName, String messageID, String body)
        throws HermesException;

    public boolean changeQueueTextMessage(String queueName, String messageID, String body)
        throws HermesException;

    public boolean deleteTopicMessage(String topicName, String messageID)
        throws HermesException;

    public boolean deleteQueueMessage(String queueName, String messageID)
        throws HermesException;

    public boolean suspendTopic(String topicName)
        throws HermesException;

    public boolean suspendQueue(String queueName)
        throws HermesException;

    public boolean resumeTopic(String topicName)
        throws HermesException;

    public boolean resumeQueue(String queueName)
        throws HermesException;

    public boolean createTopic(String topicName)
        throws HermesException;

    public boolean createQueue(String queueName)
        throws HermesException;

    public boolean deleteTopic(String topicName)
        throws HermesException;

    public boolean deleteQueue(String queueName)
        throws HermesException;
    
    /**
     * 
     * @param msgType javax.jms.TextMessage or javax.jms.BytesMessage
     * @param destinationType javax.jms.Queue or javax.jms.Topic
     * @param destinationName The name of a topic or queue
     * @param messageData The message payload, String for javax.jms.TextMessage or byte[] for javax.jms.BytesMessage
     * @param messagePriority The message prioriy (null if default is used)
     * @param messageDeliveryMode The message deliverymode (null if default is used)
     * @param messageTimeToLive The message expiration time (null if default is used)
     * @throws MBeanException
     * @throws NotSupportedException
     */
    public void submitNewMessage(String msgType, String destinationType, String destinationName, Object messageData, int messagePriority, int messageDeliveryMode, long messageTimeToLive)
        throws HermesException;

    public boolean createTopicDurableSubscriber(String topicName, String subscriberName)
        throws HermesException;

    public boolean unsubscribeDurableSubscriber(String topicName, String subscriberName)
        throws HermesException;

    public Properties getServerStatus()
        throws HermesException;

    public Properties getTopicStatistics(String topicName)
        throws HermesException;

    public Properties getQueueStatistics(String queueName)
        throws HermesException;

    public boolean isServerReady()
        throws HermesException;

    public boolean republishTopicMessage(String topicName, String messageID, String body)
        throws HermesException;

    public boolean resendQueueMessage(String queueName, String messageID, String body)
        throws HermesException;
}
