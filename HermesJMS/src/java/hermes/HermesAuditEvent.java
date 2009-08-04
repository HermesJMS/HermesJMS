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

package hermes;

import java.io.File;
import java.util.Date;

/**
 * An event used for audit, it described a get or a put to/from a queue or topic.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HermesAuditEvent.java,v 1.3 2005/07/08 19:43:25 colincrist Exp $
 */
public class HermesAuditEvent
{
    public static final int READ = 1;
    public static final int WRITE = 2;

    private String hermesId;
    private String destinationName;
    private int operation;
    private Date date;
    private File file;

    public HermesAuditEvent(String hermesId, String destinationName, Date date, int operation, File file)
    {
        this.hermesId = hermesId;
        this.destinationName = destinationName;
        this.date = date;
        this.operation = operation;
        this.file = file;
    }

    /**
     * @return Returns the date.
     */
    public Date getDate()
    {
        return date;
    }

    /**
     * @return Returns the destinationName.
     */
    public String getDestinationName()
    {
        return destinationName;
    }

    /**
     * @return Returns the file.
     */
    public File getFile()
    {
        return file;
    }

    /**
     * @return Returns the hermesId.
     */
    public String getHermesId()
    {
        return hermesId;
    }

    /**
     * @return Returns the operation.
     */
    public int getOperation()
    {
        return operation;
    }

}