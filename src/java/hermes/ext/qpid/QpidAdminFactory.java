/**
 * Copyright (c) 2011 CJSC Investment Company "Troika Dialog", http://troika.ru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. * 
 */
package hermes.ext.qpid;

import java.util.Hashtable;
import java.util.Properties;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * Hermes plugin admin factory implementation for qpid plugin.
 *
 * @see HermesAdminFactory
 * @author Barys Ilyushonak
 */
public class QpidAdminFactory
    implements HermesAdminFactory {

    private static String DEFAULT_URL = "amqp://guest:guest@clientid/test?brokerlist='tcp://localhost:5672'";
    public static final String MANAGEMENT_BROKER_ADDR = "BURL:management-direct://qmf.default.direct//broker?routingkey='broker'";
    public static final String DESTINATION_BROKER = "destination.broker";
    public static final String CONNECTIONFACTORY_HOST = "connectionfactory.host";
    public static final String QPID_CONTEXT_FACTORY = "org.apache.qpid.jndi.PropertiesFileInitialContextFactory";

    private String brokerUrl;

    @Override
    public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory)
        throws JMSException, NamingException {

        return createHermesAdmin(hermes);
    }

    /**
     * Builder method for.
     * @param brokerUrl - qpid broker url
     * @return Hashtable - for Context building
     */
    protected static Hashtable<?, ?> buildEnv(String brokerUrl) {

        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, QPID_CONTEXT_FACTORY);
        props.setProperty(CONNECTIONFACTORY_HOST, brokerUrl);
        props.setProperty(DESTINATION_BROKER, MANAGEMENT_BROKER_ADDR);
        return props;
    }

    private QpidAdmin createHermesAdmin(Hermes hermes)
        throws NamingException, JMSException {

        String url = brokerUrl;
        if (brokerUrl == null) {
            url = DEFAULT_URL;
        }
        return new QpidAdmin(hermes, new QpidManager(buildEnv(url)));
    }

    /**
     * Config property.
     * @return brokerUrl
     */
    public String getBrokerUrl() {
        return brokerUrl;
    }

    /**
     * Config property.
     * @param brokerUrl - value
     */
    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }
}
