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

package hermes.impl;

import java.util.Iterator;

import javax.jms.ConnectionMetaData;
import javax.jms.JMSException;

import org.apache.log4j.Category;
import hermes.Hermes;
import hermes.HermesException;
import hermes.ProviderMetaData;
import hermes.config.FactoryConfig;
import hermes.config.PropertyConfig;

/**
 * A Default implementation for accessing meta data related to a JMS provider.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultProviderMetaData.java,v 1.1 2004/05/01 15:52:35
 *          colincrist Exp $
 */

public class DefaultProviderMetaData implements ProviderMetaData
{
    private static final Category cat = Category.getInstance(DefaultProviderMetaData.class);

    private FactoryConfig hermesConfig;
    private String shortName;
    private Hermes hermes;
    private boolean cacheToolTipText = true;
    private String toolTipText;
    private boolean connectionSharing = true;

    public DefaultProviderMetaData(Hermes hermes, FactoryConfig hermesConfig, boolean connectionSharing) throws JMSException
    {
        this(hermes, hermesConfig, hermes.getId(), connectionSharing);
    }

    public DefaultProviderMetaData(Hermes hermes, FactoryConfig hermesConfig, String shortName, boolean connectionSharing) throws JMSException
    {
        this.hermes = hermes;
        this.hermesConfig = hermesConfig;
        this.shortName = shortName;
        this.connectionSharing = connectionSharing;

    }

    /**
     * @see hermes.impl.ProviderMetaData#getShortName()
     */
    public String getShortName() throws HermesException
    {
        return shortName;
    }

    public void setShortName(String shortName)
    {
        this.shortName = shortName;
    }

    /**
     * @see hermes.impl.ProviderMetaData#getToolTipText(ConnectionFactory)
     */
    public String getToolTipText() throws HermesException
    {
        try
        {
            if (toolTipText == null && hermes.getConnectionFactory() != null)
            {
                StringBuffer buffer = new StringBuffer();

                buffer.append("<html><b>").append(hermes.getConnectionFactory().getClass().getName()).append("</b><br>");

                for (Iterator iter = hermesConfig.getProvider().getProperties().getProperty().iterator(); iter.hasNext();)
                {
                    PropertyConfig property = (PropertyConfig) iter.next();

                    buffer.append(property.getName()).append("=").append(property.getValue());

                    if (iter.hasNext())
                    {
                        buffer.append("<br>");
                    }
                }

                buffer.append("</html>");

                toolTipText = buffer.toString();
            }
        }
        catch (JMSException e)
        {
            throw new HermesException(e.getMessage(), e);
        }

        return toolTipText;
    }

    /**
     * @see hermes.impl.ProviderMetaData#getDebugText(ConnectionFactory)
     */
    public String getDebugText() throws HermesException
    {
        return getToolTipText();
    }

    public ConnectionMetaData getConnectionMetaData() throws JMSException
    {
        return hermes.getConnection().getMetaData();

    }

    /**
     * @see hermes.impl.ProviderMetaData#getConnectionSharing()
     */
    public boolean getConnectionSharing()
    {
        return connectionSharing;
    }

}