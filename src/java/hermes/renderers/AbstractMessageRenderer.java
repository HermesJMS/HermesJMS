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

package hermes.renderers;

import hermes.browser.ConfigDialogProxy;
import hermes.browser.MessageRenderer;

import javax.jms.Message;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public abstract class AbstractMessageRenderer implements MessageRenderer {
	private Config config = new BasicConfig();

	public class BasicConfig implements Config {
		private boolean active = true;
		private String name = AbstractMessageRenderer.this.getClass().getName();

		@Override
		public boolean isActive() {
			return active;
		}

		@Override
		public void setActive(boolean active) {
			this.active = active;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getPropertyDescription(String propertyName) {
			return propertyName;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}
	}

	/**
	 * There are no configurable options on this renderer.
	 */
	@Override
	public Config createConfig() {
		return new BasicConfig();
	}

	@Override
	public Config getConfig() {
		return config;
	}

	/**
	 * There are no configurable options on this renderer
	 */
	@Override
	public void setConfig(Config config) {
		if (config == null) {
			this.config = createConfig();
		} else {
			this.config = config;
		}
	}

	@Override
	public boolean isActive() {
		return getConfig().isActive();
	}

	@Override
	public void setActive(boolean active) {
		getConfig().setActive(active);
	}

	@Override
	public JComponent getConfigPanel(ConfigDialogProxy dialogProxy) throws Exception {
		return RendererHelper.createDefaultConfigPanel(dialogProxy);
	}

	/**
	 * Backward compatability so older renderers will work with the new
	 * signature that includes the parent scroll pane.
	 */
	@Override
	public JComponent render(JScrollPane parent, Message message) {
		return render(message);
	}

	public JComponent render(Message message) {
		return null;
	}

}
