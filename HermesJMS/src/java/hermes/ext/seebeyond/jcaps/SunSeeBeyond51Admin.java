/**
 * 
 */
package hermes.ext.seebeyond.jcaps;

import hermes.Hermes;
import hermes.HermesException;
import hermes.ext.seebeyond.SeeBeyondAdmin;
import hermes.ext.seebeyond.SunSeeBeyondAbstractAdmin;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

/**
 * @author murali
 * 
 */
public class SunSeeBeyond51Admin extends SunSeeBeyondAbstractAdmin {
	private static final Logger log = Logger.getLogger(SeeBeyondAdmin.class);

	public SunSeeBeyond51Admin(SeeBeyondJCAPSAdminFactory factory, Hermes hermes, ConnectionFactory connectionFactory)
			throws HermesException {
		try {
			JMXServiceURL url = new JMXServiceURL("service:jmx:s1ashttp://" + factory.getLogicalHostIP().trim() + ":"
					+ factory.getLogicalHostPort().trim() + "/");
			Map<String,String> env = new HashMap<String,String>();
			env.put("jmx.remote.protocol.provider.pkgs", "com.sun.enterprise.admin.jmx.remote.protocol");
			env.put("USER", factory.getLogicalHostUser().trim());
			env.put("PASSWORD", factory.getLogicalHostUserPassword().trim());
			env.put("com.sun.enterprise.as.http.auth", "BASIC");
			JMXConnector mConnector = JMXConnectorFactory.connect(url, env);
			mConn = mConnector.getMBeanServerConnection();
			seeBeyondIQManager = new ObjectName(
					"com.sun.appserv:type=messaging-server-admin-mbean,jmsservertype=stcms,name=SeeBeyond_JMS_IQ_Manager");
		} catch (Exception e) {
			log.fatal("Couldn't connect to the SeeBeyond logical host.");
			throw new HermesException("Make sure the com.stc.rts.deployimpl.jar from locgicalhost/extras "
					+ "(OR) appserv-admin.jar from logicalhost/is/lib is added into the provider config.", e);
		}
	}

}
