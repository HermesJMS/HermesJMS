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

package test.jmx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;
import sun.management.ConnectorAddressLink;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JMXClientTest.java,v 1.1 2006/07/11 06:26:39 colincrist Exp $
 */

public class JMXClientTest extends TestCase
{
   private static final Logger log = Logger.getLogger(JMXClientTest.class);
   private JMXServiceURL serviceURL;
   private JMXConnector connector;
   private MBeanServerConnection server;

   protected void setUp() throws Exception
   {
      Map map = new HashMap();
      serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1616/jmxrmi");
      connector = JMXConnectorFactory.connect(serviceURL, map);
      server = connector.getMBeanServerConnection();

   }

   public void X_testMonitoredHost() throws Exception
   {
      int longestCmdLineLength = 255;
      String longestCmdLine = null;
      Set activeVms;
      MonitoredHost host;
      try
      {
         host = MonitoredHost.getMonitoredHost("service:jmx:rmi:///jndi/rmi://localhost:1616/jmxrmi");
         activeVms = host.activeVms();
      }
      catch (java.net.URISyntaxException sx)
      {
         throw new InternalError(sx.getMessage());
      }
      catch (sun.jvmstat.monitor.MonitorException mx)
      {
         throw new InternalError(mx.getMessage());
      }

      for (Object vm : activeVms)
      {
         try
         {
            int vmid = (Integer) vm;
            String address = ConnectorAddressLink.importFrom(vmid);
            if (address == null)
            {
               // not managed
               continue;
            }
            VmIdentifier vmId = new VmIdentifier(Integer.toString(vmid));
            String cmdLine = MonitoredVmUtil.commandLine(host.getMonitoredVm(vmId));
            int len = cmdLine.length();
            if (len > longestCmdLineLength)
            {
               longestCmdLineLength = len;
               longestCmdLine = cmdLine;
            }
            log.debug(vmid + " : " + address + ": " + cmdLine);
         }
         catch (Exception x)
         {
         }
      }
   }

   private void handleObject(ObjectName o) throws IOException, AttributeNotFoundException, MBeanException
   {
      

      try
      {
         MBeanInfo info = server.getMBeanInfo(o);
         if (o.getDomain().startsWith("org.apache.activemq"))
         {
            log.info(o.toString() + ": " + info.getDescription());

            log.info("attributes:") ;
            
            for (MBeanAttributeInfo beanInfo : info.getAttributes())
            {
               log.info(beanInfo.getName() + " (" + beanInfo.getType() + ") " + beanInfo.getDescription()) ;
               
               if (beanInfo.getType().equals("[Ljavax.management.ObjectName;"))
               {
                  ObjectName[] nested =  (ObjectName[]) server.getAttribute(o, beanInfo.getName()) ;
                  
                  for (ObjectName n : nested)
                  { 
                     handleObject(n ) ;
                     
                  }
                 
                    
                 
                  
               }
            }
            
            log.info("notifications:");
            
            for (MBeanNotificationInfo beanInfo : info.getNotifications())
            {
               log.info(beanInfo.getName() + ": " + beanInfo.getDescription());
            }

            log.info("operations:");
            
            for (MBeanOperationInfo beanInfo : info.getOperations())
            {
               log.info(beanInfo.getName() + ": " + beanInfo.getDescription());
            }
            
            

         }
      }
      catch (IntrospectionException e)
      {
         log.error(e.getMessage(), e);
      }
      catch (InstanceNotFoundException e)
      {
         log.error(e.getMessage(), e);
      }
      catch (ReflectionException e)
      {
         log.error(e.getMessage(), e);
      }
   }
      

  
   public void testGetBeans() throws Exception
   {
      ObjectName name = null;
      Set mbeans = server.queryNames(name, null);
      Map<ObjectName, MBeanInfo> result = new HashMap<ObjectName, MBeanInfo>(mbeans.size());

      for (Iterator iterator = mbeans.iterator(); iterator.hasNext();)
      {
         Object object = iterator.next();

         if (object instanceof ObjectName)
         {
            handleObject(((ObjectName) object)) ;
         }
      }
   }
}
