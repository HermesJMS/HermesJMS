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

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageStoreListener.java,v 1.1 2005/06/28 15:36:17 colincrist Exp $
 */

public interface MessageStoreListener extends MessageListener
{
   public void onDestination(Destination d) ;
   
   public void onDestinationDeleted(Destination d) ;
   
   public void onException(Exception e) ;
   
   public void onMessageDeleted(Message m) ;

   public void onMessageChanged(Message message);

}
