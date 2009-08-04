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

package hermes.browser.model.tree;

import hermes.browser.IconCache;
import hermes.config.JDBCStore;
/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageStoreURLTreeNode.java,v 1.2 2006/07/13 07:35:33 colincrist Exp $
 */

public class MessageStoreURLTreeNode extends AbstractTreeNode
{
   private JDBCStore storeConfig ;
   
   public MessageStoreURLTreeNode(JDBCStore storeConfig) throws ClassNotFoundException
   {
      super(storeConfig.getAlias(), storeConfig.getUrl()) ;
      
      this.storeConfig = storeConfig ;
      
      setIcon(IconCache.getIcon("jdbc")) ;
      
      if (storeConfig.getDriver() != null)
      {
         Class.forName(storeConfig.getDriver()) ;
      }
      
   }
   public MessageStoreURLTreeNode(String alias, String jdbcURL)
   {
      super(alias, jdbcURL);
      
      storeConfig = new JDBCStore() ;
      storeConfig.setAlias(alias) ;
      storeConfig.setUrl(jdbcURL) ;
      
      setIcon(IconCache.getIcon("jdbc")) ;
      
      //setOpenIcon(IconCache.getIcon(IconCache.HERMES_OPEN)) ;
   }

   public JDBCStore getConfig()
   {
      return storeConfig ;
   }
   
   public String getAlias()
   {
      return storeConfig.getAlias() ;
   }
   
   public String getTooltipText()
   {
      return getURL() ;
   }
   public String getURL()
   {
      return (String) getBean() ;
   }
   
   public boolean isConfigurable()
   {
      return !storeConfig.getAlias().equals("local") ;
   }
   public void close()
   {
      while (getChildCount() > 0)
      {
         MessageStoreTreeNode child = (MessageStoreTreeNode) getChildAt(0) ;
         child.close() ;
      }
   }
}
