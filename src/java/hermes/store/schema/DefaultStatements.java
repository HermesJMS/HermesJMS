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

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultStatements.java,v 1.4 2005/08/10 17:15:26 colincrist Exp $
 */

public class DefaultStatements implements Statements
{

   /* (non-Javadoc)
    * @see hermes.store.schema.Statements#getCreateDatabaseStatements(int)
    */
   public String[] getCreateDatabaseStatements(int maxMessageSize, int maxDestinationSize)
   {
      return new String[] { "create table storeInfo (storeid varchar (64) not null constraint storeInfo_cid unique )",
            "create table stores (storeid varchar(64) not null, destination varchar(" + maxDestinationSize + ") not null, domain integer not null, messageid varchar(256) not null)",
            "create index stores_didx on stores(destination)", "create index stores_sidx on stores(storeid)", "create index stores_midx on stores(messageid)", "create index stores_smidx on stores(storeid, messageid)",
            "create table messages (messageid varchar(256) not null, message clob(" + maxMessageSize + ") not null)",
            "create index messages_midx on messages(messageid)",
            "alter table stores alter destination set data type varchar (" + maxDestinationSize +")" };
   
   }

   /* (non-Javadoc)
    * @see hermes.store.schema.Statements#getDeleteDatabaseStatements()
    */
   public String[] getDeleteDatabaseStatements()
   {
      return new String[] { "drop table stores", "drop table messages", "drop table storeInfo" };
   }
   
   /* (non-Javadoc)
    * @see hermes.store.schema.Statements#getDepthStatement()
    */
   public String getDepthStatement()
   {
      return "select count(messageid) from stores where storeId=? and destination=?" ;
   }
   
   public String getStoresStatement()
   {
     return "select distinct(storeid) from storeInfo" ;
   }
   
   public String[] getRemoveStoreStatements()
   {
      return new String[] { "delete from stores where storeId=?", "delete from messages where messageid in (select messageid from stores where storeid=?)", "delete from storeInfo where storeid=?" } ;
   }

}
