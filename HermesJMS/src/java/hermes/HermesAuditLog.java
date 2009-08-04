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

import java.util.Collection;

import javax.jms.Destination;
import javax.jms.Message;

/**
 * Interface describing a sink for audit events.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: HermesAuditLog.java,v 1.4 2005/07/08 19:43:25 colincrist Exp $
 */
public interface HermesAuditLog
{
    public void commit();

    public void rollback();

    public void onRead(Destination from, Collection messages);

    public void onWrite(Destination to, Collection messages);

    public void onRead(Destination from, Message message);

    public void onWrite(Destination to, Message message);

    public void setListener(HermesAuditListener auditListener);

}