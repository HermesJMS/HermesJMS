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
package hermes.browser;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: IconCache.java,v 1.14 2006/07/13 07:35:32 colincrist Exp $
 */
public class IconCache
{
   private static final Logger log = Logger.getLogger(IconCache.class);
   private static final String PROPERTIES = "hermes/browser/icons/iconcache.properties";

   public static final String TOPIC = "hermes/browser/icons/topic.gif";
   public static final String QUEUE = "hermes/browser/icons/queue.gif";
   public static final String QUEUE_OR_TOPIC = "hermes/browser/icons/queueOrTopic.gif";
   public static final String UNKNOWN = "hermes/browser/icons/queueOrTopic.gif";
   public static final String CONNECTION_FACTORY = "hermes/browser/icons/connectionFactory.gif";
   public static final String XML_FILE = "hermes/browser/icons/xml_message_file.gif";
   public static final String HERMES_OPEN = "hermes/browser/icons/hermes_folder_open.gif";
   public static final String HERMES_CLOSED = "hermes/browser/icons/hermes_folder_closed.gif";
   public static final String FORM_BANNER = "hermes/browser/icons/form_banner.gif";
   public static final String JNDI = "hermes/browser/icons/jndi.gif";
   public static final String NEW_CONTEXT = "hermes/browser/icons/new_context.gif";
   public static final String ERROR = "hermes/browser/icons/error.gif";
   public static final String JNDI_LARGE = "hermes/browser/icons/jndi_large.gif";
   public static final String COPY_MESSAGES = "toolbarButtonGraphics/general/Copy16.gif";
   public static final String REMOVE_MESSAGES = "toolbarButtonGraphics/general/Remove16.gif";
   public static final String SEND_MESSAGES = "toolbarButtonGraphics/general/Import16.gif";
   public static final String WATCH_QUEUES = "toolbarButtonGraphics/general/Search16.gif";
   public static final String BROWSER_TREE = "toolbarButtonGraphics/general/Search16.gif";

   private static Map<String, ImageIcon> cacheById = new HashMap<String, ImageIcon>();
   private static Map<String, ImageIcon> cacheByLocation = new HashMap<String, ImageIcon>();

   static
   {
      Properties properties = null;

      try
      {
         properties = new Properties();
         properties.load(IconCache.class.getClassLoader().getResource(PROPERTIES).openStream());
      }
      catch (IOException e)
      {
         log.fatal("cannot load " + PROPERTIES + ": " + e.getMessage(), e);
      }

      if (properties != null)
      {
         for (Iterator iter = properties.keySet().iterator(); iter.hasNext();)
         {
            final String id = (String) iter.next();
            final String location = (String) properties.get(id);

            try
            {
               ImageIcon icon = getIcon(location);
               cacheById.put(id, icon);
            }

            catch (RuntimeException e)
            {
               log.error("cannot load image id=" + id + " from " + location + ": " + e.getMessage(), e);
            }
         }
      }
   }

  

   /**
    * Get the Icon as a system resource (i.e. from JAR/ZIP file)
    */
   public static ImageIcon getIcon(String imageName)
   {
      synchronized (cacheById)
      {
         if (cacheById.containsKey(imageName))
         {
            return cacheById.get(imageName);
         }
      }

      synchronized (cacheByLocation)
      {
         ImageIcon icon = cacheByLocation.get(imageName);

         if (icon == null)
         {
            URL imgURL = IconCache.class.getClassLoader().getResource(imageName);

            if (imgURL == null)
            {
               icon = new ImageIcon(imageName);
            }
            else
            {
               icon = new ImageIcon(imgURL);
            }

            cacheByLocation.put(imageName, icon);
         }

         return icon;
      }
   }
}