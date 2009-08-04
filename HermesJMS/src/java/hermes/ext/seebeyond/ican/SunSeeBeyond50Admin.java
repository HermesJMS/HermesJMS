/**
 * 
 */
package hermes.ext.seebeyond.ican;

import hermes.Hermes;
import hermes.HermesException;
import hermes.ext.seebeyond.SeeBeyondAdmin;
import hermes.ext.seebeyond.SunSeeBeyondAbstractAdmin;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.management.ObjectName;

import mx4j.connector.RemoteMBeanServer;
import mx4j.connector.rmi.jrmp.JRMPConnector;

import org.apache.log4j.Logger;

/**
 * @author murali
 * 
 */
public class SunSeeBeyond50Admin extends SunSeeBeyondAbstractAdmin {
	private static final Logger log = Logger.getLogger(SeeBeyondAdmin.class);
	
	private final RemoteMBeanServer rmiConn;
	
	private final String jmsServerName;

	public SunSeeBeyond50Admin(SeeBeyondICANAdminFactory factory, Hermes hermes, ConnectionFactory connectionFactory)
			throws HermesException {
		try {
			long rmiPort = Long.parseLong(factory.getRepositoryPort()) + 4;
			String connectionURL = "rmi://"+factory.getRepositoryHost()+":"+rmiPort;
			jmsServerName = factory.getJmsIQManagerName();

			Hashtable<String,String> properties = new Hashtable<String,String>();
			properties.put("java.naming.factory.initial",
					"com.sun.jndi.rmi.registry.RegistryContextFactory");
			properties.put("java.naming.provider.url", connectionURL);
			JRMPConnector connector = new JRMPConnector();
			//JRMPConnectorWrapper connector = new JRMPConnectorWrapper();
			connector.connect("EM:rmiadaptor=MonitoringService", properties);

			rmiConn = connector.getRemoteMBeanServer();
			seeBeyondIQManager = new ObjectName("EM:" + factory.getEnvironmentName() + "="
					+ factory.getLogicalhostName());
		} catch (Exception e) {
			log.fatal("Couldn't connect to the SeeBeyond logical host.");
			throw new HermesException("Please make sure you have added mx4j.jar, mx4j-tools.jar from logicalhost/stcma/lib",e);
		}
	}
	
	protected Object invoke(String operationName, Object[] params, String[] signatures) throws HermesException {
		try {
			return rmiConn.invoke(seeBeyondIQManager, operationName
					, addAtTheBeginning(jmsServerName, params)
					, (String[]) addAtTheBeginning("java.lang.String", signatures));
		} catch (Exception e) {
			throw new HermesException(e.getMessage(), e);
		}
	}
	
	private Object [] addAtTheBeginning(Object toAdd, Object [] array)
	{
		if (array == null) {
			return new String [] {toAdd.toString()};
		}
		int length = array.length + 1;
		Object [] newArray = (array[0] instanceof java.lang.String)? new String[length] : new Object[length];
		newArray [0] = toAdd;
		for (int i = 0; i < array.length; i++) {
			newArray[i+1] = array[i];
		}
		return newArray;
	}

	public List getTopics() throws HermesException {

		List topics = new ArrayList();
		for (Object topicProps : (List) invoke("getTopics", null, null))
		{
			Properties props = (Properties) topicProps;
			topics.add(props.getProperty("TOPIC_NAME"));
		}
		return topics;
	}

	public List getQueues() throws HermesException {

		List topics = new ArrayList();
		for (Object topicProps : (List) invoke("getQueues", null, null))
		{
			Properties props = (Properties) topicProps;
			topics.add(props.getProperty("QUEUE_NAME"));
		}
		return topics;
	}

	public Properties getTopicMsgProperties(String topicName, String messageID) throws HermesException {

		return new Properties();
	}

	public Properties getQueueMsgProperties(String queueName, String messageID) throws HermesException {

		return new Properties();
	}
	
	public List getSubscribers(String topicName) throws HermesException {

		return new ArrayList();
	}
	
	public Properties getTopicStatistics(String topicName) throws HermesException {
		Properties topicStats = new Properties();
		for (Object topicProps : (List) invoke("getTopics", null, null))
		{
			Properties props = (Properties) topicProps;
			if(topicName.equals(props.getProperty("TOPIC_NAME")))
			topicStats = props;
		}
		return topicStats;
	}

	public Properties getQueueStatistics(String queueName) throws HermesException {
		Properties queueStats = new Properties();
		for (Object queueProps : (List) invoke("getQueues", null, null))
		{
			Properties props = (Properties) queueProps;
			if(queueName.equals(props.getProperty("QUEUE_NAME")))
			queueStats = props;
		}
		return queueStats;
	}
}
