/* 
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
package hermes.ext.arjuna;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.arjuna.ams.admin.Admin;
import com.arjuna.ams.admin.AdminException;
import com.arjuna.ams.admin.AuthorizationAdminException;
import com.arjuna.ams.admin.StatisticalProperties;

/**
 * Administration plugin for ArjunaMS.
 * 
 * @author arnaud.simon@arjuna.com, colincrist@hermesjms.com  last changed by: $Author: colincrist $
 * @version $Id: ArjunaMSAdmin.java,v 1.8 2005/08/15 20:37:32 colincrist Exp $
 */
public class ArjunaMSAdmin extends HermesAdminSupport implements HermesAdmin
{
    private static final Logger log = Logger.getLogger(ArjunaMSAdmin.class);
    private static final String NAME = "Name";
    private Context context;
    private Admin admin;
    private ArjunaMSAdminFactory factory;

    /**
     * 
     */
    public ArjunaMSAdmin(Hermes hermes, ArjunaMSAdminFactory factory, Context context)
    {
        super(hermes);

        this.context = context;
        this.factory = factory;
    }

    /**
     * Lookup the admin from JNDI as needed.
     * 
     * @return
     * @throws JMSException
     */
    protected synchronized Admin getAdmin() throws JMSException
    {
        try
        {
            if (admin == null)
            {
                admin = (Admin) context.lookup(factory.getAdminBinding());
            }
        }
        catch (NamingException ex)
        {
            throw new HermesException(ex);
        }

        return admin ;
    }

   /* (non-Javadoc)
     * @see hermes.HermesAdmin#getDepth(javax.jms.Destination)
     */
    public int getDepth(DestinationConfig dest) throws JMSException
    {
        try
        {
            final Map stats = getAdmin().getDestinationStatistics(getRealDestinationName(dest));
            return ((Integer) stats.get(StatisticalProperties.NUM_MESSAGES_OUTSTANDING)).intValue();
        }
        catch (AdminException e)
        {
            throw new HermesException(e);
        }
    }

    /* (non-Javadoc)
     * @see hermes.HermesAdmin#close()
     */
    public void close() throws JMSException
    {
        if (admin != null)
        {
            admin.close();
            admin = null;
        }
    }

   
    public Map getStatistics(DestinationConfig destination) throws JMSException
    {
        try
        {
            return getAdmin().getDestinationStatistics(getRealDestinationName(destination));
        }
        catch (Exception e)
        {
            throw new HermesException(e);
        }
    }

    
    private DestinationConfig createDestinationConfig(Hashtable info, Domain domain)
    {
    	DestinationConfig dConfig = new DestinationConfig();
    	String name = (String) info.get(NAME);
    	
    	dConfig.setName(name);
        dConfig.setDomain(domain.getId());
        
    	return dConfig ;
    }
    
  
   public Collection discoverDestinationConfigs() throws JMSException
    {
        try
        {
            if (getHermes().getConnectionFactory() instanceof JNDIConnectionFactory)
            {
                return super.discoverDestinationConfigs() ;
            }
            
            Collection rval = new ArrayList();

            if (getHermes().getConnectionFactory() instanceof QueueConnectionFactory)
            {
                Hashtable[] queueDetails = getAdmin().getAllQueuesDetails();

                for (int i = 0; i < queueDetails.length; i++)
                {
                    Hashtable t = (Hashtable) queueDetails[i];
 
                    rval.add(createDestinationConfig(t, Domain.QUEUE));
                }
            }
            else if (getHermes().getConnectionFactory() instanceof TopicConnectionFactory)
            {
                Hashtable[] topicDetails = getAdmin().getAllTopicsDetails();

                for (int i = 0; i < topicDetails.length; i++)
                {
                    Hashtable t = (Hashtable) topicDetails[i];
                    
                    rval.add(createDestinationConfig(t, Domain.TOPIC));
                    rval.addAll(discoverDurableSubscriptions((String) t.get(NAME), null)) ;
                }
            }

            return rval;
        }
        catch (AuthorizationAdminException e)
        {
            throw new HermesException(e);
        }
        catch (AdminException e)
        {
            throw new HermesException(e);
        }
    }
}