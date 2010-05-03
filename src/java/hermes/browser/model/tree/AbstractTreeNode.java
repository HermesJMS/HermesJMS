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

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: AbstractTreeNode.java,v 1.6 2006/02/08 09:17:08 colincrist Exp $
 */
public abstract class AbstractTreeNode extends DefaultMutableTreeNode
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 3652000066106328801L;
private static final Logger log = Logger.getLogger(AbstractTreeNode.class) ;
   private String id;
   private Object bean;
   private Icon icon;
   private Icon openIcon;


   public AbstractTreeNode(String id, Object bean)
   {
      super(id);
      
      this.id = id;
      this.bean = bean;
   }

   public void setId(String id)
   {
      this.id = id ;
   }
   public String getPathFromRoot()
   {
      
      StringBuffer rval = new StringBuffer() ;
      
      for (int i = 1 ; i < getPath().length; i++)
      {
         AbstractTreeNode node = (AbstractTreeNode) getPath()[i] ;
         
         if (node != this)
         {
            rval.append(node.getId()) ;
            rval.append("/") ;
         }
         else
         {
            break  ;
         }
      }
      
      log.debug("path=" + rval.toString()) ;
      return rval.toString() ;
   }
   public boolean hasOpenIcon()
   {
      return openIcon != null ;
   }
   
   public void setOpenIcon(Icon openIcon)
   {
      this.openIcon = openIcon ;
   }
   
   public Icon getOpenIcon()
   {
      return openIcon;
   }

   public void setIcon(Icon icon)
   {
      this.icon = icon ;
   }
   
   public Icon getIcon()
   {
      return icon;
   }

   public String getId()
   {
      return id;
   }

   public Object getBean()
   {
      return bean;
   }
}
