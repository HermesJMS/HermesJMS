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

import hermes.browser.IconCache;
import hermes.fix.ChainByClOrdID;
import hermes.fix.FIXMessageListener;
import hermes.fix.FIXMessageTable;
import hermes.util.TextUtils;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public class BrowseFIXChainTask extends TaskSupport
{
   private static final Logger log = Logger.getLogger(BrowseFIXChainTask.class);
  
   private FIXMessageTable source;
   private FIXMessageListener target;
   private int startRow ;
  
   private String clOrdID ;
   
   public BrowseFIXChainTask(FIXMessageTable source, FIXMessageListener target, String clOrdID, int startRow)
   {
      super(IconCache.getIcon("hermes.file.fix"));
      
      this.source = source ;
      this.target = target;
      this.clOrdID = clOrdID ;
      this.startRow = startRow ;
   }

   public String getTitle()
   {
      return clOrdID ;
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.browser.tasks.Task#run()
    */
   public void invoke() throws Exception
   {
      int nmessages = 0;
      ChainByClOrdID chain = new ChainByClOrdID(source) ;
      
      try
      {
         nmessages = chain.filterByClOrdID(clOrdID, target, startRow) ;
      }
      catch (Exception ex)
      {
         log.error("chain stopped: " + ex.getMessage());
      }
      finally
      {
        
         log.debug("nmessages=" + nmessages);
      }
      
      notifyStatus("Found " + nmessages + " message" + TextUtils.plural(nmessages));
   }
}
