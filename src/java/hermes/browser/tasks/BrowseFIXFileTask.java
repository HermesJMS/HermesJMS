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

import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.FIXSessionBrowserDocumentComponent;
import hermes.fix.FIXMessage;
import hermes.fix.FIXMessageFilter;
import hermes.fix.FIXReader;
import hermes.fix.quickfix.FIXInputStreamReader;
import hermes.fix.quickfix.NIOFIXFileReader;
import hermes.util.TextUtils;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public class BrowseFIXFileTask extends TaskSupport
{
   private static final Logger log = Logger.getLogger(BrowseFIXFileTask.class);
   private InputStream istream;
   private String title;
   private FIXSessionBrowserDocumentComponent frame;
   private FIXReader reader;

   public BrowseFIXFileTask(final FIXSessionBrowserDocumentComponent frame, InputStream istream, String title)
   {
      super(IconCache.getIcon("hermes.file.fix"));

      this.istream = istream;
      this.title = title;
      this.frame = frame;
      
     

     
   }

   public String getTitle()
   {
      return title;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.Task#run()
    */
   public void invoke() throws Exception
   {
      int nmessages = 0;

      if (istream instanceof FileInputStream)
      {
         reader = new NIOFIXFileReader(frame.getMessageCache(), (FileInputStream) istream);
      }
      else
      {
         reader = new FIXInputStreamReader(frame.getMessageCache(), istream);
      }
      
      if (HermesBrowser.getBrowser().getConfig().getQuickFIX().isFilterSessionMsgTypes())
      {
         reader.getFilter().add(FIXMessageFilter.SESSION_MSGTYPES) ;
      }
      
      try
      {
         FIXMessage message;

         while (isRunning() && (message = reader.read()) != null)
         {
            nmessages++;
            frame.addMessage(message);
         }
      }
      catch (Throwable ex)
      {
         log.error("browse stopped: " + ex.getMessage());
      }
      finally
      {
         reader.close();

         log.debug("nmessages=" + nmessages);
      }

      notifyStatus("Read " + nmessages + " message" + TextUtils.plural(nmessages) + " from " + title);
   }
}
