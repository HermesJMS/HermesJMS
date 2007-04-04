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

package test.fix;

import hermes.fix.FIXMessage;
import hermes.fix.quickfix.NIOFIXFileReader;
import hermes.fix.quickfix.QuickFIXMessageCache;

import java.io.FileInputStream;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: TestNIOFIXReader.java,v 1.1 2006/08/01 07:29:36 colincrist Exp $
 */

public class TestNIOFIXReader extends TestCase
{
   private static final Logger log = Logger.getLogger(TestNIOFIXReader.class);

   public void testReaderSpeed() throws Exception
   {
      int messagesRead = 0;
      long startTime = System.currentTimeMillis() ;
      QuickFIXMessageCache cache = new QuickFIXMessageCache() ;
      try
      {
         NIOFIXFileReader reader = new NIOFIXFileReader(cache, new FileInputStream("BigFIX.fix"));

         
         for (;;)
         {
            FIXMessage message = reader.read();
            messagesRead++;
            
            if (messagesRead % 10000 == 0)
            {
               log.info("Read " + messagesRead) ;
            }
         }
      }
      finally
      {
         long endTime = System.currentTimeMillis() ;
         
         log.info("Read " + messagesRead + " in " + (endTime - startTime) + "ms");
      }

   }

}
