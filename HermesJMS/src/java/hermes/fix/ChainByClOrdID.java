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

package hermes.fix;


import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import quickfix.field.ClOrdID;
import quickfix.field.OrigClOrdID;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: ChainByClOrdID.java,v 1.5 2006/08/01 07:29:35 colincrist Exp $
 */

public class ChainByClOrdID
{
   private static final Logger log = Logger.getLogger(ChainByClOrdID.class);
   private FIXMessageTable messages;
   private Set<String> baseTypes = new HashSet<String>() ;
   private Set<String> followTypes = new HashSet<String>() ;
   private Set<String> orderIds = new HashSet<String> () ;
   
   public ChainByClOrdID(FIXMessageTable messages)
   {
      this.messages = messages;
      
      baseTypes.add("8") ;
      baseTypes.add("9") ;
      baseTypes.add("D") ;
      
      
      followTypes.add("F") ;
      followTypes.add("G") ;
   }

   public boolean canChain(FIXMessage message) throws FIXException
   {
      
      final String s = message.getMsgType() ;
      
      return baseTypes.contains(s) || followTypes.contains(s) ;
   }
   
   private boolean matches(FIXMessage message)
   {
      try
      {
        

         if (baseTypes.contains(message.getMsgType()))
         {
            if (orderIds.contains(message.getString(ClOrdID.FIELD)))
            {
              return true ;
            }
         }
         else if (followTypes.contains(message.getMsgType()))
         {
            if (orderIds.contains(message.getString(OrigClOrdID.FIELD)))
            {
              orderIds.add(message.getString(OrigClOrdID.FIELD)) ;
              
              return true ;
            }
            if (orderIds.contains(message.getString(ClOrdID.FIELD)))
            {
              orderIds.add(message.getString(OrigClOrdID.FIELD)) ;
              
              return true ;
            }
         }
      }
      catch (Exception ex)
      {
         log.error(ex.getMessage(), ex);
      }
      
      return false ;
   }
   
   public int filterByClOrdID(String clOrdID, FIXMessageListener listener, int startRow)
   {
      int nmessages = 0 ;
      
      orderIds.add(clOrdID) ;
      Vector<FIXMessage> cached = new Vector<FIXMessage> () ;
      
      for (int row = startRow ; row >= 0 ; row-- )
      {
         FIXMessage message = messages.getMessageAt(row) ;
         
         if (matches(message))
         {
            cached.add(0, message) ;
         }
      }
      
      for (FIXMessage message : cached)
      {
         listener.onMessage(message) ;
         nmessages++ ;
      }
      
      for (int row = startRow + 1; row < messages.getRowCount() ; row++ )
      {
         FIXMessage message = messages.getMessageAt(row) ;
         
         if (matches(message))
         {
            listener.onMessage(message) ;
            nmessages++ ;
         }
      }
      
      return nmessages ;
   }

}
