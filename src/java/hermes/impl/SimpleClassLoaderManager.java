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

import hermes.NullConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.config.ClasspathGroupConfig;
import hermes.config.ProviderExtConfig;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: SimpleClassLoaderManager.java,v 1.10 2005/06/08 10:15:40 colincrist Exp $
 */
public class SimpleClassLoaderManager implements ClassLoaderManager
{
    private static final Logger log = Logger.getLogger(SimpleClassLoaderManager.class);
    public static final String DEFAULT_LOADER = "Default";
    public static final String SYSTEM_LOADER = "System";

    private final Collection emptySet = new HashSet();
    private final Map loaderById = new TreeMap();
    private final Map configById = new TreeMap();
    private final Map factoriesById = new TreeMap();
    private final Map loaderByHermes = new TreeMap();

    public SimpleClassLoaderManager(List classPathGroups) throws IOException
    {
        for (Iterator iter = classPathGroups.iterator(); iter.hasNext();)
        {
            final ClasspathGroupConfig config = (ClasspathGroupConfig) iter.next();

            add(config);
        }
        
        System.setSecurityManager(null) ;
    }

    public void add(ClasspathGroupConfig config) throws IOException
    {
        log.debug("adding new ClasspathGroupConfig " + config.getId());

        configById.put(config.getId(), config);
        factoriesById.put(config.getId(), LoaderSupport.lookForFactories(config.getLibrary(), getClass().getClassLoader()));
    }

    private ClassLoader createClassLoader(String id) throws IOException, JAXBException
    {
        ProviderExtConfig extConfig = HermesBrowser.getConfigDAO().createDefaultProviderExtConfig(NullConnectionFactory.class.getName()) ;
        
        return createClassLoader(id, extConfig) ;
    }
    
    public ClassLoader createClassLoader(String id,  ProviderExtConfig extConfig) throws IOException
    {
        if (id.equals(SYSTEM_LOADER))
        {
            return getDefaultClassLoader();
        }

        if (loaderById.containsKey(id))
        {
            log.debug("createClassLoader id=" + id + " using cached");

            return (ClassLoader) loaderById.get(id);
        }

        log.debug("createClassLoader id=" + id + " extConfig=" + extConfig.getClassName());

        ClasspathGroupConfig config = (ClasspathGroupConfig) configById.get(id);
        ClassLoader classLoader = null;

        if (config != null)
        {
            URL[] urls = HermesBrowser.getConfigDAO().getAdminFactoryURLs();

            if (urls != null)
            {
                classLoader = LoaderSupport.createClassLoader(config.getLibrary(), urls, getClass().getClassLoader());
            }
            else
            {
                classLoader = LoaderSupport.createClassLoader(config.getLibrary(), getClass().getClassLoader());
            }
            
            loaderById.put(id, classLoader);
        }
        else
        {
            throw new IOException("No such loader: " + id) ;
            
            // classLoader = ClassLoader.getSystemClassLoader();
        }

        return classLoader;
    }

    public ClassLoader getClassLoader(String id)
    {
        if (loaderById.containsKey(id))
        {
            log.debug("getting existing classLoader for " + id) ;
            
            return (ClassLoader) loaderById.get(id);
        }
        else
        {
            try
            {
                log.debug("creating new classLoader for  " + id) ;
                
                return createClassLoader(id) ;
            }
            catch (Exception ex) 
            {
                // NOP
            }
 
            log.warn("no ClassLoader " + id + " found, returning the default loader") ;
            
            return getDefaultClassLoader();
        }
    }

    public ClassLoader getDefaultClassLoader()
    {
        if (loaderById.containsKey(DEFAULT_LOADER))
        {
            return (ClassLoader) loaderById.get(DEFAULT_LOADER);
        }
        else
        {
            return getClass().getClassLoader();
        }
    }

    public Collection getIds()
    {
        return configById.keySet();
    }

    public ClassLoader getClassLoaderByHermes(String hermesId)
    {
        return (ClassLoader) loaderByHermes.get(hermesId);
    }

    public void putClassLoaderByHermes(String hermesId, ClassLoader classLoader)
    {
        loaderByHermes.put(hermesId, classLoader);
    }

    public Collection getFactories(String id)
    {
        if (factoriesById.containsKey(id))
        {
            Collection rval = (Collection) factoriesById.get(id);

            if (rval == null)
            {
                return emptySet;
            }
            else
            {
                return rval;
            }
        }
        else
        {
            return null;
        }
    }
}