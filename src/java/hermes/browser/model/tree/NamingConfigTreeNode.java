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

package hermes.browser.model.tree;

import hermes.browser.IconCache;
import hermes.config.NamingConfig;
import hermes.config.PropertyConfig;

import java.util.Iterator;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: NamingConfigTreeNode.java,v 1.5 2005/05/24 12:58:17 colincrist Exp $
 */
public class NamingConfigTreeNode extends AbstractTreeNode
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -1032045169429697948L;
	private StringBuffer toolTip = new StringBuffer() ;
    /**
     * @param id
     * @param icon
     */
    public NamingConfigTreeNode(NamingConfig namingConfig)
    {
        super(namingConfig.getId(), namingConfig) ;

        setIcon(IconCache.getIcon("jndi.context"));
        toolTip.append("<html>") ;
        
        for (Iterator iter = namingConfig.getProperties().getProperty().iterator() ; iter.hasNext() ; )
        {
            PropertyConfig pConfig = (PropertyConfig) iter.next() ;
            
            toolTip.append(pConfig.getName()).append("=").append(pConfig.getValue()).append("<br>") ;

        }
        
        toolTip.append("classpathId=").append(namingConfig.getClasspathId()) ;
        toolTip.append("</html>") ;
    }
    
    public NamingConfig getConfig()
    {
        return (NamingConfig) getBean() ;
    }
    
    public String getToolTipText()
    {
        return toolTip.toString();
    }
}