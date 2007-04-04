/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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

package hermes.ant;

import hermes.Hermes;
import hermes.HermesInitialContextFactory;
import hermes.JAXBHermesLoader;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tools.ant.BuildException;

public class HermesFactory
{
    public static Hermes createHermes(String config, String hermes) throws BuildException, NamingException
    {
        if ( config == null)
        {
            throw new BuildException("config property for the hermes-config.xml not set");
        }

        if ( hermes == null)
        {
            throw new BuildException("hermes property for the Hermes id not set");
        }
        
        final Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, HermesInitialContextFactory.class.getName());
        props.put(Context.PROVIDER_URL, config);
        props.put("hermes.loader", JAXBHermesLoader.class.getName());
        
        final Context context = new InitialContext(props);
        final Hermes myHermes = (Hermes) context.lookup(hermes) ;
        
        context.close() ;
        return myHermes ;
    }
}
