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

package hermes.browser.transferable;

import hermes.HermesRuntimeException;
import hermes.browser.components.BrowserTree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: BrowserTreeTransferHandler.java,v 1.3 2004/11/07 11:24:40
 *          colincrist Exp $
 */

public class BrowserTreeTransferHandler extends TransferHandler
{
   private static final long serialVersionUID = 1L;
   private static final Logger log = Logger.getLogger(BrowserTreeTransferHandler.class);

   public BrowserTreeTransferHandler()
   {

   }

   protected Transferable createTransferable(JComponent component)
   {
      if (component instanceof BrowserTree)
      {
         BrowserTree tree = (BrowserTree) component;

         return new HermesAdministeredObjectTransferable(tree.getSelectedAdministeredObjectNodes());
      }
      else
      {
         throw new HermesRuntimeException("cannot create transferable, JComponent " + component.getClass().getName() + " is not a BrowserTree");
      }

   }

   public int getSourceActions(JComponent component)
   {
      if (component instanceof BrowserTree)
      {
         final BrowserTree tree = (BrowserTree) component;

         if (tree.getSelectedAdministeredObjectNodes().size() > 0)
         {
            return COPY;
         }
      }
      return NONE;
   }

   public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
   {
      if (comp instanceof BrowserTree)
      {
         BrowserTree browserTree = (BrowserTree) comp;
         
         if (browserTree.getSelectionPath() != null)
         {
            // log.debug(browserTree.getSelectionPath().getLastPathComponent()) ;
         }

         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean importData(JComponent comp, Transferable t)
   {
      if (comp instanceof BrowserTree)
      {
         BrowserTree browserTree = (BrowserTree) comp;

         return browserTree.doTransfer(t, TransferHandler.COPY);
      }
      else
      {
         return false;
      }
   }

   protected void exportDone(JComponent source, Transferable data, int action)
   {

   }
}