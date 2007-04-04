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

package hermes.renderers.fix;

import hermes.browser.ConfigDialogProxy;
import hermes.browser.MessageRenderer;
import hermes.fix.FIXException;
import hermes.fix.FIXMessage;
import hermes.fix.FIXUtils;
import hermes.fix.quickfix.QuickFIXMessage;
import hermes.fix.quickfix.QuickFIXMessageCache;
import hermes.renderers.RendererHelper;
import hermes.util.MessageUtils;

import java.io.IOException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;

/**
 * Renderer for FIX messages carried in BytesMessage or TextMessage.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: FIXMessageRenderer.java,v 1.6 2006/04/28 09:59:37 colincrist
 *          Exp $
 */
public class FIXMessageRenderer implements MessageRenderer
{
   private static final Logger log = Logger.getLogger(FIXMessageRenderer.class);
   private static final String MESSAGE_CACHE = "messageCache";
   private static final String MESSAGE_CACHE_INFO = "The number of panels to cache - can speed up the user interface when switching between messags";
   private static final String VALUE_WITH_ENUM = "displayValueWithEnum";
   private static final String VALUE_WITH_ENUM_INDO = "If true displays any enumeration values along with the descriptive text";
   private static final String SHOW_HEADER_AND_TRAINER = "displayHeaderAndTrailer";
   private static final String SHOW_HEADER_AND_TRAINER_INFO = "Display header and trailer fields";

   private QuickFIXMessageCache cache = new QuickFIXMessageCache(32) ;
   private LRUMap panelCache;
   private MyConfig currentConfig = new MyConfig();

   public class MyConfig implements Config
   {
      private int messageCache = 100;
      private boolean displayValueWithEnum = true;
      private boolean displayHeaderAndTrailer = true;
      private String name;

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public String getPropertyDescription(String propertyName)
      {
         if (propertyName.equals(MESSAGE_CACHE))
         {
            return MESSAGE_CACHE_INFO;
         }

         if (propertyName.equals(VALUE_WITH_ENUM))
         {
            return VALUE_WITH_ENUM_INDO;
         }

         if (propertyName.equals(SHOW_HEADER_AND_TRAINER))
         {
            return SHOW_HEADER_AND_TRAINER_INFO;
         }

         return propertyName;
      }

      public int getMessageCache()
      {
         return messageCache;
      }

      public void setMessageCache(int messageCache)
      {
         this.messageCache = messageCache;
      }

      public boolean getDisplayValueWithEnum()
      {
         return displayValueWithEnum;
      }

      public void setDisplayValueWithEnum(boolean displayValueWithEnum)
      {
         this.displayValueWithEnum = displayValueWithEnum;
      }

      public boolean getDisplayHeaderAndTrailer()
      {
         return displayHeaderAndTrailer;
      }

      public void setDisplayHeaderAndTrailer(boolean displayHeaderAndTrailer)
      {
         this.displayHeaderAndTrailer = displayHeaderAndTrailer;
      }
   }

   protected JComponent handleObjectMessage(final ObjectMessage objectMessage) throws JMSException
   {
      return null;
   }

   protected JComponent handleMapMessage(MapMessage mapMessage) throws JMSException
   {
      return null;
   }

   protected JComponent handleBytesMessage(BytesMessage bytesMessage) throws JMSException, IOException, ClassNotFoundException
   {
      try
      {
         bytesMessage.reset();

         final byte[] bytes = MessageUtils.asBytes(bytesMessage) ;

         return createComponent(new QuickFIXMessage(cache, bytes));
      }
      finally
      {
         bytesMessage.reset();
      }
   }

   protected JComponent handleStreamMessage(StreamMessage streamMessage) throws JMSException
   {
      return null;
   }

   protected JComponent handleTextMessage(final TextMessage textMessage) throws JMSException
   {
      String text = textMessage.getText();

      return createComponent(new QuickFIXMessage(cache, text.getBytes()));
   }

   protected JComponent createComponent(FIXMessage message)
   {
      try
      {

         return FIXUtils.createView(message, currentConfig.getDisplayHeaderAndTrailer(), currentConfig.getDisplayValueWithEnum());
      }
      catch (FIXException e)
      {
         log.error(e.getMessage(), e);
      }

      log.debug("message is not a valid FIX message, ignoring");

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.MessageRenderer#render(javax.jms.Message)
    */
   public JComponent render(Message m)
   {
      try
      {
         JComponent rval = null;

         if (getPanelMap().containsKey(m))
         {
            rval = (JComponent) getPanelMap().get(m);
         }
         else
         {
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

            if (rval != null)
            {
               getPanelMap().put(m, rval);
            }
         }

         return rval;

      }
      catch (Throwable ex)
      {
         final JTextArea textArea = new JTextArea();

         textArea.setEditable(false);
         textArea.setText("Unable to display message: " + ex.getMessage());

         log.error(ex.getMessage(), ex);
         return textArea;
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
      currentConfig = (MyConfig) config;

      panelCache = new LRUMap(currentConfig.getMessageCache());
   }

   public synchronized Config getConfig()
   {
      return currentConfig;
   }

   public synchronized LRUMap getPanelMap()
   {
      if (panelCache == null)
      {
         panelCache = new LRUMap(currentConfig.getMessageCache());
      }

      return panelCache;
   }

   public JComponent getConfigPanel(ConfigDialogProxy dialogProxy) throws Exception
   {
      return RendererHelper.createDefaultConfigPanel(dialogProxy);
   }

   public boolean canRender(Message message)
   {
      try
      {
         return FIXUtils.isFIX(message);
      }
      catch (JMSException ex)
      {
         log.error("during FIXMessage.isValid(): " + ex.getMessage(), ex);
         return false;
      }
   }

   public String getDisplayName()
   {
      return "FIX";
   }
}