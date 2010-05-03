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

package hermes;

import hermes.store.MessageStore;

import java.util.Collection;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * A message repository.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HermesRepository.java,v 1.4 2005/06/28 15:36:21 colincrist Exp $
 */

public interface HermesRepository extends MessageStore
{
    /**
     * Get all the JMS messages in the repository as a Collection. Note you
     * should not modify the colleciton directory as it may not be persisted
     * correctly. Hermes is used to re-inflate the messages from the underlying
     * persistence used (e.g. XML).
     * 
     * @param hermes
     * @return @throws
     *         JMSException
     */
    public Collection<Message> getMessages(Hermes hermes) throws JMSException;

    /**
     * Add a collection of JMS messages to the collection and persist them.
     * 
     * @param hermes
     * @param messages
     * @throws JMSException
     */
    public void addMessages(Hermes hermes, Collection<Message> messages) throws JMSException;

    /**
     * Add a single message to the collection and persist them.
     * 
     * @param hermes
     * @param message
     * @throws JMSException
     */
    public void addMessage(Hermes hermes, Message message) throws JMSException;

    /**
     * How many messages are in the repository?
     * 
     * @return
     */
    public int size();

    /**
     * Delete the repository content. Further accesss to the repository will
     * create a new one.
     */
    public void delete();

    /**
     * Get the name of this repository, typically the filename.
     * 
     * @return
     */
    public String getId();

    /**
     * Get some nice text to display in a GUI
     * 
     * @return
     */
    public String getToolTipText();
}