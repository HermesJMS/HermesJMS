/* 
 * Copyright 2003,2004 Colin Crist
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

package hermes.impl;

import hermes.Domain;
import hermes.HermesException;
import hermes.MessageFactory;
import hermes.SystemProperties;
import hermes.util.JMSUtils;
import hermes.xml.Content;
import hermes.xml.Entry;
import hermes.xml.MessageSet;
import hermes.xml.ObjectFactory;
import hermes.xml.Property;
import hermes.xml.XMLBytesMessage;
import hermes.xml.XMLMapMessage;
import hermes.xml.XMLMessage;
import hermes.xml.XMLObjectMessage;
import hermes.xml.XMLTextMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageEOFException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Category;

import com.sun.tools.xjc.generator.validator.StringOutputStream;
import com.sun.xml.bind.StringInputStream;

/**
 * Generic XML helper methods that are non-JMS specific.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: DefaultXMLHelper.java,v 1.23 2006/01/14 12:59:12 colincrist Exp $
 */

public class DefaultXMLHelper implements XMLHelper
{
   private static final Category cat = Category.getInstance(DefaultXMLHelper.class);
   private static final int XML_TEXT_MESSAGE = 1;
   private static final int XML_BYTES_MESSGAE = 2;
   private static final int XML_OBJECT_MESSAGE = 3;
   private static final int XML_MAP_MESSAGE = 4;
   private static final String BASE64_CODEC = "Base64";
   private final ThreadLocal base64EncoderTL = new ThreadLocal();

   private ObjectFactory factory = new ObjectFactory();

   public ObjectFactory getFactory()
   {
      return factory;
   }

   public Base64 getBase64()
   {
      if (base64EncoderTL.get() == null)
      {
         base64EncoderTL.set(new Base64());
      }

      return (Base64) base64EncoderTL.get();
   }

   public Content readContent(InputStream istream) throws Exception
   {
      JAXBContext jc = JAXBContext.newInstance("hermes.xml");
      Unmarshaller u = jc.createUnmarshaller();
      Content node = (Content) u.unmarshal(istream);

      return node;
   }

   public void saveContent(Content content, OutputStream ostream) throws Exception
   {
      JAXBContext jc = JAXBContext.newInstance("hermes.xml");
      Marshaller m = jc.createMarshaller();

      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      m.marshal(content, ostream);
      ostream.flush();
   }

   public void toXML(Message message, OutputStream ostream) throws JMSException, IOException
   {
      final Collection<Message> c = new HashSet<Message>();
      c.add(message);

      toXML(c, ostream);
   }

   public String toXML(Message message) throws JMSException
   {
      final Collection<Message> c = new HashSet<Message>();
      c.add(message);

      return toXML(c);
   }

   public Collection fromXML(MessageFactory hermes, InputStream istream) throws JMSException
   {
      try
      {
         return fromMessageSet(hermes, readContent(istream));
      }
      catch (Exception ex)
      {
         cat.error(ex.getMessage(), ex);

         throw new HermesException(ex);
      }
   }

   public Collection fromXML(MessageFactory hermes, String document) throws JMSException
   {
      try
      {
         return fromMessageSet(hermes, readContent(new StringInputStream(document)));
      }
      catch (Exception ex)
      {
         cat.error(ex.getMessage(), ex);

         throw new HermesException(ex);
      }
   }

   public void toXML(Collection messages, OutputStream ostream) throws JMSException, IOException
   {
      try
      {
         Content node = (Content) factory.createContent();
         MessageSet messageSet = toMessageSet(messages);

         node.getEntry().addAll(toMessageSet(messages).getEntry());
         saveContent(node, ostream);
      }
      catch (Exception ex)
      {
         cat.error(ex.getMessage(), ex);

         throw new HermesException(ex);
      }
   }

   /**
    * @@TODO
    * @param hermes
    * @param messages
    * @param ostream
    * @throws JMSException
    * @throws IOException
    */
   public void streamXML(Collection messages, OutputStream ostream) throws JMSException, IOException
   {
      try
      {
         Content node = (Content) factory.createContent();
         MessageSet messageSet = toMessageSet(messages);

         node.getEntry().addAll(toMessageSet(messages).getEntry());
         saveContent(node, ostream);
      }
      catch (Exception ex)
      {
         cat.error(ex.getMessage(), ex);

         throw new HermesException(ex);
      }
   }

   public String toXML(Collection messages) throws JMSException
   {
      try
      {
         Content node = (Content) factory.createContent();
         StringWriter writer = new StringWriter();
         MessageSet messageSet = toMessageSet(messages);

         node.getEntry().addAll(toMessageSet(messages).getEntry());
         saveContent(node, new StringOutputStream(writer));

         return writer.getBuffer().toString();
      }
      catch (Exception ex)
      {
         cat.error(ex.getMessage(), ex);

         throw new HermesException(ex);
      }
   }

   public Collection fromMessageSet(MessageFactory hermes, MessageSet messageSet) throws JMSException, IOException, ClassNotFoundException, DecoderException
   {
      Collection rval = new ArrayList();

      for (Iterator iter = messageSet.getEntry().iterator(); iter.hasNext();)
      {
         Entry entry = (Entry) iter.next();
         Message jmsMessage = null;

         switch (entry.getType())
         {
            case XML_TEXT_MESSAGE:
               jmsMessage = createMessage(hermes, entry.getTextMessage());
               break;

            case XML_MAP_MESSAGE:
               jmsMessage = createMessage(hermes, entry.getMapMessage());
               break;

            case XML_BYTES_MESSGAE:
               jmsMessage = createMessage(hermes, entry.getBytesMessage());
               break;
            case XML_OBJECT_MESSAGE:
               jmsMessage = createMessage(hermes, entry.getObjectMessage());
               break;

         }

         if (jmsMessage != null)
         {
            rval.add(jmsMessage);
         }
      }

      return rval;
   }

   public MessageSet toMessageSet(Collection messages) throws JMSException
   {
      try
      {
         MessageSet messageSet = factory.createMessageSet();

         for (Iterator iter = messages.iterator(); iter.hasNext();)
         {
            Message jmsMessage = (Message) iter.next();
            Entry entry = factory.createEntry();
            XMLMessage xmlMessage = createXMLMessage(factory, jmsMessage);

            if (xmlMessage instanceof XMLTextMessage)
            {
               entry.setType(XML_TEXT_MESSAGE);
               entry.setTextMessage((XMLTextMessage) xmlMessage);

            }
            else if (xmlMessage instanceof XMLMapMessage)
            {
               entry.setType(XML_MAP_MESSAGE);
               entry.setMapMessage((XMLMapMessage) xmlMessage);

            }
            else if (xmlMessage instanceof XMLObjectMessage)
            {
               entry.setType(XML_OBJECT_MESSAGE);
               entry.setObjectMessage((XMLObjectMessage) xmlMessage);
            }
            else if (xmlMessage instanceof XMLBytesMessage)
            {
               entry.setType(XML_BYTES_MESSGAE);
               entry.setBytesMessage((XMLBytesMessage) xmlMessage);

            }

            messageSet.getEntry().add(entry);
         }

         return messageSet;
      }
      catch (Exception ex)
      {
         cat.error(ex.getMessage(), ex);
         throw new HermesException(ex);
      }
   }

   public Message createMessage(MessageFactory hermes, XMLMessage message) throws JMSException, IOException, ClassNotFoundException, DecoderException
   {
      try
      {
         Message rval = hermes.createMessage();

         if (message instanceof XMLTextMessage)
         {
            rval = hermes.createTextMessage();

            XMLTextMessage textMessage = (XMLTextMessage) message;
            TextMessage textRval = (TextMessage) rval;

            if (BASE64_CODEC.equals(textMessage.getCodec()))
            {
               byte[] bytes = getBase64().decode(textMessage.getText().getBytes());
               textRval.setText(new String(bytes, "ASCII"));
            }
            else
            {
               textRval.setText(textMessage.getText());
            }
         }
         else if (message instanceof XMLMapMessage)
         {
            rval = hermes.createMapMessage();

            XMLMapMessage mapMessage = (XMLMapMessage) message;
            MapMessage mapRval = (MapMessage) rval;

            for (Iterator iter = mapMessage.getBodyProperty().iterator(); iter.hasNext();)
            {
               final Property property = (Property) iter.next();

               if (property.getValue() == null)
               {
                  mapRval.setObject(property.getName(), null);
               }
               else if (property.getType().equals(String.class.getName()))
               {
                  mapRval.setString(property.getName(), property.getValue());
               }
               else if (property.getType().equals(Long.class.getName()))
               {
                  mapRval.setLong(property.getName(), Long.parseLong(property.getValue()));
               }
               else if (property.getType().equals(Double.class.getName()))
               {
                  mapRval.setDouble(property.getName(), Double.parseDouble(property.getValue()));
               }
               else if (property.getType().equals(Boolean.class.getName()))
               {
                  mapRval.setBoolean(property.getName(), Boolean.getBoolean(property.getValue()));
               }
               else if (property.getType().equals(Short.class.getName()))
               {
                  mapRval.setShort(property.getName(), Short.parseShort(property.getValue()));
               }
               else if (property.getType().equals(Integer.class.getName()))
               {
                  mapRval.setInt(property.getName(), Integer.parseInt(property.getValue()));
               }
            }
         }
         else if (message instanceof XMLBytesMessage)
         {
            rval = hermes.createBytesMessage();

            XMLBytesMessage bytesMessage = (XMLBytesMessage) message;
            BytesMessage bytesRval = (BytesMessage) rval;

            bytesRval.writeBytes(getBase64().decode(bytesMessage.getBytes().getBytes()));
         }
         else if (message instanceof XMLObjectMessage)
         {
            rval = hermes.createObjectMessage();

            XMLObjectMessage objectMessage = (XMLObjectMessage) message;
            ObjectMessage objectRval = (ObjectMessage) rval;
            ByteArrayInputStream bistream = new ByteArrayInputStream(getBase64().decode(objectMessage.getObject().getBytes()));

            ObjectInputStream oistream = new ObjectInputStream(bistream);

            objectRval.setObject((Serializable) oistream.readObject());
         }

         //
         // JMS Header properties

         try
         {
            rval.setJMSDeliveryMode(message.getJMSDeliveryMode());
         }
         catch (JMSException ex)
         {
            cat.error("unable to set JMSDeliveryMode to " + message.getJMSDeliveryMode() + ": " + ex.getMessage());
         }

         try
         {
            rval.setJMSMessageID(message.getJMSMessageID());
         }
         catch (JMSException ex)
         {
            cat.error("unable to set JMSMessageID: " + ex.getMessage(), ex);
         }

         rval.setJMSExpiration(message.getJMSExpiration());
         rval.setJMSPriority(message.getJMSPriority());

         try
         {
            rval.setJMSTimestamp(message.getJMSTimestamp());
         }
         catch (JMSException ex)
         {
            cat.error("unable to set JMSTimestamp:" + ex.getMessage(), ex);
         }

         if (message.getJMSCorrelationID() != null)
         {
            rval.setJMSCorrelationID(message.getJMSCorrelationID());
         }

         if (message.getJMSReplyTo() != null && !message.getJMSReplyTo().equals("null"))
         {
            rval.setJMSReplyTo(hermes.getDestination(message.getJMSReplyTo(), Domain.getDomain(message.getJMSReplyToDomain())));
         }

         if (message.getJMSType() != null)
         {
            rval.setJMSType(message.getJMSType());
         }

         for (Iterator iter = message.getHeaderProperty().iterator(); iter.hasNext();)
         {
            Property property = (Property) iter.next();

            if (property.getValue() == null)
            {
               rval.setObjectProperty(property.getName(), null);
            }
            else if (property.getType().equals(String.class.getName()))
            {
               rval.setStringProperty(property.getName(), property.getValue());
            }
            else if (property.getType().equals(Long.class.getName()))
            {
               rval.setLongProperty(property.getName(), Long.parseLong(property.getValue()));
            }
            else if (property.getType().equals(Double.class.getName()))
            {
               rval.setDoubleProperty(property.getName(), Double.parseDouble(property.getValue()));
            }
            else if (property.getType().equals(Boolean.class.getName()))
            {
               rval.setBooleanProperty(property.getName(), Boolean.getBoolean(property.getValue()));
            }
            else if (property.getType().equals(Short.class.getName()))
            {
               rval.setShortProperty(property.getName(), Short.parseShort(property.getValue()));
            }
            else if (property.getType().equals(Integer.class.getName()))
            {
               rval.setIntProperty(property.getName(), Integer.parseInt(property.getValue()));
            }
         }

         return rval;
      }
      catch (NamingException e)
      {
         throw new HermesException(e);
      }

   }

   public XMLMessage createXMLMessage(ObjectFactory factory, Message message) throws JMSException, IOException, EncoderException
   {
      try
      {
         XMLMessage rval = factory.createXMLMessage();

         if (message instanceof TextMessage)
         {
            rval = factory.createXMLTextMessage();

            XMLTextMessage textRval = (XMLTextMessage) rval;
            TextMessage textMessage = (TextMessage) message;

            if (System.getProperty(SystemProperties.BASE64_ENCODE_TEXT_MESSAGE) != null)
            {
               byte[] bytes = getBase64().encode(textMessage.getText().getBytes());
               textRval.setText(new String(bytes, "ASCII"));
               textRval.setCodec(BASE64_CODEC);
            }
            else
            {
               textRval.setText(textMessage.getText());
            }
         }
         else if (message instanceof MapMessage)
         {
            rval = factory.createXMLMapMessage();

            XMLMapMessage mapRval = (XMLMapMessage) rval;
            MapMessage mapMessage = (MapMessage) message;

            for (Enumeration iter = mapMessage.getMapNames(); iter.hasMoreElements();)
            {
               String propertyName = (String) iter.nextElement();
               Object propertyValue = mapMessage.getObject(propertyName);
               Property xmlProperty = factory.createProperty();

               if (propertyValue != null)
               {
                  xmlProperty.setValue(propertyValue.toString());
                  xmlProperty.setType(propertyValue.getClass().getName());
               }
               xmlProperty.setName(propertyName);

               mapRval.getBodyProperty().add(xmlProperty);
            }
         }
         else if (message instanceof BytesMessage)
         {
            rval = factory.createXMLBytesMessage();

            XMLBytesMessage bytesRval = (XMLBytesMessage) rval;
            BytesMessage bytesMessage = (BytesMessage) message;
            ByteArrayOutputStream bosream = new ByteArrayOutputStream();

            bytesMessage.reset();

            try
            {
               for (;;)
               {
                  bosream.write(bytesMessage.readByte());
               }
            }
            catch (MessageEOFException ex)
            {
               // NOP
            }

            bytesRval.setBytes(new String(getBase64().encode(bosream.toByteArray())));
         }
         else if (message instanceof ObjectMessage)
         {
            rval = factory.createXMLObjectMessage();

            XMLObjectMessage objectRval = (XMLObjectMessage) rval;
            ObjectMessage objectMessage = (ObjectMessage) message;

            ByteArrayOutputStream bostream = new ByteArrayOutputStream();
            ObjectOutputStream oostream = new ObjectOutputStream(bostream);

            oostream.writeObject(objectMessage.getObject());
            oostream.flush();
            byte b[] = getBase64().encode(bostream.toByteArray());
            String s = new String(b, "ASCII");
            objectRval.setObject(s);
         }

         if (message.getJMSReplyTo() != null)
         {
            rval.setJMSReplyTo(JMSUtils.getDestinationName(message.getJMSReplyTo()));
            rval.setJMSReplyToDomain(Domain.getDomain(message.getJMSReplyTo()).getId());
         }

         rval.setJMSDeliveryMode(message.getJMSDeliveryMode());
         rval.setJMSExpiration(message.getJMSExpiration());
         rval.setJMSMessageID(message.getJMSMessageID());
         rval.setJMSPriority(message.getJMSPriority());
         rval.setJMSRedelivered(message.getJMSRedelivered());
         rval.setJMSTimestamp(message.getJMSTimestamp());
         rval.setJMSType(message.getJMSType());
         rval.setJMSCorrelationID(message.getJMSCorrelationID());

         for (final Enumeration iter = message.getPropertyNames(); iter.hasMoreElements();)
         {
            String propertyName = (String) iter.nextElement();

            if (!propertyName.startsWith("JMS"))
            {
               Object propertyValue = message.getObjectProperty(propertyName);
               Property property = factory.createProperty();

               property.setName(propertyName);

               if (propertyValue != null)
               {
                  property.setValue(propertyValue.toString());
                  property.setType(propertyValue.getClass().getName());
               }

               rval.getHeaderProperty().add(property);
            }
         }

         return rval;
      }
      catch (JAXBException ex)
      {
         throw new HermesException(ex);
      }
   }

}