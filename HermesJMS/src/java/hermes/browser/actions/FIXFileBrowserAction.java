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

package hermes.browser.actions;

import hermes.browser.IconCache;
import hermes.browser.tasks.BrowseFIXFileTask;
import hermes.browser.tasks.Task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.Icon;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: FIXFileBrowserAction.java,v 1.6 2006/05/13 21:27:48 colincrist
 *          Exp $
 */

public class FIXFileBrowserAction
{
   private static final Logger log = Logger.getLogger(FIXFileBrowserAction.class);

   private InputStream istream;
   private String title;

   public FIXFileBrowserAction(File file, int maxMessages) throws FileNotFoundException
   {
      super();

      this.istream = new FileInputStream(file);
      this.title = file.getName();
   }

   public FIXFileBrowserAction(URL url, int maxMessages) throws IOException
   {
      super();

      this.istream = url.openStream();
      this.title = url.toString();
      
   }

   public void start()
   {
      final FIXSessionBrowserDocumentComponent frame = new FIXSessionBrowserDocumentComponent(title);
      final Task task = new BrowseFIXFileTask(frame, istream, title);
      
      frame.setTask(task);

      task.start();
   }

   
   public Icon getIcon()
   {
      return IconCache.getIcon("hermes.file.xml");
   }
}
