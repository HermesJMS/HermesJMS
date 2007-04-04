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

import hermes.HermesRepository;
import hermes.HermesRepositoryListener;
import hermes.browser.model.tree.RepositoryTreeNode;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: RepositoryTreeModel.java,v 1.1 2004/07/21 19:46:13 colincrist
 *          Exp $
 */

public class RepositoryTreeModel extends DefaultTreeModel implements HermesRepositoryListener
{
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Repositories");
    private Map rep2Node = new HashMap();

    public RepositoryTreeModel()
    {
        super(new DefaultMutableTreeNode());

        setRoot(rootNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepositoryListener#onRepositoryAdded(hermes.HermesRepository)
     */
    public void onRepositoryAdded(HermesRepository repository)
    {
        RepositoryTreeNode node = new RepositoryTreeNode(repository);

        rep2Node.put(repository, node);
        rootNode.add(node);

        nodeChanged(rootNode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesRepositoryListener#onRepositoryRemoved(hermes.HermesRepository)
     */
    public void onRepositoryRemoved(HermesRepository repository)
    {
        RepositoryTreeNode node = (RepositoryTreeNode) rep2Node.remove(repository);

        rootNode.remove(node);

        nodeChanged(rootNode);
    }

}