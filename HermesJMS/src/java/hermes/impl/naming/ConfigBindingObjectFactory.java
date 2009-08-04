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

package hermes.impl.naming;

import hermes.config.HermesConfig;

import java.io.StringReader;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ConfigBindingObjectFactory.java,v 1.1 2004/10/15 09:41:18 colincrist Exp $
 */
public class ConfigBindingObjectFactory implements ObjectFactory
{
private static final Logger log = Logger.getLogger(ConfigBindingObjectFactory.class) ;
    /**
     * 
     */
    public ConfigBindingObjectFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
    {
        if (obj instanceof Reference)
        {
            final Reference ref = (Reference) obj;
            
            if (ref.getClassName().equals(ConfigBinding.class.getName()))
            {
                final RefAddr refAddr = ref.get(ConfigBinding.HERMES_XML) ;
                final StringReader reader = new StringReader((String) refAddr.getContent()) ;  
                final JAXBContext jc = JAXBContext.newInstance("hermes.config");
                final Unmarshaller u = jc.createUnmarshaller();

                try
                {
                    return new ConfigBinding((HermesConfig) u.unmarshal(new InputSource(reader))) ;
                }
                catch (Exception ex)
                {
                    log.error(ex.getMessage(), ex) ;
                    
                    throw new NamingException(ex.getMessage()) ;
                }
            }
        }

        return null;
    }

}