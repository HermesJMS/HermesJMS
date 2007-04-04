/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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

package hermes.browser.dialog.message;

import hermes.Hermes;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public class MessageEditor extends JPanel
{
    private static final long serialVersionUID = -5465641794588176447L;
    private static final Logger log = Logger.getLogger(MessageEditor.class) ;
    
    private Hermes hermes ;
    
    public MessageEditor()
    {
       init() ;
    }

    private void init()
    {
       setLayout(new BorderLayout()) ;
       
    }
}
