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

package hermes.taglib;

import hermes.Hermes;
import hermes.HermesInitialContextFactory;
import hermes.JAXBHermesLoader;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.jsp.PageContext;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: HermesSessionManager.java,v 1.1 2004/05/01 15:52:36 colincrist
 *          Exp $
 */

public class HermesSessionManager
{

    public static Hermes getHermes(PageContext pageContext, String id) throws Exception
    {
        Context context;
        Hermes hermes;

        if ((context = (Context) pageContext.getAttribute("hermesContext", PageContext.APPLICATION_SCOPE)) == null)
        {
            Properties props = new Properties();
            String hermesURL = null;

            if ((hermesURL = (String) pageContext.getAttribute("hermesURL", PageContext.APPLICATION_SCOPE)) == null)
            {
                hermesURL = System.getProperty("hermes");
            }

            props.put(Context.INITIAL_CONTEXT_FACTORY, HermesInitialContextFactory.class.getName());
            props.put(Context.PROVIDER_URL, hermesURL);
            props.put("hermes.loader", JAXBHermesLoader.class.getName());

            context = new InitialContext(props);

            pageContext.setAttribute("hermesContext", context, PageContext.APPLICATION_SCOPE);
        }

        return (Hermes) context.lookup(id);
    }

}