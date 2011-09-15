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

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.apache.qpid.transport.codec.BBDecoder;

/**
 * Hide qpid jms communication.
 *
 * Layer for qpid communication.
 * @author Barys Ilyushonak
 */
public class QpidManager {


    private static final String BROKER = "broker";
    public static final String HOST = "host";
    private static final String QMF2 = "qmf2";
    private static final String X_AMQP_0_10_APP_ID = "x-amqp-0-10.app-id";
    private static final String OBJECT = "OBJECT";
    private static final String _WHAT = "_what";
    private static final String QMF_OPCODE = "qmf.opcode";
    private static final String _QUERY_REQUEST = "_query_request";
    private static final String _SCHEMA_ID = "_schema_id";
    private static final String _CLASS_NAME = "_class_name";

    private static final int TIMEOUT = 10 * 1000;

    private final Logger log = Logger.getLogger(QpidManager.class);

    private Connection connection;
    private Session session;
    private MessageProducer sender;
    private Destination responses;
    private MessageConsumer receiver;

    private String respQueueName;

    /**
     * Init connection with brocker.
     *
     * Heavily based on code from Gordon Sim's initial JMS QMF Example.
     *
     * @param env - jndi env settings
     * @throws NamingException - if some goes wrong.
     * @throws JMSException - if some goes wrong.
     */
    public QpidManager(Hashtable<?, ?> env)
        throws NamingException, JMSException {

        Context ctx = new InitialContext(env);
        ConnectionFactory factory = (ConnectionFactory) ctx.lookup(HOST);
        Destination target = (Destination) ctx.lookup(BROKER);

        connection = factory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        sender = session.createProducer(target);
        // In the 0.8 Qpid release, the broker incorrectly required
        // the client's response queue to be bound to
        // qmf.default.direct, requiring the following address:
        respQueueName = UUID.randomUUID().toString();
        String respQueueAddress = "BURL:management-direct://qmf.default.direct//"
            + respQueueName + "?autodelete='true'";

        responses = session.createQueue(respQueueAddress);
        // responses = session.createQueue("topic:/qmf.default.direct/" +
        // UUID.randomUUID());
        // However since the 0.10 release, the simpler approach
        // commented out below would be preferred:
        // Destination responses = session.createTemporaryQueue();
        receiver = session.createConsumer(responses);
        connection.start();
    }

    /**
     * Looks up QMF classes and returns Lists of objects relating to those classes
     *
     * Heavily based on code from Gordon Sim's initial JMS QMF Example.
     *
     * @param <T> - type of list
     * @param qmfSchema -  the name of the QMF class being queried
     * @return a List of QMF Objects describing that class
     * @throws JMSException - if errors with qpid communication.
     */
    public <T> List<T> getObjects(QmfTypes qmfSchema)
        throws JMSException {

        List<T> objects = Collections.emptyList();
        MapMessage request = session.createMapMessage();
        request.setJMSReplyTo(responses);
        request.setStringProperty(X_AMQP_0_10_APP_ID, QMF2);
        request.setStringProperty(QMF_OPCODE, _QUERY_REQUEST);
        request.setString(_WHAT, OBJECT);

        Map<String, Object> schemaId = new HashMap<String, Object>();
        schemaId.put(_CLASS_NAME, qmfSchema.getValue());
        request.setObject(_SCHEMA_ID, schemaId);

        sender.send(request);
        Message response = receiver.receive(TIMEOUT);
        if (response != null) {
            if (response instanceof BytesMessage) {
                objects = decode((BytesMessage) response);
            } else {
                log.info("Received response in incorrect format: " + response);
            }
        } else {
            log.info("No response received");
        }
        return objects;
    }

    @SuppressWarnings("unchecked")
    /**
     * JMS QMF returns amqp/list types as a BytesMessage this method decodes
     * that into a Java List
     *
     * Taken from Gordon Sim's initial JMS QMF Example.
     */
    private static <T> List<T> decode(BytesMessage msg)
        throws JMSException {

        // only handles responses up to 2^31-1 bytes long
        byte[] data = new byte[(int) msg.getBodyLength()];
        msg.readBytes(data);
        BBDecoder decoder = new BBDecoder();
        decoder.init(ByteBuffer.wrap(data));
        return (List<T>) decoder.readList();
    }

    /**
     * close used resources.
     *
     * @throws JMSException - if errors with qpid communication.
     */
    public void close()
        throws JMSException {
        if (connection != null) {
            connection.close();
            log.debug("closed QPID connection");
        }
    }

    public String getRespQueueName() {
        return respQueueName;
    }

}
