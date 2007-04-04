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

package hermes.fix;

import java.nio.ByteBuffer;

import quickfix.DataDictionary;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FilePointerMessage.java,v 1.1 2006/08/01 07:29:35 colincrist Exp $
 */

public class FilePointerMessage
{
   private ByteBuffer buffer ;
   private int offset ;
   private int size ;
   private DataDictionary dictionary ;
   
   public FilePointerMessage(ByteBuffer buffer, int offset, int size)
   {
      this.buffer = buffer ;
      this.offset = offset ;
      this.size = size ;      
   }
   
   public byte[] getBytes()
   {      
      buffer.position(offset) ;
      byte[] rval = new byte[size] ;
      buffer.get(rval) ;
      
      return rval ;
   }
  
   public DataDictionary getDictionary()
   {
      return dictionary;
   }
   
   protected void setDictionary(DataDictionary dictionary)
   {
      this.dictionary = dictionary ;
   }
   
   public String toString()
   {
      return new String(getBytes()) ;
   }
}
