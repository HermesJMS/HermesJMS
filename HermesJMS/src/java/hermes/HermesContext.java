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

import hermes.impl.jms.ContextImpl;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: HermesContext.java,v 1.7 2005/07/08 19:43:25 colincrist Exp $
 */

public class HermesContext extends ContextImpl
{
    private static final Logger log = Logger.getLogger(HermesContext.class);
    public static final String FACTORIES = "/conf/factories";
    public static final String LOADER = "/conf/loader";
    public static final String CONFIG = "/conf/hermes";

    private HermesLoader loader;

    /**
     *  
     */
    public HermesContext(Hashtable environment, HermesLoader loader) throws NamingException
    {
        super(environment);

        this.loader = loader;
    }

    public void load() throws NamingException
    {
        try
        {
            for (Hermes hermes : loader.load())
            {
                try
                {
                    Hermes old = (Hermes) lookup(hermes.getId());

                    try
                    {
                        old.close();
                    }
                    catch (JMSException ex)
                    {
                        log.error(ex.getMessage(), ex);
                    }
                }
                catch (NamingException ex)
                {
                    //NOP
                }

                bind(hermes.getId(), hermes);
            }

            //context.put(FACTORIES, loader.getConnectionFactories());
            bind(LOADER, loader);
            bind(CONFIG, loader.getConfig());
        }
        catch (HermesException e)
        {
            log.error(e.getMessage(), e);

            throw new NamingException(e.getMessage());
        }
    }

}