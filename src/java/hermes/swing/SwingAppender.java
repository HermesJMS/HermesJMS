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

package hermes.swing;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4j Appender to Swing. Not nice but it works.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: SwingAppender.java,v 1.4 2007/02/23 12:33:33 colincrist Exp $
 */

public class SwingAppender extends AppenderSkeleton
{
   private static Timer timer = new Timer();
   private FastDateFormat format = FastDateFormat.getInstance("yyyy.MM.dd HH:mm:ss");
   private List cachedRows = new ArrayList();
   private long updateInterval = 500;
   private TimerTask timerTask;
   private boolean active = false;
   private String filter;
   private JTextArea textArea = new JTextArea();

   public SwingAppender(String filter)
   {
      this.filter = filter;

      textArea.setFont(new Font("Courier", Font.PLAIN, 12));
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
    */
   protected void append(LoggingEvent event)
   {
      if (!active || !checkEntryConditions())
      {
         return;
      }

      if (event.categoryName.startsWith(filter))
      {
         synchronized (cachedRows)
         {
            cachedRows.add(format.format(new Date(event.timeStamp)) + " " + event.getLevel() + " [" + event.getThreadName() + "] " + event.categoryName + " " + event.getMessage());
         }

         if (timerTask == null)
         {
            timerTask = new TimerTask()
            {
               public void run()
               {
                  updateModel();
               }
            };

            timer.schedule(timerTask, updateInterval, updateInterval);
         }
      }
   }

   public void updateModel()
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            synchronized (cachedRows)
            {
               while (cachedRows.size() > 0)
               {
                  textArea.append(cachedRows.remove(0) + "\n");
               }
            }
         }
      });
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.log4j.Appender#close()
    */
   public void close()
   {
      clear();
   }

   public void clear()
   {
      textArea.replaceRange("", 0, textArea.getCaretPosition()) ;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.apache.log4j.Appender#requiresLayout()
    */
   public boolean requiresLayout()
   {
      return false;
   }

   protected boolean checkEntryConditions()
   {
      if (closed || !active)
      {
         return false;
      }

      return true;
   }

   /**
    * @return Returns the updateInterval.
    */
   public long getUpdateInterval()
   {
      return updateInterval;
   }

   /**
    * @param updateInterval
    *           The updateInterval to set.
    */
   public void setUpdateInterval(long updateInterval)
   {
      this.updateInterval = updateInterval;
   }

   /**
    * @return Returns the table.
    */
   public JComponent getComponent()
   {
      return textArea;
   }

   /**
    * @return Returns the active.
    */
   public boolean isActive()
   {
      return active;
   }

   /**
    * @param active
    *           The active to set.
    */
   public void setActive(boolean active)
   {
      this.active = active;

      if (!isActive())
      {
         if (timerTask != null)
         {
            timerTask.cancel();
            timerTask = null;
         }
      }
   }

}