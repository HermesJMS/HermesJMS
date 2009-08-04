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

package hermes.browser;

import hermes.HermesException;
import hermes.SingletonManager;
import hermes.browser.tasks.ThreadPool;
import hermes.config.HermesConfig;

/**
 * @author cristco last changed by: $Author: colincrist $
 * @version $Id: ProxyHermesUI.java,v 1.3 2005/05/03 16:18:44 colincrist Exp $
 */
public class ProxyHermesUI implements HermesUI
{
    private UIMessageSink defaultMessageSink ;
    private HermesConfig config ;
    
    /**
     * 
     */
    public ProxyHermesUI()
    {
        super();
        
        defaultMessageSink = new UIMessageSink()
        {
            /* (non-Javadoc)
             * @see hermes.browser.UIMessageSink#add(java.lang.String)
             */
            public void add(String message)
            {
                // NOP 
            }

            /* (non-Javadoc)
             * @see hermes.browser.UIMessageSink#add(java.lang.StringBuffer)
             */
            public void add(StringBuffer message)
            {
                // NOP
            }
        } ;   
    }

    /* (non-Javadoc)
     * @see hermes.browser.HermesUI#getDefaultMessageSink()
     */
    public UIMessageSink getDefaultMessageSink()
    {
        if (HermesBrowser.getBrowser() == null)
        {
            return defaultMessageSink ;
        }
        else
        {
            return HermesBrowser.getBrowser().getDefaultMessageSink() ;
        }
    }
    
    public ThreadPool getThreadPool()
    {
        return (ThreadPool) SingletonManager.get(ThreadPool.class) ;
    }

   public void setConfig(HermesConfig config)
   {
      this.config = config ;
   }
   
   public HermesConfig getConfig() throws HermesException
   {
      if (HermesBrowser.getBrowser() != null)
      {
         return HermesBrowser.getBrowser().getConfig() ;
      }
      else
      {
         return config ;
      }
   }
}
