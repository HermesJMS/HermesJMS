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


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * The DND transferable for dragging connection factories, queues and topics from the browser tree over into
 * a JNDI context for binding.
 * 
 * @author colincrist@hermesjms.com
 */
public class JMSAdministeredObjectTransferable implements Transferable
{
   private DataFlavor[] myFlavors ;
    private HermesConfigGroup objects ;
    public static final DataFlavor FLAVOR = new DataFlavor(JMSAdministeredObjectTransferable.class, "JMSAdministeredObject") ;
    
    public JMSAdministeredObjectTransferable(HermesConfigGroup objects) 
    {
         myFlavors = new DataFlavor[] { FLAVOR } ;
         this.objects = objects ;
    }
    
    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors()
    {
        return myFlavors ;
    }

    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
        return flavor.equals(FLAVOR) ;
    }

    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    public Object getTransferData(DataFlavor arg0) throws UnsupportedFlavorException, IOException
    {
       return objects ;
    }
}
