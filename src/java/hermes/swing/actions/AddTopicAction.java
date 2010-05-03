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

import hermes.Domain;
import hermes.browser.IconCache;

import javax.swing.Action;

public class AddTopicAction extends AddDestinationAction
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -3138329573923737028L;

public AddTopicAction()
   {
      super(Domain.TOPIC) ;
      
      putValue(Action.NAME, "Add topic...");
      putValue(Action.SHORT_DESCRIPTION, "Add a new topic.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes/browser/icons/new_topic.gif"));
   }

}
