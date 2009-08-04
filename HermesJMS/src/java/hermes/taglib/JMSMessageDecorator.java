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
 * @version $Id: JMSMessageDecorator.java,v 1.1 2004/05/01 15:52:36 colincrist
 *          Exp $
 */
public class JMSMessageDecorator extends TableDecorator
{
    public String getActions()
    {
        return getViewLink();
    }

    public String getViewLink()
    {
        MessageBean message = (MessageBean) getCurrentRowObject();
        int lIndex = getListIndex();

        return "<a href=\"view-message.jsp?bean=" + message.getAttributeId() + "[" + lIndex + "]\">" + "view" + "</a>";

    }

    public String getSaveLink()
    {
        MessageBean message = (MessageBean) getCurrentRowObject();
        int lIndex = getListIndex();

        return "<a href=\"save-message.jsp?bean=" + message.getAttributeId() + "[" + lIndex + "]\">" + "save" + "</a>";

    }

    public String getDeleteLink()
    {
        MessageBean message = (MessageBean) getCurrentRowObject();
        int lIndex = getListIndex();

        return "<a href=\"delete-message.jsp?bean=" + message.getAttributeId() + "[" + lIndex + "]\">" + "delete" + "</a>";
    }

}