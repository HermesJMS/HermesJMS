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

/**
 * Utilities for displaying byte[]s in lines with hex offsets.
 */
public class DumpUtils
{
    public static final int DUMP_AS_HEX = 1;
    public static final int DUMP_AS_ALPHA = 2;
    public static final int DUMP_AS_STRING = 3;
    public static final int DUMP_AS_HEX_AND_ALPHA = 4;
	public static final int ROW_LEN = 16;

    public static String dumpBinary(byte[] data, int mode)
    {
        return mode == DUMP_AS_STRING ? TextUtils.toAsciiString(data) : dumpBinaryEx(data, mode);
    }

    private static String dumpBinaryEx(byte[] data, int mode)
    {
        StringBuffer b = new StringBuffer();

        int c = 0;

        if (data != null)
        {
            while (c < data.length)
            {
                int max = Math.min(ROW_LEN, data.length - c);

                byte[] row = new byte[max];
                for (int i = 0; i < row.length; i++)
                    row[i] = data[c + i];

                switch (mode)
                {
                    case DUMP_AS_ALPHA:
                        b.append(dumpBinaryLineAsAlpha(c, row));
                        break;
                    case DUMP_AS_HEX:
                        b.append(dumpBinaryLineAsHex(c, row));
                        break;
                    case DUMP_AS_HEX_AND_ALPHA:
                        b.append(dumpBinaryLineAsHexAndAlpha(c, row));
                        break;
                }
                c += ROW_LEN;
            }
        }
        else
        {
            b.append("No payload.");
        }

        return b.toString();
    }

    public static String dumpBinaryLineAsHexAndAlpha(long offset, byte[] data)
    {
        StringBuffer b = new StringBuffer();

        b.append(TextUtils.leftPadLong(offset, 8));
        b.append(" - ");
        b.append(TextUtils.leftAlign(TextUtils.toHexString(data, true), 
        	ROW_LEN * 3));
        b.append(" - ");
        b.append(TextUtils.toAsciiString(data));
        b.append("\r\n");

        return b.toString();
    }

    public static String dumpBinaryLineAsHex(long offset, byte[] data)
    {
        StringBuffer b = new StringBuffer();

        b.append(TextUtils.leftPadLong(offset, 8));
        b.append(" - ");
        b.append(TextUtils.toHexString(data, true));
        b.append("\r\n");

        return b.toString();
    }

    public static String dumpBinaryLineAsAlpha(long offset, byte[] data)
    {
        StringBuffer b = new StringBuffer();

        b.append(TextUtils.leftPadLong(offset, 8));
        b.append(" - ");
        b.append(TextUtils.toAsciiString(data));
        b.append("\r\n");

        return b.toString();
    }

}