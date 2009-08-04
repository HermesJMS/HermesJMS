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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.jms.Destination;
import javax.jms.Message;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: InquireTag.java,v 1.4 2005/06/20 15:28:38 colincrist Exp $
 */

public class InquireTag extends TagSupport
{

    private static final Logger log = Logger.getLogger(InquireTag.class);
    private static FastDateFormat dateFormat = FastDateFormat.getInstance("MM/dd/yyyy HH:mm:ss");
    private String hermes;
    private String destination;
    private String var;
    private String collection;

    /**
     *  
     */
    public InquireTag()
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
                lDestination = (String) pageContext.getAttribute("destination", PageContext.PAGE_SCOPE);
            }

            if (lHermes == null)
            {
                lHermes = (String) pageContext.getAttribute("hermes", PageContext.PAGE_SCOPE);
            }

            if (var == null)
            {
                var = "inquire";
            }

            DestinationStatisticsBean sbean = new DestinationStatisticsBean();
            Hermes h = HermesSessionManager.getHermes(pageContext, lHermes);
            Destination d = h.getDestination(lDestination, Domain.QUEUE);

            sbean.setName(lDestination);
            sbean.setHermes(lHermes);

            int depth = 0;

            //
            // Get the depth...

            try
            {
                // depth = h.getDepth(d); @@TODO

                sbean.setDepth(depth);
            }
            catch (Exception e)
            {
                sbean.setDepth(0);
            }

            //
            // If depth > 0 then check out the age of the message on the head
            // of the queue

            try
            {
                if (depth > 0)
                {
                    Message m = h.receiveNoWait(d);

                    if (m != null)
                    {
                        sbean.setOldest(new Date(m.getJMSTimestamp()));
                    }
                }
            }
            catch (Exception e)
            {
                sbean.setOldest(null);
            }

            if (h.getTransacted())
            {
                h.rollback();
            }

            pageContext.setAttribute(var, sbean, PageContext.PAGE_SCOPE);

            if (collection != null)
            {
                Collection c;

                if (pageContext.getAttribute(collection, PageContext.PAGE_SCOPE) == null)
                {
                    c = new ArrayList();
                    pageContext.setAttribute(collection, c, PageContext.PAGE_SCOPE);
                }
                else
                {
                    c = (Collection) pageContext.getAttribute(collection, PageContext.PAGE_SCOPE);
                }

                c.add(sbean);
            }

            return SKIP_BODY;

        }
        catch (Exception e)
        {
            log.error(e.getMessage());
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

    /**
     * @return Returns the var.
     */
    public String getVar()
    {
        return var;
    }

    /**
     * @param var
     *            The var to set.
     */
    public void setVar(String var)
    {
        this.var = var;
    }

    /**
     * @return Returns the collection.
     */
    public String getCollection()
    {
        return collection;
    }

    /**
     * @param collection
     *            The collection to set.
     */
    public void setCollection(String collection)
    {
        this.collection = collection;
    }
}