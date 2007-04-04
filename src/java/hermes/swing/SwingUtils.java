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

package hermes.swing;

import hermes.browser.IconCache;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import com.jidesoft.swing.JideButton;

/**
 * A collection of utility methods for Swing.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SwingUtils.java,v 1.4 2007/02/18 16:13:41 colincrist Exp $
 */

public class SwingUtils
{
   public static void scrollRectToVisible(Component component, Rectangle aRect)
   {
      Container parent;
      int dx = component.getX(), dy = component.getY();

      for (parent = component.getParent(); parent != null
            && (!(parent instanceof JViewport) || (((JViewport) parent).getClientProperty("HierarchicalTable.mainViewport") == null)); parent = parent
            .getParent())
      {
         Rectangle bounds = parent.getBounds();

         dx += bounds.x;
         dy += bounds.y;
      }

      if (parent != null)
      {
         aRect.x += dx;
         aRect.y += dy;

         ((JComponent) parent).scrollRectToVisible(aRect);
         aRect.x -= dx;
         aRect.y -= dy;
      }
   }

   public static JScrollPane createJScrollPane(JComponent component)
   {    
      final JScrollPane scrollPane = new JScrollPane();

      scrollPane.setViewportView(component);

      return scrollPane;
   }

   /**
    * Create a button with given tooltip.
    */
   public static JideButton createToolBarButton(String icon, String toolTip)
   {
      JideButton button = new JideButton(IconCache.getIcon(icon));

      button.setText("");
      button.setToolTipText(toolTip);
      button.setMargin(new Insets(0, 0, 0, 0));

      return button;
   }

   public static JideButton createToolBarButton(Action action)
   {
      JideButton button = new JideButton(action);

      button.setText("");
      button.setMargin(new Insets(0, 0, 0, 0));

      return button;
   }
}
