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

package hermes.swing.actions;

import java.awt.event.ActionEvent;

import hermes.browser.HermesBrowser;
import hermes.browser.actions.BrowseContextAction;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.jidesoft.document.DocumentComponentEvent;
import com.jidesoft.document.DocumentComponentListener;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JNDIAction.java,v 1.5 2005/08/15 20:37:27 colincrist Exp $
 */

public abstract class JNDIAction extends ActionSupport implements DocumentComponentListener, TreeSelectionListener
{
   public JNDIAction()
   {
      if (!HermesBrowser.getBrowser().isRestricted())
      {
         HermesBrowser.getBrowser().addDocumentComponentListener(this);
      }
   }

   protected abstract boolean checkEnabled(TreePath path);

   public void documentComponentDocked(DocumentComponentEvent arg0)
   {
      // TODO Auto-generated method stub
      
   }

   public void documentComponentFloated(DocumentComponentEvent arg0)
   {
      // TODO Auto-generated method stub
      
   }

   public void actionPerformed(ActionEvent e)
   {
      // TODO Auto-generated method stub
      
   }

   public void documentComponentMoved(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentMoving(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentActivated(DocumentComponentEvent event)
   {
      if (event.getDocumentComponent() instanceof BrowseContextAction)
      {
         final BrowseContextAction browseContext = (BrowseContextAction) event.getDocumentComponent();
         setEnabled(checkEnabled(browseContext.getContextTree().getSelectionPath()));

         addListener(event);
      }
      else
      {
         setEnabled(false);
      }
   }

   public void valueChanged(TreeSelectionEvent e)
   {
      setEnabled(checkEnabled(e.getPath()));
   }

   private void addListener(DocumentComponentEvent event)
   {
      if (event.getDocumentComponent() instanceof BrowseContextAction)
      {
         final BrowseContextAction browseContext = (BrowseContextAction) event.getDocumentComponent();

         browseContext.getContextTree().addTreeSelectionListener(this);
      }
   }

   private void removeListener(DocumentComponentEvent event)
   {
      if (event.getDocumentComponent() instanceof BrowseContextAction)
      {
         final BrowseContextAction browseContext = (BrowseContextAction) event.getDocumentComponent();

         browseContext.getContextTree().removeTreeSelectionListener(this);
      }
   }

   public void documentComponentClosed(DocumentComponentEvent event)
   {
      setEnabled(false);

      removeListener(event);
   }

   public void documentComponentClosing(DocumentComponentEvent event)
   {
      setEnabled(false);

      removeListener(event);
   }

   public void documentComponentDeactivated(DocumentComponentEvent event)
   {
      setEnabled(false);

      removeListener(event);
   }

   public void documentComponentOpened(DocumentComponentEvent event)
   {
      setEnabled(event.getDocumentComponent() instanceof BrowseContextAction);

      addListener(event);
   }
}
