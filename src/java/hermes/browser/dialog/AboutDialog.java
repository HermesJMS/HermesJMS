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

package hermes.browser.dialog;

import hermes.Hermes;
import hermes.HermesConstants;
import hermes.browser.HermesBrowser;

import java.util.Enumeration;

import javax.swing.JOptionPane;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

/**
 * @author peterlee@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: AboutDialog.java,v 1.8 2006/09/14 17:24:28 colincrist Exp $
 */
public abstract class AboutDialog
{
   public static final String HERMES_SOURCEFORGE = "http://www.hermesjms.com";

   public static void showAboutDialog(HermesBrowser browser)
   {
      final StringBuffer message = new StringBuffer();

      message.append("Hermes ").append(Hermes.VERSION).append(HermesConstants.CR);
      message.append("info@hermesjms.com").append(HermesConstants.CR);
      message.append("Config: ").append(browser.getCurrentConfigURL()).append(HermesConstants.CR) ;
      
      
      for (final Enumeration iter = Logger.getRootLogger().getAllAppenders() ; iter.hasMoreElements() ; )
      {
          final Object o = iter.nextElement() ;
          
          if (o instanceof FileAppender)
          {
              FileAppender appender = (FileAppender) o ;
              message.append("Logging to ").append(System.getProperty("user.dir")).append(System.getProperty("file.separator")).append(appender.getFile()).append(HermesConstants.CR) ; ;
          } 
          else if (o instanceof ConsoleAppender)
          {
              message.append("Logging to console.\n") ;
          }
      }
      
      message.append("JVM: " + System.getProperty("java.version")).append(" from ").append(System.getProperty("java.vendor")).append(HermesConstants.CR).append(HermesConstants.CR);
      message.append("This product includes software developed by the Apache Foundation.").append(HermesConstants.CR);
      message.append("Powered by JIDE, http://www.jidesoft.com.").append(HermesConstants.CR); 

      JOptionPane.showMessageDialog(browser, message.toString(), "About", JOptionPane.INFORMATION_MESSAGE);
   }
}
