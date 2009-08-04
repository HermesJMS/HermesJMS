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

package hermes.impl;

import hermes.Domain;
import hermes.util.JMSUtils;

import javax.jms.Destination;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationKeyWrapper.java,v 1.1 2005/08/07 09:02:51 colincrist Exp $
 */

public class DestinationKeyWrapper
{
   private Destination d;
   private Domain domain;
   private int hashCode ;

   public DestinationKeyWrapper(Destination d, Domain domain)
   {
      this.d = d;
      this.domain = domain;
      
      hashCode = (domain.toString() + JMSUtils.getDestinationName(d)).hashCode() ;
   }

   public DestinationKeyWrapper(Destination d)
   {
      this(d, Domain.getDomain(d)) ;
   }
   
   public Destination getDestination()
   {
      return d ;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof DestinationKeyWrapper)
      {
         DestinationKeyWrapper other = (DestinationKeyWrapper) obj;

         if (domain == other.domain)
         {
            if (JMSUtils.getDestinationName(d).equals(JMSUtils.getDestinationName(other.d)))
            {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public int hashCode()
   {
     return hashCode ;
   }

}
