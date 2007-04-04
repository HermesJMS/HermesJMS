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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Category;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: MessagesTransferable.java,v 1.4 2006/05/26 10:08:18 colincrist Exp $
 */

public class MessagesTransferable implements Transferable
{
    private static final Category cat = Category.getInstance(MessagesTransferable.class);
    private static DataFlavor[] flavors;
    public static final DataFlavor VM_FLAVOR = new DataFlavor(MessagesTransferable.class, "Messages") ;
    public static final DataFlavor BYTE_FLAVOR = new DataFlavor(Collection.class, "Byte Messages") ;
    
    private Collection<Object> messages ;
   
    
    static
    {
        try
        {
            flavors = new DataFlavor[] { DataFlavor.stringFlavor, VM_FLAVOR, BYTE_FLAVOR} ;
        }
        catch (Exception ex)
        {
            cat.error("unable to initialise message drag and drop:" + ex.getMessage(), ex);
        }
    }

    public MessagesTransferable(Collection<Object> messages)
    {
       this.messages = messages ;
    }

    /**
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors()
    {
        return flavors;
    }

    /**
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(DataFlavor)
     */
    public boolean isDataFlavorSupported(DataFlavor arg0)
    {
        for (int i = 0; i < flavors.length; i++)
        {
            if (flavors[i].equals(arg0))
            {
                return true;
            }
        }
        return false;
    }

    public byte[] getBytes(Object o)
    {
       return o.toString().getBytes() ;
    }
    /**
     * @see java.awt.datatransfer.Transferable#getTransferData(DataFlavor)
     */
    public Object getTransferData(DataFlavor flavour) throws UnsupportedFlavorException, IOException
    {
       if (flavour.equals(DataFlavor.stringFlavor) || flavour.getRepresentationClass().equals(String.class))
       {
          StringBuffer rval = new StringBuffer() ;
          
           for (Object o : messages)
           {
              rval.append(o.toString()).append("\n") ;
           }
           
           return rval.toString() ;
       }
       else if (flavour.isMimeTypeEqual(BYTE_FLAVOR))
       {
          Collection<byte[]> rval = new ArrayList<byte[]> () ;
          
          for (Object o : messages)
          {
             rval.add(getBytes(o)) ;
          }
          return rval ;          
       }
       else  if (flavour.isMimeTypeEqual(VM_FLAVOR))
        {
            return messages ;
        }
       
        else
        {
            throw new UnsupportedFlavorException(flavour) ;
        }
    }
}