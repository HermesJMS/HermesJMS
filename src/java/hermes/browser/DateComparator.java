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
import java.util.Date;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DateComparator.java,v 1.3 2004/09/16 20:30:49 colincrist Exp $
 */
public class DateComparator implements Comparator
{

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object arg0, Object arg1)
    {
        if (arg0 instanceof Date && arg1 instanceof Date)
        {
            Date d1 = (Date) arg0;
            Date d2 = (Date) arg1;

            if (d1.before(d2))
            {
                return -1;
            }
            else if (d1.after(d2))
            {
                return 1;
            }
        }
        return 0;
    }
}