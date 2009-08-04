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

package hermes.browser.model;

import hermes.Hermes;
import hermes.browser.model.tree.AbstractTreeNode;
import hermes.browser.model.tree.DestinationFragmentTreeNode;

import java.util.StringTokenizer;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class TreeUtils
{
   private static final Logger log = Logger.getLogger(TreeUtils.class);

   public static boolean pathContains(TreePath[] paths, Class clazz)
   {
      for (int i = 0 ; i < paths.length ; i++)
      {
         for (int j = 0 ; j < paths[i].getPathCount() ; j++)
         {
            if (clazz.isAssignableFrom(paths[i].getPath()[j].getClass()))
            {
               return true ;
            }
         }            
      }
      return false ;
   }
   
   public static void add(BrowserTreeModel model, Hermes hermes, String path, String delimiter, AbstractTreeNode root, AbstractTreeNode leaf)
   {
      AbstractTreeNode currentNode = root;

      for (StringTokenizer tokens = new StringTokenizer(path, delimiter); tokens.hasMoreTokens();)
      {
         String token = tokens.nextToken();
         boolean found = false;

         for (int i = 0; i < currentNode.getChildCount(); i++)
         {
            AbstractTreeNode node = (AbstractTreeNode) currentNode.getChildAt(i);

            if (node.getId().equals(token))
            {
               if (!tokens.hasMoreTokens())
               {
                  currentNode.remove(node);                 
                  currentNode.add(leaf);
               }
               else
               {
                  currentNode = node;
               }
               found = true;
               break;
            }
         }

         if (!found)
         {
            if (tokens.hasMoreTokens())
            {              
               DestinationFragmentTreeNode fragment = new DestinationFragmentTreeNode(hermes, token);
               currentNode.add(fragment);
               model.nodesWereInserted(currentNode, new int[] { currentNode.getIndex(fragment) }) ;
               currentNode = fragment;
            }
            else
            {               
               
               currentNode.add(leaf);
               model.nodesWereInserted(currentNode, new int[] { currentNode.getIndex(leaf) }) ;
            }
         }
      }
   }
   
   public static void expandFully(JTree tree, TreePath path)
   {
      tree.expandPath(path) ;
      TreeNode node = (TreeNode) path.getLastPathComponent() ;
      
      for (int i = 0 ; i < node.getChildCount() ; i++)
      {
         expandFully(tree, path.pathByAddingChild(node.getChildAt(i))) ;
      }
   }
}
