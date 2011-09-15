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

package hermes.renderers;

import hermes.HermesException;
import hermes.SystemProperties;
import hermes.browser.HermesBrowser;
import hermes.browser.MessageRenderer;
import hermes.config.DestinationConfig;
import hermes.config.HermesConfig;
import hermes.config.RendererConfig;
import hermes.config.SessionConfig;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

/**
 * Mangages a collection of renderers.
 *
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: RendererManager.java,v 1.7 2006/04/28 09:59:37 colincrist Exp $
 */
public class RendererManager
{
   private static final Logger log = Logger.getLogger(RendererManager.class);
   private List<MessageRenderer> renderers = new ArrayList<MessageRenderer>();

   private Map<String, MessageRenderer> renderersByClass = new HashMap<String, MessageRenderer>();
   private Map<String, MessageRenderer> renderersByName = new HashMap<String, MessageRenderer>();

   /**
    *
    */
   public RendererManager()
   {
   }

   public MessageRenderer createRenderer(ClassLoader classLoader, RendererConfig rConfig) throws InvocationTargetException, HermesException,
         InstantiationException, IllegalAccessException, ClassNotFoundException
   {
      Thread.currentThread().setContextClassLoader(classLoader);
      MessageRenderer renderer = (MessageRenderer) classLoader.loadClass(rConfig.getClassName()).newInstance();
      MessageRenderer.Config rendererConfig = renderer.createConfig();

      if (rendererConfig != null)
      {
         Properties rendererProperties = HermesBrowser.getConfigDAO().getRendererProperties(rConfig);

         BeanUtils.populate(rendererConfig, rendererProperties);
      }

      renderer.setConfig(rendererConfig);

      return renderer;
   }

   public void setConfig(ClassLoader classLoader, HermesConfig config) throws HermesException
   {
      boolean gotDefaultRenderer = false;

      for (Iterator iter = config.getRenderer().iterator(); iter.hasNext();)
      {
         RendererConfig rConfig = (RendererConfig) iter.next();

         try
         {
            MessageRenderer renderer = createRenderer(classLoader, rConfig);

            if (renderer.getClass().getName().equals(DefaultMessageRenderer.class.getName()))
            {
               gotDefaultRenderer = true;
            }

            renderersByClass.put(rConfig.getClassName(), renderer);
            renderersByName.put(renderer.getDisplayName(), renderer);
         }
         catch (Throwable t)
         {
            log.error("cannot load renderer " + rConfig.getClassName() + ": " + t.getMessage(), t);

            if (HermesBrowser.getBrowser() != null)
            {
               JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), "Cannot load renderer " + rConfig.getClassName() + ":\n" + t.getClass().getName()
                     + "\n" + t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
         }

      }

      renderers.clear();

      //
      // Handle upgrades to Hermes 1.6 where this may be missing.

      if (!gotDefaultRenderer)
      {
         RendererConfig rConfig = new RendererConfig();

         rConfig.setClassName(DefaultMessageRenderer.class.getName());

         config.getRenderer().add(rConfig);

         renderers.add(new DefaultMessageRenderer());
      }

      final StringTokenizer rendererClasses = new StringTokenizer(System.getProperty(SystemProperties.RENDERER_CLASSES,
            SystemProperties.DEFAULT_RENDERER_CLASSES), ",");

      while (rendererClasses.hasMoreTokens())
      {
         final String rendererClassName = rendererClasses.nextToken();

         if (renderersByClass.containsKey(rendererClassName))
         {
            renderers.add(renderersByClass.get(rendererClassName));
         }
         else
         {
            try
            {
               MessageRenderer renderer = (MessageRenderer) Class.forName(rendererClassName).newInstance() ;
               renderers.add(renderer);

               renderersByClass.put(rendererClassName, renderer);
               renderersByName.put(renderer.getDisplayName(), renderer);
            }
            catch (Throwable t)
            {
               log.error("cannot instantiate renderer: " + rendererClassName + ": " + t.getMessage(), t);
            }
         }
      }

      log.debug("renderer chain:") ;

      for (MessageRenderer r : renderers)
      {
         log.debug(r.getDisplayName() + ": " + r.getClass().getName()) ;
      }

      for (Iterator hIter = HermesBrowser.getConfigDAO().getAllSessions(config).iterator(); hIter.hasNext();)
      {
         SessionConfig sConfig = (SessionConfig) hIter.next();

         for (Iterator iter2 = HermesBrowser.getConfigDAO().getAllDestinations(config, sConfig.getId()).iterator(); iter2.hasNext();)
         {
            DestinationConfig dConfig = (DestinationConfig) iter2.next();

            if (dConfig.getRenderer() != null)
            {
               // @@TODO Remove the old destination specific renderers.

               dConfig.setRenderer(null) ;
            }
         }
      }
   }

   public Collection<MessageRenderer> getRenderers()
   {
      return renderers;
   }

   public void addRenderer(MessageRenderer renderer) {
       renderers.add(renderer);
       renderersByName.put(renderer.getDisplayName(), renderer);
       renderersByClass.put(renderer.getClass().getName(), renderer);
   }

   public MessageRenderer getRendererByName(String displayName)
   {
      return (MessageRenderer) renderersByName.get(displayName);
   }

   public MessageRenderer getRendererByClass(String className)
   {
      return (MessageRenderer) renderersByClass.get(className);
   }
}