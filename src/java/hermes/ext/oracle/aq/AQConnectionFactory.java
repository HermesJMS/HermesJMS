package hermes.ext.oracle.aq;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import oracle.jms.AQjmsFactory;

/**
 * @author ppolavar
 * 
 */

public class AQConnectionFactory implements ConnectionFactory {

    public static final String VERSION_STR = "$Header: /cvs/Integration/dev-tools/rib-aq-hermes-impl/src/hermes/ext/oracle/aq/AQConnectionFactory.java,v 1.1 2009/02/06 03:00:17 polavap Exp $";

    private String url;

    public AQConnectionFactory() {
    }

    public Connection createConnection() throws JMSException {
        ConnectionFactory connectionFactory = AQjmsFactory
                .getConnectionFactory(getUrl(), new Properties());
        return connectionFactory.createConnection();
    }

    public Connection createConnection(String userName, String password)
            throws JMSException {
        ConnectionFactory connectionFactory = AQjmsFactory
                .getConnectionFactory(getUrl(), new Properties());
        return connectionFactory.createConnection(userName, password);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
