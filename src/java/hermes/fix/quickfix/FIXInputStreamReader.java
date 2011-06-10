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

package hermes.fix.quickfix;

import hermes.fix.FIXException;
import hermes.fix.FIXMessage;
import hermes.fix.FIXMessageFilter;
import hermes.fix.FIXReader;
import hermes.fix.MalformedMessageException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: FIXInputStreamReader.java,v 1.2 2006/08/01 12:24:00 colincrist
 *          Exp $
 */

public class FIXInputStreamReader implements Runnable, FIXReader
{
   private static final Logger log = Logger.getLogger(FIXInputStreamReader.class);

   private ArrayList<FIXMessage> messages = new ArrayList<FIXMessage>();
   private int maxMessages = 512;
   private int maxMessageBuffer = 1024 * 512;
   private byte[] messageBuffer;
   private int messageBufferIndex = 0;
   private byte[] startOfMessage = new byte[] { '8', '=', 'F', 'I', 'X' };
   private int messageId = 0;
   private boolean keepRunning = true;
   private boolean eofReached = false;
   private InputStream istream;
   private QuickFIXMessageCache cache;
   private FIXMessageFilter filter = new FIXMessageFilter();

   public FIXMessageFilter getFilter()
   {
      return filter;
   }

   public FIXInputStreamReader(QuickFIXMessageCache cache, InputStream istream)
   {
      super();
      this.istream = istream;
      this.messageBuffer = new byte[maxMessageBuffer];
      this.cache = cache;

      new Thread(this, "FIXSniffer").start();
   }

   public void release()
   {

   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.fix.FIXReader#close()
    */
   public void close()
   {
      keepRunning = false;
      eofReached = true;

      try
      {
         istream.close();
      }
      catch (IOException ex)
      {
         log.error(ex.getMessage(), ex);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.fix.FIXReader#read()
    */
   public FIXMessage read() throws IOException
   {
      return read(-1);
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.fix.FIXReader#read(long)
    */
   public FIXMessage read(final long timeout) throws IOException
   {
      synchronized (messages)
      {
         while (messages.size() == 0)
         {
            checkEOF();

            try
            {
               if (timeout == -1)
               {
                  messages.wait();
               }
               else if (timeout == 0)
               {
                  return null;
               }
               else
               {
                  messages.wait(timeout);
               }
            }
            catch (InterruptedException e)
            {
               // NOP
            }
         }

         checkEOF();

         if (messages.size() > 0)
         {
            return messages.remove(0);
         }
         else
         {
            return null;
         }
      }
   }

   protected void checkEOF() throws EOFException
   {
      synchronized (messages)
      {
         if (messages.size() == 0 && eofReached)
         {
            throw new EOFException("EOF");
         }
      }
   }

   public void run()
   {
      try
      {
         while (keepRunning)
         {
            try
            {
               FIXMessage message = readMessage();

               if (message != null && filter.filter(message.getMsgType()))
               {
                  synchronized (messages)
                  {
                     messages.add(message);

                     if (messages.size() == 1)
                     {
                        messages.notifyAll();
                     }
                  }
               }
            }
            catch (EOFException ex)
            {
               // Normal behaviour, don't log an error message.

               return;
            }
            catch (Exception ex)
            {
               log.warn(ex.getMessage(), ex);
            }

         }
      }
      catch (Throwable ex)
      {
         log.error(ex.getMessage(), ex);
      }
      finally
      {
         eofReached = true;

         synchronized (messages)
         {
            messages.notifyAll();
         }
      }
   }

   byte readByte(InputStream istream, byte[] bytes, int offset, int length) throws IOException
   {
      int i = istream.read(bytes, offset, length);

      if (i == -1)
      {
         eofReached = true;
         throw new EOFException("EOF");
      }
      else
      {
         return (byte) i;
      }
   }

   byte readByte(InputStream istream) throws IOException
   {
      int i = istream.read();

      if (i == -1)
      {
         eofReached = true;
         throw new EOFException("EOF");
      }
      else
      {
         return (byte) i;
      }
   }

   private FIXMessage readMessage() throws IOException, MalformedMessageException, FIXException
   {
      checkEOF();

      byte b;

      // Arrays.fill(messageBuffer, (byte) 0);

      while (messageBufferIndex < startOfMessage.length)
      {
         b = readByte(istream);

         if (startOfMessage[messageBufferIndex] == b)
         {
            messageBuffer[messageBufferIndex++] = b;
         }
         else
         {
            messageBufferIndex = 0;
         }
      }

      //
      // Found a message, scan for the next tag.

      while ((b = readByte(istream)) != '\1')
      {
         messageBuffer[messageBufferIndex++] = b;
      }

      messageBuffer[messageBufferIndex] = '\0';
      String protocol = new String(messageBuffer, 0, messageBufferIndex).split("=")[1];
      messageBuffer[messageBufferIndex++] = '\1';

      b = readByte(istream);

      if (b != '9')
      {
         throw new MalformedMessageException("Tag 9 does not follow tag 8");
      }

      messageBuffer[messageBufferIndex++] = b;
      messageBuffer[messageBufferIndex++] = (byte) istream.read();

      byte[] messageLengthBuffer = new byte[16];
      int messageLengthBufferOffset = 0;

      while ((b = readByte(istream)) != '\1')
      {
         messageBuffer[messageBufferIndex++] = b;
         messageLengthBuffer[messageLengthBufferOffset++] = b;
      }

      messageLengthBuffer[messageLengthBufferOffset++] = '\1';
      messageBuffer[messageBufferIndex++] = '\1';

      final String s = new String(messageLengthBuffer).trim();
      final int messageLength = Integer.parseInt(s);

      if (messageLength > maxMessageBuffer)
      {
         throw new MalformedMessageException("BodyLength is too big, " + messageLength + " > " + maxMessageBuffer);
      }

      readByte(istream, messageBuffer, messageBufferIndex, messageLength);

      messageBufferIndex += messageLength;

      /*
       * for (int i = 0; i < messageLength; i++) {
       * messageBuffer[messageBufferIndex++] = readByte(istream); }
       */

      // Scan over the last tag
      while ((b = readByte(istream)) != '\1')
      {
         messageBuffer[messageBufferIndex++] = b;
      }

      messageBuffer[messageBufferIndex++] = '\1';

      final byte[] rval = new byte[messageBufferIndex];
      System.arraycopy(messageBuffer, 0, rval, 0, messageBufferIndex);

      messageBufferIndex = 0;

      return new QuickFIXMessage(cache, rval, null);
   }

}
