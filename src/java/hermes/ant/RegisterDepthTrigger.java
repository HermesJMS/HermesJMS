/* 
 * Copyright 2003, 2004, 2005 Colin Crist
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

package hermes.ant;

import hermes.Hermes;
import hermes.HermesWatchListener;
import hermes.HermesWatchManager;
import hermes.config.DestinationConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Target;

public class RegisterDepthTrigger extends AbstractTask implements HermesWatchListener
{
   private HermesWatchManager watchManager;
   private List selections = new ArrayList();
   private int depth = 0;
   private String target;
   private String exception;
   private String hermesProperty = "alert.hermes.id";
   private String destinationProperty = "alert.hermes.destination";
   private String messageProperty = "alert.message";

   public RegisterDepthTrigger()
   {
      watchManager = new HermesWatchManager();
      watchManager.setUpdateOnNewWatchAdded(true);
   }

   public void addConfigured(HermesSelectionType h)
   {
      selections.add(h);
   }

   public void execute() throws BuildException
   {
      if (!getProject().getTargets().containsKey(target))
      {
         throw new BuildException("target " + target + " does not exist in build file");
      }

      for (Iterator iter = selections.iterator(); iter.hasNext();)
      {
         HermesSelectionType hermesSelection = (HermesSelectionType) iter.next();

         watch(hermesSelection);
      }
   }

   private void watch(final HermesSelectionType hermesSelection) throws BuildException
   {
      try
      {
         final Hermes hermes = HermesFactory.createHermes(getConfig(), hermesSelection.getName());

         for (final Iterator iter = hermesSelection.getDestinationSelections().iterator(); iter.hasNext();)
         {
            final DestinationSelectionType destinationSelection = (DestinationSelectionType) iter.next();

            //watchManager.addWatch(hermes, destinationSelection.getName(), this);
         }
      }
      catch (NamingException e)
      {
         throw new BuildException(e);
      }
      
   }

   public void onDepthChange(Hermes hermes, DestinationConfig dConfig, long currentDepth)
   {
      if (currentDepth > depth)
      {
         final Target invokeTarget = (Target) getProject().getTargets().get(getTarget());

         log(dConfig.getName() + " on " + hermes.getId() + " has triggered, currentDepth=" + currentDepth);

         System.setProperty(getHermesProperty(), hermes.getId());
         System.setProperty(getDestinationProperty(), dConfig.getName());
         System.setProperty(getMessageProperty(), "depth=" + currentDepth);
         invokeTarget.execute();
      }
   }

   public void onException(Hermes hermes, DestinationConfig dConfig, Exception e)
   {
      // NOP
   }

   public void onOldestMessageChange(Hermes hermes, DestinationConfig dConfig, Date oldest)
   {
      // NOP
   }

   public void onPropertyChange(Hermes hermes, DestinationConfig dConfig, Map properties)
   {
      // NOP
   }

   public int getDepth()
   {
      return depth;
   }

   public void setDepth(int depth)
   {
      this.depth = depth;
   }

   public String getTarget()
   {
      return target;
   }

   public void setTarget(String target)
   {
      this.target = target;
   }

   public String getDestinationProperty()
   {
      return destinationProperty;
   }

   public void setDestinationProperty(String destinationProperty)
   {
      this.destinationProperty = destinationProperty;
   }

   public String getHermesProperty()
   {
      return hermesProperty;
   }

   public void setHermesProperty(String hermesProperty)
   {
      this.hermesProperty = hermesProperty;
   }

   public String getMessageProperty()
   {
      return messageProperty;
   }

   public void setMessageProperty(String messageProperty)
   {
      this.messageProperty = messageProperty;
   }
}
