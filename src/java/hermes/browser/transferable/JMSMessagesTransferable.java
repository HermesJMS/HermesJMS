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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.jms.JMSException;

import org.apache.log4j.Category;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: JMSMessagesTransferable.java,v 1.1 2006/05/26 10:08:18 colincrist Exp $
 */

public class JMSMessagesTransferable implements Transferable
{
    private static final Category cat = Category.getInstance(JMSMessagesTransferable.class);
    private static DataFlavor[] flavors;
    public static final DataFlavor FLAVOR = new DataFlavor(JMSMessagesTransferable.class, "JMS Messages") ;
    
    private MessageGroup messages ;
   
    static
    {
        try
        {
            flavors = new DataFlavor[] { DataFlavor.stringFlavor, FLAVOR} ;
        }
        catch (Exception ex)
        {
            cat.error("unable to initialise message drag and drop:" + ex.getMessage(), ex);
        }
    }

    public JMSMessagesTransferable(MessageGroup messages)
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

    /**
     * @see java.awt.datatransfer.Transferable#getTransferData(DataFlavor)
     */
    public Object getTransferData(DataFlavor flavour) throws UnsupportedFlavorException, IOException
    {
       if (flavour.equals(DataFlavor.stringFlavor) || flavour.getRepresentationClass().equals(String.class))
       {
           try
           {
               return messages.getHermes().toXML(messages.getSelectedMessages()) ;
           }
           catch (JMSException e)
           {
              if (e.getLinkedException() instanceof IOException)
              {
                  throw (IOException) e.getLinkedException() ;
              }
              else
              {
                  throw new HermesRuntimeException(e) ;
              }
           }
       }
       else  if (flavour.isMimeTypeEqual(FLAVOR))
        {
          
            return messages ;
        }
       
        else
        {
            throw new UnsupportedFlavorException(flavour) ;
        }
    }
}