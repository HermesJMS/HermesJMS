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
import hermes.browser.tasks.ThreadPool;
import hermes.config.HermesConfig;

/**
 * Abstract interface into the user interface so Hermes can support more than one UI, e.g. an IDE
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: HermesUI.java,v 1.2 2004/11/17 17:22:38 colincrist Exp $
 */
public interface HermesUI
{    
    public UIMessageSink getDefaultMessageSink() ;
    
    public ThreadPool getThreadPool() ;
    
    public HermesConfig getConfig() throws HermesException;
    
    public void setConfig(HermesConfig config) ;
    
}
