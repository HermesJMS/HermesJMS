/**
 * 
 */
package hermes.ext.oracle.aq;

/**
 * @author ppolavar
 * 
 */

import hermes.config.DestinationConfig;
import hermes.impl.DestinationManager;
import hermes.impl.TopicBrowser;

import javax.jms.JMSException;
import javax.jms.Session;

import oracle.jms.AQjmsSession;

import org.apache.log4j.Logger;

/**
 * A queue browser that actually works on a topic, the browse will never stop.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: AQTopicBrowser.java,v 1.1 2009/02/06 03:00:17 polavap Exp $
 */

public class AQTopicBrowser extends TopicBrowser {

    public static final String VERSION_STR = "$Header: /cvs/Integration/dev-tools/rib-aq-hermes-impl/src/hermes/ext/oracle/aq/AQTopicBrowser.java,v 1.1 2009/02/06 03:00:17 polavap Exp $";

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TopicBrowser.class);

    private Session session;

    private DestinationConfig dConfig;

    private HermesShutdownHook shutdownHook;

    /**
     * AQ TopicBrowser constructor.
     */
    public AQTopicBrowser(Session session,
            DestinationManager destinationManager, DestinationConfig dConfig,
            HermesShutdownHook shutdownHook) {
        super(session, destinationManager, dConfig);
        this.session = session;
        this.dConfig = dConfig;
        this.shutdownHook = shutdownHook;
    }

    /**
     * Stop the browser, this will stop any iteration running and unsubscribe.
     */
    public void close() throws JMSException {
        // run the super class's close
        super.close();
        if (session instanceof AQjmsSession) {
            // This doesn't work, hence had to use the AQ API
            // AQjmsSession aqJmsSession = (AQjmsSession) session;
            // aqJmsSession.unsubscribe(aqJmsSession.getTopic("polavap",
            // dConfig.getName()), dConfig.getClientID());
            shutdownHook.removeSubscriber(dConfig, true);
        }
    }
}