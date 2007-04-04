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

package hermes.ant;

import hermes.Domain;
import hermes.Hermes;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageSender.java,v 1.2 2005/08/15 20:37:28 colincrist Exp $
 */

public class MessageSender
{
   private static final Logger log = Logger.getLogger(MessageSender.class);

   public Message createMessage(Hermes hermes) throws JMSException
   {
      return hermes.createMessage() ;
   }
   
   public MessageSender create() 
   {
      return new MessageSender() ;
   }
   
   public static void main(String[] args)
   {
      new MessageSender().doMain(args) ;
   }
   
   /**
    * @param args
    */
   public void doMain(String[] args)
   {
      try
      {    
         Hermes hermes = HermesFactory.createHermes("../cfg/web-hermes-config.xml", "ActiveMQ");
         
         Destination queue = hermes.getDestination("Q1", Domain.QUEUE) ;
         Message message = createMessage(hermes) ;
         
         hermes.send(queue, message) ;
         
         if (hermes.getTransacted())
         {
            hermes.commit() ;
         }
         
         hermes.close() ;
         
         System.exit(1) ;
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
      }
   }

}
