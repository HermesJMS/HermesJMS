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

package hermes.browser;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * Helper to check which thread you're doing Swing UI work on.
 * 
 * http://www.clientjava.com/blog/2004/08/20/1093059428000.html
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ThreadCheckingRepaintManager.java,v 1.2 2004/09/16 20:30:48 colincrist Exp $
 */

public class ThreadCheckingRepaintManager extends RepaintManager
{
    public synchronized void addInvalidComponent(JComponent jComponent)
    {
        checkThread();
        super.addInvalidComponent(jComponent);
    }

    private void checkThread()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            System.out.println("Swing invocation on wrong thread, please report this bug");
            Thread.dumpStack();
        }
    }

    public synchronized void addDirtyRegion(JComponent jComponent, int i, int i1, int i2, int i3)
    {
        checkThread();
        
        super.addDirtyRegion(jComponent, i, i1, i2, i3);
    }
}

