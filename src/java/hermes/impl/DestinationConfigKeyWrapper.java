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

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;

import org.apache.commons.beanutils.BeanUtils;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationConfigKeyWrapper.java,v 1.3 2005/08/15 20:37:24 colincrist Exp $
 */

public class DestinationConfigKeyWrapper 
{
   private DestinationConfig config;
   private Hermes hermes;

   public DestinationConfigKeyWrapper(Hermes hermes, DestinationConfig config)
   {
      this.config = config;
      this.hermes = hermes;
   }
   
   public String toString()
   {
      try
      {
         return BeanUtils.describe(config).toString() ;
      }
      catch (Exception e)
      {
         return e.getMessage() ;
      }
   }
   public DestinationConfigKeyWrapper(DestinationConfig config)
   {
      this(null, config) ;
   }
   
   public DestinationConfigKeyWrapper(String destinationName, Domain domain)
   {
      config = HermesBrowser.getConfigDAO().createDestinationConfig(destinationName, domain) ;
   }

   public Hermes getHermes()
   {
      return hermes ;
   }
   
   public DestinationConfig getConfig() 
   {
      return config ;
   }
   
  

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof DestinationConfigKeyWrapper)
      {
         DestinationConfigKeyWrapper other = (DestinationConfigKeyWrapper) obj;

         if (hermes == null || hermes.getId().equals(other.hermes.getId()))
         {
            if (config.getDomain() == other.config.getDomain())
            {
               if (config.getName().equals(other.config.getName()))
               {
                  if ((config.getSelector() == null && other.config.getSelector() == null) || (config.getSelector().equals(other.config.getSelector())))
                  {
                     if (config.getDomain() == Domain.TOPIC.getId())
                     {
                        if (config.isDurable() && other.config.isDurable())
                        {
                           return config.getClientID() != null && config.getClientID().equals(other.config.getClientID());
                        }
                        else if (!config.isDurable() && !other.config.isDurable())
                        {
                           return true;
                        }
                     }
                     else
                     {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   @Override
   public int hashCode()
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(Domain.getDomain(config.getDomain())).append(config.getName()).append(config.getSelector());

      if (config.getDomain() == Domain.TOPIC.getId())
      {
         buffer.append(config.isDurable()).append(config.getClientID());
      }

      return buffer.toString().hashCode();
   }

  

}
