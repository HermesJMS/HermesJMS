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

package hermes.browser.jython;

import hermes.Hermes;
import hermes.SystemProperties;
import hermes.browser.HermesBrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.python.core.PyException;
import org.python.util.PythonInterpreter;

import com.artenum.jyconsole.JyConsole;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JythonManager.java,v 1.2 2006/09/16 15:49:24 colincrist Exp $
 */

public class JythonManager
{
   private static final Logger log = Logger.getLogger(JythonManager.class);

   private JyConsole jyConsole = new JyConsole();

   public JythonManager()
   {
      init();
   }

   public PythonInterpreter getInterpreter()
   {
      return jyConsole.getPythonInterpreter();
   }

   public JyConsole getConsole()
   {
      return jyConsole;
   }

   public void exec(String info, InputStream istream)
   {
      try
      {
         getInterpreter().execfile(istream);
      }
      catch (PyException ex)
      {
         HermesBrowser.getBrowser().getDefaultMessageSink().add("Error " + info + ": " + ex.getMessage());
         log.error(ex.getMessage(), ex);
      }
      catch (Exception ex)
      {
         HermesBrowser.getBrowser().getDefaultMessageSink().add("Error " + info + ": " + ex.getMessage());
         log.error(ex.getMessage(), ex);
      }
   }

   public void init()
   {
      log.debug("bootstraping jython...") ;
      
      exec("Bootstrapping Jython", getClass().getResourceAsStream("bootstrap.py"));

      //
      // Lets see if there is a hermes.py in the .hermes directory to bootstrap
      // from...

      File bootstrapFile = new File(System.getProperty("user.home") + SystemProperties.FILE_SEPARATOR + ".hermes" + SystemProperties.FILE_SEPARATOR
            + "hermesrc.py");

      if (bootstrapFile.exists())
      {
         try
         {
            log.debug("reading " + bootstrapFile.getName()) ;
            exec("Reading hermesrc.py", new FileInputStream(bootstrapFile));

            Hermes.ui.getDefaultMessageSink().add("Loaded hermesrc.py");

         }
         catch (FileNotFoundException e)
         {
            log.error(e.getMessage(), e);
         }
      }
      else
      {
         log.debug("Unable to locate a hermesrc.py in " + System.getProperty("user.home"));
      }

      try
      {
         if (System.getProperty("hermes.python.url") != null)
         {            
            String url = System.getProperty("hermes.python.url") ;
            log.debug("reading " + url) ;
            
            exec("Reading " + url, new URL(url).openStream());
         }
         else
         {
            log.debug("no hermes.python.url set") ;
         }
      }
      catch (Exception ex)
      {
         HermesBrowser.getBrowser().showErrorDialog(ex);
      }
   }
}
