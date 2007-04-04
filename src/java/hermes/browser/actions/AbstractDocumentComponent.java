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

package hermes.browser.actions;

import hermes.browser.components.NavigableComponent;

import javax.swing.JComponent;

import com.jidesoft.document.DocumentComponent;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: AbstractDocumentComponent.java,v 1.2 2006/05/26 10:08:19 colincrist Exp $
 */

public abstract class AbstractDocumentComponent extends DocumentComponent implements NavigableComponent
{
   public AbstractDocumentComponent(JComponent arg0, String arg1)
   {
      super(arg0, arg1);
      // TODO Auto-generated constructor stub
   }

  

}
