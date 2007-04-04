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

import hermes.browser.tasks.ThreadPool;
import hermes.impl.SimpleClassLoaderManager;
import hermes.util.JVMUtils;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: HermesInitialContextFactory.java,v 1.1 2004/05/01 15:52:34
 *          colincrist Exp $
 */

public class HermesInitialContextFactory implements InitialContextFactory
{
    private static final Logger log = Logger.getLogger(HermesInitialContextFactory.class);
    private static final String LOADER = "hermes.loader";
    private static final String EXTENSION_LOADER = "hermes.extensionLoader";

    /**
     *  
     */
    public HermesInitialContextFactory()
    {
        super();
        
        JVMUtils.forceInit(SingletonManager.class) ;
        JVMUtils.forceInit(ThreadPool.class);
        JVMUtils.forceInit(SimpleClassLoaderManager.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
     */
    public Context getInitialContext(Hashtable<?, ?> map) throws NamingException
    {
        String className = (String) map.get(LOADER);

        if (className == null)
        {
            className = JAXBHermesLoader.class.getName();
        }

        log.debug("loader=" + className);

        HermesContext context;

        try
        {
            HermesLoader loader = (HermesLoader) Class.forName(className).newInstance();

            if (map.containsKey(EXTENSION_LOADER))
            {
                loader.setExtensionLoaderClass((String) map.get(EXTENSION_LOADER));
            }

            context = new HermesContext(map, loader);

            loader.setProperties(map);
            loader.setContext(context);

            context.load();
            
            return context;
        }
        catch (InstantiationException e)
        {
            log.error(e.getMessage(), e);

            throw new NamingException(e.getMessage());
        }
        catch (IllegalAccessException e)
        {
            log.error(e.getMessage(), e);

            throw new NamingException(e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            log.error(e.getMessage(), e);

            throw new NamingException(e.getMessage());
        }
    }
}