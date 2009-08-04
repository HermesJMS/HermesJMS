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

import hermes.SingletonManager;
import hermes.browser.tasks.ThreadPool;
import hermes.impl.SimpleClassLoaderManager;
import hermes.util.JVMUtils;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Task;

public class AbstractTask extends Task
{
   private static final Logger log = Logger.getLogger(AbstractTask.class);

   private String config;
   private String hermes;

   public AbstractTask()
   {
      JVMUtils.forceInit(SingletonManager.class);
      JVMUtils.forceInit(ThreadPool.class);
      JVMUtils.forceInit(SimpleClassLoaderManager.class);
   }

   public String getConfig()
   {
      return config;
   }

   public void setConfig(String config)
   {
      this.config = config;
   }

   public String getHermes()
   {
      return hermes;
   }

   public void setHermes(String hermes)
   {
      this.hermes = hermes;
   }

}
