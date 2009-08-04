/* 
 * Copyright 2007 Colin Crist
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

package hermes.renderers;

import javax.swing.JComponent;

import hermes.browser.ConfigDialogProxy;
import hermes.browser.MessageRenderer;
import hermes.browser.MessageRenderer.Config;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public abstract class AbstractMessageRenderer implements MessageRenderer
{
   private Config config = new BasicConfig() ;
   
   public class BasicConfig implements Config
   {
      private boolean active = true;
      private String name = AbstractMessageRenderer.this.getClass().getName();

      public boolean isActive()
      {
         return active;
      }

      public void setActive(boolean active)
      {
         this.active = active;
      }

      public String getName()
      {
         return name;
      }

      public String getPropertyDescription(String propertyName)
      {
         return propertyName;
      }

      public void setName(String name)
      {
         this.name = name;
      }
   }

   /**
    * There are no configurable options on this renderer.
    */
   public Config createConfig()
   {
      return new BasicConfig() ;
   }

   public Config getConfig()
   {
      return config ;
   }

   /**
    * There are no configurable options on this renderer
    */
   public void setConfig(Config config)
   {
      if (config == null)
      {
         this.config = createConfig() ;
      }
      else
      {
         this.config = config ;
      }
   }

   public boolean isActive()
   {
      return getConfig().isActive();
   }

   public void setActive(boolean active)
   {
      getConfig().setActive(active);
   }

   public JComponent getConfigPanel(ConfigDialogProxy dialogProxy) throws Exception
   {
      return RendererHelper.createDefaultConfigPanel(dialogProxy);
   }
}
