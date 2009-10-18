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

package hermes.browser.dialog;

import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.config.HermesConfig;
import hermes.config.WatchConfig;

import java.awt.BorderLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

import com.jidesoft.grid.Property;
import com.jidesoft.grid.PropertyPane;
import com.jidesoft.grid.PropertyTable;
import com.jidesoft.grid.PropertyTableModel;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: GeneralConfigPanel.java,v 1.6 2004/07/30 17:25:15 colincrist
 *          Exp $
 */

public class GeneralConfigPanel extends JPanel
{
   private static final Logger log = Logger.getLogger(GeneralConfigPanel.class);

   private static final String AUDIT_DIRECTORY = "AuditDirectory";
   private static final String MESSAGES_DIRECTORY = "MessageDirectory";
   private static final String THREADPOOL = "ThreadPoolSize";
   private static final String MAX_CACHED_MESSAGES = "MaxCachedMessages";
   private static final String WATCH = "Watches";
   private static final String WATCH_ID = "Watch ";
   private static final String WATCH_UPDATE_FREQUENCY = "UpdateFrequency";
   private static final String WATCH_AGE_ALERT = "AlertWhenMessageOlderThan";
   private static final String WATCH_DEPTH_ALERT = "AlertWhenDepthGreaterThan";
   private static final String SHOW_AGE = "DisplayAgeOfOldestMessage";
   private static final String COPY_JMSCORRELATIONID = "CopyJMSCorrelationID";
   private static final String COPY_JMSTYPE = "CopyJMSType";
   private static final String COPY_JMSEXPIRATION = "CopyJMSExpiration";
   private static final String COPY_JMSREPLYTO = "CopyJMSReplyTo";
   private static final String COPY_JMSPRIORITY = "CopyJMSPriority";
   private static final String COPY_PROVIDER_PROPERTIES = "CopyProviderProperties";
   private static final String AUTO_REFRESH_PERIOD = "AutoRefreshBrowseTimeout";
   private static final String DISPLAY_ADMINFACTORY = "DisplayAdminFactory";
   private static final String CORRECT_DND = "UseCorrectDropSemantics";
   private static final String SELECTOR_IMPL = "MessageStoreSelector";
   private static final String CONSUMER_TIMEOUT = "ConsumerTimeoutWhenQueueBrowsing";
   private static final String QF_CACHE = "QuickFIXMessageCache";
   private static final String QF_FILTER_SESSION = "QuickFIXFilterSessionMessages";
   private static final String ENABLE_JYTHON = "EnableJython";
   private static final String SCROLL_MESSAGES_IN_BROWSE = "ScrollMessagesDuringBrowse";
   private static final String BASE64_ENCODE_MESSAGES = "Base64EncodeTextMessages";
   private static final String MESSAGE_STORE_MESSAGE_FACTORY = "MessageStoreMessageFactory";

   private static final String AUDIT_DIRECTORY_INFO = "The directory where audit files are written whenever you interact with a queue/topic.";
   private static final String MESSAGES_DIRECTORY_INFO = "The directory to hold your message repository files.";
   private static final String THREADPOOL_INFO = "The maximum size of the thread pool allowing concurrent tasks to take place.";
   private static final String MAX_CACHED_MESSAGES_INFO = "The maximum number of messages cached when you browse a queue/topic, when you've browsed more than this number the oldest messages are removed from view.";
   private static final String WATCH_INFO = "Destination watch options.";
   private static final String WATCH_AGE_ALERT_INFO = "If a message on the head of a queue/topic is more than this number of milliseconds old then alert. Set to 0 to disable.";
   private static final String WATCH_DEPTH_ALERT_INFO = "If the depth of the queue/topic is greater than this then alert. Set to 0 to disable.";
   private static final String SHOW_AGE_INFO = "Show the age rather than the put time of the oldest message.";
   private static final String WATCH_UPDATE_FREQUENCY_INFO = "Polling frequency (in milliseconds) to collect statistics.";
   private static final String COPY_JMSCORRELATIONID_INFO = "Copy JMSCorrelationID header property when drag and dropping messages.";
   private static final String COPY_JMSTYPE_INFO = "Copy JMSType header property when drag and dropping messages.";
   private static final String COPY_JMSEXPIRATION_INFO = "Copy JMSExpiration header property when drag and dropping messages.";
   private static final String COPY_JMSREPLYTO_INFO = "Copy JMSReplyTo header property when drag and dropping messages.";
   private static final String COPY_JMSPRIORITY_INFO = "Copy JMSPriority header property when drag and dropping messages.";
   private static final String COPY_PROVIDER_PROPERTIES_INFO = "Copy provider properties when duplicating messages";
   private static final String DISPLAY_ADMINFACTORY_INFO = "Display the admin factory configuration panel is the Preferences/Sessions dialog, set to false if you never use extensions to avoid screen clutter.";
   private static final String AUTO_REFRESH_PERIOD_INFO = "The number of seconds between each poll of a queue when using automatic refresh";
   private static final String CORRECT_DND_INFO = "When true, the default behavour when dropping messages on a queue is MOVE otherwise its COPY. When true you can use the control key to toggle between COPY and MOVE.";
   private static final String SELECTOR_IMPL_INFO = "The selector implementation to use with message stores";
   private static final String CONSUMER_TIMEOUT_INFO = "The timeout in milliseconds when using a consumer for browsing queues/topics to stop browsing. If 0 the default of 10s is used.";
   private static final String QF_CACHE_INFO = "Message cache for QuickFIX/J to control memory use";
   private static final String QF_FILTER_SESSION_INFO = "Filter out session level FIX messages";
   private static final String ENABLE_JYTHON_INFO = "Enable Jython";
   private static final String SCROLL_MESSAGES_IN_BROWSE_INFO = "Scroll to the newest message during a browse";
   private static final String BASE64_ENCODE_MESSAGES_INFO = "Base64 encode text messages when stored in XML or in message stores";
   private static final String MESSAGE_STORE_MESSAGE_FACTORY_INFO = "The JMS session to use when working with message stores";

   private PreferencesDialog dialog;
   private HermesConfig config;
   private PropertyTable propertyTable;
   private PropertyPane propertyPane;
   private Property auditDirectoryProperty;
   private Property messagesDirectoryProperty;
   private Property threadPoolProperty;
   private Property maxCachedMessagesProperty;
   private Property watchProperty;
   private Property copyJMSCorrelationIdProperty;
   private Property copyJMSTypeProperty;
   private Property copyJMSExpirationProperty;
   private Property copyJMSReplyToProperty;
   private Property copyJMSPriorityProperty;
   private Property copyProviderPropertiesProperty;
   private Property displayAdminFactoryProperty;
   private Property autoRefreshPeriodProperty;
   private Property correctDNDProperty;
   private Property selectorImplProperty;
   private Property consumerTimeoutProperty;
   private Property quickFIXCacheProperty;
   private Property quickFIXCacheFilterSessionProperty;
   private Property enableJythonProperty;
   private Property scrollMessagesInBrowseProperty;
   private Property base64EncodeMessagesProperty;
   private Property messageStoreMessageFactory;

   private List<Runnable> watchSetters = new ArrayList<Runnable>();

   public static class PropertyImpl extends Property
   {
      public Object value;

      /*
       * (non-Javadoc)
       * 
       * @see com.jidesoft.grid.Property#getValue()
       */
      public Object getValue()
      {
         return value;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.jidesoft.grid.Property#hasValue()
       */
      public boolean hasValue()
      {

         return value != null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see com.jidesoft.grid.Property#setValue(java.lang.Object)
       */
      public void setValue(Object value)
      {
         this.value = value;
      }

      public PropertyImpl(String name, String value)
      {
         super(name, value);
      }

      /**
       * @param arg0
       * @param arg1
       * @param arg2
       */
      public PropertyImpl(String name, Object value, String description, Class type)
      {
         super(name, description, type);

         this.value = value;
      }
   }

   public GeneralConfigPanel(PreferencesDialog dialog)
   {
      this.dialog = dialog;

      init();
   }

   private void init()
   {
      Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);

      setLayout(new BorderLayout());

      setBorder(BorderFactory.createTitledBorder(border, "General Options"));
   }

   public void updateModel()
   {
      config.setCopyJMSCorrelationID(((Boolean) copyJMSCorrelationIdProperty.getValue()).booleanValue());
      config.setCopyJMSType(((Boolean) copyJMSTypeProperty.getValue()).booleanValue());
      config.setCopyJMSExpiration(((Boolean) copyJMSExpirationProperty.getValue()).booleanValue());
      config.setCopyJMSReplyTo(((Boolean) copyJMSReplyToProperty.getValue()).booleanValue());
      config.setCopyJMSPriority(((Boolean) copyJMSPriorityProperty.getValue()).booleanValue());
      config.setCopyJMSProviderProperties(((Boolean) copyProviderPropertiesProperty.getValue()).booleanValue());
      config.setDisplayFactoryAdmin(((Boolean) displayAdminFactoryProperty.getValue()).booleanValue());
      config.setAutoBrowseRefreshRate((Integer) autoRefreshPeriodProperty.getValue());
      config.setCorrectDropSemantics((Boolean) correctDNDProperty.getValue());
      config.setSelectorImpl(((SelectorImpl) selectorImplProperty.getValue()).getClazz().getName());

      config.getQuickFIX().setCacheSize(((Integer) quickFIXCacheProperty.getValue()).intValue());
      config.getQuickFIX().setFilterSessionMsgTypes((Boolean) quickFIXCacheFilterSessionProperty.getValue());

      config.setEnableJython((Boolean) enableJythonProperty.getValue());
      config.setScrollMessagesDuringBrowse((Boolean) scrollMessagesInBrowseProperty.getValue());
      config.setBase64EncodeMessages((Boolean) base64EncodeMessagesProperty.getValue());
      config.setMessageStoreMessageFactory((String) messageStoreMessageFactory.getValue());

      if (consumerTimeoutProperty.getValue() != null)
      {
         config.setQueueBrowseConsumerTimeout(((Long) consumerTimeoutProperty.getValue()));
      }

      if (threadPoolProperty.getValue() != null)
      {
         config.setMaxThreadPoolSize(((Integer) threadPoolProperty.getValue()).intValue());
      }

      if (auditDirectoryProperty.getValue() != null)
      {
         config.setAuditDirectory(((File) auditDirectoryProperty.getValue()).getAbsolutePath());
      }

      if (messagesDirectoryProperty.getValue() != null)
      {
         config.setMessageFilesDir(((File) messagesDirectoryProperty.getValue()).getAbsolutePath());
      }

      if (maxCachedMessagesProperty.getValue() != null)
      {
         config.setMaxMessagesInBrowserPane(((Integer) maxCachedMessagesProperty.getValue()).intValue());
      }

      HermesBrowser.getBrowser().getMessageRepository().setDirectory(config.getMessageFilesDir());

      for (final Runnable r : watchSetters)
      {
         r.run();
      }
   }

   public void setHermesConfig(HermesConfig config)
   {
      this.config = config;

      List<Property> list = new ArrayList<Property>();

      watchSetters.clear();

      auditDirectoryProperty = new PropertyImpl(AUDIT_DIRECTORY, new File(config.getAuditDirectory() != null ? config.getAuditDirectory() : ""),
            AUDIT_DIRECTORY_INFO, File.class);
      messagesDirectoryProperty = new PropertyImpl(MESSAGES_DIRECTORY, new File(config.getMessageFilesDir() != null ? config.getMessageFilesDir() : ""),
            MESSAGES_DIRECTORY_INFO, File.class);
      threadPoolProperty = new PropertyImpl(THREADPOOL, new Integer(config.getMaxThreadPoolSize()), THREADPOOL_INFO, Integer.class);
      maxCachedMessagesProperty = new PropertyImpl(MAX_CACHED_MESSAGES, new Integer(config.getMaxMessagesInBrowserPane()), MAX_CACHED_MESSAGES_INFO,
            Integer.class);
      copyJMSCorrelationIdProperty = new PropertyImpl(COPY_JMSCORRELATIONID, new Boolean(config.isCopyJMSCorrelationID()), COPY_JMSCORRELATIONID_INFO,
            Boolean.class);
      copyJMSTypeProperty = new PropertyImpl(COPY_JMSTYPE, new Boolean(config.isCopyJMSType()), COPY_JMSTYPE_INFO, Boolean.class);
      copyJMSExpirationProperty = new PropertyImpl(COPY_JMSEXPIRATION, new Boolean(config.isCopyJMSExpiration()), COPY_JMSEXPIRATION_INFO, Boolean.class);
      copyJMSReplyToProperty = new PropertyImpl(COPY_JMSREPLYTO, new Boolean(config.isCopyJMSReplyTo()), COPY_JMSREPLYTO_INFO, Boolean.class);
      copyJMSPriorityProperty = new PropertyImpl(COPY_JMSPRIORITY, new Boolean(config.isCopyJMSPriority()), COPY_JMSPRIORITY_INFO, Boolean.class);
      copyProviderPropertiesProperty = new PropertyImpl(COPY_PROVIDER_PROPERTIES, new Boolean(config.isCopyJMSProviderProperties()),
            COPY_PROVIDER_PROPERTIES_INFO, Boolean.class);
      displayAdminFactoryProperty = new PropertyImpl(DISPLAY_ADMINFACTORY, new Boolean(config.isDisplayFactoryAdmin()), DISPLAY_ADMINFACTORY_INFO,
            Boolean.class);
      autoRefreshPeriodProperty = new PropertyImpl(AUTO_REFRESH_PERIOD, new Integer(config.getAutoBrowseRefreshRate()), AUTO_REFRESH_PERIOD_INFO, Integer.class);
      correctDNDProperty = new PropertyImpl(CORRECT_DND, new Boolean(config.isCorrectDropSemantics()), CORRECT_DND_INFO, Boolean.class);
      selectorImplProperty = new PropertyImpl(SELECTOR_IMPL, SelectorImpl.getWithClassName(config.getSelectorImpl()), SELECTOR_IMPL_INFO, SelectorImpl.class);
      consumerTimeoutProperty = new PropertyImpl(CONSUMER_TIMEOUT, new Long(config.getQueueBrowseConsumerTimeout() == null ? 0 : config
            .getQueueBrowseConsumerTimeout()), CONSUMER_TIMEOUT_INFO, Long.class);
      watchProperty = new PropertyImpl(WATCH, WATCH_INFO);
      quickFIXCacheProperty = new PropertyImpl(QF_CACHE, new Integer(config.getQuickFIX().getCacheSize()), QF_CACHE_INFO, Integer.class);
      quickFIXCacheFilterSessionProperty = new PropertyImpl(QF_FILTER_SESSION, new Boolean(config.getQuickFIX().isFilterSessionMsgTypes() == null ? false
            : config.getQuickFIX().isFilterSessionMsgTypes()), QF_FILTER_SESSION_INFO, Boolean.class);
      enableJythonProperty = new PropertyImpl(ENABLE_JYTHON, new Boolean(config.isEnableJython()), ENABLE_JYTHON_INFO, Boolean.class);
      scrollMessagesInBrowseProperty = new PropertyImpl(SCROLL_MESSAGES_IN_BROWSE, new Boolean(config.isScrollMessagesDuringBrowse()),
            SCROLL_MESSAGES_IN_BROWSE_INFO, Boolean.class);
      base64EncodeMessagesProperty = new PropertyImpl(BASE64_ENCODE_MESSAGES, new Boolean(config.isBase64EncodeMessages()), BASE64_ENCODE_MESSAGES_INFO,
            Boolean.class);
      messageStoreMessageFactory = new PropertyImpl(MESSAGE_STORE_MESSAGE_FACTORY, config.getMessageStoreMessageFactory(), MESSAGE_STORE_MESSAGE_FACTORY_INFO,
            Hermes.class);

      for (Iterator iter = config.getWatch().iterator(); iter.hasNext();)
      {
         final WatchConfig wConfig = (WatchConfig) iter.next();
         final Property watchIdProperty = new PropertyImpl(wConfig.getId(), WATCH_INFO + " for " + wConfig.getId());

         final Property watchFrequenceProperty = new PropertyImpl(WATCH_UPDATE_FREQUENCY, new Long(wConfig.getUpdateFrequency()), WATCH_UPDATE_FREQUENCY_INFO,
               Long.class);
         final Property watchAgeAlertProperty = new PropertyImpl(WATCH_AGE_ALERT, new Long(wConfig.getDefaultAgeAlertThreshold()), WATCH_AGE_ALERT_INFO,
               Long.class);
         final Property watchDepthAlertProperty = new PropertyImpl(WATCH_DEPTH_ALERT, new Integer(wConfig.getDefaultDepthAlertThreshold()),
               WATCH_DEPTH_ALERT_INFO, Integer.class);
         final Property showAgeProperty = new PropertyImpl(SHOW_AGE, new Boolean(wConfig.isShowAge()), SHOW_AGE_INFO, Boolean.class);

         watchIdProperty.addChild(watchFrequenceProperty);
         watchIdProperty.addChild(watchAgeAlertProperty);
         watchIdProperty.addChild(watchDepthAlertProperty);
         watchIdProperty.addChild(showAgeProperty);

         watchProperty.addChild(watchIdProperty);

         //
         // Much easier to create an object here to set the values into the
         // model.

         watchSetters.add(new Runnable()
         {
            public void run()
            {
               if (showAgeProperty.getValue() != null)
               {
                  wConfig.setShowAge(((Boolean) showAgeProperty.getValue()).booleanValue());
               }

               if (watchAgeAlertProperty.getValue() != null)
               {
                  if (watchAgeAlertProperty.getValue() instanceof Long)
                  {
                     wConfig.setDefaultAgeAlertThreshold(((Long) watchAgeAlertProperty.getValue()).longValue());
                  }
                  else
                  {
                     wConfig.setDefaultAgeAlertThreshold(((Long) watchAgeAlertProperty.getValue()).longValue());
                  }
               }

               if (watchDepthAlertProperty.getValue() != null)
               {
                  wConfig.setDefaultDepthAlertThreshold(((Integer) watchDepthAlertProperty.getValue()).intValue());
               }

               if (watchFrequenceProperty.getValue() != null)
               {
                  if (watchFrequenceProperty.getValue() instanceof Long)
                  {
                     wConfig.setUpdateFrequency(((Long) watchFrequenceProperty.getValue()).longValue());
                  }
                  else
                  {
                     wConfig.setUpdateFrequency(((Long) watchFrequenceProperty.getValue()).longValue());
                  }
               }

               HermesBrowser.getBrowser().getWatchFrame(wConfig.getId()).updateConfig();
            }
         });
      }

      auditDirectoryProperty.setEditorContext(DirectoryCellEditor.CONTEXT);
      messagesDirectoryProperty.setEditorContext(DirectoryCellEditor.CONTEXT);

      list.add(auditDirectoryProperty);
      list.add(messagesDirectoryProperty);
      list.add(threadPoolProperty);
      list.add(maxCachedMessagesProperty);
      list.add(copyJMSCorrelationIdProperty);
      list.add(copyJMSExpirationProperty);
      list.add(copyJMSPriorityProperty);
      list.add(copyJMSReplyToProperty);
      list.add(copyJMSTypeProperty);
      list.add(copyProviderPropertiesProperty);
      list.add(displayAdminFactoryProperty);
      list.add(autoRefreshPeriodProperty);
      list.add(correctDNDProperty);
      list.add(selectorImplProperty);
      list.add(consumerTimeoutProperty);
      list.add(quickFIXCacheProperty);
      list.add(quickFIXCacheFilterSessionProperty);
      list.add(enableJythonProperty);
      list.add(scrollMessagesInBrowseProperty);
      list.add(base64EncodeMessagesProperty);
      list.add(messageStoreMessageFactory);

      list.add(watchProperty);

      PropertyTableModel model = new PropertyTableModel(list);
      propertyTable = new PropertyTable(model);
      propertyTable.expandFirstLevel();

      if (propertyPane != null)
      {
         remove(propertyPane);
      }

      propertyPane = new PropertyPane(propertyTable);
      add(propertyPane, BorderLayout.CENTER);
   }
}