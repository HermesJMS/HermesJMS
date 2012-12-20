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

package hermes.renderers;

import hermes.swing.MyTextArea;

import java.awt.Font;

import javax.jms.Message;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

/**
 * A renderer that displays toString() on a JMS message in a text area.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ToStringMessageRenderer.java,v 1.3 2007/01/10 15:59:54
 *          colincrist Exp $
 */

public class ToStringMessageRenderer extends AbstractMessageRenderer {
	private static final Logger log = Logger.getLogger(ToStringMessageRenderer.class);

	public ToStringMessageRenderer() {
		super();
	}

	@Override
	public JComponent render(JScrollPane parent, Message m) {
		final JTextArea textArea = new MyTextArea();

		textArea.setEditable(false);
		textArea.setFont(Font.decode("Monospaced-PLAIN-12"));
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setText(m.toString());
		textArea.setCaretPosition(0);

		return textArea;
	}

	/**
	 * Any JMS message is rederable.
	 */
	@Override
	public boolean canRender(Message message) {
		return true;
	}

	@Override
	public String getDisplayName() {
		return "toString";
	}
}
