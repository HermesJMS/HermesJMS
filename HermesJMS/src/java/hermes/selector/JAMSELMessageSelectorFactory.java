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

import hermes.HermesException;
import hermes.MessageSelector;
import hermes.MessageSelectorFactory;

import javax.jms.JMSException;

import com.codestreet.selector.Selector;
import com.codestreet.selector.parser.InvalidSelectorException;

/**
 * Selector implementation that uses http://sourceforge.net/projects/jamsel
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: JAMSELMessageSelectorFactory.java,v 1.2 2006/07/13 07:35:33 colincrist Exp $
 */

public class JAMSELMessageSelectorFactory implements MessageSelectorFactory
{

   public JAMSELMessageSelectorFactory()
   {
      super();
      // TODO Auto-generated constructor stub
   }

   public MessageSelector create(String selector) throws JMSException
   {
      try
      {
         return new JAMSELMessageSelector(Selector.getInstance(selector)) ;
      }
      catch (InvalidSelectorException ex)
      {
         throw new HermesException(ex) ;
      }
   }

}
