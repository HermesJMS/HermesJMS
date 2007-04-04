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

package hermes.impl;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.browser.MessageRenderer;
import hermes.config.DestinationConfig;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;
import javax.naming.NamingException;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: HermesAdminAdapter.java,v 1.1 2004/07/30 17:25:14 colincrist
 *          Exp $
 */
public class HermesAdminAdapter implements HermesAdmin
{
    private Hermes hermes;
    private HermesAdmin admin;
    private HermesAdminFactory extension;

    /**
     *  
     */
    public HermesAdminAdapter(Hermes hermes, HermesAdminFactory extension)
    {
        super();

        this.hermes = hermes;
        this.extension = extension;
    }

    private synchronized HermesAdmin getAdmin() throws JMSException
    {
        if (admin == null)
        {
            try
            {              
                admin = extension.createSession(hermes, hermes.getConnectionFactory());
            }
            catch (NamingException e)
            {
                throw new HermesException(e);
            }
        }

        return admin;
    }
    
    
    
    public Enumeration createBrowserProxy(Enumeration iter) throws JMSException
    {
       return getAdmin().createBrowserProxy(iter) ;
    }
    
    public Collection discoverDestinationConfigs() throws JMSException
    {
        return getAdmin().discoverDestinationConfigs() ;
    }

    public synchronized void close() throws JMSException
    {
        try
        {
            if (admin != null)
            {
                admin.close();
            }
        }
        finally
        {
            admin = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#getDepth(javax.jms.Destination)
     */
    public int getDepth(DestinationConfig dest) throws JMSException
    {
        return getAdmin().getDepth(dest);
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#getAge(javax.jms.Destination)
     */
    public long getAge(DestinationConfig dest) throws JMSException
    {
        return getAdmin().getAge(dest);
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#truncate(javax.jms.Destination)
     */
    public int truncate(DestinationConfig dest) throws JMSException
    {
        return getAdmin().truncate(dest);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#getStatistics(javax.jms.Destination)
     */
    public Map getStatistics(DestinationConfig destination) throws JMSException
    {
        return getAdmin().getStatistics(destination);
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#getStatistics(java.util.Collection)
     */
    public Collection getStatistics(Collection destinations) throws JMSException
    {
        return getAdmin().getStatistics(destinations);
    }

    public MessageRenderer getMessageRenderer() throws JMSException
    {
       return getAdmin().getMessageRenderer() ;
    }

   public QueueBrowser createDurableSubscriptionBrowser(DestinationConfig dConfig) throws JMSException
   {
     return getAdmin().createDurableSubscriptionBrowser(dConfig) ;
   }
}