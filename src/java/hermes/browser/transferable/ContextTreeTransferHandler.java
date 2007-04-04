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

import hermes.browser.components.ContextTree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.log4j.Category;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ContextTreeTransferHandler.java,v 1.4 2004/11/07 11:24:40
 *          colincrist Exp $
 */

public class ContextTreeTransferHandler extends TransferHandler
{
    private static final long serialVersionUID = 1L;
    private static final Category cat = Category.getInstance(ContextTreeTransferHandler.class);
    private ContextTree contextTree;

    public ContextTreeTransferHandler(ContextTree contextTree)
    {
        this.contextTree = contextTree;
    }

    protected Transferable createTransferable(JComponent c)
    {
        return new JMSAdministeredObjectTransferable(new HermesConfigGroup(null, contextTree.getSelectedDestinations(), contextTree.getSelectedConnectionFactories()));
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action)
    {
        super.exportAsDrag(comp, e, action);

    }

    public int getSourceActions(JComponent c)
    {
        if ( c instanceof ContextTree)
        {
            ContextTree contextTree = (ContextTree) c;

            if ( contextTree.getSelectedDestinations().size() > 0)
            {
                return COPY_OR_MOVE;
            }

            if ( contextTree.getSelectedConnectionFactories().size() > 0)
            {
                return COPY_OR_MOVE;
            }
        }

        return NONE;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
    {
        boolean rval = comp instanceof ContextTree;
        
        if (rval)
        {
            ContextTree tree = (ContextTree) comp ;
             
            for (int i = 0 ; i < transferFlavors.length ; i++)
            {
                if (transferFlavors[i].equals(HermesAdministeredObjectTransferable.FLAVOR))
                {
                   return true ;
                }
            }
        }
        
        return false ;
    }

    public boolean importData(JComponent comp, Transferable t)
    {
        if ( comp instanceof ContextTree)
        {
            ContextTree contextTree = (ContextTree) comp;

            return contextTree.doImport(t);
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