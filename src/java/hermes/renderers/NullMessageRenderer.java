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

package hermes.renderers;

import javax.jms.Message;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 * An example renderer that does nothing.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: NullMessageRenderer.java,v 1.2 2004/09/16 20:30:48 colincrist
 *          Exp $
 */
public class NullMessageRenderer extends AbstractMessageRenderer {
	/**
    * 
    */
	public NullMessageRenderer() {
		super();
	}

	@Override
	public boolean canRender(Message message) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.browser.MessageRenderer#render(javax.jms.Message)
	 */
	@Override
	public JComponent render(JScrollPane parent, Message message) {
		return null;
	}

	@Override
	public String getDisplayName() {
		return "Null";
	}

}