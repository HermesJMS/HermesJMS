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

import hermes.config.ClasspathGroupConfig;
import hermes.config.ProviderExtConfig;

import java.io.IOException;
import java.util.Collection;

/**
 * Central entry point for all things ClassLoader related.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ClassLoaderManager.java,v 1.6 2004/09/25 16:03:35 colincrist Exp $
 */
public interface ClassLoaderManager
{
    public void add(ClasspathGroupConfig config) throws IOException;

    public ClassLoader createClassLoader(String id, ProviderExtConfig extConfig) throws IOException;
    
    public ClassLoader getClassLoader(String id);

    public ClassLoader getDefaultClassLoader();

    public Collection getIds();

    public ClassLoader getClassLoaderByHermes(String hermesId);

    public void putClassLoaderByHermes(String hermesId, ClassLoader classLoader);

    public Collection getFactories(String id);
}