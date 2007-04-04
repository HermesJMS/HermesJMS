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

package hermes.fix.quickfix;

import hermes.fix.FIXMessage;

import org.apache.log4j.Logger;

import quickfix.DataDictionary;

/**
 * This FIX message implementation maintains its byte[].
 *  
 * @author colincrist@hermesjms.com
 * @version $Id: QuickFIXMessage.java,v 1.7 2006/08/01 07:29:36 colincrist Exp $
 */

public class QuickFIXMessage extends AbstractQuickFIXMessage implements FIXMessage
{ 
   private static final Logger log = Logger.getLogger(QuickFIXMessage.class);

   byte[] bytes ;
  
   public QuickFIXMessage(QuickFIXMessageCache cache, byte[] bytes)
   {
      super(cache) ;
      this.bytes = bytes ;   
      
      getAllFields() ;
   }

  

   public QuickFIXMessage(QuickFIXMessageCache cache, byte[] bytes, DataDictionary dictionary)
   {
      this(cache, bytes) ;
      
      setDictionary(dictionary) ;
      
      getAllFields() ;
   }

   public byte[] getBytes()
   {
      return bytes;
   }
}
