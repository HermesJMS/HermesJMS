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

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.config.PropertyConfig;
import hermes.config.PropertySetConfig;
import hermes.ext.HermesAdminSupport;
import hermes.ext.qpid.qmf.QMFObject;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;


/**
 * Hermes admin implementation for qpid.
 *
 * Plugin for qpid discovery, contains main functions to get qpid management info.
 * @see hermes.HermesAdmin
 * @author Barys Ilyushonak
 */

public class QpidAdmin
    extends HermesAdminSupport
    implements hermes.HermesAdmin {

    private final Logger log = Logger.getLogger(QpidAdmin.class);
    private QpidManager qpidManager;

    /**
     * Init qpid connection.
     * @param hermes - api
     * @param qpidManager - qpidManager
     *
     * @throws NamingException - if some goes wrong
     * @throws JMSException - if some goes wrong
     */
    public QpidAdmin(Hermes hermes, QpidManager qpidManager)
        throws NamingException, JMSException {

        super(hermes);
        this.qpidManager = qpidManager;
    }

    @Override
    public int getDepth(DestinationConfig destination)
        throws JMSException {

        try {

            int result = 0;
            String name = destination.getName();
            log.info("getDepth destination name=[" + name + "]");

            List<Map<String, ?>> objects = qpidManager.getObjects(QmfTypes.QUEUE);
            for (Map<String, ?> i : objects) {

                QMFObject qmfObject = new QMFObject(i);
                if (name.equals(qmfObject.getName())) {

                    return qmfObject.getDepth().intValue();
                }
            }
            return result;
        } catch (UnsupportedEncodingException e) {

            throw new HermesException(e);
        }
    }

    @Override
    public synchronized void close()
        throws JMSException {

        qpidManager.close();
    }

    @Override
    public Collection<DestinationConfig> discoverDestinationConfigs()
            throws JMSException {

        Collection<DestinationConfig> rval = new HashSet<DestinationConfig>();

        try {

            List<Map<String, ?>> objects = qpidManager.getObjects(QmfTypes.QUEUE);
            for (Map<String, ?> i : objects) {

                QMFObject qmfObject = new QMFObject(i);
                String queueName = qmfObject.getName();

                if(acceptDestination(queueName)) {
                    DestinationConfig destinationConfig = buildDestinationConfig(queueName);
                    PropertySetConfig propertySetConfig = new PropertySetConfig();

                    for (String key : qmfObject.keySet()) {

                        PropertyConfig propertyConfig = new PropertyConfig();
                        propertyConfig.setName(key);
                        propertyConfig.setValue(qmfObject.getStringValue(key));
                        propertySetConfig.getProperty().add(propertyConfig);
                    }

                    destinationConfig.setProperties(propertySetConfig);
                    rval.add(destinationConfig);
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new HermesException(e);
        }
        return rval;
    }

    /**
     * Hide @see HermesBrowser usage for testing purpose.
     *
     * @param queueName - destination name.
     * @return DestinationConfig - hermes DestinationConfig
     */
    protected DestinationConfig buildDestinationConfig(String queueName) {
        DestinationConfig destinationConfig = HermesBrowser.getConfigDAO()
            .createDestinationConfig(queueName, Domain.QUEUE);
        return destinationConfig;
    }

    protected boolean acceptDestination(String destName) {
        return !qpidManager.getRespQueueName().equals(destName);
    }

    @Override
    public Map<String, Object> getStatistics(DestinationConfig destination)
        throws JMSException {

        Map<String, Object> rval = new HashMap<String, Object>();
        try {

            List<Map<String, ?>> objects = qpidManager.getObjects(QmfTypes.QUEUE);
            for (Map<String, ?> i : objects) {
                QMFObject qmfObject = new QMFObject(i);
                String name = qmfObject.getName();
                if (destination.getName().equals(name)) {
                    for (String key : qmfObject.keySet()) {
                        rval.put(key, qmfObject.getStringValue(key));
                    }
                    return rval;
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new HermesException(e);

        }

        return rval;
    }



}
