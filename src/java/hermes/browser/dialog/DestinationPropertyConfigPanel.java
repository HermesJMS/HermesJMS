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

import hermes.Domain;
import hermes.HermesRuntimeException;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.config.impl.PropertySetConfigImpl;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.Destination;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.xml.bind.JAXBException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.jidesoft.grid.Property;
import com.jidesoft.grid.PropertyPane;
import com.jidesoft.grid.PropertyTable;
import com.jidesoft.grid.PropertyTableModel;
import com.jidesoft.swing.JideTabbedPane;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationPropertyConfigPanel.java,v 1.2 2004/07/21 19:46:15
 *          colincrist Exp $
 */
public class DestinationPropertyConfigPanel extends JPanel
{
   private static final long serialVersionUID = 8657526639099576759L;
   private static final Logger log = Logger.getLogger(DestinationPropertyConfigPanel.class);
  

   private static final String NAME = "Name";
   private static final String SHORT_NAME = "ShortName";
   private static final String SELECTOR = "Selector";
   private static final String ISQUEUE = "Domain";
   private static final String DURABLE = "Durable";
   private static final String DURABLE_CLIENT = "DurableName";

   private static final String NAME_INFO = "The name of the queue/topic or the binding in JNDI";
   private static final String SHORT_NAME_INFO = "An alterative name you may wish to use for display purposes, for example if the real name is too long";
   private static final String ISQUEUE_INFO = "The queue or the topic domain";
   private static final String DURABLE_INFO = "Make a durable subscription if a topic";
   private static final String DURABLE_CLIENT_INFO = "The subscription name to use if this is a durable subscription to a topic.";
   private static final String SELECTOR_INFO = "The selector to use when browsing from the queue or subscribing to the topic.";

   private DestinationConfig config;
   private PropertyTable propertyTable;
   private PropertyTableModel propertyTableModel;
   private PropertyPane propertyPane;

   private Property nameProperty;
   private Property shortNameProperty;
   private Property selectorProperty;
   private Property domainProperty;
   private Property durableProperty;
   private Property durableClientIDProperty;

   private List onOK = new ArrayList();
   private Destination bean;
   private JideTabbedPane tabbedPane = new JideTabbedPane();
   private JPanel generalPanel = new JPanel();
   private BeanPropertyPanel beanPropertyPanel;

   public DestinationPropertyConfigPanel(String hermesId, Destination bean, DestinationConfig config)
   {
      try
      {
         this.bean = bean;
         this.config = config;

         final Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
         setLayout(new BorderLayout());

         add(tabbedPane);

         generalPanel.setLayout(new BorderLayout());

 
         if (bean != null)
         {
            beanPropertyPanel = new BeanPropertyPanel(bean, true, false);
            beanPropertyPanel.init();
            beanPropertyPanel.setBorder(BorderFactory.createTitledBorder(border, "Provider Properties"));
         }

         generalPanel.setBorder(BorderFactory.createTitledBorder(border, "Hermes Properties"));

         tabbedPane.add("Hermes", generalPanel);

         if (bean != null)
         {
            tabbedPane.add("Provider", beanPropertyPanel);
         }

         tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);

         init();
      }
      catch (IllegalAccessException e)
      {
         throw new HermesRuntimeException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new HermesRuntimeException(e);
      }
      catch (NoSuchMethodException e)
      {
         throw new HermesRuntimeException(e);
      }
   }

   public void init()
   {
      nameProperty = new JidePropertyImpl(NAME, NAME_INFO, String.class, config.getName());
      shortNameProperty = new JidePropertyImpl(SHORT_NAME, SHORT_NAME_INFO, String.class, config.getShortName());
      selectorProperty = new JidePropertyImpl(SELECTOR, SELECTOR_INFO, String.class, config.getSelector());
      domainProperty = new JidePropertyImpl(ISQUEUE, ISQUEUE_INFO, Domain.class, Domain.getDomain(config.getDomain()));
      durableProperty = new JidePropertyImpl(DURABLE, DURABLE_INFO, Boolean.class, Boolean.valueOf(config.isDurable()));
      durableClientIDProperty = new JidePropertyImpl(DURABLE_CLIENT, DURABLE_CLIENT_INFO, String.class, config.getClientID());

      Runnable doOnOK = new Runnable()
      {
         public void run()
         {
            log.debug("config=" + config);

            if (nameProperty.getValue() != null && !nameProperty.getValue().equals(""))
            {
               config.setName(nameProperty.getValue().toString());
            }

            if (shortNameProperty.getValue() != null && !shortNameProperty.getValue().equals(""))
            {
               config.setShortName(shortNameProperty.getValue().toString());
            }
            else
            {
               config.setShortName(null);
            }

            if (selectorProperty.getValue() != null && !selectorProperty.getValue().equals(""))
            {
               config.setSelector(selectorProperty.getValue().toString());
            }
            else
            {
               config.setSelector(null);
            }

            if (durableProperty.getValue() != null)
            {
               config.setDurable(((Boolean) durableProperty.getValue()).booleanValue());
            }

            if (domainProperty.getValue() != null)
            {
               Domain domain = (Domain) domainProperty.getValue();

               config.setDomain(domain.getId());
            }

            if (durableClientIDProperty.getValue() != null && !durableClientIDProperty.getValue().equals(""))
            {
               config.setClientID(durableClientIDProperty.getValue().toString());
            }
            else
            {
               config.setClientID(null);
            }
         }
      };

      onOK.add(doOnOK);

      //
      // Build the model and create the table...

      List model = new ArrayList();

      model.add(nameProperty);
      model.add(shortNameProperty);
      model.add(selectorProperty);
      model.add(domainProperty);
      model.add(durableProperty);
      model.add(durableClientIDProperty);

      propertyTableModel = new PropertyTableModel(model);
      propertyTable = new PropertyTable(propertyTableModel);
      propertyTable.expandAll();

      propertyPane = new PropertyPane(propertyTable);
      generalPanel.add(propertyPane, BorderLayout.CENTER);

   }

   public void doOK()
   {
      if (beanPropertyPanel != null)
      {
         log.debug("config=" + config);

         beanPropertyPanel.doOK();

         if (beanPropertyPanel.getChanges().size() > 0)
         {
            if (config.getProperties() == null)
            {
               // @@TODO Fix.

               config.setProperties(new PropertySetConfigImpl());
            }

            try
            {
               HermesBrowser.getConfigDAO().updatePropertySet(config.getProperties(), beanPropertyPanel.getChanges());
            }
            catch (JAXBException e)
            {
               log.error(e.getMessage(), e);
            }
         }
      }

      for (Iterator iter = onOK.iterator(); iter.hasNext();)
      {
         Runnable r = (Runnable) iter.next();
         r.run();
      }

   }

   public void doCancel()
   {
      // NOP
   }

   public Class getPropertyType(String propertyName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
   {
      if (bean == null)
      {
         return String.class;
      }

      return PropertyUtils.getPropertyDescriptor(bean, propertyName).getPropertyType();
   }

   public void addOKAction(Runnable r)
   {
      onOK.add(r);
   }
}