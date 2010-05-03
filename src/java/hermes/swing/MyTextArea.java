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

import java.awt.Dimension;

import javax.swing.JTextArea;
import javax.swing.text.Document;

/**
 * My own customisation of a JTextArea.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: MyTextArea.java,v 1.3 2007/02/28 10:47:22 colincrist Exp $
 */

public class MyTextArea extends JTextArea
{
   /**
	 * 
	 */
	private static final long serialVersionUID = 8434015467309784115L;

public MyTextArea()
   {
     
   }

   public MyTextArea(String text)
   {
      super(text);
   }

   public MyTextArea(Document doc)
   {
      super(doc);
   }

   public MyTextArea(int rows, int columns)
   {
      super(rows, columns);
   }

   public MyTextArea(String text, int rows, int columns)
   {
      super(text, rows, columns);
   }

   public MyTextArea(Document doc, String text, int rows, int columns)
   {
      super(doc, text, rows, columns);    
   }
   
   @Override
   public Dimension getPreferredScrollableViewportSize()
   {
      return getParent().getSize() ;     
   }
}
