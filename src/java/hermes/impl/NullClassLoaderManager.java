/* 
 * Copyright 2003,2004,2005 Colin Crist
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
import java.util.Collections;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class NullClassLoaderManager implements ClassLoaderManager
{

   public void add(ClasspathGroupConfig config) throws IOException
   {

   }

   public ClassLoader createClassLoader(String id, ProviderExtConfig extConfig) throws IOException
   {
      return getClass().getClassLoader();
   }

   public ClassLoader getClassLoader(String id)
   {
      return getClass().getClassLoader();
   }

   public ClassLoader getClassLoaderByHermes(String hermesId)
   {
      return getClass().getClassLoader();
   }

   public ClassLoader getDefaultClassLoader()
   {
      return getClass().getClassLoader();
   }

   public Collection getFactories(String id)
   {
      return Collections.EMPTY_LIST;
   }

   public Collection getIds()
   {
      return Collections.EMPTY_LIST;
   }

   public void putClassLoaderByHermes(String hermesId, ClassLoader classLoader)
   {

   }

}
