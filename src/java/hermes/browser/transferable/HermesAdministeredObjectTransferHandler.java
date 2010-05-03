/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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
import hermes.browser.components.ContextTree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public class HermesAdministeredObjectTransferHandler extends TransferHandler
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -3250317070022202875L;
private static final Logger log = Logger.getLogger(HermesAdministeredObjectTransferHandler.class);

   public boolean canImport(JComponent component, DataFlavor[] flavors)
   {
      return component instanceof ContextTree;
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
         return COPY;
      }
      else
      {
         return NONE;
      }
   }

   public boolean importData(JComponent component, Transferable t)
   {
      if (component instanceof ContextTree && t instanceof HermesAdministeredObjectTransferable)
      {
         ContextTree contextTree = (ContextTree) component;

         return contextTree.doImport((HermesAdministeredObjectTransferable) t);
      }
      else
      {
         return false;
      }

   }
}