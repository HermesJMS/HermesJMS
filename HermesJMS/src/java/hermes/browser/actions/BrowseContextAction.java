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

package hermes.browser.actions;

import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.components.ContextTree;
import hermes.browser.model.tree.ContextTreeModel;
import hermes.browser.tasks.JNDIBrowseTask;
import hermes.browser.tasks.Task;
import hermes.config.NamingConfig;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jidesoft.document.DocumentComponent;
import com.jidesoft.swing.JideScrollPane;

/**
 * A document component that contains a tree of a JNDI InitialContext.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: BrowseContextAction.java,v 1.3 2004/11/17 17:22:54 colincrist
 *          Exp $
 */
public class BrowseContextAction extends DocumentComponent
{
   private static final Logger log = Logger.getLogger(BrowseContextAction.class);
   private NamingConfig namingConfig;
   private volatile boolean keepRunning = true;
   private JPanel rootPanel;
   private boolean firstTime = true;
   private JideScrollPane treeSP = new JideScrollPane();
   private JLabel lastUpdate = new JLabel();
   private ContextTree contextTree = new ContextTree();

   /**
    * @param content
    * @param title
    * @param listener
    */
   public BrowseContextAction(NamingConfig namingConfig)
   {
      super(new JPanel(), "JNDI: " + namingConfig.getId());

      this.namingConfig = namingConfig;

      setIcon(IconCache.getIcon("jndi.context"));
      rootPanel = (JPanel) getComponent();

      update();
   }

   public void update()
   {
      Task task = new JNDIBrowseTask(namingConfig, this);

      task.start();
   }

   public ContextTree getContextTree()
   {
      return contextTree;
   }

   public synchronized void update(final ContextTreeModel model)
   {
      if (firstTime)
      {
         rootPanel.setLayout(new BorderLayout());

         // Header

         JPanel header = new JPanel();
         header.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));

         lastUpdate.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

         header.add(lastUpdate);

         // Put it together

         rootPanel.add(header, BorderLayout.NORTH);
         rootPanel.add(treeSP, BorderLayout.CENTER);
      }

      try
      {
         lastUpdate.setText("Last update " + new Date());

         contextTree.setModel(model);
         treeSP.setViewportView(contextTree);

         if (firstTime)
         {
            HermesBrowser.getBrowser().addDocumentComponent(BrowseContextAction.this);
            firstTime = false;
         }
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
      }
   }

}