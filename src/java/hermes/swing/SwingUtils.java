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
import javax.swing.JTable;
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
   public static final int VIEWPORT = 0, // take the policy of the viewport
         UNCHANGED = 1, // don't scroll if it fills the visible area, otherwise
                        // take the policy of the viewport
         FIRST = 2, // scroll the first part of the region into view
         CENTER = 3, // center the region
         LAST = 4; // scroll the last part of the region into view

   public static final int NONE = 0, TOP = 1, VCENTER = 2, BOTTOM = 4, LEFT = 8, HCENTER = 16, RIGHT = 32;

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
      //button.setMargin(new Insets(0, 0, 0, 0));
      

      return button;
   }

   private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);

   public static Rectangle getRowBounds(JTable table, int first, int last)
   {
      Rectangle result = table.getCellRect(first, -1, true);
      result = result.union(table.getCellRect(last, -1, true));
      Insets i = table.getInsets();

      result.x = i.left;
      result.width = table.getWidth() - i.left - i.right;

      return result;
   }

   public static void scroll(JComponent c, int part)
   {
      scroll(c, part & (LEFT | HCENTER | RIGHT), part & (TOP | VCENTER | BOTTOM));
   }

   public static void scroll(JComponent c, int horizontal, int vertical)
   {
      Rectangle visible = c.getVisibleRect();
      Rectangle bounds = c.getBounds();

      switch (vertical)
      {
         case TOP:
            visible.y = 0;
            break;
         case VCENTER:
            visible.y = (bounds.height - visible.height) / 2;
            break;
         case BOTTOM:
            visible.y = bounds.height - visible.height;
            break;
      }

      switch (horizontal)
      {
         case LEFT:
            visible.x = 0;
            break;
         case HCENTER:
            visible.x = (bounds.width - visible.width) / 2;
            break;
         case RIGHT:
            visible.x = bounds.width - visible.width;
            break;
      }

      c.scrollRectToVisible(visible);
   }

   /*---------------------------------------------------------------
    Scrolling with bias.
    */
   public static void scroll(JComponent c, Rectangle r, int bias)
   {
      scroll(c, r, bias, bias);
   }

   public static void scroll(JComponent c, Rectangle r, int horizontalBias, int verticalBias)
   {
      Rectangle visible = c.getVisibleRect(), dest = new Rectangle(r);

      if (dest.width > visible.width)
      {
         if (horizontalBias == VIEWPORT)
         {
            // leave as is
         }
         else if (horizontalBias == UNCHANGED)
         {
            if (dest.x <= visible.x && dest.x + dest.width >= visible.x + visible.width)
            {
               dest.x = visible.x;
               dest.width = visible.width;
            }
         }
         else
         {
            if (horizontalBias == CENTER)
               dest.x += (dest.width - visible.width) / 2;
            else if (horizontalBias == LAST)
               dest.x += dest.width - visible.width;

            dest.width = visible.width;
         }
      }

      if (dest.height > visible.height)
      {
         if (verticalBias == VIEWPORT)
         {
            // leave as is
         }
         else if (verticalBias == UNCHANGED)
         {
            if (dest.y <= visible.y && dest.y + dest.height >= visible.y + visible.height)
            {
               dest.y = visible.y;
               dest.height = visible.height;
            }
         }
         else
         {
            if (verticalBias == CENTER)
               dest.y += (dest.height - visible.height) / 2;
            else if (verticalBias == LAST)
               dest.y += dest.height - visible.height;

            dest.height = visible.height;
         }
      }

      if (!visible.contains(dest))
         c.scrollRectToVisible(dest);
   }

   /*--------------------------------------------------------
    One-direction scrolling.
    */

   public static void scrollHorizontally(JComponent c, Rectangle r)
   {
      scrollHorizontally(c, r.x, r.x + r.width);
   }

   public static void scrollHorizontally(JComponent c, int from, int to)
   {
      Rectangle visible = c.getVisibleRect();

      if (visible.x <= from && visible.x + visible.width >= to)
         return;

      visible.x = from;
      visible.width = to - from;

      c.scrollRectToVisible(visible);
   }

   public static void scrollHorizontally(JComponent c, Rectangle r, int bias)
   {
      scrollHorizontally(c, r.x, r.x + r.width, bias);
   }

   public static void scrollHorizontally(JComponent c, int from, int to, int bias)
   {
      Rectangle visible = c.getVisibleRect(), dest = new Rectangle(visible);

      dest.x = from;
      dest.width = to - from;

      if (dest.width > visible.width)
      {
         if (bias == VIEWPORT)
         {
            // leave as is
         }
         else if (bias == UNCHANGED)
         {
            if (dest.x <= visible.x && dest.x + dest.width >= visible.x + visible.width)
            {
               dest.x = visible.x;
               dest.width = visible.width;
            }
         }
         else
         {
            if (bias == CENTER)
               dest.x += (dest.width - visible.width) / 2;
            else if (bias == LAST)
               dest.x += dest.width - visible.width;

            dest.width = visible.width;
         }
      }

      if (!visible.contains(dest))
         c.scrollRectToVisible(dest);
   }

   public static void scrollVertically(JComponent c, Rectangle r)
   {
      scrollVertically(c, r.y, r.y + r.height);
   }

   public static void scrollVertically(JComponent c, int from, int to)
   {
      Rectangle visible = c.getVisibleRect();

      if (visible.y <= from && visible.y + visible.height >= to)
         return;

      visible.y = from;
      visible.height = to - from;

      c.scrollRectToVisible(visible);
   }

   public static void scrollVertically(JComponent c, Rectangle r, int bias)
   {
      scrollVertically(c, r.y, r.y + r.height, bias);
   }

   public static void scrollVertically(JComponent c, int from, int to, int bias)
   {
      Rectangle visible = c.getVisibleRect(), dest = new Rectangle(visible);

      dest.y = from;
      dest.height = to - from;

      if (dest.height > visible.height)
      {
         if (bias == VIEWPORT)
         {
            // leave as is
         }
         else if (bias == UNCHANGED)
         {
            if (dest.y <= visible.y && dest.y + dest.height >= visible.y + visible.height)
            {
               dest.y = visible.y;
               dest.height = visible.height;
            }
         }
         else
         {
            if (bias == CENTER)
               dest.y += (dest.height - visible.height) / 2;
            else if (bias == LAST)
               dest.y += dest.height - visible.height;

            dest.height = visible.height;
         }
      }

      if (!visible.contains(dest))
         c.scrollRectToVisible(dest);
   }

   /*----------------------------------------------------------
    Centering.
    */

   public static void center(JComponent c, Rectangle r, boolean withInsets)
   {
      Rectangle visible = c.getVisibleRect();

      visible.x = r.x - (visible.width - r.width) / 2;
      visible.y = r.y - (visible.height - r.height) / 2;

      Rectangle bounds = c.getBounds();
      Insets i = withInsets ? EMPTY_INSETS : c.getInsets();
      bounds.x = i.left;
      bounds.y = i.top;
      bounds.width -= i.left + i.right;
      bounds.height -= i.top + i.bottom;

      if (visible.x < bounds.x)
         visible.x = bounds.x;

      if (visible.x + visible.width > bounds.x + bounds.width)
         visible.x = bounds.x + bounds.width - visible.width;

      if (visible.y < bounds.y)
         visible.y = bounds.y;

      if (visible.y + visible.height > bounds.y + bounds.height)
         visible.y = bounds.y + bounds.height - visible.height;

      c.scrollRectToVisible(visible);
   }

   public static void centerHorizontally(JComponent c, Rectangle r, boolean withInsets)
   {
      centerHorizontally(c, r.x, r.x + r.width, withInsets);
   }

   public static void centerHorizontally(JComponent c, int from, int to, boolean withInsets)
   {
      Rectangle bounds = c.getBounds();
      Insets i = withInsets ? EMPTY_INSETS : c.getInsets();
      bounds.x = i.left;
      bounds.y = i.top;
      bounds.width -= i.left + i.right;
      bounds.height -= i.top + i.bottom;

      Rectangle visible = c.getVisibleRect();

      visible.x = from - (visible.width + from - to) / 2;

      if (visible.x < bounds.x)
         visible.x = bounds.x;

      if (visible.x + visible.width > bounds.x + bounds.width)
         visible.x = bounds.x + bounds.width - visible.width;

      c.scrollRectToVisible(visible);
   }

   public static void centerVertically(JComponent c, Rectangle r, boolean withInsets)
   {
      centerVertically(c, r.y, r.y + r.height, withInsets);
   }

   public static void centerVertically(JComponent c, int from, int to, boolean withInsets)
   {
      Rectangle bounds = c.getBounds();
      Insets i = withInsets ? EMPTY_INSETS : c.getInsets();
      bounds.x = i.left;
      bounds.y = i.top;
      bounds.width -= i.left + i.right;
      bounds.height -= i.top + i.bottom;

      Rectangle visible = c.getVisibleRect();

      visible.y = from - (visible.height + from - to) / 2;

      if (visible.y < bounds.y)
         visible.y = bounds.y;

      if (visible.y + visible.height > bounds.y + bounds.height)
         visible.y = bounds.y + bounds.height - visible.height;

      c.scrollRectToVisible(visible);
   }

   /*-----------------------------------------------------------
    Visibility.
    */

   public static boolean isVisible(JComponent c, Rectangle r)
   {
      return c.getVisibleRect().contains(r);
   }

   public static boolean isHorizontallyVisible(JComponent c, int from, int to)
   {
      Rectangle visible = c.getVisibleRect();

      return visible.x <= from && visible.x + visible.width >= to;
   }

   public static boolean isHorizontallyVisible(JComponent c, Rectangle r)
   {
      Rectangle visible = c.getVisibleRect();

      return visible.x <= r.x && visible.x + visible.width >= r.x + r.width;
   }

   public static boolean isVerticallyVisible(JComponent c, int from, int to)
   {
      Rectangle visible = c.getVisibleRect();

      return visible.y <= from && visible.y + visible.height >= to;
   }

   public static boolean isVerticallyVisible(JComponent c, Rectangle r)
   {
      Rectangle visible = c.getVisibleRect();

      return visible.y <= r.y && visible.y + visible.height >= r.y + r.height;
   }
}
