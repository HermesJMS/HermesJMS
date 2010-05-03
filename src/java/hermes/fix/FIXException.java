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

package hermes.fix;

import hermes.HermesException;

/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: FIXException.java,v 1.1 2006/05/02 21:35:43 colincrist Exp $
 */
public class FIXException extends HermesException
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -6194554603874829867L;

	/**
     * 
     */
    public FIXException()
    {
        super();
    }

    /**
     * @param ex
     */
    public FIXException(Exception ex)
    {
        super(ex);
    }

    /**
     * @param arg1
     */
    public FIXException(String arg1)
    {
        super(arg1);
    }

    /**
     * @param arg1
     * @param ex
     */
    public FIXException(String arg1, Exception ex)
    {
        super(arg1, ex);
    }
}
