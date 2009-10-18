package hermes.ext.oracle.aq;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;
import hermes.impl.ConfigDAO;
import hermes.impl.ConfigDAOImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

import oracle.AQ.AQAgent;
import oracle.AQ.AQException;
import oracle.AQ.AQOracleSession;
import oracle.AQ.AQQueue;
import oracle.AQ.AQSession;
import oracle.jms.AQjmsSession;

import org.apache.log4j.Logger;

/**
 * @author ppolavar
 * 
 */
public class AQAdmin extends HermesAdminSupport implements HermesAdmin {

    public static final String VERSION_STR = "$Header: /cvs/Integration/dev-tools/rib-aq-hermes-impl/src/hermes/ext/oracle/aq/AQAdmin.java,v 1.1 2009/02/06 03:00:17 polavap Exp $";

    // jms session object
    private AQjmsSession session;

    @SuppressWarnings("unused")
    private Hermes hermes;

    HermesShutdownHook shutdownHook;

    private static final Logger LOG = Logger.getLogger(AQAdmin.class);

    protected AQAdmin(Hermes hermes, AQjmsSession session) {
        super(hermes);
        this.session = (AQjmsSession) session;
        this.shutdownHook = new HermesShutdownHook(hermes);
        LOG.debug("Creating a new Shutdown hook " + shutdownHook);
        // register a shutdown hook
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * hermes.ext.HermesAdminSupport#getDepth(hermes.config.DestinationConfig)
     */
    @Override
    public int getDepth(DestinationConfig destConfig) {
        // not implemented
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#close()
     */
    public void close() {

        /*
         * try { // we are at the mercy of hermes to close the session, this
         * will // also close the database connection, when a new session is //
         * started a new database connection is created. LOG.debug("Closing JMS
         * session " + session); //session.close(); } catch (JMSException e) {
         * // what the heck LOG.error("Unable to close session " + session, e);
         * }
         */
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#discoverDestinationConfigs()
     */
    public Collection<DestinationConfig> discoverDestinationConfigs()
            throws JMSException {
        Collection<DestinationConfig> rval = new ArrayList<DestinationConfig>();
        Connection conn = null;
        try {
            conn = session.getDBConnection();
            String query = "SELECT T1.NAME FROM ALL_QUEUES T1,  ALL_QUEUE_TABLES T2 WHERE "
                    + " T1.QUEUE_TABLE = T2.QUEUE_TABLE  AND T1.OWNER = T2.OWNER AND T1.OWNER = \'"
                    + conn.getMetaData().getUserName()
                    + "\' AND T1.NAME NOT LIKE \'AQ$%\'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                String tableName = rs.getString(1);
                // tableName = getActualTopicName(tableName);
                LOG.debug("Fetching Topic information " + tableName);
                ConfigDAO configDao = new ConfigDAOImpl();
                DestinationConfig dc = configDao.createDestinationConfig(
                        tableName, Domain.TOPIC);
                dc.setDurable(false);
                dc.setName(tableName);
                dc.setShortName(tableName);
                String hostname = InetAddress.getLocalHost().getHostName();
                hostname = hostname.replace('-', '_');
                String subName = System.getProperty("user.name") + "_"
                        + hostname + "_Hermes";
                if (subName.length() > 30) {
                    subName = subName.substring(0, 30);
                }
                dc.setClientID(subName);
                rval.add(dc);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            final String msg = "SQL Exception encountered , check log (hermes.log) for more details ";
            LOG.error(msg, e);
            throw new JMSException(msg + e.getMessage());
        } catch (UnknownHostException e) {
            final String msg = "UnknownHostException encountered , check log (hermes.log) for more details ";
            LOG.error(msg, e);
            throw new JMSException(msg + e.getMessage());
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                final String msg = "Could not close database connection ";
                LOG.error(msg + conn, e);
                throw new JMSException(msg + " " + e.getMessage());
            }
        }
        return rval;
    }

    public Map<String, String> getStatistics(DestinationConfig destination) {
        Map<String, String> map = new HashMap<String, String>();
        Connection conn = null;
        try {
            conn = session.getDBConnection();
            LOG.debug("Getting all the subscribers for topic "
                    + destination.getName());
            List<String> list = getSubscribers(destination);
            for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
                String subscriberName = (String) iter.next();
                map.put(subscriberName, String
                        .valueOf(getSubscriberMessagesCount(destination
                                .getName(), subscriberName)));
            }
        } catch (AQException e) {
            final String msg = "AQ Exception encountered";
            LOG.error(msg, e);
            map.put("AQ Exception encountered, check logs ", e.getMessage());
            throw new RuntimeException(msg);
        } catch (SQLException e) {
            final String msg = "SQL Exception encountered , check log (hermes.log) for more details ";
            LOG.error(msg, e);
            throw new RuntimeException(msg);
        } catch (JMSException e) {
            final String msg = "JMS Exception encountered ";
            LOG.error(msg, e);
            map.put("JMS Exception encountered, check logs ", e.getMessage());
            throw new RuntimeException(msg);
        } finally {
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException ignore) {
                // ignore
            }
        }
        return map;
    }

    private long getSubscriberMessagesCount(String topicName,
            String subscriberName) throws SQLException, JMSException {
        Connection conn = session.getDBConnection();
        String query = "SELECT T1.QUEUE_TABLE FROM ALL_QUEUES T1,  ALL_QUEUE_TABLES T2 "
                + " WHERE T1.QUEUE_TABLE = T2.QUEUE_TABLE  AND T1.OWNER = T2.OWNER AND T1.OWNER = \'"
                + conn.getMetaData().getUserName()
                + "\' AND T1.NAME='"
                + topicName.toUpperCase() + "'";
        LOG.debug("Executing query " + query);
        long count = 0;
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next()) {
            String tableName = rs.getString(1);
            String query1 = "SELECT COUNT(*) FROM AQ$" + tableName
                    + " WHERE CONSUMER_NAME=\'" + subscriberName + "\'";
            LOG.debug("Executing query " + query1);
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(query1);
            if (rs1.next())
                count = rs1.getLong(1);
            rs1.close();
            stmt1.close();
        }
        rs.close();
        stmt.close();
        return count;
    }

    private List<String> getSubscribers(DestinationConfig destination)
            throws AQException, SQLException, JMSException {
        Connection conn = session.getDBConnection();
        List<String> list = new ArrayList<String>();
        AQSession aqSession = new AQOracleSession(conn);
        AQQueue queue = aqSession.getQueue(conn.getMetaData().getUserName(),
                destination.getName());
        if (queue != null) {
            AQAgent[] subscribers = queue.getSubscribers();
            if (subscribers != null) {
                for (int index = 0; index < subscribers.length; index++) {
                    list.add(subscribers[index].getName());
                }
            }
        }
        queue.close();
        aqSession.close();
        return list;
    }

    public QueueBrowser createDurableSubscriptionBrowser(
            DestinationConfig dConfig) throws JMSException {
        LOG.debug("Creating a new AQ topic browser for " + dConfig.getName());
        shutdownHook.addSubscriber(dConfig);
        return new AQTopicBrowser(getHermes().getSession(), getHermes()
                .getDestinationManager(), dConfig, shutdownHook);
    }
}
