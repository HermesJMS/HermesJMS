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

package hermes;

import hermes.config.DestinationConfig;

import java.util.Date;
import java.util.Map;

/**
 * Callback interface for watching queue and topic statistics.
 * 
 * @author colincrist@hermesjms.com
 */
public interface HermesWatchListener
{
    /**
     * Called when the depth of a queue or durable sub changes changes.
     * 
     * @param hermes
     * @param from
     * @param depth
     */
    public void onDepthChange(Hermes hermes, DestinationConfig dConfig, long depth) ;
    
    
    /** 
     * Called when the age of the oldest message changes.
     * 
     * @param hermes
     * @param from
     * @param oldest
     */
    public void onOldestMessageChange(Hermes hermes, DestinationConfig dConfig, Date oldest) ;
    
    /**
     * Called when Hermes discovers an exception whilst processing the watch. It will continue to try and implement the
     * watch request.
     * 
     * @param hermes
     * @param destination
     * @param e
     */
    public void onException(Hermes hermes, DestinationConfig dConfig, Exception e) ;
    
    /**
     * Called when any of the provider specific properties change. Only supported when a plugin is available.
     * @param hermes
     * @param from
     * @param properties
     */
    public void onPropertyChange(Hermes hermes, DestinationConfig dConfig, Map properties) ;
}
