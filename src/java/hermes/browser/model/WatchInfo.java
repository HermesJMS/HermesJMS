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

package hermes.browser.model;

import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;
import hermes.impl.DestinationConfigKeyWrapper;

import java.beans.PropertyChangeSupport;
import java.util.Map;

import javax.jms.JMSException;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: WatchInfo.java,v 1.6 2006/05/06 17:22:56 colincrist Exp $
 */
public class WatchInfo extends PropertyChangeSupport
{
    private static final Logger log = Logger.getLogger(WatchInfo.class);
    public static final String STATISTICS = "statistics";
    public static final String DEPTH = "depth";
    public static final String EXCEPTION = "exception";
    public static final String OLDEST = "oldest";

    public WatchInfo(String hermesId, DestinationConfig dConfig) throws JMSException
    {
        super(new DestinationConfigKeyWrapper(dConfig));
        this.key = new DestinationConfigKeyWrapper(dConfig) ;
        this.hermesId = hermesId;
        this.dConfig = dConfig;
        dConfig.setMyHermes(hermesId) ;
    }

    private DestinationConfigKeyWrapper key;
    private String hermesId;
    private DestinationConfig dConfig ;
    private int depth;
    private long oldest;
    private Throwable e;
    private int depthAlert;
    private long ageAlert;
    private boolean inAlert = false;
    private Map statistics = HermesAdminSupport.getDefaultStatistics();

    public int getDepth()
    {
        return depth; 
    }

    public void setDepth(final int newDepth)
    {
        if (depth != newDepth)
        {
            final int oldDepth = depth ;            
            depth = newDepth;

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    firePropertyChange(DEPTH, oldDepth, newDepth);
                }
            });
        }
    }

    public Throwable getE()
    {
        return e;
    }

    public void setE(final Throwable newE)
    {
        this.e = newE;

        if (e != null && newE != null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    firePropertyChange(EXCEPTION, e, newE);
                }
            });
        }
    }

    public boolean isInAlert()
    {
        return inAlert;
    }

    public void setInAlert(boolean inAlert)
    {
        this.inAlert = inAlert;
    }

    public long getOldest()
    {
        return oldest;
    }

    public void setOldest(final long newOldest)
    {
        if (newOldest != oldest)
        {
            final long oldOldest = oldest ;
            oldest = newOldest;

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    firePropertyChange(OLDEST, new Long(oldOldest), new Long(newOldest));
                }
            });
        }
    }

    public Map getStatistics()
    {
        return statistics;
    }

    public void setStatistics(final Map newStatistics)
    {
        final Map oldStatistics = statistics ;
        statistics = newStatistics ;

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                firePropertyChange(STATISTICS, oldStatistics, newStatistics);
            }
        });
    }

    public long getAgeAlert()
    {
        return ageAlert;
    }

    public void setAgeAlert(long ageAlert)
    {
        this.ageAlert = ageAlert;
    }

    public int getDepthAlert()
    {
        return depthAlert;
    }

    public void setDepthAlert(int depthAlert)
    {
        this.depthAlert = depthAlert;
    }

    public DestinationConfig getConfig()
    {
       return dConfig ;
    }

    public String getHermesId()
    {
        return hermesId;
    }

    public void setHermesId(String hermesId)
    {
        this.hermesId = hermesId;
    }

    public DestinationConfigKeyWrapper getKey()
    {
        return key;
    }

  
}