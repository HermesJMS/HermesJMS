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

package hermes.browser;

import hermes.Hermes;

import java.io.File;
import java.util.Iterator;

/**
 * Base for an iterator that views a File as an Iterator over one or more JMS
 * messages
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: AbstractMessageFileIterator.java,v 1.1 2004/05/01 15:52:35
 *          colincrist Exp $
 */

public abstract class AbstractMessageFileIterator implements Iterator
{
    protected File file;
    protected Hermes hermes;

    public AbstractMessageFileIterator(Hermes hermes, File file)
    {
        this.file = file;
        this.hermes = hermes;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public abstract boolean hasNext();

    /**
     * @see java.util.Iterator#next()
     */
    public abstract Object next();

    /**
     * @see java.util.Iterator#remove()
     */
    public void remove()
    {
        throw new UnsupportedOperationException("AbstractMessageFileIterator.remove()");
    }
}