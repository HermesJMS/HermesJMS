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

import hermes.MessageFactory;
import hermes.store.MessageStore;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JDBCAdapter.java,v 1.7 2005/08/21 20:48:04 colincrist Exp $
 */

public interface JDBCAdapter
{
   public void setStatements(Statements statementProvider) ;
   
   public void createDatabase(Connection connection) throws SQLException ;
   
   public void recreateDatabase(Connection connection) throws SQLException ;
     
   public Collection<String> getStores(Connection connection) throws SQLException ;
   
   public void createStore(Connection connection, String storeId) throws SQLException ;
   
   public void insert(Connection connection, String storeId, Message message) throws SQLException, JMSException ;
   
   public void remove(Connection connection, String storeId, Message message) throws SQLException, JMSException ;
   
   public void remove(Connection connection, String storeId, String destination)  throws SQLException, JMSException ;
   
   public void remove(Connection connection, String storeId) throws SQLException ;
   
   public Collection<Destination> getDestinations(Connection connection, String storeId) throws SQLException, JMSException ;
   
   public QueueBrowser getMessages(Connection connection, String storeId, MessageFactory messageFactory, MessageStore.HeaderPolicy headerPolicy) throws SQLException, JMSException ;
   
   public QueueBrowser getMessages(Connection connection, String storeId, Destination destination, MessageFactory messageFactory, MessageStore.HeaderPolicy headerPolic) throws SQLException, JMSException ;
   
   public int getDepth(Connection connection, String storeId, Destination destination) throws SQLException, JMSException ;

}
