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

package hermes.ext.fiorano;

import fiorano.jms.runtime.admin.MQAdminConnection;
import fiorano.jms.runtime.admin.MQAdminService;
import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.util.Map;
import java.util.TreeMap;

import javax.jms.Destination;
import javax.jms.JMSException;

/**
 * Plugin session for FioranoMQ.
 * 
 * @author colincrist@hermesjms.com
 */
public class FioranoAdmin extends HermesAdminSupport implements HermesAdmin
{
    private static final String DELIVERABLE_MESSGAES = "deliverable";
    private static final String UNDELETED_MESSGAES = "undeleted";

    private MQAdminConnection adminConnection;
    private MQAdminService adminService;

    /**
     * @param hermes
     */
    public FioranoAdmin(Hermes hermes, MQAdminConnection adminConnection)
    {
        super(hermes);

        this.adminConnection = adminConnection;
    }

    protected synchronized MQAdminService getAdminService() throws JMSException
    {
        if ( adminService == null)
        {
            if ( adminConnection != null)
            {
                adminService = adminConnection.getMQAdminService();
            }
            else
            {
                throw new HermesException("FioranoMQ plugin is closed");
            }
        }

        return adminService;
    }
    
    

    @Override
   public int truncate(DestinationConfig dConfig) throws JMSException
   {
       final int rval = getDepth(dConfig) ;
       
       if (dConfig.getDomain() == Domain.QUEUE.getId())
       {
          getAdminService().purgeQueueMessages(getRealDestinationName(dConfig)) ;
      }
       else if (dConfig.getDomain() == Domain.TOPIC.getId() && dConfig.isDurable())
       {
          getAdminService().purgeSubscriptionMessages(getHermes().getConnection().getClientID(), dConfig.getClientID()) ;
       }
       else
       {
          throw new HermesException("Domain for " + dConfig.getName() + " is unknown.") ;
       }
       
       return rval ;
   }

   /*
     * (non-Javadoc)
     * 
     * @see hermes.ext.HermesAdminSupport#getDepth(javax.jms.Destination)
     */
    public int getDepth(DestinationConfig dConfig) throws JMSException
    {
        if (dConfig.getDomain() == Domain.QUEUE.getId())
        {
            return (int) getAdminService().getNumberOfDeliverableMessages(getRealDestinationName(dConfig));
        }
        else
        {
            final String clientId = getHermes().getConnection().getClientID();
            final String subId = dConfig.getClientID();
            
            return (int) getAdminService().getNumberOfDeliverableMessages(clientId, subId) ;
        }
    }

    public Map getStatistics(DestinationConfig dConfig) throws JMSException
    {
        final Map<String, Object> map = new TreeMap<String, Object>();
        final Domain domain = Domain.getDomain(dConfig.getDomain());

        if ( domain == Domain.QUEUE)
        {
            map.put(DELIVERABLE_MESSGAES, new Long(getAdminService().getNumberOfDeliverableMessages(dConfig.getName())));
            map.put(UNDELETED_MESSGAES, new Long(getAdminService().getNumberOfUndeletedMessages(dConfig.getName())));
        }
        else if (dConfig.isDurable())
        {
            final String clientId = getHermes().getConnection().getClientID();
            final String subId = dConfig.getClientID();

            map.put(DELIVERABLE_MESSGAES, new Long(getAdminService().getNumberOfDeliverableMessages(dConfig.getName(), subId)));
        }

        return map;
    }

    public int truncate(Destination destination) throws JMSException
    {
        final String queueName = getHermes().getDestinationName(destination);
        final long endIndex = getAdminService().getNumberOfDeliverableMessages(queueName);

        getAdminService().purgeQueueMessages(queueName);
        return (int) endIndex;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#close()
     */
    public synchronized void close() throws JMSException
    {
        try
        {
            adminConnection.close();
        }
        finally
        {
            adminConnection = null;
            adminService = null;
        }
    }
}
