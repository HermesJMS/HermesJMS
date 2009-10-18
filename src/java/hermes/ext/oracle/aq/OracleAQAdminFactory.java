/**
 * 
 */
package hermes.ext.oracle.aq;

/**
 * @author ppolavar
 * 
 */
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.HermesException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.NamingException;

import oracle.jms.AQjmsConnection;
import oracle.jms.AQjmsSession;

import org.apache.log4j.Logger;

public class OracleAQAdminFactory implements HermesAdminFactory {

    public static final String VERSION_STR = "$Header: /cvs/Integration/dev-tools/rib-aq-hermes-impl/src/hermes/ext/oracle/aq/OracleAQAdminFactory.java,v 1.1 2009/02/06 03:00:18 polavap Exp $";

    private static final Logger LOG = Logger.getLogger(OracleAQAdminFactory.class);

    private AQjmsSession session;

    public OracleAQAdminFactory() {
        super();
    }

    public HermesAdmin createSession(Hermes hermes,
            ConnectionFactory connectionFactory) throws JMSException,
            NamingException {
        AQConnectionFactory aqFactory = null;
        if (connectionFactory instanceof AQConnectionFactory) {
            aqFactory = (AQConnectionFactory) connectionFactory;
            LOG.debug("Connection string URL " + aqFactory.getUrl());
        } else {
            throw new HermesException("Provider is not Oracle AQ");
        }

        LOG.debug("JMS ConnectionFactory implementation class is "
                + hermes.getConnection().getClass());

        AQjmsConnection aqJMS = (AQjmsConnection) hermes.getConnection();

        // AQjmsConsumer
        // if session is null or if the sessions is not open
        // create a new session
        // if (session == null || !session.isOpen()) {
        session = (AQjmsSession) aqJMS.createSession(false,
                Session.AUTO_ACKNOWLEDGE);
        LOG.debug("Creating AQjmsSession session " + session);

        LOG.debug("Underlying database connection is "
                + session.getDBConnection());
        AQAdmin ribAQ = new AQAdmin(hermes, session);
        return ribAQ;
    }

}