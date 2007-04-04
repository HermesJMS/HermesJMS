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

package hermes.ext.mq;

import hermes.browser.ConfigDialogProxy;
import hermes.browser.MessageRenderer;
import hermes.browser.model.OneRowMapTableModel;
import hermes.util.TextUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import com.ibm.mq.MQC;
import com.ibm.mq.MQMessage;
import com.jidesoft.grid.SortableTable;

/**
 * @author colincrist@hermesjms.com
 */
public class MQSeriesMessageRenderer implements MessageRenderer
{
   private static final Logger log = Logger.getLogger(MQSeriesMessageRenderer.class);

   public static class MyConfig implements Config
   {

      public String getName()
      {
         return "RFH";
      }

      public void setName(String name)
      {

      }

      public String getPropertyDescription(String propertyName)
      {
         return propertyName;
      }
   }
   private MQSeriesAdmin admin;

   public MQSeriesMessageRenderer(MQSeriesAdmin admin)
   {
      this.admin = admin;
   }

   public Config createConfig()
   {
      return new MyConfig();
   }

   public JComponent getConfigPanel(ConfigDialogProxy dialogProxy) throws Exception
   {
      return null;
   }

   public JComponent render(final Message message)
   {
      JComponent rval = null;

      try
      {
         MQMessage mqMessage = admin.getMQMessage(message);

         final Map map = new LinkedHashMap();

         try
         {
            inspectMessage(map, mqMessage);
            final OneRowMapTableModel model = new OneRowMapTableModel(map);

            rval = new SortableTable(model);
         }
         catch (IOException e)
         {
            log.error(e.getMessage(), e);
         }
      }
      catch (JMSException e)
      {
         log.error(e.getMessage(), e);
      }
      return rval;
   }

   public synchronized Config getConfig()
   {
      return null ;
   }
   
   public void setConfig(Config config)
   {
      // TODO Auto-generated method stub
   }

   public void inspectMessage(Map map, MQMessage message) throws IOException
   {
      map.put("AccountingToken", TextUtils.toHexString(message.accountingToken, false));
      map.put("ApplicationIdData", message.applicationIdData);
      map.put("ApplicationOriginData", message.applicationOriginData);
      map.put("BackoutCount", new Integer(message.backoutCount));
      map.put("CharacterSet", new Integer(message.characterSet));
      map.put("Encoding", new Integer(message.encoding));
      map.put("Expiry", new Integer(message.expiry));
      map.put("Feedback", new Integer(message.feedback));
      map.put("Format", message.format);
      map.put("DataLength", new Integer(message.getDataLength()));
      map.put("DataOffset", new Integer(message.getDataOffset()));
      map.put("TotalMessageLength", new Integer(message.getTotalMessageLength()));
      map.put("Version", new Integer(message.getVersion()));
      map.put("GroupId", TextUtils.toHexString(message.groupId, false));
      map.put("MessageFlags", new Integer(message.messageFlags));
      map.put("MessageId", TextUtils.toHexString(message.messageId, false));
      map.put("MessageSequenceNumber", new Integer(message.messageSequenceNumber));
      map.put("MessageType", new Integer(message.messageType));
      map.put("Offset", new Integer(message.offset));
      map.put("OriginalLength", new Integer(message.originalLength));
      map.put("Persistence", new Integer(message.persistence));
      map.put("PutApplicationName", message.putApplicationName);
      map.put("PutApplicationType", new Integer(message.putApplicationType));
      map.put("PutDateTime", message.putDateTime.getTime());
      map.put("ReplyToQueueManager", message.replyToQueueManagerName);
      map.put("ReplyToQueueName", message.replyToQueueName);
      map.put("Report", new Integer(message.report));
      map.put("TotalMessageLength", new Integer(message.getTotalMessageLength()));

      int dataLength = message.getTotalMessageLength();

      if (message.format.equals(MQC.MQFMT_RF_HEADER_1))
      {
         MsgUtils.MqRfh header = MsgUtils.getRfhHeader(message);

         inspectHeader(map, header);

         dataLength = message.getDataLength();
      }
      else if (message.format.equals(MQC.MQFMT_RF_HEADER_2))
      {
         MsgUtils.MqRfh2 header = MsgUtils.getRfh2Header(message);

         inspectHeader(map, header);

         dataLength = message.getDataLength();
      }
   }

   private void inspectHeader(Map map, MsgUtils.MqRfh header)
   {
      map.put("RFH.StructId", header.strucId);
      map.put("RFH.Version", new Long(header.version));
      map.put("RFH.Length", new Long(header.length));
      map.put("RFH.Encoding", new Long(header.encoding));
      map.put("RFH.EncodedCharSetId", new Long(header.codedCharSetId));
      map.put("RFH.Format", header.format);
      map.put("RFH.Flags", new Long(header.flags));

      if (header instanceof MsgUtils.MqRfh2)
      {
         MsgUtils.MqRfh2 header2 = (MsgUtils.MqRfh2) header;

         map.put("RFH2.NamesValuesCharSetId", new Long(header2.nameValuesCharSetId));
         map.put("RFH2.NamesValuesLength", new Long(header2.nameValuesLength));
      }

      for (Iterator iter = header.nameValues.entrySet().iterator(); iter.hasNext();)
      {
         Map.Entry entry = (Map.Entry) iter.next();

         map.put(entry.getKey().toString(), entry.getValue());
      }

   }

   public boolean canRender(Message message)
   {
     
      return false;
   }
   
   public String getDisplayName() 
   {
      return "RFH2" ;
   }
}
