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

import java.util.Comparator;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: LongComparator.java,v 1.3 2004/09/16 20:30:48 colincrist Exp $
 */
public class LongComparator implements Comparator
{

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object arg0, Object arg1)
    {
        if (arg0 instanceof Long && arg1 instanceof Long)
        {
            Long i1 = (Long) arg0;
            Long i2 = (Long) arg1;

            return i1.compareTo(i2);
        }
        return 0;
    }
}