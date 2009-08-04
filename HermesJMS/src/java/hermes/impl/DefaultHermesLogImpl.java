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

import hermes.Hermes;
import hermes.HermesAuditListener;
import hermes.HermesAuditLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Category;

/**
 * Writes messages to disk for audit. A bit srappy in places!
 * 
 * @author colincrist@hermesjms.com
 */
public class DefaultHermesLogImpl implements HermesAuditLog
{
    private static final Category cat = Category.getInstance(DefaultHermesLogImpl.class);
    private static final String SEPARATOR = System.getProperty("file.separator");
    private String baseDirName = ".";
    private Hermes hermes;
    private boolean active = false;
    private HermesAuditListener auditListener;

    private Map readMsgsInTx = new HashMap();
    private Map writeMsgsInTx = new HashMap();
    private Map filesInTx = new HashMap();

    /**
     * @author colincrist@hermesjms.com
     * @version $Id: DefaultHermesLogImpl.java,v 1.2 2004/05/08 15:15:45
     *          colincrist Exp $
     */
    public DefaultHermesLogImpl(Hermes hermes, String baseDirName, boolean create, boolean active) throws IOException
    {
        super();

        this.active = active;
        this.hermes = hermes;

        File baseDir = new File(baseDirName);

        if (baseDir.exists())
        {
            if (baseDir.isDirectory())
            {
                // OK
            }
            else
            {
                throw new IOException("Audit log dir " + baseDirName + " already exists as a file, reverting to '.'");
            }
        }
        else
        {
            if (create)
            {
                baseDir.mkdir();
            }
            else
            {
                throw new IOException("Audit log dir " + baseDirName + " does not exist, reverting to '.'");
            }

        }

        this.baseDirName = baseDirName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.metastuff.hermes.HermesAuditLog#onRead(java.lang.String,
     *      javax.jms.Destination, javax.jms.Message)
     */
    public void onRead(Destination from, Collection messages)
    {
        if (active)
        {
            try
            {
                File file = getAuditFile("read@", from);
                Collection c = (Collection) readMsgsInTx.get(file);

                c.addAll(messages);
            }
            catch (JMSException e)
            {
                cat.error(e.getMessage(), e);
            }
            catch (IOException e)
            {
                cat.error(e.getMessage(), e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.metastuff.hermes.HermesAuditLog#onWrite(java.lang.String,
     *      javax.jms.Destination, javax.jms.Message)
     */
    public void onWrite(Destination from, Collection messages)
    {
        if (active)
        {
            try
            {
                File file = getAuditFile("write@", from);
                Collection c = (Collection) writeMsgsInTx.get(file);

                c.addAll(messages);
            }
            catch (JMSException e)
            {
                cat.error(e.getMessage(), e);
            }
            catch (IOException e)
            {
                cat.error(e.getMessage(), e);
            }
        }
    }

    public File getAuditFile(String prefix, Destination dest) throws IOException, JMSException
    {
        DateFormat dateFormat = DateFormat.getDateInstance();
        Date now = new Date();

        String userDirName = System.getProperty("user.name");
        String dateDirName = dateFormat.format(new Date());
        String hermesDirName = hermes.getId();
        String destDirName = hermes.getDestinationName(dest);
        String fileName = prefix + now.getHours() + "_" + now.getMinutes() + "_" + now.getSeconds() + ".xml";

        destDirName = destDirName.replaceAll("queue:///", ""); // MQ hack.

        String auditFileName = baseDirName + SEPARATOR + userDirName + SEPARATOR + dateDirName + SEPARATOR + hermesDirName + SEPARATOR + destDirName
                + SEPARATOR + fileName;

        File auditFile;

        if (filesInTx.containsKey(auditFileName))
        {
            auditFile = (File) filesInTx.get(auditFileName);
        }
        else
        {
            File userDir = new File(baseDirName + SEPARATOR + userDirName);

            if (!userDir.exists())
            {
                userDir.mkdir();
            }

            File dateDir = new File(userDir.getPath() + SEPARATOR + dateDirName);

            if (!dateDir.exists())
            {
                dateDir.mkdir();
            }

            File hermesDir = new File(dateDir.getPath() + SEPARATOR + hermesDirName);

            if (!hermesDir.exists())
            {
                hermesDir.mkdir();
            }

            File destDir = new File(hermesDir.getPath() + SEPARATOR + destDirName);

            if (!destDir.exists())
            {
                destDir.mkdir();
            }

            auditFile = new File(destDir.getPath() + SEPARATOR + fileName);
            filesInTx.put(auditFileName, auditFile);
            readMsgsInTx.put(auditFile, new ArrayList());
            writeMsgsInTx.put(auditFile, new ArrayList());

            if (!auditFile.exists())
            {
                auditFile.createNewFile();
            }
        }

        return auditFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAuditLog#onRead(hermes.Hermes, javax.jms.Destination,
     *      javax.jms.Message)
     */
    public void onRead(Destination from, Message message)
    {
        if (active)
        {
            List list = new ArrayList();
            list.add(message);

            onRead(from, list);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAuditLog#onWrite(hermes.Hermes, javax.jms.Destination,
     *      javax.jms.Message)
     */
    public void onWrite(Destination to, Message message)
    {
        if (active)
        {
            List list = new ArrayList();
            list.add(message);

            onWrite(to, list);
        }
    }

    /**
     * @return Returns the active.
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * @param active
     *            The active to set.
     */
    public void setActive(boolean active)
    {
        this.active = active;

        if (!active)
        {
            rollback();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAuditLog#commit()
     */
    public void commit()
    {
        if (active)
        {
            for (Iterator iter = readMsgsInTx.entrySet().iterator(); iter.hasNext();)
            {
                try
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    FileOutputStream ostream = new FileOutputStream((File) entry.getKey());
                    Collection messages = (Collection) entry.getValue();

                    hermes.toXML(messages, ostream);
                    ostream.flush();
                    ostream.close();
                }
                catch (Exception e)
                {
                    cat.error("unable to log messages: " + e.getMessage(), e);

                    Hermes.ui.getDefaultMessageSink().add("Unable to log messages for audit: " + e.getMessage());
                }
            }

            for (Iterator iter = writeMsgsInTx.entrySet().iterator(); iter.hasNext();)
            {
                try
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    FileOutputStream ostream = new FileOutputStream((File) entry.getKey());
                    Collection messages = (Collection) entry.getValue();

                    hermes.toXML(messages, ostream);
                    ostream.flush();
                    ostream.close();
                }
                catch (Exception e)
                {
                    cat.error("unable to log messages: " + e.getMessage(), e);

                    Hermes.ui.getDefaultMessageSink().add("Unable to log messages for audit: " + e.getMessage());
                }
            }

            readMsgsInTx.clear();
            writeMsgsInTx.clear();
            filesInTx.clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAuditLog#rollback()
     */
    public void rollback()
    {
        readMsgsInTx.clear();
        writeMsgsInTx.clear();
        filesInTx.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAuditLog#setListener(hermes.HermesAuditListener)
     */
    public void setListener(HermesAuditListener auditListener)
    {
        this.auditListener = auditListener;

    }

}