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

import hermes.browser.actions.FIXMessageBrowserDocumentComponent;
import hermes.browser.tasks.BrowseFIXChainTask;
import hermes.fix.ChainByClOrdID;
import hermes.fix.FIXException;
import hermes.fix.FIXMessage;
import hermes.fix.FIXMessageTable;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.log4j.Logger;

import quickfix.field.ClOrdID;

import com.jidesoft.swing.JideMenu;

/**
 * Chain by ClOrdID
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DiscoverDestinationsAction.java,v 1.6 2005/12/14 08:11:24
 *          colincrist Exp $
 */

public class ChainByClOrdIDAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -1158910405875983784L;
private static final Logger log = Logger.getLogger(ChainByClOrdIDAction.class);
   private FIXMessageTable table;
   private ChainByClOrdID chain ;
   
   public ChainByClOrdIDAction(JideMenu menu, FIXMessageTable table)
   {
      this.table = table;
      this.chain = new ChainByClOrdID(table) ;

      putValue(Action.NAME, "By ClOrdID...");
      putValue(Action.SHORT_DESCRIPTION, "Follow messages related to this order");

      table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            doTableSelectionChanged();
         }
      });
      
      menu.addMenuListener(new MenuListener()
      {
      
         public void menuCanceled(MenuEvent e)
         {
            // TODO Auto-generated method stub
      
         }
      
         public void menuDeselected(MenuEvent e)
         {
            // TODO Auto-generated method stub
      
         }
      
         public void menuSelected(MenuEvent e)
         {
            doTableSelectionChanged();
         }
      
      }) ;
      
      setEnabled(false) ;
   }

   private void doTableSelectionChanged()
   {
      if (table.getSelectedRow() >= 0)
      {
         try
         {
            final FIXMessage message = table.getMessageAt(table.getSelectedRow());
            
            setEnabled(chain.canChain(message)) ;            
         }
         catch (FIXException e)
         {
            setEnabled(false);
            log.error(e.getMessage(), e);
         }
      }
   }

   public void actionPerformed(ActionEvent e)
   {
      if (table.getSelectedRow() >= 0)
      {
         FIXMessage message = table.getMessageAt(table.getSelectedRow());

         if (message.fieldExists(ClOrdID.FIELD))
         {
            String clOrdID = message.getString(ClOrdID.FIELD);
            FIXMessageBrowserDocumentComponent frame = new FIXMessageBrowserDocumentComponent(table.getSessionKey(), clOrdID);
            BrowseFIXChainTask task = new BrowseFIXChainTask(table, frame, clOrdID, table.getSelectedRow());

            frame.setTask(task);
            task.start();
         }
      }
   }

}
