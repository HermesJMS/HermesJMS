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

package hermes.taglib;

import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationStatisticsBean.java,v 1.1 2004/05/01 15:52:36
 *          colincrist Exp $
 */
public class DestinationStatisticsBean
{
    private static final Logger log = Logger.getLogger(DestinationStatisticsBean.class);
    private static final FastDateFormat dateFormat = FastDateFormat.getInstance("MM/dd/yyyy HH:mm:ss");
    private String name;
    private String hermes;
    private Date oldest;
    private int depth;

    /**
     *  
     */
    public DestinationStatisticsBean()
    {
        super();
    }

    /**
     * @return Returns the depth.
     */
    public int getDepth()
    {
        return depth;
    }

    /**
     * @param depth
     *            The depth to set.
     */
    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return Returns the oldest.
     */
    public String getOldest()
    {
        if (oldest != null)
        {
            return dateFormat.format(oldest);
        }
        else
        {
            return "empty";
        }
    }

    /**
     * @param oldest
     *            The oldest to set.
     */
    public void setOldest(Date oldest)
    {
        this.oldest = oldest;
    }

    private String plural(int n)
    {
        if (n > 1)
        {
            return "s";
        }
        else
        {
            return "";
        }
    }

    public String getAge()
    {
        if (oldest != null)
        {
            Date now = new Date();
            Date then = oldest;

            long diff = (now.getTime() - then.getTime()) / 1000;
            int days = (int) diff / 86400;
            int hours = (int) (diff % 86400) / 3600;
            int mins = (int) (diff % 360) / 60;
            int secs = (int) (diff % 60);

            StringBuffer rval = new StringBuffer();

            if (days > 0)
            {
                rval.append(days).append(" day").append(plural(days)).append(" ");
            }

            if (hours > 0)
            {
                rval.append(hours).append(" hour").append(plural(hours)).append(" ");
            }

            if (mins > 0)
            {
                rval.append(mins).append(" minute").append(plural(mins)).append(" ");
            }

            if (secs > 0)
            {
                rval.append(secs).append(" second").append(plural(secs)).append(" ");
            }

            return rval.toString();

        }
        else
        {
            return "empty";
        }
    }

    /**
     * @return Returns the hermes.
     */
    public String getHermes()
    {
        return hermes;
    }

    /**
     * @param hermes
     *            The hermes to set.
     */
    public void setHermes(String hermes)
    {
        this.hermes = hermes;
    }
}