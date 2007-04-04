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

import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;
import hermes.config.HermesConfig;
import hermes.config.NamingConfig;
import hermes.impl.ClassLoaderManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: HermesLoader.java,v 1.12 2005/05/13 08:16:05 colincrist Exp $
 */

public interface HermesLoader
{
    public void addDestinationConfig(Hermes hermes, DestinationConfig config) throws JMSException;

    public void replaceDestinationConfigs(Hermes hermes, Collection dConfigs) throws JMSException;

    public List<Hermes> load() throws HermesException;

    public HermesConfig getConfig() throws HermesException;

    public ClassLoaderManager getClassLoaderManager();

    public void save() throws HermesException;

    public void backup() throws HermesException;

    public void restore() throws HermesException;

    public void setProperties(Hashtable map);

    public void setContext(Context context);

    public void addConfigurationListener(HermesConfigurationListener l);

    public Iterator getConfigurationListeners();

    public Context getContext();

    public void notifyHermesRemoved(Hermes hermes);
    
    public void notifyNamingRemoved(NamingConfig namingConfig) ;

    public void setExtensionLoaderClass(String loader);
    
    public Hermes createHermes(FactoryConfig factoryConfig) throws JAXBException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException, JMSException, NamingException, NoSuchMethodException ;
    
}