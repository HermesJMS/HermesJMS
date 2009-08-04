/* 
 * Copyright 2003,2004 Peter Lee, Colin Crist
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
package hermes.ext.weblogic;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import weblogic.management.MBeanHome;
import weblogic.management.WebLogicObjectName;
import weblogic.management.runtime.JMSDestinationRuntimeMBean;

/**
 * Administration plugin for WebLogicJMS.
 * 
 * WebLogicJMSAdmin supports: (i) queue depth, (ii) display of all runtime JMS 
 * Destination statistics, and (iii) automatic queue discovery via WebLogic JMX. 
 * For the administration features to work correctly, WebLogicJMSAdminFactory
 * must be properly configured. 
 * 
 * @author leepops@sourceforge.net  last changed by: $Author $
 * @version $Id: WebLogicJMSAdmin.java,v 1.9 2005/08/15 20:37:23 colincrist Exp $
 */
public class WebLogicJMSAdmin extends HermesAdminSupport implements HermesAdmin
{
    private final static Logger log = Logger.getLogger(WebLogicJMSAdmin.class);

    private final String[] JMS_DEST_MONITOR_ATTRIB_NAMES =
        {
            "BytesCurrentCount",
            "BytesHighCount",
            "BytesPendingCount",
            "BytesReceivedCount",
            "BytesThresholdTime",
            "ConsumersCurrentCount",
            "ConsumersHighCount",
            "ConsumersTotalCount",
            "DestinationType",
            "MessagesCurrentCount",
            "MessagesHighCount",
            "MessagesPendingCount",
            "MessagesReceivedCount",
            "MessagesThresholdTime" };

    private final String JMS_DEST_NAME_ATTRIB = "Name";
    private final String JMS_DEST_JNDI_NAME_ATTRIB = "JNDIName";

    private final String[] JMS_DEST_CONFIG_ATTRIB_NAMES =
        { JMS_DEST_NAME_ATTRIB, JMS_DEST_JNDI_NAME_ATTRIB };

    private final String JMX_QUEUE_TYPE = "JMSQueue";
    private final String JMX_TOPIC_TYPE = "JMSTopic";

    private Context context;
    private WebLogicJMSAdminFactory factory;

    /**
     * JMS admin session for WebLogic Server JMS.
     */
    public WebLogicJMSAdmin(
        Hermes hermes,
        WebLogicJMSAdminFactory factory,
        Context context)
    {
        super(hermes);

        this.context = context;
        this.factory = factory;
    }

    /* (non-Javadoc)
     * @see hermes.HermesAdmin#getDepth(javax.jms.Destination)
     */
    public synchronized int getDepth(DestinationConfig dConfig) throws JMSException
    {
        Map stats = getStatistics(dConfig);

        Long i1 = (Long)stats.get("MessagesCurrentCount");
        long i2 = i1.intValue();

        return (int)i2;
    }

    /* (non-Javadoc)
     * @see hermes.HermesAdmin#close()
     */
    public void close() throws JMSException
    {
        // nop
    }

    public synchronized Collection getStatistics(Collection destinations)
        throws JMSException
    {
        final List rval = new ArrayList();

        for (Iterator iter = destinations.iterator(); iter.hasNext();)
        {
            rval.add(getStatistics((Destination)iter.next()));
        }

        return rval;
    }

    public Map getStatistics(DestinationConfig dConfig) throws JMSException
    {
       try
       {
          return getStatistics(getHermes().getDestination(dConfig.getName(), Domain.getDomain(dConfig.getDomain()))) ;
       }
       catch (NamingException ex)
       {
          throw new HermesException(ex) ;
       }
    }
    private synchronized Map getStatistics(Destination destination)
        throws JMSException
    {
        Map stats = null;

        try
        {
            JMSDestinationRuntimeMBean bean = getMBean(destination);

            AttributeList attributes =
                bean.getAttributes(JMS_DEST_MONITOR_ATTRIB_NAMES);

            stats = new TreeMap();

            for (Iterator i = attributes.iterator(); i.hasNext();)
            {
                Attribute attribute = (Attribute)i.next();
                stats.put(attribute.getName(), attribute.getValue());
            }

            log.debug(stats);

            return stats;
        }
        catch (InstanceNotFoundException e)
        {
			log.error("InstanceNotFoundException - ", e);
            throw new HermesException(e);
        }
        catch (MalformedObjectNameException e)
        {
			log.error("MalformedObjectNameException - ", e);
            throw new HermesException(e);
        }

    }

    public synchronized Collection discoverDestinationConfigs()
        throws JMSException
    {
        //
        // If accessing via JNDI then get the destinations from the context.
        
        if (getHermes().getConnectionFactory() instanceof JNDIConnectionFactory)
        {
            return super.discoverDestinationConfigs() ;
        }
        
        MBeanHome home = getHome(this.context);

        // get the MBeanServer interface            
        MBeanServer homeServer = home.getMBeanServer();

        ObjectName queueMBeanQuery = null;
        ObjectName topicMBeanQuery = null;
        try
        {
            queueMBeanQuery = getJmsQueueMBeanQuery();
            topicMBeanQuery = getJmsTopicMBeanQuery();
        }
        catch (MalformedObjectNameException e)
        {
            log.error("MalformedObjectNameException - ", e);
            throw new RuntimeException(e);
        }

        Collection queues =
            discoverDestinationConfigs(
                homeServer,
                queueMBeanQuery,
                Domain.QUEUE);
        Collection topics =
            discoverDestinationConfigs(
                homeServer,
                topicMBeanQuery,
                Domain.TOPIC);

        Collection rval = new ArrayList(queues.size() + topics.size());
        rval.addAll(queues);
        rval.addAll(topics);

        return rval;
    }

    private Collection discoverDestinationConfigs(
        MBeanServer homeServer,
        ObjectName query,
        Domain domain)
    {
        //Retrieve a list of MBeans with object names that include            
        QueryExp queryExpr = null;
        Set mBeans = null;

        mBeans = homeServer.queryNames(query, queryExpr);

        Collection rval = new ArrayList(mBeans.size());
        for (Iterator i = mBeans.iterator(); i.hasNext();)
        {
            ObjectName name = (ObjectName)i.next();
            log.debug("Matches to the MBean query: " + name.toString());

            String jndiName = null;
            String destName = null;

            try
            {
                AttributeList attributes =
                    homeServer.getAttributes(
                        name,
                        JMS_DEST_CONFIG_ATTRIB_NAMES);

                destName =
                    (String)getAttributeEx(attributes, JMS_DEST_NAME_ATTRIB);
                jndiName =
                    (String)getAttributeEx(attributes,
                        JMS_DEST_JNDI_NAME_ATTRIB);
            }
            catch (AttributeNotFoundException e)
            {
                log.error("AttributeNotFoundException - ", e);
                throw new RuntimeException(e);
            }
            catch (InstanceNotFoundException e)
            {
                log.error("InstanceNotFoundException - ", e);
                throw new RuntimeException(e);
            }
            catch (ReflectionException e)
            {
                log.error("ReflectionException - ", e);
                throw new RuntimeException(e);
            }

            DestinationConfig dConfig = new DestinationConfig();
            dConfig.setName(jndiName);
            dConfig.setShortName(destName);
            dConfig.setDomain(domain.getId());

            rval.add(dConfig);
        }

        return rval;
    }

	/**
	 * Lookup the MBeanHome from JNDI as needed.
	 * 
	 * @return
	 * @throws JMSException
	 */
	protected MBeanHome getHome(Context context) throws JMSException
	{
		try
		{
			log.debug("JNDI admin binding: " + getMBeanHomeJndiName());

			return (MBeanHome)context.lookup(getMBeanHomeJndiName());
		}
		catch (NamingException e)
		{
			log.error("NamingException - ", e);
			throw new HermesException(e);
		}

	}

	/**
	 * Returns the MBean for the JMSDestinationRuntime object representing the relevant JMS destination.
	 * @param destination
	 * @return
	 * @throws InstanceNotFoundException
	 * @throws MalformedObjectNameException
	 * @throws JMSException
	 */
	private JMSDestinationRuntimeMBean getMBean(Destination destination)
		throws InstanceNotFoundException, MalformedObjectNameException, JMSException
	{
		MBeanHome home = getHome(this.context);

		ObjectName name = getJmsDestMBeanName(destination);

		JMSDestinationRuntimeMBean bean = (JMSDestinationRuntimeMBean)home.getMBean(name);

		log.debug("Found MBean: " + bean);

		return bean;
	}

    /**
     * Returns the JNDI binding name for the MBeanHome for the relevant WebLogic server.
     * The binding name will be of the form: weblogic.management.home.&gt;server&lt;
     * @return 
     */
    private String getMBeanHomeJndiName()
    {
        return MBeanHome.JNDI_NAME + "." + this.factory.getWebLogicServer();
    }

    /**
     * Returns a JMX ObjectName to the JMSDestinationRuntime object representing the runtime MBean
     * of the destination. The object name is of the form:
     * &gt;domain&lt;:JMSServerRuntime=&gt;jmsserver&lt;,Location=&gt;server&lt;,Name=&gt;queue&lt;,ServerRuntime=&gt;server&lt;,Type=JMSDestinationRuntime
     * 
     * @param destination the JMS destination
     * @return
     * @throws MalformedObjectNameException
     */
    private ObjectName getJmsDestMBeanName(Destination destination)
        throws MalformedObjectNameException
    {
        StringBuffer buff = new StringBuffer(this.factory.getWebLogicDomain());
        buff.append(":JMSServerRuntime=");
        buff.append(this.factory.getJmsServer());
        buff.append(",Location=");
        buff.append(this.factory.getWebLogicServer());
        buff.append(",Name=");
        buff.append(destination.toString());
        buff.append(",ServerRuntime=");
        buff.append(this.factory.getWebLogicServer());
        buff.append(",Type=JMSDestinationRuntime");

        String s = buff.toString();
        log.debug("Constructed JMSDestination MBean name: " + s);

        return new WebLogicObjectName(s);
    }

    /**
     * @return JMX ObjectName query matching JMS queue destinations
     * @throws MalformedObjectNameException in the case of internal error
     */
    private ObjectName getJmsQueueMBeanQuery()
        throws MalformedObjectNameException
    {
        return getJmsDestMBeanQuery(JMX_QUEUE_TYPE);
    }

    /**
     * @return JMX ObjectName query matching JMS topic destinations
     * @throws MalformedObjectNameException in the case of internal error
     */
    private ObjectName getJmsTopicMBeanQuery()
        throws MalformedObjectNameException
    {
        return getJmsDestMBeanQuery(JMX_TOPIC_TYPE);
    }

    /**
     * @return JMX ObjectName query matching JMS topic destinations. The query is of the form:
     * &gt;domain&lt;:JMSServer=&gt;jmsserver&lt;,Type=&gt;jmsdestinationtype&lt;
     * @throws MalformedObjectNameException in the case of internal error
     */
    private ObjectName getJmsDestMBeanQuery(String destType)
        throws MalformedObjectNameException
    {
        StringBuffer buff = new StringBuffer(this.factory.getWebLogicDomain());
        buff.append(":JMSServer=");
        buff.append(this.factory.getJmsServer());
        buff.append(",Type=");
		buff.append(destType);
        buff.append(",*");

        String s = buff.toString();

        return new ObjectName(s);
    }

    private static Object getAttributeEx(AttributeList attributes, String name)
        throws AttributeNotFoundException
    {
        Object o = getAttribute(attributes, name);
        if (o == null)
            throw new AttributeNotFoundException(
                "Attribute " + name + " not found.");

        return o;
    }

    private static Object getAttribute(AttributeList attributes, String name)
    {
        Object rval = null;

        for (Iterator i = attributes.iterator(); i.hasNext();)
        {
            Attribute a = (Attribute)i.next();
            if (a.getName().equals(name))
                rval = a.getValue();
        }

        return rval;
    }

}
