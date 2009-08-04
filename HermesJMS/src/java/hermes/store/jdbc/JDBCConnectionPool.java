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

package hermes.store.jdbc;

import hermes.HermesException;
import hermes.impl.pool.ObjectPool;
import hermes.impl.pool.ObjectPoolProxy;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

/**
 * A connection pool for JDBC connections.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: JDBCConnectionPool.java,v 1.4 2005/12/14 08:11:25 colincrist Exp $
 */

public class JDBCConnectionPool extends ObjectPool<Connection>
{
   private static final Logger log = Logger.getLogger(JDBCConnectionPool.class);
   private String url;
   private boolean autoCommit;

   public JDBCConnectionPool(String url, int poolSize, boolean autoCommit)
   {
      super(poolSize);

      this.url = url;
      this.autoCommit = autoCommit;
   }

   protected Connection makeObject() throws HermesException
   {
      try
      {
         Connection connection = DriverManager.getConnection(url);

         connection.setAutoCommit(autoCommit);
         connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED) ;
         return connection;
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);

         throw new HermesException(e);
      }
   }

   protected Connection newProxyInstance(Connection connection)
   {
      return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Connection.class }, new ObjectPoolProxy<Connection>(connection,
            this, "close"));
   }

   @Override
   protected void closeObject(Connection connection)
   {
      DbUtils.closeQuietly(connection);
   }

   public boolean beforeCheckin(Connection connection)
   {
      try
      {
         if (connection.isClosed())
         {
            return false;
         }
         else
         {
            if (!connection.getAutoCommit())
            {
               connection.rollback();
            }
         }
      }
      catch (SQLException ex)
      {
         log.warn("beforeCheckin failed:" + ex.getMessage(), ex) ;
         return false;
      }
      
      return true ;
   }
}
