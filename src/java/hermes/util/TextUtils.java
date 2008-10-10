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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sun.xml.bind.StringInputStream;

/**
 * Various utilities for string formatting.
 */

public abstract class TextUtils
{
   private static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

   private static final Logger log = Logger.getLogger(TextUtils.class);

   public static String crumble(String in, int max)
   {
      if (in.length() < max)
      {
         return in ;
      }
      else
      {
         return in.substring(0, max/2) + " ... " + in.substring(in.length() - (max/2) + 1 , in.length()) ;
      }
   }
   
   public static String printException(Throwable t)
   {
      StringWriter string = new StringWriter() ;
      t.printStackTrace(new PrintWriter(string)) ;
      return string.toString() ;     
   }
   public static String getAge(Date oldest)
   {
      if (oldest != null)
      {
         Date now = new Date();
         Date then = oldest;

         long diff = (now.getTime() - then.getTime()) / 1000;
         int days = (int) diff / 86400;
         int hours = (int) (diff % 86400) / 3600;
         int mins = (int) (diff % 360) / 60;
         int secs = (int) (diff % 60);

         StringBuffer rval = new StringBuffer();

         if (days > 0)
         {
            rval.append(days).append(" day").append(plural(days)).append(" ");
         }

         if (hours > 0)
         {
            rval.append(hours).append(" hour").append(plural(hours)).append(" ");
         }

         if (mins > 0)
         {
            rval.append(mins).append(" minute").append(plural(mins)).append(" ");
         }

         if (secs > 0)
         {
            rval.append(secs).append(" second").append(plural(secs)).append(" ");
         }

         return rval.toString();

      }
      else
      {
         return "empty";
      }

   }

   public static boolean isEmpty(final String s)
   {
      return s == null || s.equals("");
   }

   /**
    * Appends spaces until length of text is at least width. Will never truncate
    * the text.
    */
   public static String leftAlign(String text, int width)
   {
      return rightPad(text, width, ' ');
   }

   /**
    * Prepends pad characters until length of text is at least width. Will never
    * truncate the text
    */
   public static String leftPad(String text, int width, char padChar)
   {
      StringBuffer result = new StringBuffer(text);
      while (result.length() < width)
      {
         result.insert(0, padChar);
      }

      return result.toString();
   }

   /**
    * Left pads number with zero's
    */
   public static String leftPadInt(int number, int width)
   {
      return leftPad(Integer.toString(number), width, '0');
   }

   /**
    * Left pads number with zero's
    */
   public static String leftPadLong(long number, int width)
   {
      return leftPad(Long.toString(number), width, '0');
   }

   public static String plural(int n)
   {
      return n == 1 ? "" : "s";
   }

   public static InputStream replaceClasspathVariables(InputStream istream) throws IOException
   {
      final StringBuffer buffer = new StringBuffer();
      final BufferedReader in = new BufferedReader(new InputStreamReader(istream));

      String line;

      while ((line = in.readLine()) != null)
      {
         buffer.append(replaceClasspathVariables(line));
         buffer.append("\n");
      }

      return new StringInputStream(buffer.toString());
   }

   public static String replaceClasspathVariables(String string) throws IOException
   {
      //
      // @TODO Horribly inefficient hack.

      while (string.contains("${"))
      {
         final int startIndex = string.indexOf("${") + 2;
         final int endIndex = string.indexOf("}");
         final String propertyName = string.substring(startIndex, endIndex);

         if (System.getProperty(propertyName) != null)
         {
            log.debug("replacing " + propertyName + " with " + System.getProperty(propertyName));

            string = string.replace("${" + propertyName + "}", System.getProperty(propertyName));
         }
         else
         {
            throw new IOException("Unknown variable " + propertyName);
         }
      }

      return string;
   }

   /**
    * Prepends spaces until length of text is at least width. Will never
    * truncate the text
    */
   public static String rightAlign(String text, int width)
   {
      return leftPad(text, width, ' ');
   }

   /**
    * Appends pad characters until length of text is at least width. Will never
    * truncate the text.
    */
   public static String rightPad(String text, int width, char padChar)
   {
      StringBuffer result = new StringBuffer(text);
      while (result.length() < width)
      {
         result.append(padChar);
      }
      return result.toString();
   }

   /**
    * Right pad number with zero's
    */
   public static String rightPadInt(int number, int width)
   {
      return rightPad(Integer.toString(number), width, '0');
   }

   /**
    * Right pad number with zero's
    */
   public static String rightPadLong(long number, int width)
   {
      return rightPad(Long.toString(number), width, '0');
   }

   /**
    * returns string of characters c, width = width
    */
   public static String stringOf(char c, int width)
   {
      StringBuffer result = new StringBuffer();
      while (result.length() < width)
      {
         result.append(c);
      }
      return result.toString();
   }

   /**
    *  
    */
   public static char toAsciiChar(byte b)
   {
      char c = (char) b;
      return Character.isISOControl(c) ? '.' : c;
   }

   public static String toAsciiString(byte[] b)
   {
      StringWriter buff = new StringWriter(b.length * 2);
      for (int i = 0; i < b.length; i++)
      {
         buff.append(toAsciiChar(b[i]));
      }

      return buff.toString();
   }

   /**
    *  
    */
   public static StringWriter toHexString(byte b)
   {
      int n = b;
      if (n < 0)
         n = 256 + n;

      int d1 = n / 16;
      int d2 = n % 16;

      StringWriter buff = new StringWriter(2);
      buff.append(hexDigits[d1]);
      buff.append(hexDigits[d2]);

      return buff;
   }

   public static String toHexString(byte[] b, boolean spacePad)
   {
      StringWriter buff = new StringWriter(b.length * 3);
      for (int i = 0; i < b.length; i++)
      {
         buff.append(toHexString(b[i]).toString());
         if (spacePad)
            buff.append(' ');
      }

      return buff.toString();
   }
}