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

package hermes.impl;

import hermes.HermesRepository;
import hermes.HermesRepositoryListener;
import hermes.HermesRepositoryManager;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Category;

/**
 * Manages repository files in a directory
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FileRepositoryManager.java,v 1.2 2004/05/23 13:45:44 colincrist
 *          Exp $
 */

public class FileRepositoryManager implements HermesRepositoryManager
{
    private static final Category cat = Category.getInstance(FileRepositoryManager.class);
    private static final Timer timer = new Timer();

    private Set listeners = new HashSet();
    private Set repositories = new HashSet();
    private Map repositoriesByName = new HashMap();
    private File root;
    private TimerTask timerTask;
    private boolean scanning = false;

    public FileRepositoryManager(File root, long period)
    {
        this.root = root;

        timerTask = new TimerTask()
        {
            public void run()
            {
                scanForNewFiles();
            }
        };

        timer.schedule(timerTask, 0, period);
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepositoryManager#addRepositoryListener(hermes.HermesRepositoryListener)
     */
    public void addRepositoryListener(HermesRepositoryListener listener)
    {
        listeners.add(listener);

    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepositoryManager#removeRepositoryListener(hermes.HermesRepositoryListener)
     */
    public void removeRepositoryListener(HermesRepositoryListener listener)
    {
        listeners.remove(listener);

    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepositoryManager#iterator()
     */
    public Iterator iterator()
    {
        return repositories.iterator();
    }

    private void scanForNewFiles()
    {
        if (!scanning)
        {
            synchronized (this)
            {
                scanning = true;

                try
                {
                    File[] files = root.listFiles();
                    Set fileNames = new HashSet();

                    if (files != null)
                    {
                        for (int i = 0; i < files.length; i++)
                        {
                            File file = files[i];

                            fileNames.add(file.getName());

                            if (!repositoriesByName.containsKey(file.getName()))
                            {
                                FileRepository repository = new FileRepository(file);

                                repositories.add(repository);
                                repositoriesByName.put(file.getName(), repository);

                                for (Iterator iter = listeners.iterator(); iter.hasNext();)
                                {
                                    HermesRepositoryListener l = (HermesRepositoryListener) iter.next();

                                    cat.debug("new repository:" + repository.getId());

                                    l.onRepositoryAdded(repository);
                                }
                            }
                        }
                    }

                    for (Iterator iter = repositoriesByName.keySet().iterator(); iter.hasNext();)
                    {
                        String fileName = (String) iter.next();

                        if (!fileNames.contains(fileName))
                        {
                            HermesRepository repository = (HermesRepository) repositoriesByName.get(fileName);

                            iter.remove();
                            repositories.remove(repository);

                            for (Iterator iter2 = listeners.iterator(); iter2.hasNext();)
                            {
                                HermesRepositoryListener l = (HermesRepositoryListener) iter2.next();

                                cat.debug("removed repository: " + repository.getId());

                                l.onRepositoryRemoved(repository);
                            }
                        }
                    }
                }
                catch (Throwable ex)
                {
                    cat.error("during directory scanning: " + ex.getMessage(), ex);
                }

                scanning = false;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepositoryManager#setDirectory(java.lang.String)
     */
    public void setDirectory(String newDir)
    {
        synchronized (this)
        {
            root = new File(newDir);
            
            if (!root.exists())
            {
               root.mkdir() ;
            }
        }
    }

    public String getDirectory()
    {
        return root.getPath();
    }

}