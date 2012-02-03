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

package hermes.browser.components;

import hermes.Domain;
import hermes.HermesRuntimeException;
import hermes.JNDIConnectionFactory;
import hermes.JNDIQueueConnectionFactory;
import hermes.JNDITopicConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.browser.dialog.BeanPropertyDialog;
import hermes.browser.dialog.BindToolDialog;
import hermes.browser.model.tree.AbstractTreeNode;
import hermes.browser.model.tree.ConnectionFactoryTreeNode;
import hermes.browser.model.tree.ContextTreeModel;
import hermes.browser.model.tree.ContextTreeNode;
import hermes.browser.model.tree.ExceptionTreeNode;
import hermes.browser.model.tree.QueueTopicTreeNode;
import hermes.browser.model.tree.QueueTreeNode;
import hermes.browser.model.tree.TopicTreeNode;
import hermes.browser.transferable.ContextTreeTransferHandler;
import hermes.browser.transferable.HermesAdministeredObjectTransferable;
import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;

import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jms.Destination;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.jidesoft.swing.JidePopupMenu;
import com.jidesoft.swing.JideSwingUtilities;

/**
 * A JTree for rendering the content of a JNDI Context. It also serves up
 * default Hermes configurations from selected nodes.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ContextTree.java,v 1.22 2007/01/13 14:12:40 colincrist Exp $
 */
public class ContextTree extends JTree
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 7260213971170750304L;

private static final Logger log = Logger.getLogger(ContextTree.class);

   private JidePopupMenu popupMenu;

   /**
    * 
    */
   public ContextTree()
   {

      init();

   }

   /**
    * Returns a collection of FactoryConfigs (i.e. a new Hermes session
    * configuration) for all of the ConnectionFactory nodes in the current tree
    * selection.
    * 
    * @return
    */
   public Collection<FactoryConfig> getSelectedConnectionFactories()
   {
      List<FactoryConfig> rval = new ArrayList<FactoryConfig>();

      try
      {
         if (getSelectionPaths() != null)
         {
            for (final TreePath selectionPath : getSelectionPaths())
            {
               Object component = selectionPath.getLastPathComponent();

               if (component instanceof AbstractTreeNode)
               {
                  AbstractTreeNode node = (AbstractTreeNode) component;

                  if (node instanceof ConnectionFactoryTreeNode)
                  {
                     ContextTreeNode ctxNode = (ContextTreeNode) node.getParent();
                     ConnectionFactoryTreeNode cfNode = (ConnectionFactoryTreeNode) node;
                     String binding = getAbsoluteBinding(cfNode);
                     Class clazz = null;

                     if (cfNode.getBean() instanceof QueueConnectionFactory && cfNode.getBean() instanceof TopicConnectionFactory)
                     {
                        clazz = JNDIConnectionFactory.class;
                     }
                     else if (cfNode.getBean() instanceof QueueConnectionFactory)
                     {
                        clazz = JNDIQueueConnectionFactory.class;
                     }
                     else
                     {
                        clazz = JNDITopicConnectionFactory.class;
                     }

                     FactoryConfig factoryConfig = HermesBrowser.getConfigDAO().createJNDIFactoryConfig(ctxNode.getConfig().getClasspathId(), "S:" + System.currentTimeMillis()
                           + ":" + node.getId(), binding, ctxNode.getConfig().getProperties(), clazz.getName());

                     rval.add(factoryConfig);
                  }
               }
            }
         }
      }
      catch (JAXBException e)
      {
         log.error(e.getMessage(), e);
      }

      return rval;
   }

   public boolean doImport(Transferable t)
   {
      try
      {
         final ContextTreeModel model = (ContextTreeModel) getModel();
         final String bindingRoot = getAbsoluteBinding((TreeNode) getSelectionPath().getLastPathComponent());
         final Collection objects = (Collection) t.getTransferData(HermesAdministeredObjectTransferable.FLAVOR);
         final BindToolDialog bindTool = new BindToolDialog(HermesBrowser.getBrowser(), model.getContextFactory(), bindingRoot, objects);

         log.debug("got " + objects.size() + " objects to bind relative to " + bindingRoot);

         bindTool.pack();
         JideSwingUtilities.centerWindow(bindTool);
         bindTool.show();

         return true;
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
         throw new HermesRuntimeException(e);
      }
   }

   protected String getAbsoluteBinding(TreeNode leaf)
   {
      final ContextTreeModel model = (ContextTreeModel) getModel();
      final TreeNode[] path = (TreeNode[]) model.getPathToRoot(leaf);
      final StringBuffer rval = new StringBuffer();

      for (int i = 1; i < path.length; i++)
      {
         AbstractTreeNode node = (AbstractTreeNode) path[i];

         rval.append(node.getId());

         if (i != path.length - 1)
         {
            if (!node.getId().endsWith("/"))
            {
               rval.append("/");
            }
         }
      }

      return rval.toString();
   }

   /**
    * Returns a collection of DestinationConfigs for all of the Destination
    * (Queue/Topic) instances in the current selection.
    * 
    * @return
    */
   public Collection<DestinationConfig> getSelectedDestinations()
   {
      List<DestinationConfig> rval = new ArrayList<DestinationConfig>();

      if (getSelectionPaths() != null)
      {
         for (final TreePath selectionPath : getSelectionPaths())
         {
            Object component = selectionPath.getLastPathComponent();

            if (component instanceof AbstractTreeNode)
            {
               AbstractTreeNode node = (AbstractTreeNode) component;

               if (node instanceof QueueTopicTreeNode || node instanceof QueueTreeNode || node instanceof TopicTreeNode)
               {
                  DestinationConfig config = HermesBrowser.getConfigDAO().createDestinationConfig();
                  String binding = getAbsoluteBinding(node);

                  config.setName(binding);
                  config.setDomain(Domain.getDomain((Destination) node.getBean()).getId());

                  rval.add(config);
               }
            }
         }
      }
      return rval;
   }

   private void init()
   {
      //
      // Use the Hermes open/closed icons and delegate to the nodes for
      // everything else

      getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

      setCellRenderer(new ContextTreeCellRenderer());
      setDragEnabled(true);
      setTransferHandler(new ContextTreeTransferHandler(this));

      addMouseListener(new MouseAdapter()
      {
         public void mousePressed(MouseEvent e)
         {      	
             onMouseClicked(e);
         }

         public void mouseReleased(MouseEvent e)
         {
             onMouseClicked(e);

         }

         public void mouseClicked(MouseEvent e)
         {
            if (e.getClickCount() == 2)
            {
               onDoubleClick();
            }
         }
      });

      popupMenu = PopupMenuFactory.createContextTreePopup(this);
   }

   /**
    * Show the popup menu.
    * 
    * @param e
    */
   private void onMouseClicked(MouseEvent e)
   {
      TreePath currentPath = getPathForLocation(e.getX(), e.getY());

      if (currentPath != null)
      {
         if (e.isPopupTrigger())
         {
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
         }
      }
   }

   public void onDoubleClick()
   {
      if (getSelectionPath() != null)
      {
         if (getSelectionPath().getLastPathComponent() instanceof ExceptionTreeNode)
         {
            ExceptionTreeNode node = (ExceptionTreeNode) getSelectionPath().getLastPathComponent();

            HermesBrowser.getBrowser().showErrorDialog(node.getException().getClass().getName() + "\n" + node.getException());
         }
         else if (getSelectionPath().getLastPathComponent() instanceof AbstractTreeNode)
         {
            AbstractTreeNode node = (AbstractTreeNode) getSelectionPath().getLastPathComponent();

            if (node instanceof ContextTreeNode)
            {
               // NOP
            }
            else
            {
               BeanPropertyDialog dialog = new BeanPropertyDialog(HermesBrowser.getBrowser(), node.getBean(), false);
               dialog.pack();
               JideSwingUtilities.centerWindow(dialog);
               dialog.show();
            }
         }
      }
   }
}