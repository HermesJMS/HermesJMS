/* 
 * Copyright 2003,2004,2005,2006 Colin Crist
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

package hermes.browser;

import hermes.ConnectionListener;
import hermes.Domain;
import hermes.Hermes;
import hermes.HermesConfigurationListener;
import hermes.HermesContext;
import hermes.HermesException;
import hermes.HermesInitialContextFactory;
import hermes.HermesLoader;
import hermes.HermesRepositoryManager;
import hermes.JAXBHermesLoader;
import hermes.JNDIContextFactory;
import hermes.SingletonManager;
import hermes.SystemProperties;
import hermes.browser.actions.ActionFactory;
import hermes.browser.actions.BrowserAction;
import hermes.browser.components.ActionsPanel;
import hermes.browser.components.BrowserTree;
import hermes.browser.components.DockableToolPanel;
import hermes.browser.components.Log4JOutputViewer;
import hermes.browser.components.NavigableComponent;
import hermes.browser.components.WatchDockableFrame;
import hermes.browser.dialog.ClasspathIdCellEdtitor;
import hermes.browser.dialog.DirectoryCellEditor;
import hermes.browser.dialog.DomainCellEditor;
import hermes.browser.dialog.HermesCellEditor;
import hermes.browser.dialog.SelectorImpl;
import hermes.browser.dialog.SelectorImplCellEditor;
import hermes.browser.jython.JythonDockableFrame;
import hermes.browser.model.BrowserTreeModel;
import hermes.browser.tasks.HermesBrowserTaskListener;
import hermes.browser.tasks.TaskSupport;
import hermes.browser.tasks.ThreadPool;
import hermes.config.DestinationConfig;
import hermes.config.HermesConfig;
import hermes.config.NamingConfig;
import hermes.config.WatchConfig;
import hermes.fix.FIXPrettyPrinter;
import hermes.fix.FIXUtils;
import hermes.impl.ConfigDAO;
import hermes.impl.ConfigDAOImpl;
import hermes.impl.FileRepositoryManager;
import hermes.impl.SimpleClassLoaderManager;
import hermes.renderers.RendererManager;
import hermes.util.JVMUtils;
import hermes.util.TextUtils;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

import jsyntaxpane.DefaultSyntaxKit;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.log4j.Logger;
import org.python.core.PyException;

import com.jidesoft.action.CommandBar;
import com.jidesoft.action.DefaultDockableBarDockableHolder;
import com.jidesoft.comparator.ObjectComparatorManager;
import com.jidesoft.converter.ObjectConverterManager;
import com.jidesoft.dialog.JideOptionPane;
import com.jidesoft.docking.DefaultDockingManager;
import com.jidesoft.docking.event.DockableFrameAdapter;
import com.jidesoft.docking.event.DockableFrameEvent;
import com.jidesoft.document.DocumentComponent;
import com.jidesoft.document.DocumentComponentEvent;
import com.jidesoft.document.DocumentComponentListener;
import com.jidesoft.document.DocumentPane;
import com.jidesoft.grid.CellEditorManager;
import com.jidesoft.grid.CellRendererManager;
import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.plaf.basic.ThemePainter;
import com.jidesoft.status.MemoryStatusBarItem;
import com.jidesoft.status.ProgressStatusBarItem;
import com.jidesoft.status.StatusBar;
import com.jidesoft.status.TimeStatusBarItem;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideScrollPane;
import com.jidesoft.swing.SplashScreen;
import com.jidesoft.utils.Lm;

/**
 * HermesBrowser. A Swing GUI for working with JMS
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HermesBrowser.java,v 1.94 2007/02/18 19:01:44 colincrist Exp $
 */
public class HermesBrowser extends DefaultDockableBarDockableHolder implements HermesUI
{
   public static final int DEFAULT_MAX_CACHED_MESSAGES = 1000;
   public static final long DEFAULT_QUEUE_BROWSE_CONSUMER_TIMEOUT = 10000;

   private static final String DEFAULT_PROFILE_NAME = "hermes.layout";
   private static final String HERMES_TITLE = "Hermes " + Hermes.VERSION;
   private static final Logger log = Logger.getLogger(HermesBrowser.class);
   private static final RendererManager rendererManager = new RendererManager();
   private static final ConfigDAO configDAO = new ConfigDAOImpl();
   private static final long serialVersionUID = 995079090594726460L;
   private static HermesBrowser ui;
   private static final String USER_PROFILE_NAME = "hermes.layout." + System.getProperty("user.name");
   private boolean restricted = System.getProperty(SystemProperties.RESTRICTED) != null;

   public static HermesBrowser getBrowser()
   {

      return ui;
   }

   public static ConfigDAO getConfigDAO()
   {
      return configDAO;
   }

   public static RendererManager getRendererManager()
   {
      return rendererManager;
   }

   @Override
   public void setConfig(HermesConfig config)
   {
      // TODO Auto-generated method stub
      
   }

   public static void main(String[] args)
   {
      log.debug("Hermes Browser " + Hermes.VERSION + " starting...");

      Hermes.events.addConnectionListener(new ConnectionListener()
      {
         public void onConnectionOpen(Hermes hermes)
         {
            log.debug("Connection " + hermes.getId() + " opened");
         }

         public void onConnectionClosed(Hermes hermes)
         {
            log.debug("Connection " + hermes.getId() + " closed");
         }
      });

      //
      // Need to bootstrap objects into the singleton manager... hack for now.

      JVMUtils.forceInit(SingletonManager.class);
      JVMUtils.forceInit(ThreadPool.class);
      JVMUtils.forceInit(SimpleClassLoaderManager.class);

      //
      // Commented out as this is for debug use only.
      // RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());

      //
      // Note this is the license for the JIDE Framework, it is licenced
      // to Colin Crist and the Hermes project and should not be used for any
      // other purpose
      //

      Lm.verifyLicense("Colin Crist", "Hermes", "9vkNAfxF1lvVyW7uZXYjpxFskycSGLw1");

      //
      // See http://www.jidesoft.com for licensing terms.

      //
      // Register a converter from a String to a File with PropertyUtils.

      ConvertUtils.register(new Converter()
      {
         public Object convert(Class arg0, Object filename)
         {
            return new File((String) filename);
         }
      }, File.class);

      SplashScreen.create(IconCache.getIcon("hermes.splash"));
      SplashScreen.show();

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            try
            {

               ui = new HermesBrowser(HERMES_TITLE);

               ui.initJIDE();

               try
               {
                  ui.loadConfig();
               }
               catch (NamingException ex)
               {
                  log.fatal("cannot initialise hermes: " + ex.getMessage(), ex);

                  return;
               }
               catch (HeadlessException ex)
               {
                  log.fatal("cannot initialise hermes browser, no head: " + ex.getMessage(), ex);
                  return;
               }

               ui.initUI();
               ui.init();

               ui.getLayoutPersistence().setProfileKey(ui.getUserProfileName());
               ui.getLayoutPersistence().loadLayoutData();

               // This must be done after the layout has been set otherwise the
               // frames are hidden.

               final ArrayList<WatchConfig> tmpList = new ArrayList<WatchConfig>(ui.getConfig().getWatch());

               ui.getLoader().getConfig().getWatch().clear();

               for (WatchConfig wConfig : tmpList)
               {
                  ui.createWatch(wConfig);
               }

               ui.firstLoad = false;
            }
            catch (Exception ex)
            {
               log.fatal("cannot initialise hermes browser: " + ex.getMessage(), ex);
            }
         }
      });
   }

   private static void setStatusMessage(final String statusMessage)
   {
      if (ui != null && ui.progressStatus != null && statusMessage != null)
      {
         if (SwingUtilities.isEventDispatchThread())
         {
            ui.progressStatus.setStatus(statusMessage);
         }
         else
         {
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  ui.progressStatus.setStatus(statusMessage);
               }
            });
         }
      }
   }

   private ActionFactory actionFactory = new ActionFactory(this);
   private ActionsPanel actionsPane;
   private DocumentPane browserPane;

   //
   // Structure for this window

   private JythonDockableFrame jythonFrame;
   private BrowserTreeDockableFrame browserTreePane;
   private Context context;
   private String currentConfig;
   private UIMessageSink defaultMessageSink;
   private boolean firstLoad = true;
   private File lastOpenConfigDirectory;
   private File lastUploadDirectory;

   //
   // Hermes related stuff

   private HermesLoader loader;
   private JMenuBar menuBar;
   private ProgressStatusBarItem progressStatus;

   //
   // Standard contents

   private Map<String, WatchDockableFrame> queueWatchFrames = new HashMap<String, WatchDockableFrame>();
   private HermesRepositoryManager repManager;
   private StatusBar statusBar;
   private DockableToolPanel toolsPane;

   //
   // Listeners...

   private Set<DocumentComponentListener> documentComponentListeners = new HashSet<DocumentComponentListener>();
   private Set<ListSelectionListener> messageSelectionListeners = new HashSet<ListSelectionListener>();

   /**
    * Start the GUI up....
    */
   public HermesBrowser(String arg0) throws HeadlessException
   {
      super(arg0);

      // Funnell interesting messages onto the UI...

      defaultMessageSink = new UIMessageSink()
      {
         /*
          * (non-Javadoc)
          * 
          * @see hermes.browser.UIMessageSink#add(java.lang.String)
          */
         public void add(String message)
         {
            setStatusMessage(message);

         }

         /*
          * (non-Javadoc)
          * 
          * @see hermes.browser.UIMessageSink#add(java.lang.StringBuffer)
          */
         public void add(StringBuffer message)
         {
            setStatusMessage(message.toString());
         }
      };

      // Listen to asynchronous tasks...

      TaskSupport.addGlobalListener(new HermesBrowserTaskListener(this));
   }

   /**
    * A repository manager monitors a directory for XML files containing JMS
    * messages.
    * 
    * @return
    */
   public HermesRepositoryManager getRepositoryManager()
   {
      return repManager;
   }

   /**
    * Register that the user has created a new DestinationConfig, i.e. queue or
    * topic in the main tree.
    * 
    * @param hermes
    * @param dConfig
    * @throws JMSException
    */
   public void addDestinationConfig(Hermes hermes, DestinationConfig dConfig) throws JMSException
   {
      loader.addDestinationConfig(hermes, dConfig);
   }

   /**
    * Add a listener for messages selected in a browse document. The listener
    * will be added to the currently active table and removed when its
    * deactivated or closed.
    * 
    * @param listener
    */
   public void addMessageSelectionListener(ListSelectionListener listener)
   {
      messageSelectionListeners.add(listener);
   }

   /**
    * Add a listener to any document component that gets created in the future.
    * 
    * @param listener
    */
   public void addDocumentComponentListener(DocumentComponentListener listener)
   {
      documentComponentListeners.add(listener);
   }

   public int getSendPersistence()
   {
      return DeliveryMode.PERSISTENT;
   }

   /**
    * Sets the configuration URL.
    * 
    * @param currentConfig
    */
   public void setCurrentConfig(String currentConfig)
   {
      this.currentConfig = currentConfig;
   }

   /**
    * Add an BrowserAction to the document pane, registering the required
    * listeners and giving it focus.
    * 
    * @param action
    */
   public void addDocumentComponent(final DocumentComponent document)
   {
      log.debug("addDocument() name=" + document.getName());

      if (getDocumentPane().isDocumentOpened(document.getName()))
      {
         DocumentComponent existing = getDocumentPane().getDocument(document.getName());

         if (document != existing)
         {
            getDocumentPane().closeDocument(document.getName());
         }
      }

      //
      // Add/remove listeners for document lifecycle.

      for (final DocumentComponentListener listener : documentComponentListeners)
      {
         document.addDocumentComponentListener(listener);
      }

      document.addDocumentComponentListener(AutoRefreshCheckBox.getInstance());

      document.addDocumentComponentListener(new DocumentComponetListenerSupport()
      {
         @Override
         public void documentComponentClosed(DocumentComponentEvent arg0)
         {
            for (final DocumentComponentListener listener : documentComponentListeners)
            {
               document.removeDocumentComponentListener(listener);
            }

            document.removeDocumentComponentListener(AutoRefreshCheckBox.getInstance());
         }
      });

      //
      // Add/remove listeners for navigation control

      if (document instanceof NavigableComponent)
      {
         final NavigableComponent component = (NavigableComponent) document;

         for (final ListSelectionListener listener : messageSelectionListeners)
         {
            component.getListSelectionModel().addListSelectionListener(listener);
         }

         document.addDocumentComponentListener(new DocumentComponetListenerSupport()
         {
            @Override
            public void documentComponentClosed(DocumentComponentEvent arg0)
            {
               for (final ListSelectionListener listener : messageSelectionListeners)
               {
                  component.getListSelectionModel().removeListSelectionListener(listener);
               }
            }
         });
      }

      getDocumentPane().openDocument(document);
      getDocumentPane().setActiveDocument(document.getName());

   }

   /**
    * Create a new dockable panel for watching all the queues configured on the
    * given Hermes.
    * 
    * @param watchId
    * @param hermes
    */
   public synchronized void addOrCreateWatch(String watchId, Hermes hermes)
   {
      WatchDockableFrame frame = null;

      if (queueWatchFrames.containsKey(watchId))
      {
         frame = queueWatchFrames.get(watchId);
      }
      else
      {
         WatchConfig wConfig = HermesBrowser.getConfigDAO().createWatchConfig();
         wConfig.setId(watchId);

         frame = createWatch(wConfig);
      }

      frame.addWatch(hermes);
   }

   /**
    * Create/add the given destination to the new/existing watch panel.
    * 
    * @param watchId
    * @param hermes
    * @param destination
    */
   public synchronized void addOrCreateWatch(String watchId, Hermes hermes, DestinationConfig destination)
   {
      WatchDockableFrame frame = null;

      if (queueWatchFrames.containsKey(watchId))
      {
         frame = queueWatchFrames.get(watchId);
      }
      else
      {
         WatchConfig wConfig = HermesBrowser.getConfigDAO().createWatchConfig();
         wConfig.setId(watchId);

         frame = createWatch(wConfig);
      }

      DestinationConfig dConfig = HermesBrowser.getConfigDAO().duplicateForWatch(destination, hermes);

      frame.addWatch(hermes.getId(), dConfig);
   }

   /**
    * Backup the configuration into <config>.backup.
    * 
    * @throws HermesException
    */
   public void backupConfig() throws HermesException
   {
      loader.backup();
   }

   /**
    * Close all the watch panel.
    */
   private void closeWatches()
   {
      for (final Map.Entry<String, WatchDockableFrame> entry : queueWatchFrames.entrySet())
      {
         final WatchDockableFrame frame = entry.getValue();
         frame.close();
         getDockingManager().removeFrame(entry.getKey());
      }
   }

   /**
    * Create a new Watch dockable frame from the given watch configuration and
    * add it to the docking manager and the XML configuration.
    * 
    * @param watchConfig
    */
   private WatchDockableFrame createWatch(final WatchConfig wConfig)
   {
      final WatchDockableFrame frame = new WatchDockableFrame(wConfig);
      queueWatchFrames.put(wConfig.getId(), frame);

      frame.addDockableFrameListener(new DockableFrameAdapter()
      {
         public void dockableFrameHidden(DockableFrameEvent arg0)
         {
            log.debug("watch frame " + wConfig.getId() + " removed, clearing up");

            try
            {
               getConfig().getWatch().remove(wConfig);
               queueWatchFrames.remove(wConfig.getId());
            }
            catch (HermesException ex)
            {
               Hermes.ui.getDefaultMessageSink().add("Unable to remove watch " + wConfig.getId() + " from configuration: " + ex.getMessage());
            }

            getDockingManager().removeFrame(wConfig.getId());
            frame.close();
         }
      });

      getDockingManager().addFrame(frame);
      frame.setVisible(true);

      try
      {
         getConfig().getWatch().add(wConfig);
      }
      catch (HermesException ex)
      {
         Hermes.ui.getDefaultMessageSink().add("Unable to add watch " + wConfig.getId() + " to configuration: " + ex.getMessage());
      }

      frame.updateNow();

      return frame;
   }

   /**
    * Get the (inappopriately named) action factory to create long running
    * browse tasks.
    * 
    * @return
    */
   public ActionFactory getActionFactory()
   {
      return actionFactory;
   }

   /**
    * Get the panal containing the long running actions (tasks).
    * 
    * @return
    */
   public ActionsPanel getActionsPanel()
   {
      return actionsPane;
   }

   /**
    * Get the configuration tree of sessions, contexts etc.
    * 
    * @return
    */
   public BrowserTree getBrowserTree()
   {
      return browserTreePane.getBrowserTree();
   }

   public Context createContext(String id) throws JMSException, NamingException, InvocationTargetException, IOException, IllegalAccessException, NoSuchMethodException
   {
      final NamingConfig config = getBrowserTree().getBrowserModel().getNamingConfigTreeNode(id).getConfig() ;
      final JNDIContextFactory factory = new JNDIContextFactory(config) ;
      return factory.createContext() ;
   }
   /**
    * Get the current configuration model.
    * 
    * @return
    * @throws HermesException
    */
   public HermesConfig getConfig() throws HermesException
   {
      return loader.getConfig();
   }

   /**
    * Get the current context of configured Hermes instances.
    * 
    * @return
    */
   public Context getContext()
   {
      return context;
   }

   /**
    * Get the URL of the current configuration.
    * 
    * @return
    */
   public String getCurrentConfigURL()
   {
      if (currentConfig == null)
      {
         String rval = System.getProperty("hermes");

         if (rval == null)
         {
            File dotHermes = new File(JVMUtils.getUserHome() + File.separator + ".hermes");

            if (!dotHermes.exists())
            {
               if (dotHermes.mkdir())
               {
                  log.debug("created new directory: " + dotHermes.getAbsolutePath());
               }
               else
               {

                  log.error("could not create directory: " + dotHermes.getAbsolutePath());
                  log.error("properties set are listed to stdout.");

                  System.getProperties().list(System.out);
                  System.exit(1);

               }
            }

            File hermesXML = new File(dotHermes, "hermes-config.xml");

            try
            {
               if (!hermesXML.exists())
               {
                  hermesXML.createNewFile();
                  InputStream istream = getClass().getClassLoader().getResourceAsStream("hermes/bootstrap/default-hermes-config.xml"); // ClassLoader.getSystemResourceAsStream("hermes/bootstrap/default-hermes-config.xml");
                  OutputStream ostream = new FileOutputStream(hermesXML);

                  int i;
                  while ((i = istream.read()) != -1)
                  {
                     ostream.write(i);
                  }

                  ostream.close();

                  log.debug("bootstrapped empty config to " + hermesXML.getPath());
               }

               return hermesXML.toURI().getPath();

            }
            catch (IOException e)
            {
               log.error(e.getMessage(), e);

            }

            return null;
         }
         else
         {
            return rval;
         }
      }
      else
      {
         return currentConfig;
      }
   }

   /**
    * Get the sink for informative messages during tasks.
    */
   public UIMessageSink getDefaultMessageSink()
   {
      return defaultMessageSink;
   }

   /**
    * Get the document pane where queue/topic/context browses.
    * 
    * @return
    */
   public DocumentPane getDocumentPane()
   {
      return browserPane;
   }

   /**
    * Get the current loader used to create Hermes instances from the XML.
    * 
    * @return
    */
   public HermesLoader getLoader()
   {
      return loader;
   }

   /**
    * Gets the maximum number of messages to be displayed in a message browse
    * table, -1 means show them all which could get a bit memory hungry.
    * 
    * @return
    * @throws HermesException
    */
   public int getMaxMessagesInBrowserPane() throws HermesException
   {
      return (loader.getConfig().getMaxMessagesInBrowserPane() == 0) ? DEFAULT_MAX_CACHED_MESSAGES : loader.getConfig().getMaxMessagesInBrowserPane();

   }

   public long getQueueBrowseConsumerTimeout() throws HermesException
   {
      return (loader.getConfig().getQueueBrowseConsumerTimeout() == 0) ? DEFAULT_QUEUE_BROWSE_CONSUMER_TIMEOUT : loader.getConfig()
            .getQueueBrowseConsumerTimeout();
   }

   /**
    * Gets the repository manager for XML message files.
    * 
    * @return
    */
   public HermesRepositoryManager getMessageRepository()
   {
      return repManager;
   }

   /**
    * Get the threadpool to dispatch long running tasks to.
    */
   public ThreadPool getThreadPool()
   {
      return ThreadPool.get();
   }

   /**
    * Get the profile name to save/load layout configuration with.
    * 
    * @return
    */
   public String getUserProfileName()
   {
      return USER_PROFILE_NAME;
   }

   /**
    * Get the watch frame for the give name.
    * 
    * @return Returns the queueWatchPane.
    */
   public WatchDockableFrame getWatchFrame(String id)
   {
      return (WatchDockableFrame) queueWatchFrames.get(id);
   }

   /**
    * Initialisation of non-GUI components - can be run on any thread.
    * 
    * @throws NamingException
    * @throws JMSException
    */
   private void init() throws NamingException, JMSException, IOException
   {
      // Set up the repository manager

      HermesConfig config = (HermesConfig) context.lookup(HermesContext.CONFIG);

      if (config.getMessageFilesDir() == null)
      {
         config.setMessageFilesDir("./messages");
      }

      File repDir = new File(TextUtils.replaceClasspathVariables(config.getMessageFilesDir()));

      try
      {
         loader.save();
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);
      }

      if (!repDir.exists())
      {
         repDir.mkdir();
      }

      repManager = new FileRepositoryManager(repDir, 5000);
      repManager.addRepositoryListener((BrowserTreeModel) getBrowserTree().getModel());

      log.debug("setting maxThreadPoolSize");

      if (config.getMaxThreadPoolSize() == 0)
      {
         config.setMaxThreadPoolSize(1);
      }

      ThreadPool.get().setThreads(config.getMaxThreadPoolSize());
   }

   private void initJIDE() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      try
      {
         LookAndFeelFactory.installJideExtension();
      }
      catch (IllegalArgumentException e)
      {
         log.error("l&f incompatible with JIDE, trying metal: " + e.getMessage(), e);

         UIManager.setLookAndFeel(new MetalLookAndFeel());
         LookAndFeelFactory.installJideExtension();
      }

      ObjectConverterManager.initDefaultConverter();
      CellEditorManager.initDefaultEditor();
      CellRendererManager.initDefaultRenderer();
      ObjectComparatorManager.initDefaultComparator();
      
      LookAndFeelFactory.UIDefaultsCustomizer uiDefaultsCustomizer = new LookAndFeelFactory.UIDefaultsCustomizer() {
         public void customize(UIDefaults defaults) {
             ThemePainter painter = (ThemePainter) defaults.get("Theme.painter");
             defaults.put("OptionPaneUI", "com.jidesoft.plaf.basic.BasicJideOptionPaneUI");
             defaults.put("OptionPane.showBanner", Boolean.FALSE); // show banner or not. default is true
             
             // set both bannerBackgroundDk and // set both bannerBackgroundLt to null if you don't want gradient
             defaults.put("OptionPane.bannerBackgroundDk", painter != null ? painter.getOptionPaneBannerDk() : null);
             defaults.put("OptionPane.bannerBackgroundLt", painter != null ? painter.getOptionPaneBannerLt() : null);
             defaults.put("OptionPane.bannerBackgroundDirection", Boolean.TRUE); // default is true

             // optionally, you can set a Paint object for BannerPanel. If so, the three UIDefaults related to banner background above will be ignored.
             defaults.put("OptionPane.bannerBackgroundPaint", null);

             defaults.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder(6, 6, 6, 6));
             defaults.put("OptionPane.buttonOrientation", new Integer(SwingConstants.RIGHT));
         }
     };
     uiDefaultsCustomizer.customize(UIManager.getDefaults());

   }

   /**
    * Initialisation of GUI components, must be run on the Swing dispatch
    * thread.
    * 
    * @throws ClassNotFoundException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws UnsupportedLookAndFeelException
    * @throws HermesException
    */
   private void initUI() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, HermesException
   {
      if (System.getProperty("swing.defaultlaf") == null)
      {
         try
         {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            /**
             * http://hermesjms.com/jira/browse/HJMS-16 I've not implemented
             * this fully as the L&F class name is not all we need to persist,
             * JIDE seems to add in lots of extension stuff and I don't see how
             * to persist/restor them yet. if
             * (TextUtils.isEmpty(getConfig().getLookAndFeel())) {
             * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
             * else { UIManager.setLookAndFeel(getConfig().getLookAndFeel()) ; }
             */
         }
         catch (Throwable e)
         {
            log.error("cannot load l&f, trying metal: " + e.getMessage(), e);

            UIManager.setLookAndFeel(new MetalLookAndFeel());
         }
      }
      else
      {
         UIManager.setLookAndFeel(System.getProperty("swing.defaultlaf"));
      }

      if (getConfig().isEnableJython() && jythonFrame == null)
      {
         jythonFrame = new JythonDockableFrame();
         getDockingManager().addFrame(jythonFrame);
      }

      if (!getConfig().isEnableJython() && jythonFrame != null)
      {
         getDockingManager().removeFrame(jythonFrame.getKey());
         jythonFrame = null;
      }

      CellEditorManager.registerEditor(Domain.class, new DomainCellEditor());
      CellEditorManager.registerEditor(SelectorImpl.class, new SelectorImplCellEditor());
      CellEditorManager.registerEditor(File.class, new DirectoryCellEditor(), DirectoryCellEditor.CONTEXT);
      CellEditorManager.registerEditor(String.class, new ClasspathIdCellEdtitor(), ClasspathIdCellEdtitor.CONTEXT);
      CellEditorManager.registerEditor(Hermes.class, new HermesCellEditor()) ;
      
      ObjectComparatorManager.registerComparator(Date.class, new DateComparator());
      ObjectComparatorManager.registerComparator(Integer.class, new IntegerComparator());
      ObjectComparatorManager.registerComparator(Long.class, new LongComparator());

      DefaultSyntaxKit.initKit();
      
      browserPane = new MainDocumentPane();
      // Shows us the memory usage and lets the user GC

      MemoryStatusBarItem memoryStatusBar = new MemoryStatusBarItem();
      memoryStatusBar.setPreferredWidth(100);

      // General informative messages go here..

      progressStatus = new ProgressStatusBarItem();

      statusBar = new StatusBar();
      statusBar.add(progressStatus, JideBoxLayout.VARY);
      statusBar.add(new TimeStatusBarItem(), JideBoxLayout.FLEXIBLE);
      statusBar.add(memoryStatusBar, JideBoxLayout.FLEXIBLE);

      // The tools panel includes the running tasks, Jython console and log
      // console.

      actionsPane = new ActionsPanel();

      toolsPane = new DockableToolPanel();
      toolsPane.addToolPanel("Tasks", new JideScrollPane(actionsPane));
      toolsPane.addToolPanel("Log", new Log4JOutputViewer(""));

      browserTreePane = new BrowserTreeDockableFrame();
      browserTreePane.setLoader(loader);

      // Get the menu bar initialised...

      setJMenuBar(new MenuBar(this));
      setIconImage(IconCache.getIcon("hermes.icon").getImage());

      //
      // Layout management initialisation

      getDockingManager().setUsePref(false);
      getDockingManager().setProfileKey(USER_PROFILE_NAME);
      getDockingManager().setInitSplitPriority(DefaultDockingManager.SPLIT_SOUTH_NORTH_EAST_WEST);
      getDockingManager().getWorkspace().add(browserPane, BorderLayout.CENTER);
      getDockingManager().addFrame(browserTreePane);
      getDockingManager().addFrame(toolsPane);

      getDockableBarManager().addDockableBar(new MainToolBar());
      getDockableBarManager().addDockableBar(new MessageToolBar());
      getDockableBarManager().addDockableBar(new ConfigurationToolBar());
      getDockableBarManager().addDockableBar(new JNDIToolBar());

      getContentPane().add(statusBar, BorderLayout.AFTER_LAST_LINE);

      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      toFront();

      setStatusMessage("Ready");
      SplashScreen.hide();

      //
      // Raise the swing thread to max priority

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
         }
      });
   }

   /**
    * Bit of a hack this one it tells us if there is a some kind of browse task
    * running and the user has selected one or more messages in the tree.
    * 
    * @return
    */
   public boolean isBrowseActionSelected()
   {
      if (browserPane.getActiveDocument() instanceof BrowserAction)
      {
         final BrowserAction action = (BrowserAction) browserPane.getActiveDocument();

         if (action.getSelectedMessages().size() > 0)
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Initialise the underlying Hermes that we're gonna do all our work with
    * 
    * @throws HermesException
    * @throws NamingException
    */
   public void loadConfig() throws NamingException, HermesException
   {
      Properties props = new Properties();
      Context oldContext = context;
      HermesConfig oldConfig = null;

      props.put(Context.INITIAL_CONTEXT_FACTORY, HermesInitialContextFactory.class.getName());
      props.put(Context.PROVIDER_URL, getCurrentConfigURL());
      props.put("hermes.loader", JAXBHermesLoader.class.getName());

      log.debug("props=" + props);

      Iterator listeners = null;

      if (loader != null)
      {
         listeners = loader.getConfigurationListeners();
         oldConfig = loader.getConfig();
      }

      if (oldConfig != null)
      {
         Set naming = new HashSet();
         naming.addAll(oldConfig.getNaming());

         for (Iterator iter = naming.iterator(); iter.hasNext();)
         {
            NamingConfig oldNaming = (NamingConfig) iter.next();

            loader.notifyNamingRemoved(oldNaming);
         }
      }

      context = new InitialContext(props);
      loader = (HermesLoader) context.lookup(HermesContext.LOADER);

      if (listeners != null)
      {
         while (listeners.hasNext())
         {
            loader.addConfigurationListener((HermesConfigurationListener) listeners.next());
         }
      }

      if (oldContext != null)
      {
         for (NamingEnumeration iter = oldContext.listBindings(""); iter.hasMoreElements();)
         {
            Binding binding = (Binding) iter.next();

            try
            {
               if (oldContext.lookup(binding.getName()) instanceof Hermes)
               {
                  Hermes hermes = (Hermes) oldContext.lookup(binding.getName());
                  Hermes newHermes = null;

                  try
                  {
                     newHermes = (Hermes) context.lookup(hermes.getId());
                  }
                  catch (NamingException e)
                  {
                     // NOP
                  }

                  if (newHermes == null)
                  {
                     loader.notifyHermesRemoved(hermes);
                  }
               }
            }
            catch (NamingException ex)
            {
               // NOP
            }
         }
      }

      if (!firstLoad)
      {
         closeWatches();
         final ArrayList tmpList = new ArrayList();
         tmpList.addAll(loader.getConfig().getWatch());
         loader.getConfig().getWatch().clear();

         for (Iterator iter = tmpList.iterator(); iter.hasNext();)
         {
            WatchConfig wConfig = (WatchConfig) iter.next();
            createWatch(wConfig);
         }
      }
      
      setTitle("HermesJMS - " + TextUtils.crumble(getCurrentConfigURL(), 100)) ;
   }

   /**
    * Replace all the destinations that are on a Hermes configuration, used
    * after a discover has occured. Not the best codepath.
    * 
    * @param hermes
    * @param dConfigs
    * @throws JMSException
    */
   public void replaceDestinationConfigs(Hermes hermes, Collection dConfigs) throws JMSException
   {
      loader.replaceDestinationConfigs(hermes, dConfigs);
   }

   /**
    * Restore the configuration from a backup.
    * 
    * @throws HermesException
    */
   public void restoreConfig() throws HermesException
   {
      loader.restore();
   }

   /**
    * Save the current configuration.
    * 
    * @throws HermesException
    */
   public void saveConfig() throws HermesException
   {
      if (!isRestrictedWithWarning())
      {
         loader.save();
      }
   }

   public void showInformationDialog(final String message)
   {
      Runnable r = new Runnable()
      {
         public void run()
         {
            JOptionPane.showMessageDialog(HermesBrowser.this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
         }
      };

      if (SwingUtilities.isEventDispatchThread())
      {
         r.run();
      }
      else
      {
         SwingUtilities.invokeLater(r);
      }
   }

   /**
    * Show an error dialog with the given message, will dispatch to the evet
    * thread if needed.
    * 
    * @param message
    */
   public void showErrorDialog(final String message)
   {
      Runnable r = new Runnable()
      {
         public void run()
         {
            JOptionPane.showMessageDialog(HermesBrowser.this, message, "Error", JOptionPane.ERROR_MESSAGE);
         }
      };

      if (SwingUtilities.isEventDispatchThread())
      {
         r.run();
      }
      else
      {
         SwingUtilities.invokeLater(r);
      }
   }

   /**
    * Show an error message with the message from the exception and also log it
    * to Log4j.
    * 
    * @param message
    * @param t
    */
   public void showErrorDialog2(final String message, final Throwable t)
   {
      log.error(t.getMessage(), t);

      Runnable r = new Runnable()
      {
         public void run()
         {
            if (t instanceof PyException)
            {
               PyException pyT = (PyException) t;

               JOptionPane.showMessageDialog(HermesBrowser.this, message + ": " + pyT.traceback.dumpStack(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            else
            {

               JOptionPane.showMessageDialog(HermesBrowser.this, message + ": " + t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
         }
      };

      if (SwingUtilities.isEventDispatchThread())
      {
         r.run();
      }
      else
      {
         SwingUtilities.invokeLater(r);
      }
   }

   public void showErrorDialog(final String message, final Throwable t)
   {
      Runnable r = new Runnable()
      {
         public void run()
         {
            String detail = null;

            if (t instanceof PyException)
            {
               StringBuffer s = new StringBuffer();
               PyException pyT = (PyException) t;
               pyT.traceback.dumpStack(s);

               detail = s.toString();
            }
            else
            {
               StringWriter s = new StringWriter();
               PrintWriter p = new PrintWriter(s);
               t.printStackTrace(p);
               detail = s.toString();                
            }

            JideOptionPane optionPane = new JideOptionPane(message, JOptionPane.ERROR_MESSAGE,
                  JideOptionPane.CLOSE_OPTION, UIManager.getIcon("OptionPane.errorIcon"));
            optionPane.setTitle(message);
            
            
            if (detail != null)
            {
               optionPane.setDetails(detail);
            }
            JDialog dialog = optionPane.createDialog(HermesBrowser.this, "Error");
            dialog.setResizable(true);
            dialog.pack();
            dialog.setVisible(true);
         }
      };

      if (SwingUtilities.isEventDispatchThread())
      {
         r.run();
      }
      else
      {
         SwingUtilities.invokeLater(r);
      }
   }

   /**
    * Show an error message with the message from the exception and also log it
    * to Log4j.
    * 
    * @param message
    * @param t
    */
   public void showErrorDialog(Throwable t)
   {
      showErrorDialog("Error: ", t);
   }

   /**
    * Is this Hermes running in the restricted mode? If so then a dialog is show
    * telling the user they are not permissioned to do the action.
    * 
    * @return
    */
   public boolean isRestrictedWithWarning()
   {
      if (isRestricted())
      {
         showErrorDialog("You do not have permissions to perform this action.");
      }

      return isRestricted();
   }

   /**
    * Are message stores disabled?
    * 
    * @return
    */
   public boolean isMessageStoresDisabled()
   {
      return System.getProperty(SystemProperties.DISABLE_MESSAGE_STORES) != null;
   }

   /**
    * Is this Hermes running in the restricted mode?
    * 
    * @return
    */
   public boolean isRestricted()
   {
      return restricted;
   }

   public CommandBar createDockableBar(String name)
   {
      return new CommandBar(name);
   }

   public Hermes getHermes()
   {
      if (getDocumentPane().getActiveDocument() instanceof BrowserAction)
      {
         BrowserAction action = (BrowserAction) getDocumentPane().getActiveDocument();
         return action.getHermes();
      }
      else
      {
         return null;
      }
   }

   public static void browseQueue(String hermesId, String queue) throws NamingException, JMSException
   {
      Hermes hermes = (Hermes) HermesBrowser.getBrowser().getContext().lookup(hermesId);
      DestinationConfig config = hermes.getDestinationConfig(queue, Domain.QUEUE);

      HermesBrowser.getBrowser().getActionFactory().createQueueBrowseAction(hermes, config);
   }

   public void setFIXPrettyPrinter(FIXPrettyPrinter printer)
   {
      FIXUtils.setPrettyPrinter(printer) ;
   }
   
   public FIXPrettyPrinter getFIXPrettyPrinter()
   {
      return FIXUtils.getPrettyPrinter() == null ? FIXUtils.getDefaultPrettyPrinter() : FIXUtils.getPrettyPrinter() ;
   }
   
   public Collection<Message> getSelectedMessages()
   {
      if (getDocumentPane().getActiveDocument() instanceof BrowserAction)
      {
         BrowserAction action = (BrowserAction) getDocumentPane().getActiveDocument();
         return action.getSelectedMessages();

      }
      else
      {
         return Collections.EMPTY_LIST;
      }
   }
}