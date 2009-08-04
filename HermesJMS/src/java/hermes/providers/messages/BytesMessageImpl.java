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

package hermes.providers.messages;

import java.nio.ByteBuffer;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageEOFException;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: BytesMessageImpl.java,v 1.2 2006/04/28 09:59:37 colincrist Exp $
 */

public class BytesMessageImpl extends MessageImpl implements BytesMessage
{
   private static final int defaultSize = 64 * 1024;
   private ByteBuffer bytes;
   private int maxPosition = 0;
   private int position = 0;

   public BytesMessageImpl(byte[] someBytes)
   {
      bytes = ByteBuffer.wrap(someBytes);

   }

   public BytesMessageImpl()
   {
      bytes = ByteBuffer.allocate(defaultSize);
   }

   public BytesMessageImpl(int size)
   {
      bytes = ByteBuffer.allocate(size);
   }

   public long getBodyLength() throws JMSException
   {
      return bytes.capacity();
   }

   public boolean readBoolean() throws JMSException
   {
      return false;
   }

   public byte readByte() throws JMSException
   {
      try
      {
         return bytes.get(position++);
      }
      catch (IndexOutOfBoundsException ex)
      {
         throw new MessageEOFException("EOF");
      }
   }

   public int readUnsignedByte() throws JMSException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public short readShort() throws JMSException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public int readUnsignedShort() throws JMSException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public char readChar() throws JMSException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public int readInt() throws JMSException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public long readLong() throws JMSException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public float readFloat() throws JMSException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public double readDouble() throws JMSException
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public String readUTF() throws JMSException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public int readBytes(byte[] arg0) throws JMSException
   {
      try
      {
         if (bytes.capacity() < position + arg0.length)
         {
            bytes.get(arg0, position, bytes.capacity());
            return bytes.capacity() - position;
         }
         else
         {
            bytes.get(arg0, position, position + arg0.length);
            return arg0.length;
         }
      }
      catch (IndexOutOfBoundsException ex)
      {
         throw new MessageEOFException("EOF");
      }
      finally
      {
         position += arg0.length;
      }
   }

   public int readBytes(byte[] arg0, int arg1) throws JMSException
   {
      try
      {
         if (bytes.capacity() < position + arg1)
         {
            arg1 = bytes.capacity() - position;
         }

         bytes.get(arg0, position, position + arg1);
         return arg1;
      }
      catch (IndexOutOfBoundsException ex)
      {
         throw new MessageEOFException("EOF");
      }
      finally
      {
         position += arg1;
      }
   }

   public void writeBoolean(boolean arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeByte(byte arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeShort(short arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeChar(char arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeInt(int arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeLong(long arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeFloat(float arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeDouble(double arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeUTF(String arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeBytes(byte[] arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeBytes(byte[] arg0, int arg1, int arg2) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void writeObject(Object arg0) throws JMSException
   {
      // TODO Auto-generated method stub

   }

   public void reset() throws JMSException
   {
      position = 0;
      bytes.rewind() ;
   }

   public String toString()
   {
      return new String(bytes.array());
   }

}
