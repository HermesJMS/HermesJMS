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

import hermes.browser.IconCache;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

/**
 * A JMS domain object, i.e. a queue or a topic.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: Domain.java,v 1.10 2006/04/12 13:44:09 colincrist Exp $
 */
public class Domain
{
   private static final Logger log = Logger.getLogger(Domain.class) ;

   /*
    * Domain information for destinations
    */
   public static final Domain QUEUE = new Domain("QUEUE", 1, IconCache.getIcon("jms.queue"));
   public static final Domain TOPIC = new Domain("TOPIC", 2, IconCache.getIcon("jms.topic"));
   public static final Domain UNKNOWN = new Domain("UNKNOWN", 3, IconCache.getIcon("jms.queueOrTopic"));
   private String text;
   private int id;
   private Icon icon;

   /**
    *  
    */
   Domain(String text, int id, ImageIcon icon)
   {
      super();

      this.text = text;
      this.id = id;
      this.icon = icon;
   }

   public Icon getIcon()
   {
      return icon;
   }

   public int getId()
   {
      return id;
   }

   public String toString()
   {
      return text;
   }

   public int hashCode()
   {
      return text.hashCode();
   }

   public boolean equals(Object other)
   {
      if (other instanceof Domain)
      {
         Domain d = (Domain) other;

         return d.getId() == id;
      }

      return false;
   }

   public static Domain getDomain(Destination destination)
   {
      if (destination instanceof Queue && destination instanceof Topic)
      {
         //
         // This is an interesting hack to deal with WebLogic as it implements both domains. If we see the object 
         // is somewhere in the WLS JMS packages then see if we can get the "topic" property. We must do this dynamically
         // as it may be loaded in a different class loader (so instanceof will fail) AND we don't want this part of the
         // Hermes codebase to be coupled to any provider.
         
         if (destination.getClass().getName().startsWith("weblogic.jms"))
         {
            try
            {
               final Boolean isTopic = (Boolean) PropertyUtils.getProperty(destination, "topic");

               return isTopic ? Domain.TOPIC : Domain.QUEUE;
            }
            catch (Throwable e)
            {
               log.error(e.getMessage(), e) ;
               
               return Domain.UNKNOWN ;
            }
         }
         else
         {
            return Domain.UNKNOWN;
         }
      }
      else if (destination instanceof Queue)
      {
         return Domain.QUEUE;
      }
      else
      {
         return Domain.TOPIC;
      }
   }

   public static Domain getDomain(int encoding)
   {
      if (encoding == 1)
      {
         return QUEUE;
      }
      else if (encoding == 2)
      {
         return TOPIC;
      }
      else
      {
         return UNKNOWN;
      }
   }
}