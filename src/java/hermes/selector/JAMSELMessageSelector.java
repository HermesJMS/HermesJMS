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

package hermes.selector;

import hermes.MessageSelector;

import javax.jms.JMSException;
import javax.jms.Message;

import com.codestreet.selector.Selector;
import com.codestreet.selector.jms.ValueProvider;
import com.codestreet.selector.parser.Result;

/**
 * Selector implementation that uses http://sourceforge.net/projects/jamsel
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: JAMSELMessageSelector.java,v 1.2 2006/07/13 07:35:33 colincrist Exp $
 */

public class JAMSELMessageSelector implements MessageSelector
{
   private Selector selector ;

   public JAMSELMessageSelector(Selector selector)
   {
      super();
      
      this.selector = selector ;
   }

   public boolean matches(Message message) throws JMSException
   {
      if (selector.eval(ValueProvider.valueOf(message), null) == Result.RESULT_FALSE)
      {
         return false ;
      }
      else
      {
         return true ;
      }
   }
}
