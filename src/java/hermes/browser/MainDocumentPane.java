/* 
 * Copyright 2007 Colin Crist
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

import com.jidesoft.document.DocumentPane;
import com.jidesoft.swing.JideTabbedPane;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class MainDocumentPane extends DocumentPane implements DocumentPane.TabbedPaneCustomizer
{
   /**
	 * 
	 */
	private static final long serialVersionUID = -3341716163846520437L;

public MainDocumentPane()
   {
      setTabbedPaneCustomizer(this) ;
   }
   
   public void customize(JideTabbedPane tabbedPane)
   {    
      tabbedPane.setShowCloseButton(true);
      tabbedPane.setShowCloseButtonOnTab(true);
      tabbedPane.setShowCloseButtonOnSelectedTab(true); 
      tabbedPane.setUseDefaultShowCloseButtonOnTab(false);     
   }  
}
