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

package hermes.browser.tasks;

import hermes.browser.DocumentComponetListenerSupport;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.FIXSessionBrowserDocumentComponent;
import hermes.fix.FIXMessage;
import hermes.fix.FIXMessageFilter;
import hermes.fix.FIXReader;
import hermes.fix.SessionKey;
import hermes.fix.quickfix.FIXInputStreamReader;
import hermes.fix.quickfix.NIOFIXFileReader;
import hermes.fix.quickfix.QuickFIXMessageCache;
import hermes.util.TextUtils;

import java.awt.EventQueue;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import com.jidesoft.document.DocumentComponentEvent;

import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

/**
 * @author colincrist@hermesjms.com
 */
public class BrowseFIXFileTask extends TaskSupport {
	private static final Logger log = Logger.getLogger(BrowseFIXFileTask.class);
	private InputStream istream;
	private String title;
	private FIXReader reader;
	private Map<SessionKey, FIXSessionBrowserDocumentComponent> documents = new HashMap<SessionKey, FIXSessionBrowserDocumentComponent>();
	private QuickFIXMessageCache messageCache;

	public BrowseFIXFileTask(final QuickFIXMessageCache messageCache, InputStream istream, String title) {
		super(IconCache.getIcon("hermes.file.fix"));
		this.messageCache = messageCache;
		this.istream = istream;
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hermes.browser.tasks.Task#run()
	 */
	public void invoke() throws Exception {
		int nmessages = 0;

		if (istream instanceof FileInputStream) {
			reader = new NIOFIXFileReader(messageCache, (FileInputStream) istream);
		} else {
			reader = new FIXInputStreamReader(messageCache, istream);
		}

		if (HermesBrowser.getBrowser().getConfig().getQuickFIX().isFilterSessionMsgTypes()) {
			reader.getFilter().add(FIXMessageFilter.SESSION_MSGTYPES);
		}

		try {
			FIXMessage message;

			while (isRunning() && (message = reader.read()) != null) {
				nmessages++;
				SessionKey key1 = new SessionKey(message.getMessage().getHeader().getString(SenderCompID.FIELD), message.getMessage().getHeader().getString(TargetCompID.FIELD));
				SessionKey key2 = new SessionKey(message.getMessage().getHeader().getString(TargetCompID.FIELD), message.getMessage().getHeader().getString(SenderCompID.FIELD));

				FIXSessionBrowserDocumentComponent document = documents.get(key1);
				
				if (document == null) {
					document = new FIXSessionBrowserDocumentComponent(messageCache, key1);
					documents.put(key1, document);
					documents.put(key2, document);					
				}			
				
				document.addDocumentComponentListener(new DocumentComponetListenerSupport(){

					@Override
					public void documentComponentClosed(DocumentComponentEvent arg0) {
						stop() ;
					}
					
				}) ;
				
				document.addMessage(message);
			}
		} catch (Throwable ex) {
			log.error("browse stopped: " + ex.getMessage());
		} finally {
			reader.close();

			log.debug("nmessages=" + nmessages);
		}

		notifyStatus("Read " + nmessages + " message" + TextUtils.plural(nmessages) + " from " + title);
	}
}
