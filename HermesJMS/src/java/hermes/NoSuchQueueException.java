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

package hermes;

/**
 * The queue referened does not exist
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: NoSuchQueueException.java,v 1.1 2004/05/01 15:52:34 colincrist
 *          Exp $
 */
public class NoSuchQueueException extends HermesException
{
    private static final long serialVersionUID = 1L;

	/**
     * NoSuchQueueException constructor .
     * 
     * @param ex
     *            java.lang.Exception
     */
    public NoSuchQueueException(Exception ex)
    {
        super(ex);
    }

    /**
     * NoSuchQueueException constructor.
     * 
     * @param arg1
     *            java.lang.String
     */
    public NoSuchQueueException(String arg1)
    {
        super(arg1);
    }

    /**
     * NoSuchQueueException constructo.
     * 
     * @param arg1
     *            java.lang.String
     * @param ex
     *            java.lang.Exception
     */
    public NoSuchQueueException(String arg1, Exception ex)
    {
        super(arg1, ex);
    }

 
}