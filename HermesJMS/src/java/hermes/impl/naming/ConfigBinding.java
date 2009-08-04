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

import java.io.StringWriter;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.apache.log4j.Logger;

/**
 * Wrapper for a Hermes configuration bound into JNDI.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ConfigBinding.java,v 1.1 2004/10/15 09:41:18 colincrist Exp $
 */
public class ConfigBinding implements Referenceable
{
    private static final Logger log = Logger.getLogger(ConfigBinding.class);
    static final String HERMES_XML = "hermesXML" ;
    private HermesConfig hermesConfig;

    /**
     * 
     */
    public ConfigBinding(HermesConfig hermesConfig)
    {
        super();
    }

    /* (non-Javadoc)
     * @see javax.naming.Referenceable#getReference()
     */
    public Reference getReference() throws NamingException
    {
        try
        {
            /*
             * Marshall
             */
            
            StringWriter stringWriter = new StringWriter();
            JAXBContext jc = JAXBContext.newInstance("hermes.config");
            Marshaller m = jc.createMarshaller();

            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(getConfig(), stringWriter);

            /*
             * Create the reference content
             */
           
            StringRefAddr refAddr = new StringRefAddr(HERMES_XML, stringWriter.getBuffer().toString()) ;
            
            return new Reference(getClass().getName(), refAddr) ;
        }
        catch (PropertyException e)
        {
            log.error(e.getMessage(), e);
            
            throw new NamingException("cannot create HermesConfig reference: " + e.getMessage()) ;
        }
        catch (JAXBException e)
        {
            log.error(e.getMessage(), e);
            
            throw new NamingException("cannot create HermesConfig reference: " + e.getMessage()) ;
        }
    }

    public HermesConfig getConfig() throws NamingException
    {
        if (hermesConfig == null)
        {
            throw new NamingException("Hermes configuration is null") ;
        }
        
        return hermesConfig;
    }
}