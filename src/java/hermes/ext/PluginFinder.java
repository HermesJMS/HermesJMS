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

package hermes.ext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: PluginFinder.java,v 1.1 2006/10/09 19:58:39 colincrist Exp $
 */

public class PluginFinder
{
   private Map<String, String> mapping = new HashMap<String, String>() ;
   
   public PluginFinder()
   {
      mapping.put("activemq", "ActiveMQ") ;
      mapping.put("tibjms", "Tibco EMS") ;
      mapping.put("progress.message", "SonicMQ") ;
      mapping.put("com.ibm.mq.jms", "IBM WebSphere MQ") ;
   }
   
   public String find(String className)
   {
      for (String key : mapping.keySet())
      {
         if (className.contains(key))
         {
            return mapping.get(key) ;
         }
      }
      return "Default" ;      
   }
}
