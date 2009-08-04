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

import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.components.MessagesDeleteable;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.jidesoft.document.DocumentComponentEvent;
import com.jidesoft.document.DocumentComponentListener;

/**
 * Delete any selected messages from the queue.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DeleteMessagesAction.java,v 1.2 2005/05/19 16:16:09 colincrist
 *          Exp $
 */

public class DeleteMessagesAction extends AbstractAction implements DocumentComponentListener, ListSelectionListener, TableModelListener
{
   public DeleteMessagesAction()
   { 
      putValue(Action.NAME, "Delete");
      putValue(Action.SHORT_DESCRIPTION, "Delete selected messages");
      putValue(Action.SMALL_ICON, IconCache.getIcon("hermes.messages.delete"));
      putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0, false)) ; 
      

      setEnabled(false);
      
      HermesBrowser.getBrowser().addDocumentComponentListener(this);
      HermesBrowser.getBrowser().addMessageSelectionListener(this);
   }

   private final void attachListener(final Object source)
   {
      if (source instanceof MessagesDeleteable)
      {
         MessagesDeleteable action = (MessagesDeleteable) source;
         action.getTableModel().addTableModelListener(this);
      }
   }

   private final void detachListener(final Object source)
   {
      if (source instanceof MessagesDeleteable)
      {
         MessagesDeleteable action = (MessagesDeleteable) source;
         action.getTableModel().removeTableModelListener(this);
      }
   }
   
   public void actionPerformed(ActionEvent event)
   {
      if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof MessagesDeleteable)
      {
         final MessagesDeleteable d = (MessagesDeleteable) HermesBrowser.getBrowser().getDocumentPane().getActiveDocument();

         d.delete();
      }
   }

   private void checkEnabled()
   {     
      setEnabled(HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() instanceof MessagesDeleteable) ;
   }
   
   public void valueChanged(ListSelectionEvent e)
   {
      checkEnabled() ;
   }

   public void documentComponentActivated(DocumentComponentEvent event)
   {
      attachListener(event.getSource()) ;
      checkEnabled() ;
   }

   public void documentComponentClosed(DocumentComponentEvent event)
   {
      detachListener(event.getSource()) ;
      checkEnabled() ;
   }

   public void documentComponentClosing(DocumentComponentEvent event)
   {
      checkEnabled() ;
   }

   public void documentComponentDeactivated(DocumentComponentEvent event)
   {
      detachListener(event.getSource()) ;
      checkEnabled() ;
   }

   public void documentComponentMoved(DocumentComponentEvent event)
   {
      checkEnabled() ;      
   }

   public void documentComponentMoving(DocumentComponentEvent event)
   {
      checkEnabled() ;
   }

   public void documentComponentOpened(DocumentComponentEvent event)
   {
      attachListener(event.getSource()) ;
      checkEnabled() ;
   }

   public void tableChanged(TableModelEvent e)
   {
      checkEnabled() ;
   }
}
