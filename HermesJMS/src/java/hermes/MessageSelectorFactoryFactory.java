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

package hermes;

import hermes.browser.HermesBrowser;

import javax.jms.JMSException;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageSelectorFactoryFactory.java,v 1.2 2005/09/02 14:58:59 colincrist Exp $
 */

public class MessageSelectorFactoryFactory
{
   private static final Logger log = Logger.getLogger(MessageSelectorFactoryFactory.class) ;
   
   public static MessageSelectorFactory create() throws JMSException
   {
      try
      {
         if (HermesBrowser.getBrowser() == null)
         {
            return (MessageSelectorFactory) Class.forName(SystemProperties.SELECTOR_FACTORY).newInstance();
         }
         else
         {
            return (MessageSelectorFactory) Class.forName(HermesBrowser.getBrowser().getConfig().getSelectorImpl()).newInstance() ;
         }
      }
      catch (Exception e)
      {
         throw new HermesException("Unable to initialise message selector support on message stores: " + e.getMessage(), e);
      }
   }
}
