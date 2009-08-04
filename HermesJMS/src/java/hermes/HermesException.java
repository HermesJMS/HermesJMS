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

import javax.jms.JMSException;

/**
 * The base of all Hermes exceptions.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HermesException.java,v 1.9 2005/12/14 08:11:25 colincrist Exp $
 */

public class HermesException extends JMSException
{
   private static final long serialVersionUID = 1L;

   /**
    * HermesException constructor.
    * 
    * @param arg1
    *           java.lang.String
    */
   public HermesException()
   {
      super("");
   }

   public HermesException(Exception ex)
   {
      super(ex.getMessage());

      setLinkedException(ex);
   }

   /**
    * HermesException constructor.
    * 
    * @param arg1
    *           java.lang.String
    */
   public HermesException(String arg1)
   {
      super(arg1);
   }

   /**
    * HermesException constructor.
    * 
    * @param arg1
    *           java.lang.String
    */
   public HermesException(String arg1, Exception ex)
   {
      super(arg1);

      setLinkedException(ex);
   }

   @Override
   public String getMessage()
   {
      if (getLinkedException() == null)
      {
         return super.getMessage();
      }
      else
      {
         if (super.getMessage() == null)
         {
            return "No message, linked exception is : " + getLinkedException().getMessage();
         }
         else
         {
            if (super.getMessage().equals(getLinkedException().getMessage()))
            {
               return super.getMessage();
            }
            else
            {
               return super.getMessage() + " caused by " + getLinkedException().getMessage();
            }
         }
      }
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