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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: GetXMLTag.java,v 1.3 2004/09/16 20:30:50 colincrist Exp $
 */

public class GetXMLTag extends TagSupport
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4411357673938326778L;

	private static final Logger log = Logger.getLogger(GetXMLTag.class);

    private String message;

    public int doStartTag() throws JspException
    {

        try
        {
            if (message == null)
            {
                pageContext.getOut().print("No message found");
            }
            else
            {
                MessageBean mbean = (MessageBean) pageContext.getAttribute(message, PageContext.SESSION_SCOPE);

                pageContext.getOut().print(mbean.getXML());
            }
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);

            throw new JspException("GetXMLTag: " + e.getMessage());
        }

        return SKIP_BODY;
    }

    /**
     *  
     */
    public GetXMLTag()
    {
        super();
    }

    /**
     * @return Returns the message.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param message
     *            The message to set.
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

}