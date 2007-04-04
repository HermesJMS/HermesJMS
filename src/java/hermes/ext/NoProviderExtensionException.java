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

package hermes.ext;

import hermes.HermesException;

/**
 * Throw when the provider has no native extension available to support non-JMS
 * operations.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: NoProviderExtensionException.java,v 1.1 2004/05/01 15:52:35
 *          colincrist Exp $
 */
public class NoProviderExtensionException extends HermesException
{

    /**
     *  
     */
    public NoProviderExtensionException()
    {
        super();
    }

    /**
     * @param ex
     */
    public NoProviderExtensionException(Exception ex)
    {
        super(ex);
    }

    /**
     * @param arg1
     */
    public NoProviderExtensionException(String arg1)
    {
        super(arg1);
    }

    /**
     * @param arg1
     * @param ex
     */
    public NoProviderExtensionException(String arg1, Exception ex)
    {
        super(arg1, ex);
    }
}