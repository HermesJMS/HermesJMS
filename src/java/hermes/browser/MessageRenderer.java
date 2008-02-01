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

package hermes.browser;

import javax.jms.Message;
import javax.swing.JComponent;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MessageRenderer.java,v 1.7 2006/04/28 09:59:37 colincrist Exp $
 */

public interface MessageRenderer
{
    public interface Config
    {
        /**
         * Get some descriptive short name for this renderer
         * 
         * @return
         */
        public String getName();
        
        public void setName(String name) ;

        public String getPropertyDescription(String propertyName);
        
        public boolean isActive() ;
      
        public void setActive(boolean active) ;
    }

    /**
     * Called by Hermes to get a JMS message rendered as a JComponent
     * 
     * @param message
     * @return
     */
    public JComponent render(Message message);

    /**
     * Factory method to construct a configuration object.
     * 
     * @param properties
     */
    public Config createConfig();

    /**
     * Called when the configuration is updated by the GUI
     */
    public void setConfig(Config config);
    
    /**
     * Called by the GUI to get the current configuration.
     */
    public Config getConfig() ;

    /**
     * Called to create a JComponent to allow the stored properties to be
     * configured, you can access and update the Properties from the
     * ConfigDialogProxy, they will be stored if the user hits OK on the
     * Renderers dialog.
     * 
     * @param dialogProxy
     * @return
     */
    public JComponent getConfigPanel(ConfigDialogProxy dialogProxy) throws Exception;

    /** 
     * Called to quickly check if this renderer can render a message.
     */
    public boolean canRender(Message message) ;
    
    /**
     * Get the name to show in the tabbbed pane
     */
    public String getDisplayName() ;
    
    
    /** 
     * Set the active state of this renderer. Inactive renderers are not called by the GUI
     * but they still exist for configuration purposes
     * 
     * @param active
     */
    public void setActive(boolean active) ;
    
    /**
     * Is this renderer active? 
     */
    public boolean isActive() ;
}