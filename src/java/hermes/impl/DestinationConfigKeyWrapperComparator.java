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

import java.util.Comparator;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationConfigKeyWrapperComparator.java,v 1.2 2005/08/10 17:15:26 colincrist Exp $
 */

public class DestinationConfigKeyWrapperComparator implements Comparator
{

   public int compare(Object o1, Object o2)
   {
      if (o1 instanceof DestinationConfigKeyWrapper && o2 instanceof DestinationConfigKeyWrapper)
      {
         final DestinationConfigKeyWrapper lhs = (DestinationConfigKeyWrapper) o1 ;
         final DestinationConfigKeyWrapper rhs = (DestinationConfigKeyWrapper) o2 ;
         
        
         if (lhs.getHermes().getId().equals(rhs.getHermes().getId()))
         {
            if (lhs.getConfig().getDomain() == rhs.getConfig().getDomain())
            {
               if (lhs.getConfig().getName().equals(rhs.getConfig().getName()))
               {
                  if (lhs.getConfig().getSelector() != null && rhs.getConfig().getSelector() != null)
                  {
                     return lhs.getConfig().getSelector().compareTo(rhs.getConfig().getSelector()) ;
                  }
                  
                  if (lhs.getConfig().getSelector() != null && rhs.getConfig().getSelector() == null)
                  {
                     return -1 ;
                  }
                  
                  if (lhs.getConfig().getSelector() == null && rhs.getConfig().getSelector() != null)
                  {
                     return 1 ;
                  }
                  
                  if (lhs.getConfig().getDomain() == Domain.TOPIC.getId())
                  {
                     if (lhs.getConfig().isDurable() && rhs.getConfig().isDurable())
                     {
                        return lhs.getConfig().getClientID().compareTo(rhs.getConfig().getClientID()) ;
                     }
                     
                     if (lhs.getConfig().isDurable())
                     {
                        return 1 ;
                     }
                     else
                     {
                        return -1 ;                     }
                     
                     
                  }
                  
                  return 0 ;
               }
               else
               {
                  return lhs.getConfig().getName().compareTo(rhs.getConfig().getName()) ;
               }
            }
            else
            {
               return lhs.getConfig().getDomain() == Domain.QUEUE.getId() ? -1 : 1 ;
            }
         }
         else
         {
            return lhs.getHermes().getId().compareTo(rhs.getHermes().getId()) ;
         }
      }
      
      return -1 ;
   }

}
