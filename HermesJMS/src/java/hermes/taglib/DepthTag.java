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

import hermes.Domain;
import hermes.Hermes;

import java.io.IOException;

import javax.jms.Destination;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: DepthTag.java,v 1.6 2005/08/15 20:37:30 colincrist Exp $
 */
public class DepthTag extends TagSupport
{
    private static final Logger log = Logger.getLogger(DepthTag.class);
    private String hermes;
    private String destination;

    /**
     *  
     */
    public DepthTag()
    {
        super();
    }

    public int doEndTag() throws JspException
    {

        try
        {
            String lDestination = destination;
            String lHermes = hermes;

            if (lDestination == null)
            {

                lDestination = (String) pageContext.getAttribute("destination", PageContext.SESSION_SCOPE);

            }

            if (lHermes == null)
            {

                lHermes = (String) pageContext.getAttribute("hermes", PageContext.SESSION_SCOPE);
            }

            final Hermes h = HermesSessionManager.getHermes(pageContext, lHermes);
            final Destination d = h.getDestination(lDestination, Domain.QUEUE);
            final int depth = 0 ; //  h.getDepth(d) ; 

            if (depth > 0)
            {
                pageContext.getOut().write("<b>");
                pageContext.getOut().write(Integer.toString(depth));
                pageContext.getOut().write("</b>");
            }
            else if (depth == 0)
            {
                pageContext.getOut().write("empty");

            }
            else
            {
                pageContext.getOut().write("unknown");
            }

            return SKIP_BODY;

        }
        catch (Exception e)
        {
            log.error(e.getMessage());

            try
            {
                pageContext.getOut().write("unknown");
            }
            catch (IOException e1)
            {
                log.error(e1.getMessage(), e1);
            }
        }

        return SKIP_BODY;
    }

    /**
     * @return Returns the destination.
     */
    public String getDestination()
    {
        return destination;
    }

    /**
     * @param destination
     *            The destination to set.
     */
    public void setDestination(String destination)
    {
        this.destination = destination;
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