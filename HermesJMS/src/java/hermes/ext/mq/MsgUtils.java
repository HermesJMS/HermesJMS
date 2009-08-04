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

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.ibm.mq.MQMessage;

/**
 * @author colincrist@hermesjms.com
 */
public class MsgUtils
{
    public static class MqRfh
    {
       public String strucId;
       public long version;
       public long length;
       public long encoding;
       public long codedCharSetId;
       public String format;
       public long flags;
       public Map nameValues;
    }

    public static class MqRfh2 extends MqRfh
    {
       public long nameValuesCharSetId;
       public long nameValuesLength;
    }

    public static MqRfh getRfhHeader(MQMessage message) throws EOFException, IOException
    {
       MqRfh ret = new MqRfh();
       int c = readRfhCommon(message, ret);

       String strNameValues = message.readString((int)ret.length - c);

       ret.nameValues = getNameValues("RFH.", strNameValues);

       return ret;
    }

    public static MqRfh2 getRfh2Header(MQMessage message) throws EOFException, IOException
    {
       MqRfh2 ret = new MqRfh2();
       int c = readRfhCommon(message, ret);

       ret.nameValuesCharSetId = message.readInt4(); c += 4;

       Map maps = new HashMap();
       
       do
       {
          ret.nameValuesLength = message.readInt4(); c += 4;
          String strNameValues = message.readString((int)ret.nameValuesLength); c += ret.nameValuesLength;
          Map nameValues = getNameValues("RFH2.", strNameValues);
          maps.putAll(nameValues);
       }
       while (c < ret.length);

       ret.nameValues = maps;

       return ret;
    }

    private static int readRfhCommon(MQMessage message, MqRfh ret) throws EOFException, IOException
    {
       int c = 0;

       ret.strucId = message.readString(4); c += 4;
       ret.version = message.readInt4(); c += 4;
       ret.length = message.readInt4(); c += 4;
       ret.encoding = message.readInt4(); c += 4;
       ret.codedCharSetId = message.readInt4(); c += 4;
       ret.format = message.readString(8); c += 8;
       ret.flags = message.readInt4(); c += 4;

       return c;
    }

    private static Map getNameValues(String prefix, String s)
    {
       Map ret = new HashMap();

       // TO DO: string can contain spaces within double qoutes
       
       for (StringTokenizer t = new StringTokenizer(s, " ");
            t.hasMoreTokens(); )
       {
          String name = t.nextToken();
          String value = null;

          if (t.hasMoreTokens())
             value = t.nextToken();

          ret.put(prefix + name, value);
       }

       return ret;
    }
}
