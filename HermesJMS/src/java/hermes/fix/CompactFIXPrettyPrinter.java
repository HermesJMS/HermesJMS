/* 
 * Copyright 2007 Colin Crist
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

import java.util.Map;

import quickfix.Field;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class CompactFIXPrettyPrinter implements FIXPrettyPrinter
{
   public String print(FIXMessage message)
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

}
