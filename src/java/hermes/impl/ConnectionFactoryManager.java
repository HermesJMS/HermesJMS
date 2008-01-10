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

import hermes.Domain;
import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.config.DestinationConfig;
import hermes.config.ProviderConfig;
import hermes.config.ProviderExtConfig;
import hermes.ext.ExtensionFinder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author cristco last changed by: $Author: colincrist $
 * @version $Id: ConnectionFactoryManager.java,v 1.13 2005/10/21 08:37:22 colincrist Exp $
 */
public interface ConnectionFactoryManager extends JMSManager
{
    /**
     * Add a statically configured destination reachable via this
     * ConnectionFactory
     */
    public void addDestinationConfig(DestinationConfig destConfig);
    
    public void removeDestinationConfig(DestinationConfig dConfig) ;

    /**
     * Connect. No implementation for a connection factory.
     */
    public void connect() throws javax.jms.JMSException;

    public void close() throws JMSException ;
    
    /**
     * Get some short description of this connection factory
     */
    public String getConnectionFactoryType();

    public ConnectionFactory getConnectionFactory() throws JMSException;

    public DestinationConfig getDestinationConfig(String id, Domain domain);

    public DestinationConfig getDestinationConfig(Destination d) throws JMSException;

    public Collection getDestinationConfigs();

    public Object getObject() throws JMSException;

    public void setProvider(ProviderConfig pConfig) throws InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, IOException ;

    public void setId(String id);

    public String getId();

    /**
     * @return Returns the extension.
     */
    public HermesAdminFactory getExtension(ProviderExtConfig extConfig) throws HermesException;

    /**
     * @return Returns the extensionFinder.
     */
    public ExtensionFinder getExtensionFinder();

    /**
     * @param extensionFinder
     *            The extensionFinder to set.
     */
    public void setExtensionFinder(ExtensionFinder extensionFinder);
    
    public Context createContext() throws NamingException, JMSException ;
}