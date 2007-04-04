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

import hermes.Domain;
import hermes.HermesException;
import hermes.providers.file.FileMessageFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: StoreMessageFactory.java,v 1.2 2005/08/15 20:37:27 colincrist Exp $
 */

public class StoreMessageFactory extends FileMessageFactory
{
   @Override
   public Destination getDestination(String name, Domain domain) throws JMSException, NamingException
   {
      if (domain == Domain.QUEUE)
      {
         return new MessageStoreQueue(name) ;
      }
      else if (domain == Domain.TOPIC)
      {
         return new MessageStoreTopic(name) ;
      }
      else
      {
         throw new HermesException("Invalid domain.") ;
      }
   }

}
