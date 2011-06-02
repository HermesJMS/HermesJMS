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

package hermes.store;

import hermes.Domain;
import hermes.HermesException;
import hermes.MessageFactory;
import hermes.store.jdbc.JDBCConnectionPool;
import hermes.store.schema.JDBCAdapter;
import hermes.util.JMSUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.naming.NamingException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultMessageStore.java,v 1.2 2005/06/29 11:04:11 colincrist
 *          Exp $
 */

public class SingleUserMessageStore implements MessageStore {
	private static final Logger log = Logger.getLogger(SingleUserMessageStore.class);
	private String storeId;
	private String jdbcURL;
	private JDBCAdapter adapter;
	private List<MessageStoreListener> listeners = new ArrayList<MessageStoreListener>();
	private MessageFactory defaultFactory = new StoreMessageFactory();
	private Set<Destination> destinations = new HashSet<Destination>();
	private Map<Destination, Integer> depths = new HashMap<Destination, Integer>();
	private JDBCConnectionPool connectionPool;
	private ThreadLocal<Connection> writerTL = new ThreadLocal<Connection>();

	public SingleUserMessageStore(String storeId, String jdbcURL, boolean doCreate) throws JMSException {
		this.storeId = storeId;
		this.jdbcURL = jdbcURL;
		this.connectionPool = new JDBCConnectionPool(jdbcURL, 2, false);
		this.adapter = StoreUtils.getJDBCAdapter(jdbcURL);
		try {
			Connection connection = connectionPool.get();

			if (doCreate) {
				adapter.createDatabase(connection);
			}

			adapter.createStore(connection, storeId);
			connection.commit();

			destinations.addAll(getDestinationsFromDatabase());

			for (Destination d : destinations) {
				getDepth(d);
			}
		} catch (SQLException ex) {
			throw new HermesException(ex);
		}
	}

	public String getURL() {
		return jdbcURL;
	}

	private Connection getWriterConnection() throws HermesException {
		if (writerTL.get() == null) {
			writerTL.set(connectionPool.get());
		}

		return writerTL.get();
	}

	public String getTooltipText() {
		return jdbcURL;
	}

	public String getId() {
		return storeId;
	}

	public Collection<Destination> getDestinations() throws JMSException {
		return destinations;
	}

	public Collection<Destination> getDestinationsFromDatabase() throws JMSException {
		final Connection connection = connectionPool.get();

		try {
			return adapter.getDestinations(connection, getId());
		} catch (SQLException ex) {
			throw new HermesException(ex);
		} finally {
			if (connection != null) {
				DbUtils.closeQuietly(connection);
			}
		}
	}

	public QueueBrowser visit() throws JMSException {
		return visit(defaultFactory, MessageStore.HeaderPolicy.MESSAGEID_AND_DESTINATION);
	}

	public QueueBrowser visit(Destination d) throws JMSException {
		return visit(defaultFactory, d, MessageStore.HeaderPolicy.MESSAGEID_AND_DESTINATION);
	}

	public QueueBrowser visit(MessageFactory factory, HeaderPolicy headerPolicy) throws JMSException {
		final Connection connection = connectionPool.get();

		try {
			return adapter.getMessages(connection, getId(), factory, headerPolicy);
		} catch (SQLException ex) {
			throw new HermesException(ex);
		}

		//
		// Connection is closed later by the MessageResultSetHandler...

	}

	public QueueBrowser visit(MessageFactory factory, Destination d, HeaderPolicy headerPolicy) throws JMSException {
		final Connection connection = connectionPool.get();

		try {
			return adapter.getMessages(connection, getId(), d, factory, headerPolicy);
		} catch (SQLException ex) {

			throw new HermesException(ex);
		}

		//
		// Connection is closed later by the MessageResultSetHandler...
	}

	public void delete() throws JMSException {
		final Connection connection = getWriterConnection();

		try {
			adapter.remove(connection, storeId);
		} catch (SQLException ex) {
			throw new HermesException(ex);
		}
	}

	protected Destination createStoreDestination(Destination from) throws JMSException {
		try {
			if (from instanceof MessageStoreQueue || from instanceof MessageStoreTopic) {
				return from;
			} else {
				return defaultFactory.getDestination(JMSUtils.getDestinationName(from), Domain.getDomain(from));
			}
		} catch (NamingException ex) {
			throw new HermesException(ex);
		}
	}

	public synchronized void store(Message message) throws JMSException {
		try {
			adapter.insert(getWriterConnection(), getId(), message);

			final Destination from = createStoreDestination(message.getJMSDestination());

			if (!destinations.contains(from)) {
				destinations.add(from);

				for (MessageStoreListener l : listeners) {
					l.onDestination(from);
				}
			}

			for (MessageStoreListener l : listeners) {
				l.onMessage(message);
			}

			synchronized (depths) {
				if (depths.containsKey(from)) {
					depths.put(from, depths.get(from) + 1);
				}
			}
		} catch (SQLException ex) {
			throw new HermesException(ex);
		}
	}

	public int getDepth(Destination d) throws JMSException {
		d = createStoreDestination(d);

		synchronized (depths) {
			if (depths.containsKey(d)) {
				return depths.get(d);
			}
		}

		final Connection connection = connectionPool.get();

		try {
			int depth = adapter.getDepth(connection, getId(), d);

			synchronized (depths) {
				depths.put(d, depth);
			}

			return depth;
		} catch (SQLException e) {
			throw new HermesException(e);
		} finally {
			DbUtils.closeQuietly(connection);
		}
	}

	public void delete(Message message) throws JMSException {
		final Connection connection = getWriterConnection();
		final Destination destination = createStoreDestination(message.getJMSDestination());

		try {
			adapter.remove(connection, getId(), message);

			for (final MessageStoreListener l : listeners) {
				l.onMessageDeleted(message);
			}

			synchronized (depths) {
				if (depths.containsKey(destination)) {
					depths.put(destination, depths.get(destination) - 1);
				}
			}

			if (getDepth(destination) == 0) {
				destinations.remove(destination);

				synchronized (depths) {
					depths.remove(destination);
				}

				for (final MessageStoreListener l : listeners) {
					l.onDestinationDeleted(destination);
				}
			}
		} catch (SQLException ex) {
			throw new HermesException(ex);
		}
	}

	public void delete(Destination d) throws JMSException {
		final Connection connection = getWriterConnection();

		try {
			adapter.remove(connection, getId(), JMSUtils.getDestinationName(d));

			for (final MessageStoreListener l : listeners) {
				l.onDestinationDeleted(d);
			}

			depths.remove(d);

			destinations.remove(createStoreDestination(d));
		} catch (SQLException ex) {
			throw new HermesException(ex);
		}
	}

	public void rollback() throws JMSException {
		try {
			if (writerTL.get() != null) {
				writerTL.get().rollback();
				writerTL.get().close();
			}
		} catch (SQLException e) {
			throw new HermesException(e);
		} finally {
			writerTL.set(null);
		}
	}

	public void checkpoint() throws JMSException {
		try {
			if (writerTL.get() != null) {
				writerTL.get().commit();
				writerTL.get().close();
			}
		} catch (SQLException e) {
			throw new HermesException(e);
		} finally {
			writerTL.set(null);
		}
	}

	public void close() throws JMSException {
		try {
			getWriterConnection().close();
		} catch (SQLException e) {
			throw new HermesException(e);
		} finally {
			writerTL.set(null);
		}
	}

	public void addMessageListener(MessageStoreListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void removeMessageListener(MessageStoreListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public void update(Message message) throws Exception {
		final Connection connection = getWriterConnection();
		final Destination destination = createStoreDestination(message.getJMSDestination());

		try {
			adapter.update(connection, getId(), message);

			for (final MessageStoreListener l : listeners) {
				l.onMessageChanged(message);
			}
		} catch (SQLException ex) {
			throw new HermesException(ex) ;
		}

	}
}
