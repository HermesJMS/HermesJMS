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


import org.apache.log4j.Logger;

import com.codestreet.selector.parser.IValueProvider;
import com.codestreet.selector.parser.Identifier;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXMessageValueProvider.java,v 1.4 2006/07/17 21:20:53
 *          colincrist Exp $
 */

public class FIXMessageValueProvider implements IValueProvider
{
   private static final Logger log = Logger.getLogger(FIXMessageValueProvider.class);
   private FIXMessage message;

   public FIXMessageValueProvider(FIXMessage message)
   {
      super();

      this.message = message;
   }

   public Object getValue(Object i, Object correlation)
   {
      try
      {
         Identifier identifier = (Identifier) i;

         String tagName = identifier.getIdentifier();
         int tag = message.getDictionary().getFieldTag(tagName);

         return message.getObject(tag);
      }
      catch (NoSuchFieldException ex)
      {
         return null;
      }
   }
}
