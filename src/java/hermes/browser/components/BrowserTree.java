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
import hermes.Hermes;
import hermes.HermesException;
import hermes.HermesRuntimeException;
import hermes.JNDIContextFactory;
import hermes.browser.HermesBrowser;
import hermes.browser.dialog.BindToolDialog;
import hermes.browser.model.BrowserTreeModel;
import hermes.browser.model.tree.DestinationConfigTreeNode;
import hermes.browser.model.tree.HermesTreeNode;
import hermes.browser.model.tree.MessageStoreDestinationTreeNode;
import hermes.browser.model.tree.MessageStoreTreeNode;
import hermes.browser.model.tree.MessageStoreURLTreeNode;
import hermes.browser.model.tree.NamingConfigTreeNode;
import hermes.browser.model.tree.RepositoryTreeNode;
import hermes.browser.tasks.AddToMessageStoreTask;
import hermes.browser.tasks.SendMessageTask;
import hermes.browser.transferable.BrowserTreeTransferHandler;
import hermes.browser.transferable.HermesAdministeredObjectTransferable;
import hermes.browser.transferable.HermesConfigGroup;
import hermes.browser.transferable.JMSAdministeredObjectTransferable;
import hermes.browser.transferable.JMSMessagesTransferable;
import hermes.browser.transferable.MessageGroup;
import hermes.browser.transferable.MessagesTransferable;
import hermes.config.DestinationConfig;
import hermes.config.FactoryConfig;
import hermes.impl.HTMLBeanHelper;
import hermes.store.MessageStore;
import hermes.swing.actions.ActionRegistry;
import hermes.swing.actions.BrowseDestinationOrContextAction;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import com.jidesoft.swing.JideSwingUtilities;
import com.xduke.xswing.DataTipManager;

/**
 * The main tree holding sessions, queues, topics, contexts etc etc.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: BrowserTree.java,v 1.57 2007/02/18 16:13:39 colincrist Exp $
 */

public class BrowserTree extends JTree implements TreeSelectionListener, DropTargetListener
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -4898641148498137544L;
private static final Logger log = Logger.getLogger(BrowserTree.class);
   private List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
   private BrowserTreeModel model;
   private DataFlavor[] myFlavours;
   private JPopupMenu popupMenu;
   private HermesTreeNode lastSelectedHermesTreeNode;
   private int lastDndAction;

   /**
    * BrowserTree constructor comment.
    */

   public BrowserTree() throws HermesException
   {
      super();

      model = new BrowserTreeModel();
      setModel(model);

      setTransferHandler(new BrowserTreeTransferHandler());
      setDragEnabled(true);
      

   }

   public int getLastDndAction()
   {
      return lastDndAction;
   }

   public void setLastDnDAction(int lastDndAction)
   {
      this.lastDndAction = lastDndAction;
   }

   /**
    * Get the model correctly typed.
    * 
    * @return
    */
   public BrowserTreeModel getBrowserModel()
   {
      return (BrowserTreeModel) getModel();
   }

   public Collection<MessageStore> getMessageStores()
   {
      return model.getMessageStores();
   }

   public void remove(MessageStoreURLTreeNode node)
   {
      MutableTreeNode parent = (MutableTreeNode) node.getParent();
      node.close();
      parent.remove(node);
      getBrowserModel().nodeStructureChanged(parent);
   }

   public void remove(MessageStoreTreeNode node)
   {
      MutableTreeNode parent = (MutableTreeNode) node.getParent();
      node.close();
      parent.remove(node);
      getBrowserModel().nodeStructureChanged(parent);
   }

   /**
    * Is there a selection in the tree?
    * 
    * @return true if there is a selection, false if none.
    */
   public boolean hasSelection()
   {
      return getSelectionPath() != null && getSelectionPath().getLastPathComponent() != null;
   }

   /**
    * Returns the last object in the lead selection
    */
   public Object getLastSelectedPathComponent()
   {
      return getSelectionPath().getLastPathComponent();
   }

   public DestinationConfigTreeNode getFirstSelectedDestinationNode()
   {
      List<DestinationConfigTreeNode> nodes = getSelectedDestinationNodes();

      if (nodes.size() > 0)
      {
         return nodes.get(0);
      }
      else
      {
         return null;
      }
   }

   /**
    * If there is queue or topic in the selection path, return it, otherwise
    * return null.
    */
   public List<DestinationConfigTreeNode> getSelectedDestinationNodes()
   {
      final TreePath[] paths = getSelectionPaths();
      final List<DestinationConfigTreeNode> rval = new ArrayList<DestinationConfigTreeNode>();

      if (paths != null)
      {
         for (int i = 0; i < paths.length; i++)
         {
            if (paths[i].getLastPathComponent() instanceof DestinationConfigTreeNode)
            {
               rval.add((DestinationConfigTreeNode) paths[i].getLastPathComponent());
            }
         }
      }

      return rval;
   }

   public Destination getSelectedMessageStoreDestination()
   {
      if (getSelectionPath() != null && getSelectionPath().getLastPathComponent() instanceof MessageStoreDestinationTreeNode)
      {
         return ((MessageStoreDestinationTreeNode) getSelectionPath().getLastPathComponent()).getDestination();
      }
      else
      {
         return null;
      }
   }

   public MessageStore getSelectedMessageStore()
   {
      if (getSelectionPath() != null)
      {
         if (getSelectionPath().getLastPathComponent() instanceof MessageStoreTreeNode)
         {
            return ((MessageStoreTreeNode) getSelectionPath().getLastPathComponent()).getMessageStore();
         }
         else if (getSelectionPath().getLastPathComponent() instanceof MessageStoreDestinationTreeNode)
         {
            MessageStoreDestinationTreeNode dNode = (MessageStoreDestinationTreeNode) getSelectionPath().getLastPathComponent();

            return ((MessageStoreTreeNode) dNode.getParent()).getMessageStore();
         }
         else if (getSelectionPath().getLastPathComponent() instanceof RepositoryTreeNode)
         {
            RepositoryTreeNode dNode = (RepositoryTreeNode) getSelectionPath().getLastPathComponent();
            return dNode.getRepository() ;
            
         }
      }

      return null;
   }

   /**
    * Returns the last Hermes session node that was in a selection path.
    */
   public HermesTreeNode getLastSelectedHermesTreeNode()
   {
      if (lastSelectedHermesTreeNode == null)
      {
         return model.getFirstHermesTreeNode();
      }
      else
      {
         return lastSelectedHermesTreeNode;
      }
   }

   /**
    * If there is a Hermes node in the current selection path return it,
    * otherwise return null.
    */
   public HermesTreeNode getSelectedHermesNode()
   {
      if (getSelectionPath() != null)
      {
         if (getSelectionPath().getLastPathComponent() instanceof HermesTreeNode)
         {
            return (HermesTreeNode) getSelectionPath().getLastPathComponent();
         }
         else if (getSelectionPath().getLastPathComponent() instanceof DestinationConfigTreeNode)
         {
            DestinationConfigTreeNode destinationNode = (DestinationConfigTreeNode) getSelectionPath().getLastPathComponent();

            return (HermesTreeNode) destinationNode.getHermesTreeNode();
         }
      }

      return null;
   }
   
   

   private boolean doJavaFileTransfer(List files)
   {
      List<DestinationConfigTreeNode> nodes = getSelectedDestinationNodes();

      if (nodes.size() > 0)
      {
         final StringBuffer question = new StringBuffer();
         final DestinationConfigTreeNode node = nodes.get(0);

         question.append("Are you sure you want to upload ");

         if (files.size() == 1)
         {
            question.append(" this file to " + node.getDestinationName());
         }
         else
         {
            question.append(" these " + files.size() + " files to " + node.getDestinationName());
         }
         
         question.append(" ?") ;

         if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), question.toString(), "Please confirm.", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
         {

            HermesBrowser.getBrowser().getActionFactory().createSimpleSendMessageAction(getSelectedHermesNode().getHermes(), node.getDestinationName(),
                  node.getDomain(), files, SendMessageTask.MAYBE_XML, false);
         }
         else
         {
            Hermes.ui.getDefaultMessageSink().add("File upload cancelled.");
         }
      }

      return false;
   }

   private boolean doTextMessageTransfer(Collection messages)
   {
      List<DestinationConfigTreeNode> nodes = getSelectedDestinationNodes();

      lastDndAction = TransferHandler.COPY;

      if (nodes.size() > 0 && messages.size() > 0)
      {
         final DestinationConfigTreeNode destinationNode = nodes.get(0);
         final HermesTreeNode hermesNode = getSelectedHermesNode();

         if (hermesNode != null && destinationNode != null)
         {
            HermesBrowser.getBrowser().getActionFactory().createMessageCopyAction(hermesNode.getHermes(), destinationNode.getDestinationName(),
                  destinationNode.getDomain(), messages);
         }
      }

      return true;
   }

   private boolean doBytesMessageTransfer(Collection<byte[]> messages)
   {
      List<DestinationConfigTreeNode> nodes = getSelectedDestinationNodes();

      lastDndAction = TransferHandler.COPY;

      if (nodes.size() > 0 && messages.size() > 0)
      {
         final DestinationConfigTreeNode destinationNode = nodes.get(0);
         final HermesTreeNode hermesNode = getSelectedHermesNode();

         if (hermesNode != null && destinationNode != null)
         {
            HermesBrowser.getBrowser().getActionFactory().createMessageCopyAction(hermesNode.getHermes(), destinationNode.getDestinationName(),
                  destinationNode.getDomain(), messages);
         }
      }

      return true;
   }

   private boolean doMessagesTransfer(MessageGroup messages, int action)
   {
      List<DestinationConfigTreeNode> nodes = getSelectedDestinationNodes();

      if (nodes.size() > 0)
      {
         final DestinationConfigTreeNode destinationNode = nodes.get(0);

         final HermesTreeNode hermesNode = getSelectedHermesNode();

         if (hermesNode != null && destinationNode != null)
         {
            if (action == TransferHandler.COPY)
            {
               HermesBrowser.getBrowser().getActionFactory().createMessageCopyAction(hermesNode.getHermes(), destinationNode.getDestinationName(),
                     destinationNode.getDomain(), messages.getSelectedMessages());
            }
            else
            {
               HermesBrowser.getBrowser().getActionFactory().createMessageMoveAction(hermesNode.getHermes(), destinationNode.getDestinationName(),
                     destinationNode.getDomain(), messages.getSelectedMessages());
            }
         }
      }
      else
      {
         if (getSelectionPath() != null)
         {
            if (getSelectionPath().getLastPathComponent() instanceof RepositoryTreeNode)
            {
               final RepositoryTreeNode node = (RepositoryTreeNode) getSelectionPath().getLastPathComponent();

               try
               {
                  node.getRepository().addMessages(messages.getHermes(), messages.getSelectedMessages());
               }
               catch (JMSException e)
               {
                  throw new HermesRuntimeException(e);
               }
            }
            else if (getSelectionPath().getLastPathComponent() instanceof MessageStoreTreeNode)
            {
               final MessageStoreTreeNode node = (MessageStoreTreeNode) getSelectionPath().getLastPathComponent();

               HermesBrowser.getBrowser().getThreadPool().invokeLater(new AddToMessageStoreTask(node.getMessageStore(), messages.getSelectedMessages()));
            }
         }
         else
         {
            HermesBrowser.getBrowser().showErrorDialog("No session/destination selected for copy");
         }

      }
      return true;
   }

   private boolean doContextContentTransfer(HermesConfigGroup transferData) throws NamingException, JMSException
   {
      if (transferData.getDestinations().size() != 0 && transferData.getFactories().size() != 0)
      {
         HermesBrowser.getBrowser().showErrorDialog("Can only drop ConnectionFactories or Destinations, not both");

         return false;
      }

      if (transferData.getDestinations().size() > 0)
      {
         Hermes hermes = null;

         if (transferData.getHermesId() != null)
         {
            hermes = (Hermes) HermesBrowser.getBrowser().getContext().lookup(transferData.getHermesId());
         }
         else if (getSelectedHermesNode() != null)
         {
            hermes = getSelectedHermesNode().getHermes();
         }

         if (hermes != null)
         {
            StringBuffer question = new StringBuffer();

            if (transferData.getDestinations().size() == 1)
            {
               question.append("Do you want to add this destination to " + hermes.getId() + " ?");
            }
            else
            {
               question.append("Do you want to add these " + transferData.getDestinations().size() + " destinations to " + hermes.getId() + " ?");
            }

            if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), question.toString(), "Please confirm.", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
            {
               for (Iterator iter = transferData.getDestinations().iterator(); iter.hasNext();)
               {
                  DestinationConfig dConfig = (DestinationConfig) iter.next();

                  if (dConfig.getDomain() == Domain.UNKNOWN.getId())
                  {
                     Object options[] = { "Queue", "Topic" };

                     int n = JOptionPane.showOptionDialog(HermesBrowser.getBrowser(), "The destination " + dConfig.getName()
                           + " implements both Queue and Topic interfaces, please select the domain you wish to use it in.", "Select domain",
                           JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

                     if (n == JOptionPane.YES_OPTION)
                     {
                        dConfig.setDomain(Domain.QUEUE.getId());
                        HermesBrowser.getBrowser().addDestinationConfig(hermes, dConfig);
                     }
                     else if (n == JOptionPane.NO_OPTION)
                     {
                        dConfig.setDomain(Domain.TOPIC.getId());
                        HermesBrowser.getBrowser().addDestinationConfig(hermes, dConfig);
                     }
                     else
                     {
                        // NOP
                     }
                  }
                  else
                  {
                     HermesBrowser.getBrowser().addDestinationConfig(hermes, dConfig);
                  }
               }

               HermesBrowser.getBrowser().saveConfig();

            }
         }
         else
         {
            Hermes.ui.getDefaultMessageSink().add("No session selected for drop target");
         }
      }
      else
      {
         final String newName = JOptionPane.showInputDialog(HermesBrowser.getBrowser(), "Session name:", "");

         if (newName != null && !newName.equals(""))
         {
            if (!getAllHermesIds().contains(newName))
            {
               FactoryConfig fConfig = (FactoryConfig) transferData.getFactories().iterator().next();

               HermesBrowser.getConfigDAO().renameSession(fConfig, newName);

               HermesBrowser.getBrowser().getConfig().getFactory().add(fConfig);
               HermesBrowser.getBrowser().saveConfig();
               HermesBrowser.getBrowser().loadConfig();               
            }
            else
            {
               HermesBrowser.getBrowser().showErrorDialog("Session " + newName + " is already in use.");
            }
         }
      }

      return true;
   }

   private boolean doLocalTransfer(Transferable t, int action)
   {
      if (HermesBrowser.getBrowser().isRestrictedWithWarning())
      {
         return false;
      }

      try
      {

         if (t.isDataFlavorSupported(JMSMessagesTransferable.FLAVOR))
         {
            return doMessagesTransfer((MessageGroup) t.getTransferData(JMSMessagesTransferable.FLAVOR), action);
         }

         if (t.isDataFlavorSupported(JMSAdministeredObjectTransferable.FLAVOR))
         {
            return doContextContentTransfer((HermesConfigGroup) t.getTransferData(JMSAdministeredObjectTransferable.FLAVOR));
         }

         if (t.isDataFlavorSupported(MessagesTransferable.BYTE_FLAVOR))
         {

            return doBytesMessageTransfer((Collection<byte[]>) t.getTransferData(MessagesTransferable.BYTE_FLAVOR));

         }
         if (t.isDataFlavorSupported(HermesAdministeredObjectTransferable.FLAVOR))
         {
            if (getSelectionPath().getLastPathComponent() instanceof NamingConfigTreeNode)
            {
               final NamingConfigTreeNode namingConfigTreeNode = (NamingConfigTreeNode) getSelectionPath().getLastPathComponent();
               final Collection objects = (Collection) t.getTransferData(HermesAdministeredObjectTransferable.FLAVOR);
               final JNDIContextFactory contextFactory = new JNDIContextFactory(namingConfigTreeNode.getConfig());
               final BindToolDialog bindTool = new BindToolDialog(HermesBrowser.getBrowser(), contextFactory, "", objects);

               bindTool.pack();
               JideSwingUtilities.centerWindow(bindTool);
               bindTool.show();

               return true;
            }
         }
      }
      catch (Exception ex)
      {
         log.error(ex.getMessage(), ex);
         JOptionPane.showMessageDialog(this, "During transfer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }

      return false;
   }

   /**
    * Handle a data transfer into the tree - this can be initiated via dnd or
    * directly.
    * 
    * @@TODO DnD - it works but is all messed up and needs doing PROPERLY.
    */
   public boolean doTransfer(Transferable t, int action)
   {
      try
      {
         log.debug(t.getTransferDataFlavors()[0]);

         if (t.getTransferDataFlavors()[0].isMimeTypeEqual("application/x-java-file-list"))
         {
            return doJavaFileTransfer((List) t.getTransferData(DataFlavor.javaFileListFlavor));

         }
         else
         {
            return doLocalTransfer(t, action);
         }
      }
      catch (UnsupportedFlavorException e)
      {
         log.error(e.getMessage(), e);
      }
      catch (IOException e)
      {
         log.error(e.getMessage(), e);
      }

      return false;
   }

   public void dragEnter(DropTargetDragEvent dtde)
   {
      // NOP
   }

   public void dragExit(DropTargetEvent dte)
   {
      // NOP
   }

   public void dragOver(DropTargetDragEvent dtde)
   {
      /*
       * The idea here is to change the selection path as the user drags an item
       * over - it works but it does not seem to expand...
       */

      final TreePath path = getPathForLocation(dtde.getLocation().x, dtde.getLocation().y);

      if (path != null)
      {
         final TreeNode leafNode = (TreeNode) path.getLastPathComponent();

         if (leafNode.getChildCount() > 0)
         {
            final TreePath newPath = path.pathByAddingChild(leafNode.getChildAt(0));
            expandPath(newPath);
            setSelectionPath(path);
            repaint();
         }
      }
   }

   public void drop(DropTargetDropEvent dtde)
   {
      // NOP
   }

   public void dropActionChanged(DropTargetDragEvent dtde)
   {
      // NOP

   }

   public Collection<String> getAllHermesIds()
   {
      return model.getAllHermesIds();
   }

   private String getDestinationTooltip(Hermes hermes, DestinationConfig dConfig) throws JMSException, NamingException
   {
      Map map = hermes.getStatistics(dConfig);

      return HTMLBeanHelper.format(map);
   }

   /**
    * Returns all the administered objects nodes, i.e. queues, topics and
    * connection factories in the selection paths.
    * 
    * @return
    */
   public Collection<TreeNode> getSelectedAdministeredObjectNodes()
   {
      final TreePath paths[] = getSelectionModel().getSelectionPaths();
      final Collection<TreeNode> rval = new ArrayList<TreeNode>();

      if (paths != null)
      {
         for (int i = 0; i < paths.length; i++)
         {
            TreePath path = paths[i];

            if (path.getLastPathComponent() instanceof DestinationConfigTreeNode)
            {
               DestinationConfigTreeNode destinationNode = (DestinationConfigTreeNode) path.getLastPathComponent();
               rval.add(destinationNode);
            }
            else if (path.getLastPathComponent() instanceof HermesTreeNode)
            {
               HermesTreeNode hermesNode = (HermesTreeNode) path.getLastPathComponent();
               rval.add(hermesNode);
            }
         }
      }

      return rval;
   }

   /**
    * Gets some nice text to display in a tooltip.
    */
   public String getToolTipText(MouseEvent evt)
   {
      if (getRowForLocation(evt.getX(), evt.getY()) == -1)
      {
         return null;
      }

      TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
      Object component = curPath.getLastPathComponent();

      if (component != null)
      {
         if (component instanceof HermesTreeNode)
         {
            try
            {
               return ((HermesTreeNode) component).getHermes().getMetaData().getToolTipText();
            }
            catch (JMSException ex)
            {
               log.error(ex.getMessage(), ex);
            }
         }

         if (component instanceof NamingConfigTreeNode)
         {
            try
            {
               NamingConfigTreeNode nNode = (NamingConfigTreeNode) component;

               return nNode.getToolTipText();
            }
            catch (Exception ex)
            {
               log.error(ex.getMessage(), ex);
            }
         }
         
         if (component instanceof DestinationConfigTreeNode)
         {
            return ((DestinationConfigTreeNode) component).getToolTipText() ;
         }

         if (component instanceof RepositoryTreeNode)
         {
            return ((RepositoryTreeNode) component).getRepository().getToolTipText();
         }

         if (component instanceof MessageStoreTreeNode)
         {
            return ((MessageStoreTreeNode) component).getMessageStore().getTooltipText();
         }

         if (component instanceof MessageStoreURLTreeNode)
         {
            return ((MessageStoreURLTreeNode) component).getTooltipText();
         }
      }

      return null;
   }

   public void init()
   {
      getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
      DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) getCellRenderer();

      setCellRenderer(new BrowserTreeCellRenderer());

      //
      // Keep up to date with whats clicked when....

      getSelectionModel().addTreeSelectionListener(this);

      addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            maybeDoBrowse(e);
         }

         public void mousePressed(MouseEvent e)
         {
             maybeDoPopup(e);
         }

         public void mouseReleased(MouseEvent e)
         {
            maybeDoPopup(e);
         }
      });

      DataTipManager.get().register(this);

      try
      {
         getDropTarget().addDropTargetListener(this);
      }
      catch (TooManyListenersException e1)
      {
         log.error(e1.getMessage(), e1);
      }

      popupMenu = PopupMenuFactory.createBrowserTreePopup(this);
   }

   public boolean isCurrentSelectionADestination()
   {
      return getSelectionPath() != null && getSelectionPath().getLastPathComponent() instanceof DestinationConfigTreeNode;
   }

   public boolean isCurrentSelectionASession()
   {
      return getSelectionPath() != null && getSelectionPath().getLastPathComponent() instanceof HermesTreeNode;
   }

   public void maybeDoBrowse(MouseEvent e)
   {
      if (e.getClickCount() == 2)
      {
         TreePath path = getPathForLocation(e.getX(), e.getY());

         if (path != null)
         {
            // setSelectionPath(path);
            ActionRegistry.getAction(BrowseDestinationOrContextAction.class).actionPerformed(null);
         }
      }
   }

   boolean maybeDoPopup(MouseEvent e)
   {
      if (e.isPopupTrigger())
      {
         popupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
      return true;
   }

   public void nodeStructureChanged(TreeNode node)
   {
      model.nodeStructureChanged(node);
   }

   /**
    * Called whenever the value of the selection changes, we watch this to cache
    * whenever HermesTreeNode is in the selection path and to change the
    * components tooltip text.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      final TreePath treePath = e.getNewLeadSelectionPath();

      try
      {
         if (treePath != null)
         {
            for (int i = 0; i < treePath.getPathCount(); i++)
            {
               if (treePath.getPathComponent(i) instanceof HermesTreeNode)
               {
                  final HermesTreeNode node = (HermesTreeNode) treePath.getPathComponent(i);
                  lastSelectedHermesTreeNode = node;

                  setToolTipText(node.getHermes().getMetaData().getToolTipText());
               }
               else if (treePath.getPathComponent(i) instanceof DestinationConfigTreeNode)
               {
                  final DestinationConfigTreeNode node = (DestinationConfigTreeNode) treePath.getPathComponent(i);

                  setToolTipText(node.getDestinationName());
               }
               else if (treePath.getPathComponent(i) instanceof RepositoryTreeNode)
               {
                  final RepositoryTreeNode node = (RepositoryTreeNode) treePath.getPathComponent(i);

                  setToolTipText(node.getRepository().getId());
               }
               else if (treePath.getPathComponent(i) instanceof MessageStoreTreeNode)
               {
                  final MessageStoreTreeNode node = (MessageStoreTreeNode) treePath.getPathComponent(i);

                  setToolTipText(node.getMessageStore().getTooltipText());
               }
               else if (treePath.getPathComponent(i) instanceof MessageStoreURLTreeNode)
               {
                  final MessageStoreURLTreeNode node = (MessageStoreURLTreeNode) treePath.getPathComponent(i);

                  setToolTipText(node.getURL());
               }
            }
         }
      }
      catch (JMSException ex)
      {
         Hermes.ui.getDefaultMessageSink().add(ex.getMessage());
      }
   }
}