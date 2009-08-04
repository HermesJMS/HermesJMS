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

import hermes.Domain;
import hermes.HermesRuntimeException;
import hermes.MessageFactory;
import hermes.impl.DefaultXMLHelper;
import hermes.impl.XMLHelper;
import hermes.store.MessageStore;
import hermes.store.MessageStoreQueue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.naming.NamingException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageResultSetHandler.java,v 1.2 2005/08/15 20:37:34 colincrist Exp $
 */

public class MessageResultSetHandler implements QueueBrowser
{
   private static final Logger log = Logger.getLogger(MessageResultSetHandler.class);
   private ResultSet resultSet;
   private List<Message> cachedMessages = new ArrayList<Message>();
   private XMLHelper xmlHelper = new DefaultXMLHelper();
   private MessageFactory messageFactory;
   private Connection connection;
   private MessageStore.HeaderPolicy headerPolicy;
   private PreparedStatement statement;
   private boolean resultSetEmpty = false;
   private Enumeration enumeration ;

   public MessageResultSetHandler(Connection connection, PreparedStatement statement, MessageFactory messageFactory, MessageStore.HeaderPolicy headerPolicy)
         throws SQLException
   {
      this.connection = connection;
      this.messageFactory = messageFactory;
      this.headerPolicy = headerPolicy;
      this.statement = statement;
      this.resultSet = statement.executeQuery();
      
      enumeration = new Enumeration()
      {
      
         public Object nextElement()
         {
            return getNextMessageQuietly() ;
         }
      
         public boolean hasMoreElements()
         {
           return hasNextMessage() ;
         }
      
      } ;
   }

   public boolean hasNextMessage()
   {
      return !resultSetEmpty || cachedMessages.size() > 0;
   }

   public Message getNextMessage() throws JMSException, SQLException, NamingException
   {
      if (cachedMessages.size() > 0)
      {
         return cachedMessages.remove(0);
      }
      else
      {
         if (resultSet.next())
         {
            final Collection<Message> c = xmlHelper.fromXML(messageFactory, resultSet.getClob(3).getAsciiStream());

            for (Message m : c)
            {
               if (headerPolicy != MessageStore.HeaderPolicy.NO_HEADER)
               {
                  Destination d = messageFactory.getDestination(resultSet.getString(1), Domain.getDomain(resultSet.getInt(2)));

                  if (headerPolicy == MessageStore.HeaderPolicy.DESTINATION_ONLY)
                  {
                     m.setJMSDestination(d);
                  }
                  else if (headerPolicy == MessageStore.HeaderPolicy.MESSAGEID_ONLY)
                  {
                     m.setJMSMessageID(resultSet.getString(4));
                  }
                  else if (headerPolicy == MessageStore.HeaderPolicy.MESSAGEID_AND_DESTINATION)
                  {
                     m.setJMSDestination(d);
                     m.setJMSMessageID(resultSet.getString(4));
                  }
               }

               cachedMessages.add(m);
            }

            if (cachedMessages.size() > 0)
            {
               return cachedMessages.remove(0);
            }
            else
            {
               return null;
            }
         }
         else
         {
            resultSetEmpty = true;
            close();
            return null;
         }
      }
   }

   public void close()
   {
      log.debug("closing MessageResultSetHandler...");

      DbUtils.closeQuietly(statement);
      DbUtils.closeQuietly(connection);

      connection = null;
   }

   public Enumeration getEnumeration() throws JMSException
   {
     return enumeration ;
   }

   public String getMessageSelector() throws JMSException
   {
     return null;
   }

   public Queue getQueue() throws JMSException
   {
      return new MessageStoreQueue("") ;
   }

   public boolean hasNext()
   {
      return hasNextMessage();
   }

   public Message getNextMessageQuietly()
   {
      try
      {
         return getNextMessage();
      }
      catch (Exception ex)
      {
         close() ;
         
         throw new HermesRuntimeException(ex);
      }
   }
}
