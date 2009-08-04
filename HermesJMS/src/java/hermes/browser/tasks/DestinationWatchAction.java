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

import hermes.Hermes;
import hermes.SingletonManager;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.model.QueueWatchTableModel;
import hermes.browser.model.WatchInfo;
import hermes.impl.ClassLoaderManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * @author colincrist@yaho.com last changed by: $Author: colincrist $
 * @version $Id: DestinationWatchAction.java,v 1.5 2005/08/15 20:37:31 colincrist Exp $
 */
public class DestinationWatchAction extends TaskSupport
{
    private static final Logger log = Logger.getLogger(DestinationWatchAction.class);
   
    private static final Set activeWatches = new HashSet();
    private QueueWatchTableModel watchModel;
    private String id;
    private JComponent forRepaint ;

    /**
     * @param content
     * @param title
     * @param listener
     */
    public DestinationWatchAction(String id, JComponent forRepaint, QueueWatchTableModel watchModel) 
    {
        super(IconCache.getIcon("hermes.watch"));

        this.watchModel = watchModel;
        this.id = id;
        this.forRepaint = forRepaint ;
    }
    
    public boolean isDuplicate()
    {
        synchronized (activeWatches)
        {
           return activeWatches.contains(id) ;
        }
    }

    public String getTitle()
    {
       return "Watch updating " + id ;
    }
   

    /* (non-Javadoc)
     * @see hermes.browser.actions.HermesAction#doAction()
     */
    public void invoke() throws Exception
    {
        synchronized (activeWatches)
        {
            if (activeWatches.contains(id))
            {
                log.info("previous watch id=" + id + " still running, not starting timed action");

                return;
            }
            
            activeWatches.add(id) ;
        }

        log.info("watch action for " + id + " starting");

        try
        {
            final Map hermesToClose = new HashMap();
            boolean localHasAlert = false;

            for (int i = 0; i < watchModel.getRowCount(); i++)
            {
                boolean deleted = false;
                final WatchInfo info = watchModel.getRow(i);

                try
                {
                    ClassLoaderManager classLoaderManager = (ClassLoaderManager) SingletonManager.get(ClassLoaderManager.class) ;
                    Thread.currentThread().setContextClassLoader(classLoaderManager.getClassLoaderByHermes(info.getHermesId())) ;
                    
                    Hermes hermes = null;

                    if (!hermesToClose.keySet().contains(info.getHermesId()))
                    {
                        hermes = (Hermes) HermesBrowser.getBrowser().getContext().lookup(info.getHermesId());
                        hermesToClose.put(info.getHermesId(), hermes);
                    }
                    else
                    {
                        hermes = (Hermes) hermesToClose.get(info.getHermesId());
                    }

                    if (updateWatchInfo(hermes, info))
                    {
                        localHasAlert = true;
                    }

                    info.setE(null);
                }
                catch (Throwable e)
                {
                    log.error(e.getMessage(), e);

                    info.setE(e);
                }
            }

            for (Iterator iter = hermesToClose.entrySet().iterator(); iter.hasNext();)
            {
                Map.Entry entry = (Map.Entry) iter.next();
                Hermes hermes = (Hermes) entry.getValue();

                try
                {
                    if (hermes != null)
                    {
                        hermes.close();
                    }
                }
                catch (JMSException e1)
                {
                    log.error("closing " + hermes.getId() + ": " + e1.getMessage(), e1);
                }
            }
        }
        catch (Throwable t)
        {
            log.error(t.getMessage(), t);
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                forRepaint.repaint() ;
            }
        }) ;
        
        
        log.debug("watch action for " + id + " finished");
        
        synchronized (activeWatches)
        {
            activeWatches.remove(id) ;
        }
    }

    private boolean updateWatchInfo(Hermes hermes, WatchInfo info) throws JMSException, NamingException
    {
        log.debug("updating " + hermes.getId() + " " + info.getConfig().getName()) ;
        
        info.setDepth(hermes.getDepth(info.getConfig()));

        if (info.getDepth() > 0)
        {
            info.setOldest(hermes.getAge(info.getConfig()));
        }
        else
        {
            info.setOldest(0);
        }

        info.setStatistics(hermes.getStatistics(info.getConfig()));

        if (info.isInAlert())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}