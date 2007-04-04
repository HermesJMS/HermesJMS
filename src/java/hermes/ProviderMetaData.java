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

import javax.jms.ConnectionMetaData;
import javax.jms.JMSException;

/**
 * A ProviderMetaData contains and extends the standard JMS metadata
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ProviderMetaData.java,v 1.3 2004/09/16 20:30:41 colincrist Exp $
 */

public interface ProviderMetaData
{
    /**
     * Get a short name for this provider
     */
    public String getShortName() throws HermesException;

    /**
     * Get a string suitable for use as a tooltip text
     */
    public String getToolTipText() throws HermesException;

    /**
     * Get some debug text describing this provider
     */
    public String getDebugText() throws HermesException;

    /**
     * Get the connection metadata
     */
    public ConnectionMetaData getConnectionMetaData() throws JMSException;

    /**
     * Is Connection sharing in use? i.e. Is there a shared Connection for all
     * threads to use or is there a Connection per thread?
     */
    public boolean getConnectionSharing();
}