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
import org.displaytag.decorator.ColumnDecorator;
import org.displaytag.exception.DecoratorException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: JMSTimestampDecorator.java,v 1.1 2004/05/01 15:52:36 colincrist
 *          Exp $
 */
public class JMSTimestampDecorator implements ColumnDecorator
{
    private static final Logger log = Logger.getLogger(JMSTimestampDecorator.class);
    private FastDateFormat dateFormat = FastDateFormat.getInstance("MM/dd/yyyy HH:mm:ss");

    /**
     *  
     */
    public JMSTimestampDecorator()
    {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.displaytag.decorator.ColumnDecorator#decorate(java.lang.Object)
     */
    public String decorate(Object arg0) throws DecoratorException
    {
        return dateFormat.format(new Date(((Long) arg0).longValue()));
    }
}