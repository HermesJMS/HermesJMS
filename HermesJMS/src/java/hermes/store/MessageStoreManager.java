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

import hermes.HermesException;
import hermes.store.schema.JDBCAdapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jms.JMSException;

import org.apache.derby.impl.jdbc.EmbedSQLException;
import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageStoreManager.java,v 1.7 2005/08/21 20:47:56 colincrist
 *          Exp $
 */

public class MessageStoreManager
{
   private static final Logger log = Logger.getLogger(MessageStoreManager.class);
   private static final Set<String> dbsCreated = new HashSet<String>();

   public static Collection<MessageStore> find() throws JMSException
   {
      return find(StoreUtils.getDefaultConnectionURL());
   }

   public static MessageStore create(String storeId) throws JMSException
   {
      return create(StoreUtils.getDefaultConnectionURL(), storeId);
   }

   public static MessageStore create(String url, String storeId) throws JMSException
   {
      return new SingleUserMessageStore(storeId, url, !dbsCreated.contains(url));
   }

   public static Collection<MessageStore> find(String url) throws JMSException
   {
      try
      {
         final JDBCAdapter adapter = StoreUtils.getJDBCAdapter(url);
         final Connection connection = DriverManager.getConnection(url);

         if (!dbsCreated.contains(url))
         {
            adapter.createDatabase(connection);
            dbsCreated.add(url);
         }

         final Collection<MessageStore> rval = new ArrayList<MessageStore>();

         for (String storeId : adapter.getStores(connection))
         {
            rval.add(create(url, storeId));
         }

         connection.close();

         return rval;
      }
      catch (SQLException e)
      {
         log.error(e.getMessage(), e);

         if (e.getNextException() != null)
         {
            log.error(e.getNextException().getMessage(), e.getNextException());
         }

         if (e instanceof EmbedSQLException)
         {
            EmbedSQLException es = (EmbedSQLException) e;

            if ("XJ040.C".equals(es.getMessageId()))
            {
               throw new HermesException("Is another instance of HermesJMS running?", e);
            }
         }

         throw new HermesException(e);
      }
   }
}
