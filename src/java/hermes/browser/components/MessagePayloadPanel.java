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
 */

package hermes.browser.components;

import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.MessageRenderer;

import java.awt.BorderLayout;

import javax.jms.Message;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;

import com.jidesoft.swing.JideScrollPane;

/**
 * A panel for displaying JMS messages
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: MessagePayloadPanel.java,v 1.3 2004/07/30 17:25:13 colincrist
 *          Exp $
 */
public class MessagePayloadPanel extends JPanel
{
   private static final Logger log = Logger.getLogger(MessagePayloadPanel.class);
   private final JTabbedPane tabbedPane = new JTabbedPane();

   private String destinationName;

   public MessagePayloadPanel(String destinationName)
   {
      super();
      this.destinationName = destinationName;

      init();
   }

   public MessagePayloadPanel(Hermes hermes, String destinationName, Message message)
   {
      super();
      this.destinationName = destinationName;

      init();

      setMessage(hermes, message);
   }

   private void init()
   {
      tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
      setLayout(new BorderLayout());
      add(tabbedPane, BorderLayout.CENTER);
   }

   public void setMessage(Hermes hermes, Message m)
   {
      String selectedTitle = null;
      int selectedIndex = 0;

      if (tabbedPane.getSelectedIndex() > -1)
      {
         selectedTitle = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
      }

      tabbedPane.removeAll();

      for (MessageRenderer renderer : HermesBrowser.getRendererManager().getRenderers())
      {
         if (renderer.canRender(m))
         {
            JComponent component = new JideScrollPane(renderer.render(m)) ;
            
            tabbedPane.add(renderer.getDisplayName(), component);

            if (renderer.getDisplayName().equals(selectedTitle))
            {
               selectedIndex = tabbedPane.getTabCount() - 1;
               tabbedPane.setSelectedIndex(selectedIndex);
            }
         }
      }

      tabbedPane.setSelectedIndex(selectedIndex);
   }
}