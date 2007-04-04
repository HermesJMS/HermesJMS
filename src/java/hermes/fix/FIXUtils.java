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

import hermes.util.ByteUtils;

import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.JComponent;

import quickfix.Field;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXUtils.java,v 1.7 2006/08/01 07:29:35 colincrist Exp $
 */

public class FIXUtils
{

  

   public FIXUtils()
   {
      super();
   }

   public static JComponent createView(FIXMessage message, boolean displayHeaderAndTrailer, boolean displayValueWithEnum) throws FIXException
   {
      return new FIXMessageViewTable(new FIXMessageViewTableModel(message));
   }
   
  

   public static String prettyPrint(FIXMessage message)
   {
      StringBuffer rval = new StringBuffer();
      StringBuffer line = new StringBuffer() ;
            
      line.append(message.getString(SenderCompID.FIELD)).append(" -> ").append(message.getString(TargetCompID.FIELD)).append(":") ;
      
      Map<Integer, Field> fields =  message.getAllFields() ;
      for (final Map.Entry<Integer, Field>  entry : fields.entrySet())
      {
         
         if (line.length() > 80)
         {
            rval.append(line).append("\n") ;
            line = new StringBuffer() ;
         }
         final Field field = entry.getValue() ;
         
         Object fieldValue = message.getObject(entry.getValue());
         String fieldValueName = message.getDictionary().getValueName(field.getTag(), fieldValue.toString()) ;
         
         String tagText = message.getDictionary().getFieldName(field.getTag()) + "<" + field.getTag() + ">=" + fieldValue.toString() ;
         
         if (fieldValueName != null)
         {
            tagText = tagText + "<" + fieldValueName + ">" ;
         }
         
         
         if (line.length() != 0)
         {
            line.append(" ") ;
         }
         else
         {
            line.append("    ") ;
         }
         
         line.append(tagText) ;
      }
      
      if (line.length() > 0)
      {
         rval.append(line) ;
      }
      
      return rval.toString() ;
   }

  

   public static boolean isFIX(Message message) throws JMSException
   {
      if (message instanceof TextMessage)
      {
         return ((TextMessage) message).getText().startsWith("8=FIX");
      }

      if (message instanceof BytesMessage)
      {
         try
         {
            final byte[] prefix = new byte[5];
            ((BytesMessage) message).reset();
            ((BytesMessage) message).readBytes(prefix);

            return ByteUtils.startsWith(prefix, "8=FIX");
         }
         finally
         {
            ((BytesMessage) message).reset();
         }
      }

      return false;
   }

}
