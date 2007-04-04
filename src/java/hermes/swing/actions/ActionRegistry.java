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

package hermes.swing.actions;

import hermes.HermesRuntimeException;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

/**
 * Registry for all the actions available.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ActionRegistry.java,v 1.3 2006/10/09 19:58:38 colincrist Exp $
 */

public class ActionRegistry
{
   private static Map<Class, Action> actions = new HashMap<Class, Action>();

   public static Action getAction(Class clazz) 
   {
      if (actions.containsKey(clazz))
      {
         return actions.get(clazz);
      }
      else
      {
         try
         {
            Action action = (Action) clazz.newInstance();
            
            actions.put(clazz, action);
            return action;
         }
         catch (InstantiationException e)
         {
           throw new HermesRuntimeException(e) ;
         }
         catch (IllegalAccessException e)
         {
            throw new HermesRuntimeException(e) ;
         }
      }
   }

}
