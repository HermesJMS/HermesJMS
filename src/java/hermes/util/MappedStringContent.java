/* 
 * Copyright 2007 Colin Crist
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

import java.nio.CharBuffer;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class MappedStringContent implements AbstractDocument.Content
{
   private static final Logger log = Logger.getLogger(MappedStringContent.class);
   private CharBuffer chars;

   public class MyPosition implements Position
   {
      private int offset;

      MyPosition(int offset)
      {
         this.offset = offset;
      }

      public int getOffset()
      {
         return offset;
      }
   }

   public MappedStringContent(CharBuffer chars)
   {
      this.chars = chars;
   }

   public Position createPosition(int offset) throws BadLocationException
   {
      CheckLocation(offset);

      return new MyPosition(offset);
   }

   public void getChars(int where, int len, Segment txt) throws BadLocationException
   {
      log.debug("getChars() where=" + where + " len=" + len);
      chars.position(where);
      char[] rval = new char[len];

      chars.get(rval, where, len);
      txt.array = rval;
      txt.count = len;
      txt.offset = 0;      
   }

   private final void CheckLocation(int offset) throws BadLocationException
   {
      if (offset >= chars.limit())
      {
         throw new BadLocationException("Max size is " + chars.limit(), offset);
      }
   }

   public String getString(int where, int len) throws BadLocationException
   {
      CheckLocation(where);

      char[] rval = new char[len];
      chars.get(rval, where, len);
      String string = new String(rval);
      log.debug("getString() rval=" + string);
      return string;
   }

   public UndoableEdit insertString(int where, String str) throws BadLocationException
   {

      return null;
   }

   public int length()
   {
      log.debug("length=" + chars.limit());

      return chars.limit();
   }

   public UndoableEdit remove(int where, int nitems) throws BadLocationException
   {

      return null;
   }

}
