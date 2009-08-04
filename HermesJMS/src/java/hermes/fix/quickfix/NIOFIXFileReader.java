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

import hermes.HermesRuntimeException;
import hermes.fix.FIXException;
import hermes.fix.FIXMessage;
import hermes.fix.FIXMessageFilter;
import hermes.fix.FIXReader;
import hermes.fix.MalformedMessageException;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: NIOFIXFileReader.java,v 1.5 2006/08/25 11:33:48 colincrist Exp $
 */

public class NIOFIXFileReader implements FIXReader, Runnable
{
   private static final Logger log = Logger.getLogger(NIOFIXFileReader.class);
   private static byte[] startOfMessage = new byte[] { '8', '=', 'F', 'I', 'X' };
   private FileInputStream istream;
   private MappedByteBuffer parseBuffer;
   private MappedByteBuffer readBuffer;
   private Object lock = new Object();
   private int position = 0;
   private int mappedStart;

   private BlockingQueue<FIXMessage> messages = new ArrayBlockingQueue<FIXMessage>(8192);

   private QuickFIXMessageCache messageCache;

 private FIXMessageFilter filter = new FIXMessageFilter() ;
   
   public FIXMessageFilter getFilter()
   {
      return filter ;
   }
   
   public NIOFIXFileReader(QuickFIXMessageCache messageCache, FileInputStream istream) throws IOException
   {
      this.istream = istream;
      this.messageCache = messageCache;

      map();

      new Thread(this).start();
   }

   public FIXMessage read() throws IOException
   {
      return read(-1);
   }

   public FIXMessage read(final long timeout)
   {
      try
      {
         FIXMessage rval = messages.poll(100, TimeUnit.MILLISECONDS);

         while (rval == null && istream.getChannel().isOpen())
         {
            rval = messages.poll(100, TimeUnit.MILLISECONDS);
         }

         return rval;
      }
      catch (Exception ex)
      {
         log.error(ex.getMessage(), ex);
         return null;
      }
   }

   public byte[] getBytes(int offset, int length)
   {
      synchronized (lock)
      {
         byte[] bytes = new byte[length];
         readBuffer.position(offset);
         readBuffer.get(bytes);
         return bytes;
      }
   }

   private void waitAndRemap() throws InterruptedException, IOException
   {
      Thread.sleep(500);

      map();
   }

   public void run()
   {
      try
      {
         while (istream.getChannel().isOpen())
         {
            try
            {

               final FIXMessage m = readMessage();

               if (m != null && filter.filter(m.getMsgType()))
               {
                  messages.put(m);
               }

            }
            catch (BufferUnderflowException ex)
            {
               waitAndRemap();
            }
            catch (IllegalArgumentException ex)
            {
               waitAndRemap();
            }
         }

         log.debug("channel closed");
      }
      catch (Throwable ex)
      {
         log.error(ex.getMessage(), ex);
      }
   }

   protected FIXMessage readMessage() throws MalformedMessageException, FIXException, BufferUnderflowException, InterruptedException, IOException
   {
      parseBuffer.position(position);

      byte b;
      int startOfMessageIndex = 0;

      while (startOfMessageIndex < startOfMessage.length)
      {
         b = parseBuffer.get();

         if (startOfMessage[startOfMessageIndex] == b)
         {
            startOfMessageIndex++;
         }
         else
         {
            startOfMessageIndex = 0;
         }
      }

      int startOfMessageOffset = parseBuffer.position() - startOfMessage.length;

      //
      // Found a message, scan for the next tag.

      byte[] protocolAsBytes = new byte[12];
      int protocolAsBytesIndex = 0;

      while ((b = parseBuffer.get()) != '\1')
      {
         protocolAsBytes[protocolAsBytesIndex++] = b;
      }

      protocolAsBytes[protocolAsBytesIndex++] = '\0';

      String protocol = "FIX" + new String(protocolAsBytes).trim();

      b = parseBuffer.get();

      if (b != '9')
      {
         position = parseBuffer.position();

         throw new MalformedMessageException("Tag 9 does not follow tag 8");
      }

      parseBuffer.get();

      byte[] messageLengthBuffer = new byte[16];
      int messageLengthBufferOffset = 0;

      while ((b = parseBuffer.get()) != '\1')
      {
         messageLengthBuffer[messageLengthBufferOffset++] = b;
      }

      messageLengthBuffer[messageLengthBufferOffset++] = '\1';

      final String s = new String(messageLengthBuffer).trim();
      final int fixLength = Integer.parseInt(s);

      parseBuffer.position(parseBuffer.position() + fixLength);

      // Scan over the last tag

      while ((b = parseBuffer.get()) != '\1')
      {

      }

      final int messageLength = parseBuffer.position() - startOfMessageOffset;
      position = parseBuffer.position();

      return new NIOQuickFIXMessage(messageCache, this, mappedStart + startOfMessageOffset, messageLength, QuickFIXUtils.getDictionary(protocol));

   }

   private void map() throws IOException
   {
      synchronized (lock)
      {
         FileChannel channel = istream.getChannel();

         if (channel.isOpen())
         {
            if (parseBuffer == null || channel.size() > mappedStart)
            {
               if (parseBuffer != null)
               {
                  clean(parseBuffer);
               }

               if (readBuffer != null)
               {
                  clean(readBuffer);
               }

               // log.debug("mapping in FIX file, mappedStart=" + mappedStart +
               // " channel.size()=" + channel.size());

               mappedStart += position;

               parseBuffer = channel.map(FileChannel.MapMode.READ_ONLY, mappedStart, channel.size() - mappedStart);
               readBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

               position = 0;
            }
         }
      }
   }

   private final void clean(final MappedByteBuffer buffer)
   {    
      AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            try
            {
               Method getCleanerMethod = buffer.getClass().getMethod("cleaner", new Class[0]);
               getCleanerMethod.setAccessible(true);
               sun.misc.Cleaner cleaner = (sun.misc.Cleaner) getCleanerMethod.invoke(buffer, new Object[0]);
               cleaner.clean();
            }
            catch (Exception e)
            {
               log.error(e.getMessage(), e);
            }
            return null;
         }
      });
   }

   public void release()
   {
      synchronized (lock)
      {
         if (readBuffer != null)
         {
            log.debug("releasing read memory map") ;
            
            clean(readBuffer);
            readBuffer = null;
         }
      }
   }

   @Override
   protected void finalize() throws Throwable
   {
      release();
   }

   public void close()
   {
      synchronized (lock)
      {
         try
         {
            if (istream != null)
            {

               istream.getChannel().close();
               istream.close();

               if (parseBuffer != null)
               {
                  log.debug("releasing parse memory map") ;
                  
                  clean(parseBuffer);                  
               }

               parseBuffer = null;
            }
         }
         catch (IOException ex)
         {
            throw new HermesRuntimeException(ex);
         }
      }
   }
}
