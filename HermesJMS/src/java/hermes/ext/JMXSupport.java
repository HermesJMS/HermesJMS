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

package hermes.ext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JMXSupport.java,v 1.1 2006/07/11 06:26:41 colincrist Exp $
 */

public class JMXSupport
{
   private static final Logger log = Logger.getLogger(JMXSupport.class);
   private JMXServiceURL serviceURL;
   private JMXConnector connector;
   private MBeanServerConnection server;
   private Map env = new HashMap();

   public JMXSupport(JMXServiceURL serviceURL)
   {

   }

   public synchronized MBeanServerConnection getMBeanServerConnection() throws IOException
   {
      if (connector == null)
      {
         connector = JMXConnectorFactory.connect(serviceURL, env);
      }

      server = connector.getMBeanServerConnection();

      return server;
   }

   public synchronized void close() throws IOException
   {
      try
      {
         if (connector != null)
         {
            connector.close();
         }
      }
      finally
      {
         server = null;
         connector = null;
      }
   }
}
