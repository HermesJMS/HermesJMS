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

package hermes.renderers;

import hermes.swing.MyTextArea;
import hermes.util.MessageUtils;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

/**
 * Tries to render the message in some simple way.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultMessageRenderer.java,v 1.4 2004/07/30 17:25:13
 *          colincrist Exp $
 */

public class DefaultMessageRenderer extends AbstractMessageRenderer
{
   private static final Logger log = Logger.getLogger(DefaultMessageRenderer.class);
   private static final String BYTESISSTRING = "bytesIsString";
   private static final String BYTESISOBJECT = "bytesIsObject";
   private static final String BYTESISOBJECTSIZE = "bytesIsObjectBufferSize";
   private static final String TOSTRINGOBJECT = "toStringOnObjectMessage";
   private static final String MESSAGE_CACHE = "messageCache";
   private static final String BYTESISSTRING_INFO = "Treat a BytesMessage as a sequence of 8 bit characters and convert to a String";
   private static final String BYTESISOBJECT_INFO = "Treat a BytesMessage as a Serialized Java object";
   private static final String BYTESISOBJECTSIZE_INFO = "Buffer size to use as temporary storage (ignored with JMS 1.1 providers as size is available on the message)";
   private static final String TOSTRINGOBJECT_INFO = "Just call toString() on any Object in an ObjectMessage";
   private static final String MESSAGE_CACHE_INFO = "The number of panels to cache - can speed up the user interface when switching between messags";

   private LRUMap panelCache;

   /**
    * Configuration bean for this renderer
    * 
    * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
    * @version $Id: DefaultMessageRenderer.java,v 1.4 2004/07/30 17:25:13
    *          colincrist Exp $
    */

   public class MyConfig extends AbstractMessageRenderer.BasicConfig
   {
      private String name;
      private boolean bytesIsObject = false;
      private int bytesIsObjectBufferSize = 64 * 1024;
      private boolean toStringOnObjectMessage = false;
      private int messageCache = 100;
      private boolean bytesIsString = false;

      public String toString()
      {
         return name + ".MyConfig: " + "bytesIsObject=" + bytesIsObject + ", bytesIsObjectBufferSize=" + bytesIsObjectBufferSize + ", toStringOnObjectMessage="
               + toStringOnObjectMessage + ", messageCache=" + messageCache + ", bytesIsString=" + bytesIsString;
      }

      public int getMessageCache()
      {
         return messageCache;
      }

      public void setMessageCache(int messageCache)
      {
         this.messageCache = messageCache;
      }

      /**
       * @return Returns the bytesIsObject.
       */
      public boolean isBytesIsObject()
      {
         return bytesIsObject;
      }

      /**
       * @param bytesIsObject
       *           The bytesIsObject to set.
       */
      public void setBytesIsObject(boolean bytesIsObject)
      {
         this.bytesIsObject = bytesIsObject;
      }

      /**
       * @return Returns the bytesIsObjectSize.
       */
      public int getBytesIsObjectBufferSize()
      {
         return bytesIsObjectBufferSize;
      }

      /**
       * @param bytesIsObjectSize
       *           The bytesIsObjectSize to set.
       */
      public void setBytesIsObjectBufferSize(int bytesIsObjectBufferSize)
      {
         this.bytesIsObjectBufferSize = bytesIsObjectBufferSize;
      }

      /**
       * @return Returns the name.
       */
      public String getName()
      {
         return name;
      }

      /**
       * @param name
       *           The name to set.
       */
      public void setName(String name)
      {
         this.name = name;
      }

      public String getPropertyDescription(String propertyName)
      {
         if (propertyName.equals(BYTESISOBJECT))
         {
            return BYTESISOBJECT_INFO;
         }

         if (propertyName.equals(BYTESISOBJECTSIZE))
         {
            return BYTESISOBJECTSIZE_INFO;
         }

         if (propertyName.equals(TOSTRINGOBJECT))
         {
            return TOSTRINGOBJECT_INFO;
         }

         if (propertyName.equals(MESSAGE_CACHE))
         {
            return MESSAGE_CACHE_INFO;
         }

         if (propertyName.equals(BYTESISSTRING))
         {
            return BYTESISSTRING_INFO;
         }

         return propertyName;
      }

      /**
       * @return Returns the toStringOnObjectMessage.
       */
      public boolean isToStringOnObjectMessage()
      {
         return toStringOnObjectMessage;
      }

      /**
       * @param toStringOnObjectMessage
       *           The toStringOnObjectMessage to set.
       */
      public void setToStringOnObjectMessage(boolean toStringOnObjectMessage)
      {
         this.toStringOnObjectMessage = toStringOnObjectMessage;
      }

      public boolean isBytesIsString()
      {
         return bytesIsString;
      }

      public void setBytesIsString(boolean bytesIsString)
      {
         this.bytesIsString = bytesIsString;
      }
   }

  

   /**
    * DefaultMessageRenderer constructor comment.
    */
   public DefaultMessageRenderer()
   {
      super();
   }

   /**
    * Show the TextMessage in a JTextArea.
    * 
    * @param textMessage
    * @return
    * @throws JMSException
    */
   protected JComponent handleTextMessage(final TextMessage textMessage) throws JMSException
   {
      //
      // Show the text in a JTextArea, you can edit the message in place and
      // then drop it onto another queue/topic.

      final String text = textMessage.getText() ;
      final JTextArea textPane = new JTextArea() ;
      
      
      
      //final CharBuffer bytes = CharBuffer.wrap(text.subSequence(0, text.length())) ;
      //final JTextArea textPane = new MyTextArea(new PlainDocument(new MappedStringContent(bytes))) ; 

      textPane.setEditable(false);
      textPane.setFont(Font.decode("Monospaced-PLAIN-12"));
      textPane.setLineWrap(true) ;
      textPane.setWrapStyleWord(true);
      
      textPane.append(text) ;      
 
      textPane.getDocument().addDocumentListener(new DocumentListener()
      {
         public void doChange()
         {
            try
            {
               textMessage.setText(textPane.getText());
            }
            catch (JMSException e)
            {
               JOptionPane.showMessageDialog(textPane, "Unable to update the TextMessage: " + e.getMessage(), "Error modifying message content",
                     JOptionPane.ERROR_MESSAGE);

               try
               {
                  textPane.setText(textMessage.getText());
               }
               catch (JMSException e1)
               {
                  log.error(e1.getMessage(), e1);
               }

               textPane.setEditable(false);
               textPane.getDocument().removeDocumentListener(this);
            }
         }

         public void changedUpdate(DocumentEvent arg0)
         {
            doChange();
         }

         public void insertUpdate(DocumentEvent arg0)
         {
            doChange();
         }

         public void removeUpdate(DocumentEvent arg0)
         {
            doChange();
         }
      });
    
      textPane.setCaretPosition(0);

      return textPane;
   }

   /**
    * Depending on configuration, show the object via toString() or a list of
    * properties.
    * 
    * @param objectMessage
    * @return
    * @throws JMSException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    * @throws NoSuchMethodException
    */
   protected JComponent handleObjectMessage(final ObjectMessage objectMessage) throws JMSException, IllegalAccessException, InvocationTargetException,
         NoSuchMethodException
   {
      //
      // Unserialize the object and display all its properties

      Serializable obj = objectMessage.getObject();

      if (obj instanceof JComponent)
      {
         return (JComponent) obj;
      }
      else
      {
         JTextArea textPane = new JTextArea();
         StringBuffer buffer = new StringBuffer();
         MyConfig currentConfig = (MyConfig) getConfig() ;
         
         if (obj == null)
         {
        	 buffer.append("Payload is null") ;
         }
         else if (currentConfig.isToStringOnObjectMessage())
         {
            buffer.append(obj.toString());
         }
         else
         {
            buffer.append(obj.toString()).append("\n\nProperty list\n");

            for (Iterator iter = PropertyUtils.describe(obj).entrySet().iterator(); iter.hasNext();)
            {
               Map.Entry entry = (Map.Entry) iter.next();

               buffer.append(entry.getKey().toString()).append("=").append(entry.getValue()).append("\n");
            }
         }

         textPane.setEditable(false);
         textPane.setText(buffer.toString());

         return textPane;
      }
   }

   /**
    * Show the MapMessage as a tree.
    * 
    * @param mapMessage
    * @return
    * @throws JMSException
    */
   protected JComponent handleMapMessage(MapMessage mapMessage) throws JMSException
   {
      //
      // Show as a tree, one level deep.

      DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
      Object key = null;

      for (Enumeration iter = mapMessage.getMapNames(); iter.hasMoreElements();)
      {
         try
         {
            key = iter.nextElement();

            if (key instanceof String)
            {
               rootNode.add(new DefaultMutableTreeNode(key.toString() + "=" + mapMessage.getObject((String) key)));
            }
            else
            {
               log.error("unsupported key " + key);
            }

         }
         catch (Exception ex)
         {
            if (key != null)
            {
               rootNode.add(new DefaultMutableTreeNode(key.toString() + "= <cannot access field: " + ex.getMessage() + ">"));
            }
            else
            {
               log.error(ex.getMessage(), ex);
            }
         }
      }

      return new JTree(rootNode);
   }

   /**
    * Show a BytesMessage either as a java object or just a size.
    * 
    * @param bytesMessage
    * @return
    * @throws JMSException
    * @throws IOException
    * @throws ClassNotFoundException
    */
   protected JComponent handleBytesMessage(BytesMessage bytesMessage) throws JMSException, IOException, ClassNotFoundException
   {
      final JTextArea textPane = new MyTextArea() ; 
      final MyConfig currentConfig = (MyConfig) getConfig() ; 
      
      textPane.setEditable(false);
      bytesMessage.reset();

      if (currentConfig.isBytesIsObject())
      {
         final byte[] bytes = MessageUtils.asBytes(bytesMessage);
         final ByteArrayInputStream bistream = new ByteArrayInputStream(bytes);
         final ObjectInputStream oistream = new ObjectInputStream(bistream);
         final Object o = oistream.readObject();

         textPane.setWrapStyleWord(true);
         textPane.setText(o.toString());
      }
      else if (currentConfig.isBytesIsString())
      {
         try
         {
            final StringBuffer sb = new StringBuffer();

            sb.append(MessageUtils.asBytes(bytesMessage));
            textPane.setWrapStyleWord(true);
            textPane.setText(sb.toString());

            return textPane;
         }
         catch (JMSException e)
         {
            textPane.setText(e.getMessage());
         }
      }
      else
      {
         final long size = MessageUtils.getBodyLength(bytesMessage);
         textPane.setText("byte[size=" + size + "]");
      }

      return textPane;
   }

   /**
    * List out all the properties in the stream message.
    * 
    * @param streamMessage
    * @return
    * @throws JMSException
    */
   protected JComponent handleStreamMessage(StreamMessage streamMessage) throws JMSException
   {
      JTextArea textPane = new JTextArea();
      StringBuffer buffer = new StringBuffer();

      textPane.setEditable(false);

      streamMessage.reset();

      try
      {
         for (;;)
         {
            buffer.append(streamMessage.readObject().toString()).append("\n");
         }
      }
      catch (MessageEOFException ex)
      {
         // NOP
      }

      return textPane;
   }

   /**
    * Render the message, delegates to typed methods.
    */
   public JComponent render(final Message m)
   {
      try
      {
         if (getPanelMap().containsKey(m))
         {
            return (JComponent) getPanelMap().get(m);
         }

         JComponent rval = null;

         if (m instanceof TextMessage)
         {
            rval = handleTextMessage((TextMessage) m);
         }
         else if (m instanceof javax.jms.ObjectMessage)
         {
            rval = handleObjectMessage((ObjectMessage) m);
         }
         else if (m instanceof javax.jms.MapMessage)
         {
            rval = handleMapMessage((MapMessage) m);
         }
         else if (m instanceof BytesMessage)
         {
            rval = handleBytesMessage((BytesMessage) m);
         }
         else if (m instanceof StreamMessage)
         {
            rval = handleStreamMessage((StreamMessage) m);
         }
         else
         {
            JTextArea textPane = new JTextArea();

            textPane.setEditable(false);
            textPane.setText("Message has no Payload");

            rval = textPane;
         }

         getPanelMap().put(m, rval);

         return rval;

      }
      catch (Throwable ex)
      {
         final JTextArea textPane = new JTextArea();

         textPane.setEditable(false);

         final StringWriter string = new StringWriter();
         final PrintWriter writer = new PrintWriter(string);

         ex.printStackTrace(writer);
         writer.flush();

         textPane.setText("Unable to display message:\n" + string.toString());

         log.error(ex.getMessage(), ex);

         return textPane;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.MessageRenderer#createConfig()
    */
   public Config createConfig()
   {
      return new MyConfig();
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.MessageRenderer#setConfig(hermes.browser.MessageRenderer.Config)
    */
   public synchronized void setConfig(Config config)
   {
      final MyConfig currentConfig = (MyConfig) config ;
      panelCache = new LRUMap(currentConfig.getMessageCache());
      super.setConfig(config) ;
   }

  

   public synchronized LRUMap getPanelMap()
   {
      if (panelCache == null)
      {
         final MyConfig currentConfig = (MyConfig) getConfig() ;
         panelCache = new LRUMap(currentConfig.getMessageCache());
      }

      return panelCache;
   }

   /**
    * This is the catch all renderer so will always render a message.
    */
   public boolean canRender(Message message)
   {
      return true;
   }

   public String getDisplayName()
   {
      return "Payload";
   }
}