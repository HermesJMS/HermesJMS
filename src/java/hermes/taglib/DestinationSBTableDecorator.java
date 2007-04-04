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

import org.displaytag.decorator.TableDecorator;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DestinationSBTableDecorator.java,v 1.1 2004/05/01 15:52:36
 *          colincrist Exp $
 */
public class DestinationSBTableDecorator extends TableDecorator
{
    /**
     *  
     */
    public DestinationSBTableDecorator()
    {
        super();
    }

    public String getPrettyDepth()
    {
        DestinationStatisticsBean sbean = (DestinationStatisticsBean) getCurrentRowObject();

        switch (sbean.getDepth())
        {
            case -1:
                return "unknown";

            case 0:
                return "empty";

            default:
                return Integer.toString(sbean.getDepth());

        }
    }

    public String getBrowseUrl()
    {
        DestinationStatisticsBean sbean = (DestinationStatisticsBean) getCurrentRowObject();

        return "<a href=\"doBrowse.jsp?hermes=" + sbean.getHermes() + "&destination=" + sbean.getName() + "\">" + sbean.getName() + "</a>";
    }
}