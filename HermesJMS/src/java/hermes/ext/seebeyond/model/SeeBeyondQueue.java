/**
 * 
 */
package hermes.ext.seebeyond.model;

import java.util.Properties;

/**
 * @author siddavatamm
 *
 */
public class SeeBeyondQueue {
	public static final String QUEUE_NAME_KEY = "QUEUE_NAME";
	public static final String CURRENT_RECEIVERS_KEY = "CURRENT_RECEIVERS";
	public static final String FIRST_ENQUEUE_TIME_KEY = "FIRST_ENQUEUE_TIME";
	public static final String LAST_ENQUEUE_TIME_KEY = "LAST_ENQUEUE_TIME";
	public static final String MIN_SEQ_KEY = "MIN_SEQ";
	public static final String MAX_SEQ_KEY = "MAX_SEQ";
	public static final String MESSAGE_COUNT_KEY = "MESSAGE_COUNT";
	
	private String queueName;
	private String currentReceivers;
	private String firstEnqueueTime;
	private String lastEnqueueTime;
	private String minSequence;
	private String maxSequence;
	private String messageCount;
	
	public SeeBeyondQueue(String queueName)
	{
		this.queueName = queueName;
	}

	public SeeBeyondQueue(Properties stats)
	{
		this.queueName = stats.getProperty(QUEUE_NAME_KEY);
		this.currentReceivers = stats.getProperty(CURRENT_RECEIVERS_KEY);
		this.firstEnqueueTime = stats.getProperty(FIRST_ENQUEUE_TIME_KEY);
		this.lastEnqueueTime = stats.getProperty(LAST_ENQUEUE_TIME_KEY);
		this.minSequence = stats.getProperty(MIN_SEQ_KEY);
		this.maxSequence = stats.getProperty(MAX_SEQ_KEY);
		this.messageCount = stats.getProperty(MESSAGE_COUNT_KEY);
	}

	public String getCurrentReceivers() {
		return currentReceivers;
	}

	public void setCurrentReceivers(String currentReceivers) {
		this.currentReceivers = currentReceivers;
	}

	public String getFirstEnqueueTime() {
		return firstEnqueueTime;
	}

	public void setFirstEnqueueTime(String firstEnqueueTime) {
		this.firstEnqueueTime = firstEnqueueTime;
	}

	public String getLastEnqueueTime() {
		return lastEnqueueTime;
	}

	public void setLastEnqueueTime(String lastEnqueueTime) {
		this.lastEnqueueTime = lastEnqueueTime;
	}

	public String getMaxSequence() {
		return maxSequence;
	}

	public void setMaxSequence(String maxSequence) {
		this.maxSequence = maxSequence;
	}

	public String getMinSequence() {
		return minSequence;
	}

	public void setMinSequence(String minSequence) {
		this.minSequence = minSequence;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(String messageCount) {
		this.messageCount = messageCount;
	}

}
