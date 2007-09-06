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

package hermes.ext;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.config.DestinationConfig;

import java.util.Collection;
import java.util.Enumeration;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

import org.apache.log4j.Logger;

/**
 * Default implementation that tries to use JMS to implement some of the admin
 * functions
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultHermesAdmin.java,v 1.2 2004/07/21 19:46:16 colincrist
 *          Exp $
 */
public class DefaultHermesAdmin extends HermesAdminSupport implements HermesAdmin
{
    private static final Logger log = Logger.getLogger(DefaultHermesAdmin.class);
    private boolean sizeDialogShown = false;
    private int maxSize = 100;

    public DefaultHermesAdmin(Hermes hermes)
    {
        super(hermes);
    }

   /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#getDepth(javax.jms.Destination)
     */
    public int getDepth(DestinationConfig dest) throws JMSException
    {
        if (dest.getDomain() == Domain.QUEUE.getId())
        {
            final QueueBrowser browser = getHermes().createBrowser(dest);
            int depth = 0;

            try
            {
                for (Enumeration iter = browser.getEnumeration(); iter.hasMoreElements();)
                {
                    iter.nextElement();
                    depth++;

                    if ( depth > maxSize)
                    {
                        throw new HermesException("The default admin provider only implements queue depth functionality up to " + maxSize + " messages");
                    }
                }
            }
            finally
            {
                browser.close();
            }

            return depth;
        }
        else
        {
            throw new HermesException("Cannot get the depth of a topic without a plugin.") ;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#close()
     */
    public void close() throws JMSException
    {
        // NOP
    }
}