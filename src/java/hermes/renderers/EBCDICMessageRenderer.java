package hermes.renderers;

import hermes.browser.MessageRenderer.Config;
import hermes.renderers.DefaultMessageRenderer.MyConfig;
import hermes.util.DumpUtils;
import hermes.util.MessageUtils;

import java.awt.Font;
import java.io.StringWriter;

import javax.jms.Message;
import javax.swing.JComponent;
import javax.swing.JTextArea;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * A renderer that displays EBCDIC Data in ASCII Format on a JMS message in a
 * text area.
 * 
 * @author sxchapma@yahoo.com
 * @version $Id: EBCDICRenderer.java,v 1.0 2008/10/03 15:59:54
 */

public class EBCDICMessageRenderer extends AbstractMessageRenderer
{
   private static final Logger log = Logger.getLogger(EBCDICMessageRenderer.class);

   private static String MAX_MESSAGE_SIZE = "maxMessageSize";
   private static String MAX_MESSAGE_SIZE_INFO = "Maximum message size";
   private static String UNDISPLAYABLE_CHAR = "." ;
   private static String ROW_LENGTH = "rowLength";
   private static String ROW_LENGTH_INFO = "Row length to display on screen";
   private static String UNDISPLAYABLE_CHAR_INFO = "Character to display when the EBCDIC character cannot be represented in ASCII" ;

   public static final int DUMP_AS_EBCDIC = 5;
   public char[] chart = null;

   public MyConfig config = new MyConfig();

   static
   {
      
   }

   public class MyConfig extends AbstractMessageRenderer.BasicConfig
   {
      private int maxMessageSize = 5 * 1024 * 1024;;
      private int rowLength = 16;
      private boolean active = true;
      private String name = "EBCDIC";
      private String undisplayableChar = "." ;

      @Override
      public String getName()
      {
         return name;
      }

      @Override
      public String getPropertyDescription(String propertyName)
      {
         if (MAX_MESSAGE_SIZE.equals(propertyName))
         {
            return MAX_MESSAGE_SIZE_INFO;
         }

         if (ROW_LENGTH.equals(propertyName))
         {
            return ROW_LENGTH_INFO;
         }
         
         if (UNDISPLAYABLE_CHAR.equals(propertyName))
         {
            return UNDISPLAYABLE_CHAR_INFO ;
         }

         return propertyName;
      }

      public String getUndisplayableChar()
      {
         return undisplayableChar;
      }

      public void setUndisplayableChar(String undisplayableChar)
      {
         this.undisplayableChar = undisplayableChar;
      }

      @Override
      public boolean isActive()
      {
         return active;
      }

      @Override
      public void setActive(boolean active)
      {
         this.active = active;
      }

      @Override
      public void setName(String name)
      {
         this.name = name;
      }

      public int getMaxMessageSize()
      {
         return maxMessageSize;
      }

      public void setMaxMessageSize(int maxMessageSize)
      {
         this.maxMessageSize = maxMessageSize;
      }

      public int getRowLength()
      {
         return rowLength;
      }

      public void setRowLength(int rowLength)
      {
         this.rowLength = rowLength;
      }

   }

   public EBCDICMessageRenderer()
   {
      super();

   }

   public boolean isActive()
   {
      return true;
   }

   @Override
   public Config createConfig()
   {
      return new MyConfig();
   }

   @Override
   public void setConfig(Config config)
   {
      this.config = (MyConfig) config;
      
      updateMappings() ;
      
      super.setConfig(config);
   }

   private void updateMappings()
   {
      chart = new char[255];
      
      for (int i = 0; i < 255; i++)
      {
         chart[i] = config.getUndisplayableChar().charAt(0);
      }
      chart[64] = ' ';
      chart[76] = '<';
      chart[77] = '(';
      chart[78] = '+';
      chart[79] = '|';
      chart[80] = '&';
      chart[90] = '!';
      chart[91] = '$';
      chart[92] = '*';
      chart[93] = ')';
      chart[94] = ';';
      chart[95] = '_';
      chart[96] = '-';
      chart[97] = '/';
      chart[107] = ',';
      chart[108] = '%';
      chart[109] = '_';
      chart[110] = '>';
      chart[111] = '?';
      chart[122] = ':';
      chart[123] = '#';
      chart[124] = '@';
      chart[125] = '`';
      chart[126] = '=';
      chart[127] = '\"';
      chart[129] = 'a';
      chart[130] = 'b';
      chart[131] = 'c';
      chart[132] = 'd';
      chart[133] = 'e';
      chart[134] = 'f';
      chart[135] = 'g';
      chart[136] = 'h';
      chart[137] = 'i';
      chart[145] = 'j';
      chart[146] = 'k';
      chart[147] = 'l';
      chart[148] = 'm';
      chart[149] = 'n';
      chart[150] = 'o';
      chart[151] = 'p';
      chart[152] = 'q';
      chart[153] = 'r';
      chart[162] = 's';
      chart[163] = 't';
      chart[164] = 'u';
      chart[165] = 'v';
      chart[166] = 'w';
      chart[167] = 'x';
      chart[168] = 'y';
      chart[169] = 'z';
      chart[193] = 'A';
      chart[194] = 'B';
      chart[195] = 'C';
      chart[196] = 'D';
      chart[197] = 'E';
      chart[198] = 'F';
      chart[199] = 'G';
      chart[200] = 'H';
      chart[201] = 'I';
      chart[209] = 'J';
      chart[210] = 'K';
      chart[211] = 'L';
      chart[212] = 'M';
      chart[213] = 'N';
      chart[214] = 'O';
      chart[215] = 'P';
      chart[216] = 'Q';
      chart[217] = 'R';
      chart[226] = 'S';
      chart[227] = 'T';
      chart[228] = 'U';
      chart[229] = 'V';
      chart[230] = 'W';
      chart[231] = 'X';
      chart[232] = 'Y';
      chart[233] = 'Z';
      chart[240] = '0';
      chart[241] = '1';
      chart[242] = '2';
      chart[243] = '3';
      chart[244] = '4';
      chart[245] = '5';
      chart[246] = '6';
      chart[247] = '7';
      chart[248] = '8';
      chart[249] = '9';
   }
   public void setActive(boolean inActive)
   {
      super.setActive(inActive);
   }

   public JComponent render(Message m)
   {

      final JTextArea myRender = new JTextArea();

      myRender.setEditable(false);
      myRender.setFont(Font.decode("Monospaced-PLAIN-12"));

      byte[] messagebytes = null;

      try
      {
         messagebytes = MessageUtils.asBytes(m);
      }
      catch (Exception j)
      {
         log.error(j.getMessage(), j) ;
      }

      try
      {
         myRender.setText(dumpBinaryEx(messagebytes, DUMP_AS_EBCDIC, config.getMaxMessageSize()));
         myRender.setCaretPosition(0);
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e) ;
      }

      return myRender;
   }

   private String dumpBinaryEx(byte[] data, int mode, int maxSize)
   {
      StringWriter b = new StringWriter();

      int c = 0;

      if (data != null)
      {
         while (c < data.length)
         {
            int max = Math.min(config.getRowLength(), data.length - c);

            byte[] row = new byte[max];
            for (int i = 0; i < row.length; i++)
               row[i] = data[c + i];

            switch (mode)
            {
               case DUMP_AS_EBCDIC:
                  DumpUtils.dumpBinaryLineAsHexAndEBCDIC(b, c, chart, row, config.getRowLength());
                  break;
            }
            c += config.getRowLength();

            if (c > maxSize)
            {
               b.append("Message too big");
               break;
            }
         }
      }
      else
      {
         b.append("No payload.");
      }

      return b.toString();
   }

   

   /**
    * Any JMS message is readable.
    */
   public boolean canRender(Message message)
   {
      return true;
   }

   public String getDisplayName()
   {
      return "EBCDIC";
   }
}