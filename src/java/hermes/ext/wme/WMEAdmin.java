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

package hermes.ext.wme;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.jms.JMSException;

import org.apache.log4j.Logger;

import COM.activesw.api.client.BrokerAdminClient;
import COM.activesw.api.client.BrokerEvent;
import COM.activesw.api.client.BrokerException;
import COM.activesw.api.client.BrokerField;

import com.wm.broker.jms.QueueAdmin;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: WMEAdmin.java,v 1.8 2005/08/21 20:48:11 colincrist Exp $
 */
public class WMEAdmin extends HermesAdminSupport implements HermesAdmin
{
    private static final Logger log = Logger.getLogger(WMEAdmin.class) ;
    private static final String QUEUE_LENGTH_FIELD = "queueLength";
    private BrokerAdminClient brokerClient;

    public WMEAdmin(Hermes hermes, BrokerAdminClient brokerClient)
    {
        super(hermes);

        this.brokerClient = brokerClient;
    }

    private BrokerAdminClient getBrokerClient() throws JMSException
    {
        if (brokerClient == null)
        {
            throw new HermesException("No BrokerClient");
        }

        return brokerClient;
    }

    /* (non-Javadoc)
     * @see hermes.ProviderExtensionSession#size(javax.jms.Destination)
     */
    public int getDepth(DestinationConfig destination) throws JMSException
    {
        try
        {
           if (destination.getDomain() == Domain.QUEUE.getId())
           {
            synchronized (this)
            {
                final BrokerEvent clientStats = getBrokerClient().getClientStatsById(getRealDestinationName(destination));
                final BrokerField field = clientStats.getField(QUEUE_LENGTH_FIELD) ;
                
                return Integer.parseInt(field.value.toString()) ;
            }
           }
           else
           {
              throw new HermesException("WebMethods plugin can only get depth in the queue domain.") ;
           }
        }
        catch (BrokerException e)
        {
            throw new HermesException(e);
        }
    }

    /* (non-Javadoc)
     * @see hermes.ProviderExtensionSession#close()
     */
    public void close() throws JMSException
    {
        try
        {
            synchronized (this)
            {
                brokerClient.disconnect();
                brokerClient = null;
            }
        }
        catch (BrokerException e)
        {
            throw new HermesException(e);
        }
    }

    public Iterator getDestinations() throws JMSException
    {
        Collection rval = new ArrayList();

        return rval.iterator();
    }

    public int truncate(DestinationConfig destination) throws JMSException
    {
        try
        {
            QueueAdmin queueAdmin = (QueueAdmin) getHermes().getSession().createQueue(destination.getName()) ; 
            
            synchronized (this)
            {
                int rval = getDepth(destination);
                brokerClient.clearClientQueueById(queueAdmin.getName());
                
                log.debug("truncated queue=" + queueAdmin.getName() + ", size=" + rval) ;
                return rval;
            }
        }
        catch (BrokerException e)
        {
            throw new HermesException(e);
        }
    }
    
    public Map getStatistics(DestinationConfig destination) throws JMSException
    {
        Map rval = new TreeMap() ;
        
        try
        {
            QueueAdmin queueAdmin = (QueueAdmin) getHermes().getSession().createQueue(destination.getName()) ; 

            synchronized (this)
            {
                final BrokerEvent clientStats = getBrokerClient().getClientStatsById(queueAdmin.getName());

                String[] fieldNames = clientStats.getFieldNames(null) ;
                
                for (int i = 0 ; i < fieldNames.length ; i++)
                {
                    BrokerField field = clientStats.getField(fieldNames[i]) ;
                    
                    if (field.value != null)
                    {
                        rval.put(fieldNames[i], field.value.toString()) ;
                    }
                }
            } 
            
            return rval ;
        }
        catch (BrokerException e)
        {
            throw new HermesException(e);
        }
    }
}