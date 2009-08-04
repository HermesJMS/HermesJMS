/* 
 * Copyright 2003,2004,2005 Colin Crist
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

/**
 * A Collection of utilities that work on bytes and byte arrays.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: ByteUtils.java,v 1.1 2006/04/28 09:59:37 colincrist Exp $
 */

public class ByteUtils
{
   public static boolean startsWith(byte[] bytes, String text)
   {
      char[] chars = text.toCharArray() ;
      
      if (chars.length > bytes.length)
      {
         return false ;
      }
      else
      {
         for (int i = 0 ; i < chars.length ;i++)
         {
            if (bytes[i] != chars[i])
            {
               return false ;
            }
         }
         
         return true ;
      }
   }
  

}
