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

package hermes.browser.dialog;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: AbstractRendererConfigPanel.java,v 1.2 2004/09/16 20:30:50 colincrist Exp $
 */
public class AbstractRendererConfigPanel extends JPanel
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 5396435230901109135L;

	/**
     * 
     */
    public AbstractRendererConfigPanel()
    {
        super();
    }

    /**
     * @param isDoubleBuffered
     */
    public AbstractRendererConfigPanel(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
    }

    /**
     * @param layout
     */
    public AbstractRendererConfigPanel(LayoutManager layout)
    {
        super(layout);
    }

    /**
     * @param layout
     * @param isDoubleBuffered
     */
    public AbstractRendererConfigPanel(LayoutManager layout, boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
    }

}
