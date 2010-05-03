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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: HermesBrowseTag.java,v 1.4 2005/06/20 15:28:38 colincrist Exp $
 */
public class HermesBrowseTag extends TagSupport
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1924937071561397743L;

	private static final Logger log = Logger.getLogger(HermesBrowseTag.class);

    private String hermes;
    private String destination;
    private String collection;
    private String params;
    private String raise;

    private String decorator = MessageDecorator.class.getName();

    /**
     *  
     */
    public HermesBrowseTag()
    {
        super();
    }

    public int doStartTag() throws JspTagException
    {
        Hermes h = null;
        Collection paramSet = new ArrayList();
        Map headerMap = new HashMap();

        if (params != null)
        {
            for (StringTokenizer tokens = new StringTokenizer(params, ","); tokens.hasMoreTokens();)
            {
                paramSet.add(tokens.nextToken());
            }
        }

        if (raise != null)
        {
            for (StringTokenizer tokens = new StringTokenizer(raise, ","); tokens.hasMoreTokens();)
            {
                String keyVal = (String) tokens.nextToken();

                StringTokenizer tokens2 = new StringTokenizer(keyVal, "=");

                headerMap.put(tokens2.nextToken(), tokens2.nextToken());
            }

        }

        hermes = (String) pageContext.getAttribute("hermes", PageContext.SESSION_SCOPE);
        destination = (String) pageContext.getAttribute("destination", PageContext.SESSION_SCOPE);
        collection = (String) pageContext.getAttribute("collection", PageContext.SESSION_SCOPE);

        if (collection == null)
        {
            collection = "messages";
        }

        log.debug("hermes=" + hermes + ", destination=" + destination + ", params=" + paramSet);

        try
        {
            final MessageDecorator dec = (MessageDecorator) Class.forName(decorator).newInstance();
            final Collection c = new ArrayList();
            final Map map = new HashMap();

            h = HermesSessionManager.getHermes(pageContext, hermes);

            Destination d = h.getDestination(destination, Domain.QUEUE);
            QueueBrowser browser = h.createBrowser(d);

            long i = 0;

            for (Enumeration e = browser.getEnumeration(); e.hasMoreElements();)
            {
                Message m = (Message) e.nextElement();

                if (m != null)
                {
                    Object mbean = dec.decorate(collection, h, Long.toString(i), m, paramSet, headerMap);

                    pageContext.setAttribute(getCollection() + "[" + Long.toString(i) + "]", mbean, PageContext.SESSION_SCOPE);
                    map.put(Long.toString(i++), mbean);
                    c.add(mbean);
                }
            }

            pageContext.setAttribute(getCollection(), c, PageContext.SESSION_SCOPE);
            pageContext.setAttribute(getCollection() + ".map", c, PageContext.SESSION_SCOPE);

            browser.close();

            log.debug("read " + c.size() + " message(s)");
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            throw new JspTagException(e.getMessage());
        }
        finally
        {
            try
            {
                if (h != null)
                {
                    h.close();
                }
            }
            catch (JMSException e1)
            {
                log.error(e1.getMessage(), e1);
            }
        }
        return SKIP_BODY;
    }

    public int doEndTag()
    {
        return EVAL_PAGE;
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
     * @return Returns the decorator.
     */
    public String getDecorator()
    {
        return decorator;
    }

    /**
     * @param decorator
     *            The decorator to set.
     */
    public void setDecorator(String decorator)
    {
        this.decorator = decorator;
    }

    /**
     * @return Returns the params.
     */
    public String getParams()
    {
        return params;
    }

    /**
     * @param params
     *            The params to set.
     */
    public void setParams(String params)
    {
        this.params = params;
    }

    /**
     * @return Returns the header.
     */
    public String getRaise()
    {
        return raise;
    }

    /**
     * @param header
     *            The header to set.
     */
    public void setRaise(String raise)
    {
        this.raise = raise;
    }
}