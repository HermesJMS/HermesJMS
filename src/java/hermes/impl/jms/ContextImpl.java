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
package hermes.impl.jms;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * This is a very simple implementation of a JNDI context that delegates to a HashMap. It does not support
 * any subcontexts and has no backing store.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: ContextImpl.java,v 1.4 2006/07/13 07:35:33 colincrist Exp $
 */

public class ContextImpl implements Context
{
    private Hashtable environment;
    private Map context = new TreeMap();

    /**
     * Adapter over an interator that provides an enumeration of javax.naming.Binding objects.
     * 
     * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
     * @version $Id: ContextImpl.java,v 1.4 2006/07/13 07:35:33 colincrist Exp $
     */
    private class BindingEnumerationIterator implements NamingEnumeration
    {
        private Iterator iter;

        public BindingEnumerationIterator(Iterator iter)
        {
            this.iter = iter;
        }

        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#close()
         */
        public void close() throws NamingException
        {
            // NOP
        }

        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#hasMore()
         */
        public boolean hasMore() throws NamingException
        {
            return iter.hasNext();
        }

        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#next()
         */
        public Object next() throws NamingException
        {
            final Object name = iter.next();
            final Object object = context.get(name);
            final String className = (object == null) ? Object.class.getName() : object.getClass().getName();
            final Binding binding = new Binding((String) name, className, object);

            return binding;
        }

        /* (non-Javadoc)
         * @see java.util.Enumeration#hasMoreElements()
         */
        public boolean hasMoreElements()
        {
            return iter.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Enumeration#nextElement()
         */
        public Object nextElement()
        {
            return iter.next();
        }
    }

    /**
     * An Adapter of an interator that provides an enumeration of javax.naming.ClassNamePair(s).
     * 
     * @author cristco last changed by: $Author: colincrist $
     * @version $Id: ContextImpl.java,v 1.4 2006/07/13 07:35:33 colincrist Exp $
     */
    private class NameClassPairEnumerationIterator implements NamingEnumeration
    {
        private Iterator iter;

        public NameClassPairEnumerationIterator(Iterator iter)
        {
            this.iter = iter;
        }

        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#close()
         */
        public void close() throws NamingException
        {
            // NOP
        }

        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#hasMore()
         */
        public boolean hasMore() throws NamingException
        {
            return iter.hasNext();
        }

        /* (non-Javadoc)
         * @see javax.naming.NamingEnumeration#next()
         */
        public Object next() throws NamingException
        {
            final Object name = iter.next();
            final Object object = context.get(name);
            final String className = (object == null) ? Object.class.getName() : object.getClass().getName();
            final NameClassPair ncPair = new NameClassPair((String) name, className);

            return ncPair;
        }

        /* (non-Javadoc)
         * @see java.util.Enumeration#hasMoreElements()
         */
        public boolean hasMoreElements()
        {
            return iter.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Enumeration#nextElement()
         */
        public Object nextElement()
        {
            return iter.next();
        }
    }

    /**
     * 
     */
    public ContextImpl(Hashtable environment) throws NamingException
    {
        super();

        this.environment = environment;
    }

    /**
     * {@inheritDoc} 
     */
    public void close() throws NamingException
    {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc} 
     */
    public String getNameInNamespace() throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public void destroySubcontext(String binding) throws NamingException
    {
        assertBindingExist(binding);

        if (context.get(binding) instanceof Context)
        {
            context.remove(binding);
        }
        else
        {
            throw new NamingException("binding " + binding + " is not a Context");
        }
    }

    /**
     * {@inheritDoc} 
     */
    public void unbind(String arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");

    }

    /**
     * {@inheritDoc} 
     */
    public Hashtable getEnvironment() throws NamingException
    {
        return environment;
    }

    /**
     * {@inheritDoc} 
     */
    public void destroySubcontext(Name arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");

    }

    /**
     * {@inheritDoc} 
     */
    public void unbind(Name arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public Object lookup(String binding) throws NamingException
    {
        assertBindingExist(binding);

        return context.get(binding);
    }

    /**
     * {@inheritDoc} 
     */
    public Object lookupLink(String arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public Object removeFromEnvironment(String arg0) throws NamingException
    {
        return environment.remove(arg0);
    }

    /**
     * {@inheritDoc} 
     */
    public void bind(String arg0, Object arg1) throws NamingException
    {
        context.put(arg0, arg1);
    }

    /**
     * {@inheritDoc} 
     */
    public void rebind(String arg0, Object arg1) throws NamingException
    {
        context.put(arg0, arg1);
    }

    /**
     * {@inheritDoc} 
     */
    public Object lookup(Name arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public Object lookupLink(Name arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public void bind(Name arg0, Object arg1) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public void rebind(Name arg0, Object arg1) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public void rename(String oldBinding, String newBinding) throws NamingException
    {
        assertBindingExist(oldBinding);
        assertBindingDoesNotExist(newBinding);

        Object object = context.remove(oldBinding);
        context.put(newBinding, object);

    }

    private void assertBindingExist(String binding) throws NameNotFoundException
    {
        if (!context.containsKey(binding))
        {
            throw new NameNotFoundException("binding " + binding + " does not exist");
        }
    }

    private void assertBindingDoesNotExist(String binding) throws NamingException
    {
        if (context.containsKey(binding))
        {
            throw new NamingException("binding " + binding + " already exists");
        }
    }

    /**
     * {@inheritDoc} 
     */
    public Context createSubcontext(String binding) throws NamingException
    {
        assertBindingDoesNotExist(binding);

        Context ctx = new ContextImpl(environment);
        bind(binding, ctx);

        return ctx;
    }

    /**
     * {@inheritDoc} 
     */
    public Context createSubcontext(Name arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public void rename(Name arg0, Name arg1) throws NamingException
    {
        throw new NamingException("Not Implemented");

    }

    /**
     * {@inheritDoc} 
     */
    public NameParser getNameParser(String arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public NameParser getNameParser(Name arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public NamingEnumeration list(String binding) throws NamingException
    {
        if (binding.equals(""))
        {
            return new NameClassPairEnumerationIterator(context.keySet().iterator());
        }

        assertBindingExist(binding);

        Object object = context.get(binding);

        if (object instanceof Context)
        {
            Context ctx = (Context) object;

            return ctx.list("");
        }
        else
        {
            throw new NamingException("binding " + binding + " is not a Context");
        }
    }

    /**
     * {@inheritDoc} 
     */
    public NamingEnumeration listBindings(String binding) throws NamingException
    {
        if (binding.equals(""))
        {
            return new BindingEnumerationIterator(context.keySet().iterator());
        }

        assertBindingExist(binding);

        Object object = context.get(binding);

        if (object instanceof Context)
        {
            Context ctx = (Context) object;

            return ctx.listBindings("");
        }
        else
        {
            throw new NamingException("binding " + binding + " is not a Context");
        }
    }

    /**
     * {@inheritDoc} 
     */
    public NamingEnumeration list(Name arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public NamingEnumeration listBindings(Name arg0) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public Object addToEnvironment(String arg0, Object arg1) throws NamingException
    {
        Object rval = environment.remove(arg0);
        environment.put(arg0, arg1);

        return rval;
    }

    /**
     * {@inheritDoc} 
     */
    public String composeName(String arg0, String arg1) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }

    /**
     * {@inheritDoc} 
     */
    public Name composeName(Name arg0, Name arg1) throws NamingException
    {
        throw new NamingException("Not Implemented");
    }
}

