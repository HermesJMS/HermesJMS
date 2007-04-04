/* 
 * Copyright 2003,2004 Peter Lee, Colin Crist
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

package hermes.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.log4j.Logger;

/**
 * Various utilities for java.io.
 */
public abstract class IoUtils
{
   private static final Logger log = Logger.getLogger(IoUtils.class);

   public static void closeQuietly(Reader o)
   {
      try
      {
         if (o != null)
         {
            o.close();
         }
      }
      catch (IOException e)
      {
         // NOP
      }
   }

   public static void closeQuietly(Writer o)
   {
      try
      {
         if (o != null)
         {
            o.close();
         }
      }
      catch (IOException e)
      {
         // NOP
      }
   }

   public static void closeQuietly(OutputStream o)
   {
      try
      {
         if (o != null)
         {
            o.close();
         }
      }
      catch (IOException e)
      {
         // NOP
      }
   }

   public static void closeQuietly(InputStream o)
   {
      try
      {
         if (o != null)
         {
            o.close();
         }
      }
      catch (IOException e)
      {
         // NOP
      }
   }
}
