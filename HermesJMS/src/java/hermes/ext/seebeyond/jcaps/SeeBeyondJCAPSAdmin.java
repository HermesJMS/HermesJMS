package hermes.ext.seebeyond.jcaps;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.ext.seebeyond.SeeBeyondAdmin;

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.swing.ProgressMonitor;

import org.apache.log4j.Logger;

public class SeeBeyondJCAPSAdmin extends SeeBeyondAdmin {

	private static final Logger log = Logger.getLogger(SeeBeyondJCAPSAdmin.class);
	
	/**
	 * 
	 */
	public SeeBeyondJCAPSAdmin(SeeBeyondJCAPSAdminFactory factory, Hermes hermes, ConnectionFactory connectionFactory)
			throws JMSException {
		super(hermes);

		// Set context classloader from Hermes.

		this.connectionFactory = connectionFactory;
		this.factory = factory;

			if (factory.getLogicalHostIP() == null 
					|| factory.getLogicalHostPort() == null
					|| factory.getLogicalHostUser() == null
					|| factory.getLogicalHostUserPassword() == null) {
				throw new HermesException("Edit the session and provide the following plugin properties: \r\n"
						+ "logicalHost, logicalHostPort, logicalHostUser, logicalHostUserPassword");
			} else {
				seeBeyondAdmin = new SunSeeBeyond51Admin(factory, hermes, connectionFactory);
			}

	}
	
	@Override
	public int truncate(DestinationConfig dConfig) throws JMSException {
		ProgressMonitor monitor = new ProgressMonitor(HermesBrowser.getBrowser(), "Deleting from " + dConfig.getName(),
				"Discovering size of " + dConfig.getName(), 0, 102);
		int rval = 0;
		String destName = getRealDestinationName(dConfig);
		monitor.setMillisToDecideToPopup(100);
		monitor.setMillisToPopup(400);

		try {
			if (dConfig.getDomain() == Domain.QUEUE.getId()) {
				Properties queueStats = seeBeyondAdmin.getQueueStatistics(destName);
				int start = Integer.parseInt(queueStats.getProperty("MIN_SEQ"));
				int finish = Integer.parseInt(queueStats.getProperty("MAX_SEQ"));
				for (int i = start; i <= finish; i++) {
					// Properties headerProps = (Properties) headers;
					seeBeyondAdmin.deleteQueueMessage(destName, "" + i);
					rval++;
					if (monitor.isCanceled())
						return rval;
				}
			} else if (dConfig.getDomain() == Domain.QUEUE.getId()){
				Properties queueStats = seeBeyondAdmin.getTopicStatistics(destName);
				int start = Integer.parseInt(queueStats.getProperty("FIRST_SEQ"));
				int finish = Integer.parseInt(queueStats.getProperty("LAST_SEQ"));
				for (int i = start; i <= finish; i++) {
					// Properties headerProps = (Properties) headers;
					seeBeyondAdmin.deleteTopicMessage(destName, "" + i);
					rval++;
					if (monitor.isCanceled())
						return rval;
				}
			} else
			{
				log.error("Unsupported destination domain " + dConfig.getDomain());
			}
		} catch (HermesException ex) {
			throw ex;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new HermesException(e);
		} finally {
			if (monitor != null)
				monitor.close();
		}

		return rval;
	}

	
}
