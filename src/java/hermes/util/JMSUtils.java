/* 
 * Copyright 2003,2004,2005 Colin Crist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package hermes.util;

import java.util.Collection;
import java.util.Iterator;

import hermes.Domain;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * Miscellaneous JMS utility functions.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: JMSUtils.java,v 1.6 2005/10/21 08:37:21 colincrist Exp $
 */

public class JMSUtils {
	public static String getFilenameFromMessageID(String messageId) {
		return messageId.replace('/', '_').replace(':', '_').replace('\\', '_').replace("<", "_").replace(">", "_");
	}

	public static void closeQuietly(QueueBrowser browser) {
		try {
			if (browser != null) {
				browser.close();
			}
		} catch (JMSException ex) {
			// NOP
		}
	}

	/**
	 * Is the argument in the queue domain
	 */
	public static boolean isQueue(Connection o) {
		return o instanceof QueueConnection;
	}

	/**
	 * Is the argument in the queue domain
	 */
	public static boolean isQueue(ConnectionFactory o) {
		return o instanceof QueueConnectionFactory;
	}

	/**
	 * Is the argument in the queue domain
	 */
	public static boolean isQueue(Destination o) {
		return Domain.getDomain(o) == Domain.QUEUE;
	}

	/**
	 * Is the argument in the queue domain
	 */
	public static boolean isQueue(MessageConsumer o) {
		return o instanceof QueueReceiver;
	}

	/**
	 * Is the argument in the queue domain
	 */
	public static boolean isQueue(MessageProducer o) {
		return o instanceof QueueSender;
	}

	/**
	 * Is the argument in the queue domain
	 */
	public static boolean isQueue(Session o) {
		return o instanceof QueueSession;
	}

	public static String getDestinationName(Destination to) {
		if (to == null) {
			return "";
		}

		try {
			if (isQueue(to)) {
				return ((Queue) to).getQueueName();
			} else {
				return ((Topic) to).getTopicName();
			}
		} catch (JMSException ex) {
			return ex.getMessage();
		}
	}

	public static String createMessageSelectorUsingIn(Collection<String> messageIds) {
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("JMSMessageID in (") ; 
		for (Iterator iter = messageIds.iterator(); iter.hasNext();) {
			sqlBuffer.append("\'").append(iter.next()).append("\'");

			if (iter.hasNext()) {
				sqlBuffer.append(", ");
			}
		}
		sqlBuffer.append(")") ;

		return sqlBuffer.toString();
	}
	
	public static String createMessageSelectorUsingOr(Collection<String> messageIds) {
		StringBuffer sqlBuffer = new StringBuffer();

		for (Iterator iter = messageIds.iterator(); iter.hasNext();) {
			sqlBuffer.append("JMSMessageID = \'").append(iter.next()).append("\'");

			if (iter.hasNext()) {
				sqlBuffer.append(" or ");
			}
		}

		return sqlBuffer.toString();
	}

}
