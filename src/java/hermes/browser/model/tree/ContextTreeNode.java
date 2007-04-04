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

package hermes.browser.model.tree;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesException;
import hermes.JNDIContextFactory;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.config.NamingConfig;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermejms.com last changed by: $Author: colincrist $
 * @version $Id: ContextTreeNode.java,v 1.23 2006/09/16 15:49:25 colincrist Exp $
 */
public class ContextTreeNode extends AbstractTreeNode
{
   private static final Logger log = Logger.getLogger(ContextTreeNode.class);
   private NamingConfig config;
   private boolean isNameInNamespaceCheck = true ;

   /**
    * 
    */
   private ContextTreeNode(Set<String> visited, String id, NamingConfig config, Context context) throws HermesException
   {
      super(id, context);

      this.config = config;

      setIcon(IconCache.getIcon("hermes.tree.folder.opened"));
      setContext(visited, context);
   }

   public ContextTreeNode(String id, NamingConfig config, Context context) throws HermesException
   {
      this(new HashSet<String>(), id, config, context);
   }

   public NamingConfig getConfig()
   {
      return config;
   }

   public JNDIContextFactory getContextFactory() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException
   {
      return new JNDIContextFactory(config);
   }

   private void setContext(Set<String> visited, Context context) throws HermesException
   {
      try
      {
         Hermes.ui.getDefaultMessageSink().add("Searching context " + getId() + "...");
         log.debug("Searching context " + getId() + "...");

        try
        {
            visited.add(context.getNameInNamespace());
        }
        catch (OperationNotSupportedException ex)
        {
           // The JNDI provider does not support this operation so we cannot check for duplicates.
           
           isNameInNamespaceCheck = false ;
        }

         for (final NamingEnumeration<NameClassPair> iter = context.list(""); iter.hasMore();)
         {
            final NameClassPair entry = iter.next();

            AbstractTreeNode child = null;

            try
            {
               final Object object = context.lookup(entry.getName());

               log.debug("found " + entry.getClassName() + " bound at " + entry.getName());

               if (object instanceof Destination)
               {
                  final Domain domain = Domain.getDomain((Destination) object);

                  if (domain == Domain.TOPIC)
                  {
                     child = new TopicTreeNode(entry.getName(), (Topic) object);
                  }
                  else if (domain == Domain.QUEUE)
                  {
                     child = new QueueTreeNode(entry.getName(), (Queue) object);
                  }
                  else
                  {
                     child = new QueueTopicTreeNode(entry.getName(), (Destination) object);
                  }
               }
               else if (object instanceof ConnectionFactory)
               {
                  child = new ConnectionFactoryTreeNode(entry.getName(), (ConnectionFactory) object);
               }
               else if (object instanceof Context)
               {
                  if (isNameInNamespaceCheck && visited.contains(((Context) object).getNameInNamespace()))
                  {
                     log.debug("skipping already visited context " + getId());
                  }
                  else
                  {
                     child = new ContextTreeNode(visited, entry.getName(), config, (Context) object);
                  }
               }
               else
               {
                  child = new UnknownTreeNode(entry.getName(), object);
               }
            }
            catch (Throwable ex)
            {
               child = new ExceptionTreeNode(entry.getName(), ex);
            }

            if (child != null)
            {
               add(child);
            }
         }
      }
      catch (NamingException ex)
      {
         log.error(ex.getMessage(), ex);

         if (ex.getCause() != null)
         {
            Hermes.ui.getDefaultMessageSink().add(ex.getCause().getMessage());
         }
         else
         {
            Hermes.ui.getDefaultMessageSink().add(ex.getMessage());
         }

         throw new HermesException(ex);
      }
      finally
      {
         try
         {
            context.close();
         }
         catch (Throwable e)
         {
            HermesBrowser.getBrowser().showErrorDialog(e.getClass().getName() + " thrown when closing the Context", e);
         }
      }
   }
}