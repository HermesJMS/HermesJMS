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

package hermes;

import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.impl.DestinationConfigKeyWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.JMSException;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 */
public class HermesWatchManager
{
    private static final Logger log = Logger.getLogger(HermesWatchManager.class);
    public static final int DEFAULT_DEPTH_ALERT = 0 ;
    public static final long DEFAULT_AGE_ALERT = 0 ;
    private static class State
    {
        public Hermes hermes;
        public DestinationConfig dConfig;
        public long depth;
        public Date oldest;
        public Exception e;
        public Map statistics;
        public Collection<HermesWatchListener> listeners = new ArrayList<HermesWatchListener>();
    }

    private final Timer timer = new Timer();
    private final Map<DestinationConfigKeyWrapper, State> watchStatistics = new HashMap<DestinationConfigKeyWrapper, State>();
    private boolean keepRunning = true;
    private long timeout = 30 * 1000;
    private boolean updateOnNewWatchAdded = false ;

    public HermesWatchManager()
    {
        timer.schedule(new TimerTask()
        {
            public void run()
            {
                doUpdate();
            }
        }, timeout, timeout);
    }

    public void clear()
    {
        synchronized (watchStatistics)
        {
            watchStatistics.clear();
        }
    }

    public void updateNow()
    {
        timer.schedule(new TimerTask()
        {
            public void run()
            {
                doUpdate();
            }
        }, 0);
    }

    public void close()
    {
        clear();
        keepRunning = false;
        timer.cancel();
    }

    public void addWatch(Hermes hermes, DestinationConfig destination, HermesWatchListener listener) throws JMSException
    {
        if ( !keepRunning)
        {
            throw new HermesException("WatchManager is not running");
        }

        final DestinationConfig dConfig = HermesBrowser.getConfigDAO().duplicateForWatch(destination, hermes) ;
               
        synchronized (watchStatistics)
        {
            final DestinationConfigKeyWrapper key = new DestinationConfigKeyWrapper(dConfig) ;
            State stats;
            
            if ( watchStatistics.containsKey(key))
            {
                stats = watchStatistics.get(key);
            }
            else
            {
                stats = new State();
                stats.hermes = hermes;
                stats.dConfig = dConfig;
                watchStatistics.put(key, stats);
            }

            stats.listeners.add(listener);
        }
        
        if (isUpdateOnNewWatchAdded())
        {
            updateNow() ;
        }
    }

    public void removeWatch(Hermes hermes, String destination, HermesWatchListener listener) throws JMSException
    {
        if ( !keepRunning)
        {
            throw new HermesException("WatchManager is not running");
        }

        synchronized (watchStatistics)
        {
            State stats;

            if ( watchStatistics.containsKey(hermes.getId() + ":" + destination))
            {
                stats = (State) watchStatistics.get(hermes.getId() + ":" + destination);
                stats.listeners.remove(listener);

                if ( stats.listeners.size() == 0)
                {
                    watchStatistics.remove(hermes.getId() + ":" + destination);
                }
            }
            else
            {
                throw new HermesException("No watch exists for " + destination);
            }
        }
    }

    private void doUpdate()
    {
        Map<DestinationConfigKeyWrapper, State> watchStatisticsCopy = new HashMap<DestinationConfigKeyWrapper, State>();
        Set<Hermes> hermesToClose = new HashSet<Hermes>();

        synchronized (watchStatistics)
        {
            watchStatisticsCopy.putAll(watchStatistics);
        }

        for (Map.Entry<DestinationConfigKeyWrapper, State> entry: watchStatisticsCopy.entrySet() )
        {
            State stats = (State) entry.getValue();
            
            try
            {
               updateWatchStatistics(stats);
            }
            catch (Throwable t)
            {
               log.error(t.getMessage(), t) ;               
            }
            finally
            {
               hermesToClose.add(stats.hermes);
            }
        }

        for (Hermes hermes : hermesToClose)
        {
           log.debug("closing Hermes " + hermes.getId());

            try
            {
                hermes.close();
            }
            catch (JMSException e)
            {
                log.error(e.getMessage(), e);
            }
        }

    }

    private void updateWatchStatistics(State stats)
    {
        final Hermes hermes = stats.hermes;
        final long previousDepth = stats.depth;

        try
        {
            try
            {
                stats.depth = hermes.getDepth(stats.dConfig);
            }
            catch (JMSException ex)
            {
                log.error("cannot get depth: " + ex.getMessage());
                stats.depth = 0;
            }

            try
            {
                stats.oldest = stats.depth > 0 ? new Date(hermes.getAge(stats.dConfig)) : null;
            }
            catch (JMSException ex)
            {
                log.error("cannot get oldest " + ex.getMessage());
                stats.oldest = null;
            }

            stats.statistics = hermes.getStatistics(stats.dConfig);

            if ( previousDepth != stats.depth)
            {
                for (final HermesWatchListener listener : stats.listeners)
                {
                  listener.onDepthChange(hermes, stats.dConfig, stats.depth);
                }
            }

			for (final HermesWatchListener listener : stats.listeners)
            {
                listener.onOldestMessageChange(hermes, stats.dConfig, stats.oldest);
            }

			for (final HermesWatchListener listener : stats.listeners)
            {
                listener.onPropertyChange(hermes, stats.dConfig, stats.statistics);
            }

            if ( stats.e != null)
            {
                stats.e = null;

				for (final HermesWatchListener listener : stats.listeners)
                {
                    listener.onException(hermes, stats.dConfig, null);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);

            if ( stats.e == null)
            {
                stats.e = e;

				for (final HermesWatchListener listener : stats.listeners)
                {
                    listener.onException(hermes, stats.dConfig, e);
                }
            }
        }
        catch (Throwable t)
        {
            log.error(t.getMessage(), t);
        }
    }

    public boolean isUpdateOnNewWatchAdded()
    {
        return updateOnNewWatchAdded;
    }

    public void setUpdateOnNewWatchAdded(boolean updateOnNewWatchAdded)
    {
        this.updateOnNewWatchAdded = updateOnNewWatchAdded;
    }
    
    
}