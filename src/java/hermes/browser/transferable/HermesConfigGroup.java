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

package hermes.browser.transferable;

import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;

import java.util.Collection;

/**
  * @author colincrist@hermesjms.com
  * @version $Id: HermesConfigGroup.java,v 1.1 2005/05/26 17:45:42 colincrist Exp $
  */

public class HermesConfigGroup
{
    private Collection<DestinationConfig> destinations;
    private Collection<FactoryConfig> factories;
    private String hermesId;

    public HermesConfigGroup(String hermesId, Collection<DestinationConfig> destinations, Collection<FactoryConfig> factories)
    {
        this.destinations = destinations;
        this.factories = factories;
        this.hermesId = hermesId;
    }

    public Collection<DestinationConfig> getDestinations()
    {
        return destinations;
    }

    public Collection<FactoryConfig> getFactories()
    {
        return factories;
    }

    public String getHermesId()
    {
        return hermesId;
    }
}