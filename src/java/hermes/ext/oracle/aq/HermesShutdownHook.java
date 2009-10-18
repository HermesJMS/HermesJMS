/**
 * 
 */
package hermes.ext.oracle.aq;

import hermes.Hermes;
import hermes.config.DestinationConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.JMSException;

import oracle.AQ.AQAgent;
import oracle.AQ.AQDriverManager;
import oracle.AQ.AQException;
import oracle.AQ.AQQueue;
import oracle.AQ.AQSession;
import oracle.jms.AQjmsSession;

import org.apache.log4j.Logger;

/**
 * @author ppolavar
 * 
 */
public class HermesShutdownHook extends Thread {

    public static final String VERSION_STR = "$Header: /cvs/Integration/dev-tools/rib-aq-hermes-impl/src/hermes/ext/oracle/aq/HermesShutdownHook.java,v 1.1 2009/02/06 03:00:17 polavap Exp $";

    private static final Logger LOG = Logger
            .getLogger(HermesShutdownHook.class);

    private final static String AQ_DRIVER_CLASS = "oracle.AQ.AQOracleDriver";

    private Hermes hermes = null;

    private static final List<DestinationConfig> list = new ArrayList<DestinationConfig>();

    public HermesShutdownHook(Hermes hermes) {
        this.hermes = hermes;
    }

    public void run() {
        LOG
                .debug("Shutting down, removing all the durable subscribers created in this session...");
        try {
            removeAllSubscribers();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void addSubscriber(DestinationConfig dConfig) {
        LOG.debug("Adding Subscriber " + dConfig.getClientID() + " to topic "
                + dConfig.getName());

        for (Iterator<DestinationConfig> iter = list.iterator(); iter.hasNext();) {
            DestinationConfig element = (DestinationConfig) iter.next();
            if (dConfig.getName().equals(element.getName())) {
                LOG.debug("Subscriber already in for this topic, returning");
                return;
            }
        }
        list.add(dConfig);
    }

    public void removeAllSubscribers() throws JMSException {
        for (int i = 0; i < list.size(); i++) {
            DestinationConfig dConfig = (DestinationConfig) list.get(i);
            removeSubscriber(dConfig, false);
        }
    }

    public void removeSubscriber(DestinationConfig dConfig, boolean remList)
            throws JMSException {
        AQSession aqSession = null;
        AQQueue aqQueue = null;
        if (hermes.getSession() != null) {
            try {
                AQjmsSession aqjmsSession = (AQjmsSession) hermes.getSession();
                Class.forName(AQ_DRIVER_CLASS);
                Connection conn = aqjmsSession.getDBConnection();
                aqSession = AQDriverManager.createAQSession(conn);
                aqQueue = aqSession.getQueue(conn.getMetaData().getUserName(),
                        dConfig.getName());
                AQAgent[] aqAgents = aqQueue.getSubscribers();
                String clientId = dConfig.getClientID();
                clientId = clientId.replaceAll("\"", "");
                LOG.info("Client id is " + clientId);
                for (int i = 0; i < aqAgents.length; i++) {
                    // LOG.debug("Durable Subscriber Name is "
                    // + aqAgents[i].getName());
                    // AQ keeps them as uppercase, unfortunate
                    LOG.debug("AQ agent is " + aqAgents[i].getName());
                    if (aqAgents[i].getName().equals(clientId.toUpperCase())) {
                        LOG.debug("Before remove subscriber "
                                + aqAgents[i].getName());
                        aqQueue.removeSubscriber(aqAgents[i]);
                        LOG.info("Successfully removed durable subscriber "
                                + clientId + " for topic " + dConfig.getName());
                        break;
                    }
                }
            } catch (AQException e) {
                final String msg = "AQ Exception encountered , check log (hermes.log) for more details ";
                LOG.error(msg, e);
            } catch (SQLException e) {
                final String msg = "SQL Exception encountered , check log (hermes.log) for more details ";
                LOG.error(msg, e);
            } catch (ClassNotFoundException e) {
                final String msg = "ClassNotFoundException encountered , check log (hermes.log) for more details ";
                LOG.error(msg, e);
            } finally {
                if (aqQueue != null) {
                    aqQueue.close();
                }
                if (aqSession != null) {
                    aqSession.close();
                }
            }
        }
        if (remList)
            list.remove(dConfig);
    }
}
