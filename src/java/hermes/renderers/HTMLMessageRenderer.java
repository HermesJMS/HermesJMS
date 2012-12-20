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

import hermes.HermesException;
import hermes.browser.HermesBrowser;
import hermes.util.MessageUtils;
import hermes.util.XmlUtils;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class HTMLMessageRenderer extends AbstractMessageRenderer {
	private static final Logger log = Logger.getLogger(HTMLMessageRenderer.class);

	private Transformer transformer;

	private Transformer getTransformer() throws HermesException {
		if (transformer == null && HermesBrowser.getBrowser().getConfig().getHTMLRendererXSL() != null) {
			TransformerFactory tFactory = TransformerFactory.newInstance();

			try {
				transformer = tFactory.newTransformer(new StreamSource(new File(HermesBrowser.getBrowser().getConfig().getHTMLRendererXSL())));
			} catch (Exception e) {
				log.error(e);
			}
		}
		return transformer;
	}

	public HTMLMessageRenderer() {
		super();
	}

	@Override
	public JComponent render(JScrollPane parent, Message m) {
		JEditorPane pane = new JEditorPane();
		pane.setEditable(false);
		pane.setContentType("text/html");

		try {
			String input = MessageUtils.asString(m);
			StringWriter output = new StringWriter();
			getTransformer().setURIResolver(new URIResolver() {

				@Override
				public Source resolve(String arg0, String arg1) throws TransformerException {
					log.info("resolve(" + arg0 + "," + arg1);
					return null;
				}
			});

			getTransformer().transform(new StreamSource(new StringReader(input)), new StreamResult(output));
			pane.setText(output.toString());
		} catch (Throwable e) {
			pane.setText(e.getMessage());
			log.error("exception converting message to HTML: ", e);
		}

		pane.setCaretPosition(0);

		return pane;
	}

	@Override
	public boolean canRender(Message message) {
		try {
			if (HermesBrowser.getBrowser().getConfig().getHTMLRendererXSL() != null && message instanceof TextMessage) {
				final String text = ((TextMessage) message).getText();
				return XmlUtils.isXML(text);
			}
		} catch (JMSException e) {
			log.error("error getting text: " + e.getMessage(), e);
		}

		return false;
	}

	@Override
	public String getDisplayName() {
		return "HTML";
	}
}
