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

package hermes.store.schema;

import hermes.Domain;
import hermes.Hermes;
import hermes.MessageFactory;
import hermes.browser.HermesBrowser;
import hermes.impl.DefaultXMLHelper;
import hermes.impl.XMLHelper;
import hermes.store.MessageStore;
import hermes.store.MessageStoreFolder;
import hermes.store.MessageStoreQueue;
import hermes.store.MessageStoreTopic;
import hermes.store.jdbc.MessageResultSetHandler;
import hermes.util.JMSUtils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.swing.ProgressMonitor;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.apache.tools.ant.filters.StringInputStream;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultJDBCAdapter.java,v 1.2 2005/06/29 11:04:10 colincrist
 *          Exp $
 */

public class DefaultJDBCAdapter implements JDBCAdapter
{
   private static final Logger log = Logger.getLogger(DefaultJDBCAdapter.class);
   private XMLHelper xmlHelper = new DefaultXMLHelper();
   private int maxMessageSize = 1024 * 1024;
   private int maxDestinationSize = 5000 ;

   private Statements statements;

   public DefaultJDBCAdapter() throws IOException
   {
      try
      {
         Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
      }
      catch (ClassNotFoundException e)
      {
         log.error("default Derby JDBC driver not loaded: " + e.getMessage(), e);
      }
   }

   public void setStatements(Statements statements)
   {
      this.statements = statements;
   }

   public Collection<String> getStores(Connection connection) throws SQLException
   {
      final QueryRunner runner = new QueryRunner();
      final ArrayList<String> stores = new ArrayList<String>();

      Hermes.ui.getDefaultMessageSink().add("Getting message stores....");

      runner.query(connection, statements.getStoresStatement(), new ResultSetHandler()
      {
         public Object handle(ResultSet rs) throws SQLException
         {
            while (rs.next())
            {
               stores.add(rs.getString(1));
            }
            return stores;
         }
      });

      Hermes.ui.getDefaultMessageSink().add("Getting message stores.... done.");

      return stores;
   }

   public void remove(Connection connection, String storeId) throws SQLException
   {
      final QueryRunner runner = new QueryRunner();

      for (final String statment : statements.getRemoveStoreStatements())
      {
         runner.update(connection, statment, new Object[] { storeId });
      }
   }

   private synchronized String getNextMessageId(String storeId)
   {
      return "ID:" + storeId + "-" + UUID.randomUUID() ;
   }


   @Override
   public void update(Connection connection, String id, Message message)  throws SQLException, JMSException {
	   final String messageAsXMLString = xmlHelper.toXML(message);
	      final InputStream messageAsXML = new StringInputStream(messageAsXMLString);   	
	      
	      final PreparedStatement pstmt = connection.prepareStatement("update messages set message = ? where messageid = ?");
	      pstmt.setString(1, message.getJMSMessageID()) ;
	      pstmt.setAsciiStream(2, messageAsXML, messageAsXMLString.length()) ;
	      pstmt.execute() ;
	      pstmt.close() ;
   }
   
   public void insert(Connection connection, String storeId, Message message) throws SQLException, JMSException
   {
      final String destinationName = message.getJMSDestination() == null ? "default" : JMSUtils.getDestinationName(message.getJMSDestination());
      final Domain domain = message.getJMSDestination() == null ? Domain.QUEUE : Domain.getDomain(message.getJMSDestination());
      final String messageAsXMLString = xmlHelper.toXML(message);
      final InputStream messageAsXML = new StringInputStream(messageAsXMLString);
      final String messageId = getNextMessageId(storeId);

      //
      // DBUtils does not seem to correctly deal with CLOBS, so we have to use
      // normal JDBC...
      //
      // runner.update(connection, "insert into messages values (?, ?)", new
      // Object[] { message.getJMSMessageID(), messageAsXML });

      final PreparedStatement pstmt = connection.prepareStatement("insert into messages values (?, ?)");

      pstmt.setString(1, messageId);
      pstmt.setAsciiStream(2, messageAsXML, messageAsXMLString.length());

      pstmt.execute();
      pstmt.close();

      final QueryRunner runner = new QueryRunner();

      runner.update(connection, "insert into stores values (?, ?, ?, ?)", new Object[] { storeId, destinationName, domain.getId(), messageId });
   }

   public void remove(Connection connection, String storeId, String destination) throws SQLException, JMSException
   {
      final QueryRunner runner = new QueryRunner();

      if (runner.update(connection, "delete from messages where messageid in (select messageid from stores where storeid=? and destination=?)", new Object[] {
            storeId, destination }) > 0)
      {
         runner.update(connection, "delete from stores where storeid=? and destination=?", new Object[] { storeId, destination });
      }
   }

   public void remove(Connection connection, String storeId, Message message) throws SQLException, JMSException
   {
      final QueryRunner runner = new QueryRunner();

      if (runner.update(connection, "delete from stores where storeid=? and messageid=?", new Object[] { storeId, message.getJMSMessageID() }) > 0)
      {
         runner.update(connection, "delete from messages where messageid=?", new Object[] { message.getJMSMessageID() });
      }
      else
      {
         throw new SQLException("No message id=" + message.getJMSMessageID() + " exists in store=" + storeId);
      }
   }

   public Collection<Destination> getDestinations(Connection connection, String storeId) throws SQLException, JMSException
   {
      final Collection<Destination> destinations = new ArrayList<Destination>();
      final QueryRunner runner = new QueryRunner();

      Hermes.ui.getDefaultMessageSink().add("Getting message store destinations....");

      runner.query(connection, "select distinct destination, domain from stores where storeId=? ", new Object[] { storeId },
            new ResultSetHandler()
            {
               public Object handle(ResultSet rs) throws SQLException
               {
                  while (rs.next())
                  {
                	  final Domain domain = Domain.getDomain(rs.getInt(2)) ;
                	  if (domain.equals(Domain.QUEUE)) {
                          destinations.add( new MessageStoreQueue(rs.getString(1)));
                	  } else if (domain.equals(Domain.TOPIC)) {
                		  destinations.add(new MessageStoreTopic(rs.getString(1))) ;
                	  } else if (domain.equals(Domain.FOLDER)) {
                		  destinations.add(new MessageStoreFolder(rs.getString(1))) ;
                	  }
                     
                  }

                  return destinations;
               }
            });

      Hermes.ui.getDefaultMessageSink().add("Getting message store folders.... done.");

      return destinations;
   }

   private PreparedStatement createPreparedStatement(Connection connection, String sql, Object[] params) throws SQLException
   {
      final PreparedStatement stmt = connection.prepareStatement(sql);

      if (params != null)
      {
         for (int i = 0; i < params.length; i++)
         {
            if (params[i] != null)
            {
               stmt.setObject(i + 1, params[i]);
            }
            else
            {
               stmt.setNull(i + 1, Types.OTHER);
            }
         }
      }

      return stmt;
   }

   public QueueBrowser getMessages(Connection connection, String storeId, Destination destination, MessageFactory messageFactory,
         MessageStore.HeaderPolicy headerPolicy) throws SQLException, JMSException
   {
      final QueryRunner runner = new QueryRunner();
      final PreparedStatement stmt = createPreparedStatement(
            connection,
            "select stores.destination, stores.domain, messages.message, messages.messageid from stores, messages where stores.storeId=? and destination=? and stores.messageid = messages.messageid order by messages.messageid",
            new Object[] { storeId, messageFactory.getDestinationName(destination) });

      return new MessageResultSetHandler(connection, stmt, messageFactory, headerPolicy);

   }

   public QueueBrowser getMessages(Connection connection, String storeId, final MessageFactory messageFactory, MessageStore.HeaderPolicy headerPolicy)
         throws SQLException, JMSException
   {
      final QueryRunner runner = new QueryRunner();
      final PreparedStatement stmt = createPreparedStatement(
            connection,
            "select stores.destination, stores.domain, messages.message, messages.messageid from stores, messages where stores.storeId=? and stores.messageid = messages.messageid order by messages.messageid",
            new Object[] { storeId });

      return new MessageResultSetHandler(connection, stmt, messageFactory, headerPolicy);
   }

   private void executeStatements(Connection connection, String[] statements) throws SQLException
   {
      final StringBuffer message = new StringBuffer();
      ProgressMonitor progressMonitor = null;

      if (HermesBrowser.getBrowser() != null)
      {
         progressMonitor = new ProgressMonitor(HermesBrowser.getBrowser(), "Initialising message stores... ", "Connecting...", 0, statements.length);

         progressMonitor.setMillisToDecideToPopup(100);
         progressMonitor.setMillisToPopup(400);
      }

      final QueryRunner runner = new QueryRunner();

      for (int i = 0; i < statements.length; i++)
      {
         try
         {
            log.debug("executing: " + statements[i]);

            if (progressMonitor != null)
            {
               progressMonitor.setProgress(statements.length);
               progressMonitor.setNote("Executing statement " + i + " of " + statements.length);
            }

            runner.update(connection, statements[i]);
         }
         catch (SQLException ex)
         {
            log.error(ex.getMessage());
         }
      }
   }

   public void createStore(Connection connection, String storeId) throws SQLException
   {
      try
      {
         final QueryRunner runner = new QueryRunner();

         runner.update(connection, "insert into storeInfo values ( ? )", new Object[] { storeId });
      }
      catch (SQLException ex)
      {
         log.debug("swallowing " + ex.getMessage());
      }

   }

   public void recreateDatabase(Connection connection) throws SQLException
   {
      executeStatements(connection, statements.getDeleteDatabaseStatements());

      connection.commit();

      createDatabase(connection);
   }

   public void createDatabase(Connection connection) throws SQLException
   {
      Hermes.ui.getDefaultMessageSink().add("Initialising message stores...");

      executeStatements(connection, statements.getCreateDatabaseStatements(maxMessageSize, maxDestinationSize));

      connection.commit();

      Hermes.ui.getDefaultMessageSink().add("Initialising message stores... done.");
   }

   public int getDepth(Connection connection, String storeId, Destination destination) throws SQLException, JMSException
   {
      final QueryRunner runner = new QueryRunner();

      return (Integer) runner.query(connection, statements.getDepthStatement(), new Object[] { storeId, JMSUtils.getDestinationName(destination) },
            new ResultSetHandler()
            {
               public Object handle(ResultSet rs) throws SQLException
               {
                  rs.next();

                  return rs.getInt(1);
               }
            });
   }

}
