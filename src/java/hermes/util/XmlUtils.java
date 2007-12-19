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
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utilities for pretty printing XML (uses Apache Xerces).
 */
public abstract class XmlUtils
{
   private static final Logger log = Logger.getLogger(XmlUtils.class);

   public static String prettyPrintXml(byte[] bytes)
   {
      String r = new String(bytes);
      String s = null;

      try
      {
         s = XmlUtils.prettyPrintXml(r);
      }
      catch (IOException e1)
      {
         log.error(e1.getMessage(), e1);
         s = DumpUtils.dumpBinary(bytes, DumpUtils.DUMP_AS_STRING);
      }
      catch (ParserConfigurationException e1)
      {
         log.error(e1.getMessage(), e1);
         s = DumpUtils.dumpBinary(bytes, DumpUtils.DUMP_AS_STRING);
      }
      catch (SAXException e1)
      {
         log.error(e1.getMessage(), e1);
         s = DumpUtils.dumpBinary(bytes, DumpUtils.DUMP_AS_STRING);
      }

      return s;
   }

   public static String prettyPrintXml(String s) throws IOException, ParserConfigurationException, SAXException
   {

      if (isXML(s))
      {
         StringReader reader = null;
         InputSource source = null;
         String ret = null;

         try
         {
            reader = new StringReader(s);
            source = new InputSource(reader);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(source);

            ret = prettyPrintXml(doc);
         }
         finally
         {
            IoUtils.closeQuietly(reader);
         }

         return ret;
      }
      else
      {
         return s;
      }
   }

   public static String prettyPrintXml(Document doc) throws IOException
   {
      StringWriter writer = null;
      String ret = null;

      try
      {
         writer = new StringWriter();
         OutputFormat format = new OutputFormat(doc);
         format.setLineWidth(80);
         format.setIndenting(true);
         format.setIndent(2);
         XMLSerializer serializer = new XMLSerializer(writer, format);
         serializer.serialize(doc);

         ret = writer.toString();
      }
      finally
      {
         IoUtils.closeQuietly(writer);
      }

      return ret;
   }

   public static boolean isXML(final String s)
   {
      return s != null && s.startsWith("<?xml");
   }
}
