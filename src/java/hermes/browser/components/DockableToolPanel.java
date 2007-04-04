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

package hermes.browser.components;

import hermes.browser.IconCache;

import javax.swing.JComponent;

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.swing.JideScrollPane;
import com.jidesoft.swing.JideTabbedPane;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DockableToolPanel.java,v 1.6 2005/05/19 16:16:10 colincrist Exp $
 */
public class DockableToolPanel extends DockableFrame
{
    private JideTabbedPane tabbedPane = new JideTabbedPane();

    public DockableToolPanel()
    {
        super("Tools", IconCache.getIcon("hermes.toolbox"));

        getContext().setInitMode(DockContext.STATE_AUTOHIDE);
        getContext().setInitSide(DockContext.DOCK_SIDE_SOUTH);
        getContext().setDockedHeight(100);
        setDockedHeight(100) ;
        setAvailableButtons(DockableFrame.BUTTON_AUTOHIDE | DockableFrame.BUTTON_FLOATING) ;
        

        getContentPane().add(tabbedPane);
    }

    public void addToolPanel(String tab, JComponent component)
    {
        tabbedPane.addTab(tab, new JideScrollPane(component));
    }
}