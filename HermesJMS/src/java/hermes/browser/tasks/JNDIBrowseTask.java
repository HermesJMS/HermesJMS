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
import hermes.browser.actions.BrowseContextAction;
import hermes.browser.components.ContextTreeModelFactory;
import hermes.browser.model.tree.ContextTreeModel;
import hermes.config.NamingConfig;

import javax.swing.SwingUtilities;

/**
 * @author colincrist@hermesjms.com
 */
public class JNDIBrowseTask extends TaskSupport
{
    private NamingConfig namingConfig;
    private BrowseContextAction action;

    /**
     * @param icon
     */
    public JNDIBrowseTask(NamingConfig namingConfig, BrowseContextAction action)
    {
        super(IconCache.getIcon("jndi.context"));

        this.namingConfig = namingConfig;
        this.action = action;
    }

    public String getTitle()
    {
        return "Browse JNDI";
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.browser.tasks.Task#run()
     */
    public void invoke() throws Exception
    {
        final ContextTreeModel model = ContextTreeModelFactory.create(namingConfig);

        SwingUtilities.invokeAndWait(new Runnable()
        {
            public void run()
            {
                action.update(model);
            }
        });
    }
}
