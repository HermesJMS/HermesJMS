/**
 * 
 */
package hermes.ext.seebeyond.model;

import java.util.Properties;

/**
 * @author siddavatamm
 *
 */
public class SeeBeyondTopic {
	
	public static final String TOPIC_NAME_KEY = "TOPIC_NAME";
	public static final String CURRENT_SUBSCRIBERS_KEY = "CURRENT_SUBSCRIBERS";
	public static final String TOTAL_SUBSCRIBERS_KEY = "TOTAL_SUBSCRIBERS";
	public static final String LOWEST_SUBSCRIBER_SEQ_KEY = "LOWEST_SUBSCRIBER_SEQ";
	public static final String HIGHEST_SUBSCRIBER_SEQ_KEY = "HIGHEST_SUBSCRIBER_SEQ";
	public static final String FIRST_ENQUEUE_TIME_KEY = "FIRST_ENQUEUE_TIME";
	public static final String LAST_ENQUEUE_TIME_KEY = "LAST_ENQUEUE_TIME";
	public static final String FIRST_SEQ_KEY = "FIRST_SEQ";
	public static final String LAST_SEQ_KEY = "LAST_SEQ";
	public static final String MESSAGE_COUNT_KEY = "MESSAGE_COUNT";
	
	private String topicName;
	private String currentSubscribers;
	private String totalSubscribers;
	private String lowestSubscriberSequence;
	private String highestSubscriberSequence;
	private String firstEnqueueTime;
	private String lastEnqueueTime;
	private String firstSequence;
	private String lastSequence;
	private String messageCount;
	
	public SeeBeyondTopic(String topicName)
	{
		this.topicName = topicName;
	}

	public SeeBeyondTopic(Properties stats)
	{
		this.topicName = stats.getProperty(TOPIC_NAME_KEY);
		this.currentSubscribers = stats.getProperty(CURRENT_SUBSCRIBERS_KEY);
		this.totalSubscribers = stats.getProperty(TOTAL_SUBSCRIBERS_KEY);
		this.lowestSubscriberSequence = stats.getProperty(LOWEST_SUBSCRIBER_SEQ_KEY);
		this.highestSubscriberSequence = stats.getProperty(HIGHEST_SUBSCRIBER_SEQ_KEY);
		this.firstEnqueueTime = stats.getProperty(FIRST_ENQUEUE_TIME_KEY);
		this.lastEnqueueTime = stats.getProperty(LAST_ENQUEUE_TIME_KEY);
		this.firstSequence = stats.getProperty(FIRST_SEQ_KEY);
		this.lastSequence = stats.getProperty(LAST_SEQ_KEY);
		this.messageCount = stats.getProperty(MESSAGE_COUNT_KEY);
	}

	
	public String getCurrentSubscribers() {
		return currentSubscribers;
	}
	public void setCurrentSubscribers(String currentSubscribers) {
		this.currentSubscribers = currentSubscribers;
	}
	public String getFirstEnqueueTime() {
		return firstEnqueueTime;
	}
	public void setFirstEnqueueTime(String firstEnqueueTime) {
		this.firstEnqueueTime = firstEnqueueTime;
	}
	public String getFirstSequence() {
		return firstSequence;
	}
	public void setFirstSequence(String firstSequence) {
		this.firstSequence = firstSequence;
	}
	public String getHighestSubscriberSequence() {
		return highestSubscriberSequence;
	}
	public void setHighestSubscriberSequence(String highestSubscriberSequence) {
		this.highestSubscriberSequence = highestSubscriberSequence;
	}
	public String getLastEnqueueTime() {
		return lastEnqueueTime;
	}
	public void setLastEnqueueTime(String lastEnqueueTime) {
		this.lastEnqueueTime = lastEnqueueTime;
	}
	public String getLastSequence() {
		return lastSequence;
	}
	public void setLastSequence(String lastSequence) {
		this.lastSequence = lastSequence;
	}
	public String getLowestSubscriberSequence() {
		return lowestSubscriberSequence;
	}
	public void setLowestSubscriberSequence(String lowestSubscriberSequence) {
		this.lowestSubscriberSequence = lowestSubscriberSequence;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	public String getTotalSubscribers() {
		return totalSubscribers;
	}
	public void setTotalSubscribers(String totalSubscribers) {
		this.totalSubscribers = totalSubscribers;
	}

	public String getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(String messageCount) {
		this.messageCount = messageCount;
	}
}
