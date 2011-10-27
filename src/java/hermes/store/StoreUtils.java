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
import hermes.HermesRuntimeException;
import hermes.SystemProperties;
import hermes.store.schema.DefaultJDBCAdapter;
import hermes.store.schema.DefaultStatements;
import hermes.store.schema.JDBCAdapter;
import hermes.store.schema.Statements;
import hermes.util.JVMUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: StoreUtils.java,v 1.6 2006/08/08 18:01:28 colincrist Exp $
 */

public class StoreUtils
{
   private static final Logger log = Logger.getLogger(StoreUtils.class) ;
   private static JDBCAdapter adapter ;
   private static Map<String, Statements> statements = new HashMap<String, Statements>() ;
   private static String DERBY = "derby" ;
   static 
   {
      statements.put(DERBY, new DefaultStatements()) ;
   }
   
   public static String getDefaultConnectionURL()
   {
      if (System.getProperty(SystemProperties.MESSAGE_STORE_JDBC_URL) != null)
      {
         return System.getProperty(SystemProperties.MESSAGE_STORE_JDBC_URL) ;
      }
      else
      {
         String name = JVMUtils.getUserHome() + File.separator + ".hermes" + File.separator + "MessageStores" ;
         return getConnectionURL(name) ;
      }
   }
   public static String getConnectionURL(String provider, String databaseName, String options) 
   {
      return "jdbc:" + provider + ":" + databaseName + ";" + options ;
   }
   
   public static String getConnectionURL(String databaseName)
   {
      return "jdbc:derby:" + databaseName + ";create=true" ;
   }
   
   public static String getProvider(String url) 
   {
      return url.split(":")[1] ;
   }
   
   public static Statements getStatements(String url) throws HermesException
   {
      String provider = getProvider(url) ;
      
      log.debug("looking for statement for " + provider) ;
      
      if (statements.containsKey(provider))
      {
         return statements.get(provider) ;
      }
      else
      {
    	  log.error("no statements for provider " + provider + ", trying default for " + DERBY) ;
          return statements.get(DERBY) ;
      }
   }
   
   public synchronized static JDBCAdapter getJDBCAdapter(String url) throws HermesException
   {
      if (adapter == null)
      {
         try
         {
            adapter = (JDBCAdapter) Class.forName(System.getProperty("hermes.jdbc.adapter", DefaultJDBCAdapter.class.getName())).newInstance() ;
            adapter.setStatements(getStatements(url)) ;
         }
         catch (Exception ex)
         {
            throw new HermesRuntimeException(ex) ;
         }
      }
      
      return adapter ;
   }
}
