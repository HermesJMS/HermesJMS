/* 
 * Copyright 2003,2004 Colin Crist
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

import hermes.MessageFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: XMLHelper.java,v 1.3 2005/06/28 15:36:14 colincrist Exp $
 */

public interface XMLHelper
{
   public Collection<Message> fromXML(MessageFactory hermes, InputStream istream) throws JMSException;

   public Collection<Message> fromXML(MessageFactory hermes, String document) throws JMSException;

   public void toXML(Collection<Message> messages, OutputStream ostream) throws JMSException, IOException;

   public String toXML(Collection<Message> messages) throws JMSException;

   public void toXML(Message message, OutputStream ostream) throws JMSException, IOException;

   public String toXML(Message message) throws JMSException;
}