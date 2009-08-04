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
 */

package hermes.browser.components;

import hermes.Hermes;
import hermes.JNDIContextFactory;
import hermes.SingletonManager;
import hermes.browser.model.tree.ContextTreeModel;
import hermes.browser.model.tree.ContextTreeNode;
import hermes.config.NamingConfig;
import hermes.impl.ClassLoaderManager;
import hermes.impl.LoaderSupport;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * A factory for creating a ContextTree from a configuration bean.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ContextTreeModelFactory.java,v 1.9 2005/10/21 08:37:22 colincrist Exp $
 */
public class ContextTreeModelFactory
{
   public static final ContextTreeModel create(NamingConfig namingConfig) throws JMSException, NamingException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException
   {
        
       Hermes.ui.getDefaultMessageSink().add("Creating InitialContext for "+ namingConfig.getId()) ;
       
       //
       // Create and populate the context bean...
       
       JNDIContextFactory contextBean = new JNDIContextFactory() ;
       ClassLoaderManager classLoaderManager = (ClassLoaderManager) SingletonManager.get(ClassLoaderManager.class) ;
       
       contextBean._setDelegateClassLoader(classLoaderManager.getClassLoader(namingConfig.getClasspathId())) ;
      
       LoaderSupport.populateBean(contextBean, namingConfig.getProperties()) ;
       
       Context rootContext = contextBean.createContext() ;
       
       ContextTreeNode rootNode = new ContextTreeNode(namingConfig.getId(), namingConfig, rootContext) ;
       ContextTreeModel model = new ContextTreeModel(contextBean, rootNode) ;
       
       Hermes.ui.getDefaultMessageSink().add("Finished searching InitialContext "+ namingConfig.getId()) ;
      
       return model ;
   }
}
