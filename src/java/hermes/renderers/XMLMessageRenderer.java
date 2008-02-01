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

package hermes.renderers;

import hermes.browser.MessageRenderer;
import hermes.swing.MyTextArea;
import hermes.util.MessageUtils;
import hermes.util.XmlUtils;

import java.awt.Font;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

/**
 * A renderer that displays toString() on a JMS message in a text area.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: XMLMessageRenderer.java,v 1.3 2007/02/18 16:13:41 colincrist Exp $
 */

public class XMLMessageRenderer extends AbstractMessageRenderer
{
   private static final Logger log = Logger.getLogger(XMLMessageRenderer.class);

   public XMLMessageRenderer()
   {
      super();
      // TODO Auto-generated constructor stub
   }

   public JComponent render(Message m)
   {
      //
      // Raw Panel.

      final JTextArea textArea = new MyTextArea() ; 

      textArea.setEditable(false);     
      textArea.setFont(Font.decode("Monospaced-PLAIN-12")) ;   
      textArea.setLineWrap(true) ;
      textArea.setWrapStyleWord(true) ;
      
      
      try
      {
          byte[] bytes = MessageUtils.asBytes(m);
       
          textArea.setLineWrap(true) ;
          textArea.setText(XmlUtils.prettyPrintXml(bytes));
          textArea.setCaretPosition(0) ;
      }
      catch (Throwable e)
      {
         textArea.setText(e.getMessage()) ;
          log.error("exception converting message to byte[]: ", e);
      }

      textArea.setCaretPosition(0);

      return textArea ;
   }


   /**
    * Any JMS message is rederable.
    */
   public boolean canRender(Message message)
   {
      try
      {
         if (message instanceof TextMessage)
         {
            final String text = ((TextMessage) message).getText() ;
            
            return XmlUtils.isXML(text) ;
         }
      }
      catch (JMSException e)
      {
         log.error("error getting text: " + e.getMessage(), e) ;
      }
      
      return false ;
   }

   public String getDisplayName()
   {
      return "XML";
   }
}
