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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * The root of all Hermes runtime exceptions.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HermesRuntimeException.java,v 1.1 2004/05/01 15:52:34
 *          colincrist Exp $
 */

public class HermesRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
	private Exception linkedException ;

    public HermesRuntimeException(Exception ex)
    {
        super(ex.getMessage());
        
        linkedException = ex ;

    }

    /**
     * HermesException constructor.
     * 
     * @param arg1
     *            java.lang.String
     */
    public HermesRuntimeException(String arg1)
    {
        super(arg1);
    }

    /**
     * HermesException constructor.
     * 
     * @param arg1
     *            java.lang.String
     */
    public HermesRuntimeException(String arg1, Exception linkedException)
    {
        super(arg1);

        this.linkedException = linkedException ;
    }
    
    public Exception getLinkedException()
    {
       return linkedException ;
    }

    /**
     * HermesException constructor.
     * 
     * @param arg1
     *            java.lang.String
     * @param arg2
     *            java.lang.String
     */
    public HermesRuntimeException(String arg1, String arg2)
    {
        super(arg1 + ": " + arg2);
    }
    
    @Override
    public void printStackTrace()
    {
       super.printStackTrace();

       if (getLinkedException() != null)
       {
          getLinkedException().printStackTrace();
       }
    }

    @Override
    public void printStackTrace(PrintStream s)
    {
       super.printStackTrace(s);

       if (getLinkedException() != null)
       {
          getLinkedException().printStackTrace(s);
       }
    }

    @Override
    public void printStackTrace(PrintWriter s)
    {
       super.printStackTrace(s);

       if (getLinkedException() != null)
       {
          getLinkedException().printStackTrace(s);
       }
    }
}