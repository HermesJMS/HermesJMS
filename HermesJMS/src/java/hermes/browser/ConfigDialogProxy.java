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

/**
 * Interface to allow user defined renderers to store properties and interact
 * with with the dialog.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ConfigDialogProxy.java,v 1.4 2004/09/16 20:30:49 colincrist Exp $
 */

public interface ConfigDialogProxy
{
    /**
     * Tell the dialog that something has changed in the properties, this will
     * ungrey the OK and Apply buttons if needed.
     */
    public void setDirty();

    /**
     * Get the properties stored for your renderer. When the user hits OK
     * they'll be stored for you.
     * 
     * @return
     */
    public MessageRenderer.Config getConfig();

}