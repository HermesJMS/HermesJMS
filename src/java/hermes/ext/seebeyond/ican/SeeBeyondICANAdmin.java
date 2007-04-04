package hermes.ext.seebeyond.ican;

import hermes.Hermes;
import hermes.HermesException;
import hermes.ext.seebeyond.SeeBeyondAdmin;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.log4j.Logger;

public class SeeBeyondICANAdmin extends SeeBeyondAdmin {

	private static final Logger log = Logger.getLogger(SeeBeyondICANAdmin.class);

	/**
	 * 
	 */
	public SeeBeyondICANAdmin(SeeBeyondICANAdminFactory factory, Hermes hermes, ConnectionFactory connectionFactory)
			throws JMSException {
		super(hermes);

		// Set context classloader from Hermes.

		this.connectionFactory = connectionFactory;
		this.factory = factory;

			if (factory.getRepositoryHost() == null || factory.getRepositoryPort() == null
					|| factory.getLogicalhostName() == null || factory.getEnvironmentName() == null
					|| factory.getJmsIQManagerName() == null) {
				throw new HermesException("Edit the session and provide the following plugin properties: \r\n"
						+ "repositoryHost, repositoryPort, logicalHostName, environmentName, "
						+ "jmsServerName as provided in ICAN environment.");
			} else {
				seeBeyondAdmin = new SunSeeBeyond50Admin(factory, hermes, connectionFactory);
			}

	}
	
}
