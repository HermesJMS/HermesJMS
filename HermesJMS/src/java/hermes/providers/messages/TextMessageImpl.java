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

package hermes.providers.messages;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: TextMessageImpl.java,v 1.3 2004/09/16 20:30:49 colincrist Exp $
 */
public class TextMessageImpl extends MessageImpl implements TextMessage
{
    private String text = "";

    /**
     *  
     */
    public TextMessageImpl()
    {
        super();
    }

    /**
     *  
     */
    public TextMessageImpl(String text)
    {
        super();

        this.text = text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#clearBody()
     */
    public void clearBody() throws JMSException
    {
        text = "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.TextMessage#getText()
     */
    public String getText() throws JMSException
    {
        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.TextMessage#setText(java.lang.String)
     */
    public void setText(String arg0) throws JMSException
    {
        text = arg0;
    }

}