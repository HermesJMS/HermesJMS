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

package hermes.browser.components;

import javax.swing.ListSelectionModel;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: NavigableComponent.java,v 1.1 2006/05/26 10:08:20 colincrist Exp $
 */

public interface NavigableComponent
{
   public boolean isNavigableForward() ;
   
   public boolean isNavigableBackward() ;
   
   public void navigateForward() ;
   
   public void navigateBackward() ;
   
   public ListSelectionModel getListSelectionModel() ;

}
