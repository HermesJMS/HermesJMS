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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXMessageFilter.java,v 1.1 2006/08/25 11:33:49 colincrist Exp $
 */

public class FIXMessageFilter
{
   public static final Set<String> SESSION_MSGTYPES = new HashSet<String>() ;
   
   static
   {
      SESSION_MSGTYPES.addAll(Arrays.asList(new String[] { "0", "A", "2", "1", "3", "4", "5" })) ;
   }
   
   private Set<String> msgTypeFilters = new HashSet<String>() ;
   
   public FIXMessageFilter()
   {
      
   }
   
   public FIXMessageFilter(Collection<String> msgTypeFilters)
   {
      this.msgTypeFilters.addAll(msgTypeFilters) ;
   }
   
   public void addMsgType(String msgType)
   {
      msgTypeFilters.add(msgType) ;
   }
   
   public void removeMsgType(String msgType)
   {
      msgTypeFilters.remove(msgType) ;
   }
   
   public void clear()
   {
      msgTypeFilters.clear() ;
   }
   
   public void add(Collection<String> newMsgTypeFilters)
   {
      msgTypeFilters.addAll(newMsgTypeFilters) ;
   }
   
   public boolean filter(String msgType)
   {
      return !msgTypeFilters.contains(msgType) ;
   }

}
