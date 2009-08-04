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

import java.util.Collection;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageRecorder.java,v 1.2 2005/08/07 09:02:51 colincrist Exp $
 */

public interface MessageRecorder
{
   public void start() throws JMSException ;
   
   public void stop() throws JMSException ;
   
   public void record(Topic topic) throws JMSException ;
   
   public void record(Topic topic, String selector) throws JMSException ;
    
   public void snap(Queue queue) throws JMSException ;
   
   public void snap(Queue queue, String selector) throws JMSException ;
   
   public Collection<Destination> getDestinations() ;
   
   public MessageStore getMessageStore() ;
}
