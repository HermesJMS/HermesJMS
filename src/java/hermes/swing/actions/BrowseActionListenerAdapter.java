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

/**
 * Help superclass that will ensure the action is only enabled if the
 * DocumentComponent is showing and there are messages selected in the
 * BrowserAction.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: BrowseActionListenerAdapter.java,v 1.3 2005/05/20 10:36:38
 *          colincrist Exp $
 */

import hermes.browser.HermesBrowser;
import hermes.browser.actions.BrowseContextAction;
import hermes.browser.actions.BrowserAction;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import com.jidesoft.document.DocumentComponentEvent;
import com.jidesoft.document.DocumentComponentListener;

public abstract class BrowseActionListenerAdapter extends AbstractAction implements DocumentComponentListener, ListSelectionListener, TableModelListener
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -2062440164714409247L;
private static final Logger log = Logger.getLogger(BrowseActionListenerAdapter.class);
   private boolean tableListener = false;
   private boolean checkSelection = true;
   private boolean checkRunning = false;
   private Set<Class> documentTypes = new HashSet<Class> ()  ;

   public BrowseActionListenerAdapter()
   {
      HermesBrowser.getBrowser().addDocumentComponentListener(this);
      HermesBrowser.getBrowser().addMessageSelectionListener(this);
      
   }

   protected void addDocumentType(Class clazz)
   {
      documentTypes.add(clazz) ;      
   }
   public BrowseActionListenerAdapter(boolean tableListener, boolean checkSelection, boolean checkRunning)
   {
      this();
      this.tableListener = tableListener;
      this.checkSelection = checkSelection;
   }

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

   protected void checkEnabled(Object object)
   {
      if (object instanceof BrowserAction)
      {
         final BrowserAction browseAction = (BrowserAction) object;

         if (checkSelection)
         {
            setEnabled(browseAction.hasSelection());
         }
         else
         {
            setEnabled(true);
         }

         if (checkRunning)
         {
            setEnabled(!browseAction.isRunning());
         }
      }
      else if (object instanceof BrowseContextAction)
      {
         setEnabled(true) ;
      }
      else 
      {
         for (Class clazz : documentTypes)
         {
            if (clazz.isAssignableFrom(object.getClass()))
            {
               setEnabled(true);
            }
         }
      }
      
   }

   public final void tableChanged(TableModelEvent e)
   {
      if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() != null)
      {
         checkEnabled(HermesBrowser.getBrowser().getDocumentPane().getActiveDocument());
      }
   }

   protected void attachListener(Object source)
   {
      if (source instanceof BrowserAction)
      {
         BrowserAction action = (BrowserAction) source;
         action.getMessageHeaderTable().getModel().addTableModelListener(this);
      }
   }

   protected void detachListener(Object source)
   {
      if (source instanceof BrowserAction)
      {
         BrowserAction action = (BrowserAction) source;
         action.getMessageHeaderTable().getModel().removeTableModelListener(this);
      }
   }

   public void documentComponentMoved(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentMoving(DocumentComponentEvent arg0)
   {
      // NOP
   }

   public void documentComponentOpened(DocumentComponentEvent event)
   {
      checkEnabled(event.getSource());
      attachListener(event.getSource());
   }

   public void documentComponentClosing(DocumentComponentEvent event)
   {
      setEnabled(false);
      detachListener(event.getSource());
   }

   public void documentComponentClosed(DocumentComponentEvent event)
   {
      setEnabled(false);
      detachListener(event.getSource());
   }

   public void documentComponentActivated(DocumentComponentEvent event)
   {
      checkEnabled(event.getSource());
      attachListener(event.getSource());
   }

   public void documentComponentDeactivated(DocumentComponentEvent event)
   {
      checkEnabled(event.getSource());
      detachListener(event.getSource());
   }

   public void valueChanged(final ListSelectionEvent event)
   {
      if (HermesBrowser.getBrowser().getDocumentPane().getActiveDocument() != null)
      {
         checkEnabled(HermesBrowser.getBrowser().getDocumentPane().getActiveDocument());
      }
      else
      {
         setEnabled(false);
      }
   }
}
