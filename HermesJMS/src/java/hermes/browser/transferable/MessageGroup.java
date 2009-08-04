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

package hermes.browser.transferable;

import hermes.Hermes;

import java.util.Collection;

import javax.jms.Message;

/**
 * A group of messages taking part in drag and drop.
 * 
  * @author colincrist@hermesjms.com
  * @version $Id: MessageGroup.java,v 1.2 2005/06/17 14:34:37 colincrist Exp $
  */

public class MessageGroup
 {
   private Hermes hermes ;
   private Collection<Message> selectedMessages ;
   
     public MessageGroup(Hermes hermes, Collection<Message> selectedMessages)
     {
         this.hermes = hermes ;
         this.selectedMessages = selectedMessages ;
     }
      
     public Hermes getHermes()
     {
         return hermes;
     }
     
     public Collection<Message> getSelectedMessages()
     {
         return selectedMessages;
     }
 }