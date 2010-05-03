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

package hermes.browser.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelListener;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: FitScrollPane.java,v 1.6 2007/01/13 14:12:40 colincrist Exp $
 */
public class FitScrollPane extends JScrollPane implements ComponentListener
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 2992851393266585658L;

public FitScrollPane()
   {
      initScrollPane();
   }

   public FitScrollPane(Component view)
   {
      super(view);
      initScrollPane();
   }

   public FitScrollPane(Component view, int vsbPolicy, int hsbPolicy)
   {
      super(view, vsbPolicy, hsbPolicy);
      initScrollPane();
   }

   public FitScrollPane(int vsbPolicy, int hsbPolicy)
   {
      super(vsbPolicy, hsbPolicy);
      initScrollPane();
   }

   private void initScrollPane()
   {
      setBorder(BorderFactory.createLineBorder(Color.GRAY));
      setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
      getViewport().getView().addComponentListener(this);
      removeMouseWheelListeners();
   }

   // remove MouseWheelListener as there is no need for it in FitScrollPane.
   private void removeMouseWheelListeners()
   {
      MouseWheelListener[] listeners = getMouseWheelListeners();
      for (int i = 0; i < listeners.length; i++)
      {
         MouseWheelListener listener = listeners[i];
         removeMouseWheelListener(listener);
      }
   }

   public void updateUI()
   {
      super.updateUI();
      removeMouseWheelListeners();
   }

   public void componentResized(ComponentEvent e)
   {
      setSize(getSize().width, getPreferredSize().height);      
   }

   public void componentMoved(ComponentEvent e)
   {
   }

   public void componentShown(ComponentEvent e)
   {
   }

   public void componentHidden(ComponentEvent e)
   {
   }

   public Dimension getPreferredSize()
   {
      if (getViewport() != null && getViewport().getView() != null)
      {
         getViewport().setPreferredSize(getViewport().getView().getPreferredSize());
      }
      
      return super.getPreferredSize();
   }
}
