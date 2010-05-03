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

package hermes.swing.actions;

import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.dialog.MapPropertyDialog;
import hermes.browser.model.tree.MessageStoreURLTreeNode;
import hermes.config.JDBCStore;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: CreateNewJDBCAction.java,v 1.1 2005/08/21 20:47:57 colincrist Exp $
 */

public class CreateNewJDBCAction extends ActionSupport
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 6886184039646547926L;

public CreateNewJDBCAction()
   {
      super();

      putValue(Action.NAME, "New JDBC...");
      putValue(Action.SHORT_DESCRIPTION, "Connect to new database containing message stores.");
      putValue(Action.SMALL_ICON, IconCache.getIcon("jdbc.new"));
   }

   protected void doOnOK(Map<String, String> properties)
   {
      try
      {
         final JDBCStore storeConfig = HermesBrowser.getConfigDAO().addJDBCStore(HermesBrowser.getBrowser().getConfig(), properties.get("alias"), properties.get("driver"),
               properties.get("url"));
         final MessageStoreURLTreeNode node = new MessageStoreURLTreeNode(storeConfig);

         HermesBrowser.getBrowser().getBrowserTree().getBrowserModel().addMessageStoreURLTreeNode(node);
         HermesBrowser.getBrowser().saveConfig();
      }
      catch (Exception e)
      {
         HermesBrowser.getBrowser().showErrorDialog(e);
      }
   }

   public void actionPerformed(ActionEvent e)
   {
      final Map<String, String> properties = new HashMap<String, String>();

      if (e.getSource() != null && e.getSource() instanceof JDBCStore)
      {
         JDBCStore config = (JDBCStore) e.getSource();

         properties.put("alias", config.getAlias());
         properties.put("url", config.getUrl());
         properties.put("driver", config.getDriver());
      }
      else
      {
         properties.put("alias", "myJDBC");
         properties.put("url", "jdbc:provider://<hostname>[<:port>]/<dbname>[?<param1>=<value1>]");
         properties.put("driver", "com.mydriver.MyDriver");
      }

      final MapPropertyDialog dialog = new MapPropertyDialog(HermesBrowser.getBrowser(), "JDBC Properties",
            "\nYou need to provide an alias for this database and its URL and driver.\n", properties, true);

      dialog.addOKAction(new Runnable()
      {
         public void run()
         {
            doOnOK(properties);
         }
      });

      dialog.show();

   }
}
