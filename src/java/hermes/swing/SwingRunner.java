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

package hermes.swing;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

/**
 * @author colincrist@hermesjms.com
 */
public class SwingRunner
{
    public static void invokeLater(Runnable r)
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            r.run() ;
        }
        else
        {
            SwingUtilities.invokeLater(r) ;
        }
    }
    
    public static void invokeAndWait(Runnable r) throws InterruptedException, InvocationTargetException
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            r.run() ;
        }
        else
        {
            SwingUtilities.invokeAndWait(r) ;
        }
    }
}
