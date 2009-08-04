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

package hermes;

import hermes.config.DestinationConfig;
import hermes.config.NamingConfig;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: HermesConfigurationListener.java,v 1.1 2004/05/01 15:52:22
 *          colincrist Exp $
 */

public interface HermesConfigurationListener
{
    public void onNamingAdded(NamingConfig namingConfig) ;
    
    public void onNamingRemoved(NamingConfig namingConfig) ;
    
    public void onHermesAdded(Hermes hermes);

    public void onHermesRemoved(Hermes hermes);

    public void onDestinationAdded(Hermes hermes, DestinationConfig destinationConfig);

    public void onDestinationRemoved(Hermes hermes, DestinationConfig destinationConfig);
}