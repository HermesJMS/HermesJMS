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

package hermes.ext.joram;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.JNDIConnectionFactory;
import hermes.browser.HermesBrowser;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.QueueBrowser;

import org.apache.log4j.Logger;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.User;



/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: JoramAdmin.java,v 1.8 2006/01/14 12:59:12 colincrist Exp $
 */

/**
 * Contributor : BADOLLE Fabien, ScalAgent Distributed Technologies, (7/2007)
 */

public class JoramAdmin extends HermesAdminSupport implements HermesAdmin 
{
    private static final Logger log = Logger.getLogger(JoramAdmin.class);
    private JoramAdminFactory factory;
    private Hermes hermes;
    private JNDIConnectionFactory jndiCF;
    private boolean connected = false;
    private User user;

    /**
     * 
     */
    public JoramAdmin(JoramAdminFactory factory, Hermes hermes, JNDIConnectionFactory jndiCF)
    {
	super(hermes);
	this.factory = factory;
	this.jndiCF = jndiCF;
    }
   
    private synchronized void checkConnected() throws JMSException
    {
	if (!connected) {
	    try {
		
		AdminModule.connect(factory.getHostname(), factory.getPort(), factory.getUsername(), factory.getPassword(), factory.getCnxTimer());
		
	    }catch (ConnectException e) {
		throw new HermesException(e);
	    }catch (UnknownHostException e) {
		throw new HermesException(e);
	    }catch (AdminException e) {
		throw new HermesException(e);
	    }

	    connected = true;
	}
    }

    protected User getUser(String userName) throws JMSException
    {
	checkConnected();
	try{
	    for (User u : AdminModule.getUsers()) {

		if (u.getName().equals(userName)) {
		    return u;
		}
	    }
         
	    throw new HermesException("No such user " + userName) ;
	}catch (ConnectException e) {
	    throw new HermesException(e);
	}catch (AdminException e) {
	    throw new HermesException(e);
	}
    }

    public synchronized Collection discoverDestinationConfigs() throws JMSException
    {
	checkConnected();
	final Collection rval = new ArrayList();
	try{
	    for (Destination d : AdminModule.getDestinations())
		{
		    final DestinationConfig dConfig = new DestinationConfig();
		    
		    dConfig.setName(d.getName());
		    dConfig.setShortName(d.getAdminName());
		   
		    if (d.getType() == Destination.QUEUE_TYPE){
			dConfig.setDomain(Domain.QUEUE.getId());
			rval.add(dConfig);
		    }  else if(d.getType()==Destination.TOPIC_TYPE){
			dConfig.setDomain(Domain.TOPIC.getId());
			rval.add(dConfig);
			rval.addAll(discoverDurableSubscriptions(dConfig.getName(),dConfig.getName()));
		    }		   
		}
	}
	catch (Exception ex){
	    throw new HermesException(ex) ;
	}
	
	return rval;
    }







    
    @Override
	protected Collection discoverDurableSubscriptions(String topicName, String jndiName) throws JMSException
    {
     
	final ArrayList rval = new ArrayList() ;
	checkConnected();
      
	try {
	    for (User u : AdminModule.getUsers()) {
		if(u.getName() != factory.getUsername()){
		    final Subscription[] subs = u.getSubscriptions() ;
            
		    for (int i = 0 ; i < subs.length ; i++) {
			if (subs[i].getTopicId().equals(topicName)) {
			    DestinationConfig dConfig = HermesBrowser.getConfigDAO().createDestinationConfig() ;
                  
			    dConfig.setName(jndiName) ;
			    dConfig.setShortName(u.getProxyId()) ;
			    dConfig.setDurable(true) ;
			    dConfig.setClientID(subs[i].getName()) ;
			    dConfig.setDomain(Domain.TOPIC.getId()) ;
                  
			    rval.add(dConfig) ;
			}
		    }            
		}
	    }

	}catch (ConnectException e) {
	    throw new HermesException(e);
	}catch (AdminException e) {
	    throw new HermesException(e);
	}

	return rval ;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#getDepth(javax.jms.Destination)
     */
    public int getDepth(DestinationConfig dest) throws JMSException
    {
	checkConnected();
	try {
	    if (dest.getDomain() == Domain.QUEUE.getId()) {
		Queue queue = (Queue) getHermes().getDestination(dest.getName(), Domain.QUEUE);
	      
		return queue.getPendingMessages();
	      
	    } else {
		Topic topic = (Topic) getHermes().getDestination(dest.getName(), Domain.TOPIC) ;
         
        
		throw new HermesException("Cannot get depth, " + dest.getName() + " is a topic");
	    }
      
	}catch (ConnectException e) {
	    throw new HermesException(e);
	}catch (AdminException e) {
	    throw new HermesException(e);
	}catch (Exception e){
	    throw new HermesException(e);
	}
     
    }
  
   
    public QueueBrowser createDurableSubscriptionBrowser(final DestinationConfig dConfig) throws JMSException
    {
	checkConnected() ;
	try {
	    Topic topic = (Topic) getHermes().getSession().createTopic(dConfig.getName());
	    checkConnected();
	    final List messages= new ArrayList();
	 
	    int fin =0;
	    String[] ids = topic.getSubscriberIds();
	    for(int i=0;i< ids.length && fin == 0 ;i++){
		if(dConfig.getShortName() !=null && (dConfig.getShortName()).equals(ids[i])){
		    User u = new User("user"+i,ids[i]);
		    final Subscription[] subs = u.getSubscriptions() ;
		    for (int j = 0 ; j < subs.length && fin == 0 ; j++) {
			if( (subs[j].getName()).equals(dConfig.getClientID()) ){
			    final String[] msgids = u.getMessageIds(dConfig.getClientID()) ;
			    for(int k=0;k< msgids.length;k++)
				messages.add(u.readMessage(dConfig.getClientID(),msgids[k]));
			    fin=1;
			}
		    }
		}else if(dConfig.getShortName() ==null){
		    throw new HermesException(new Exception("ShortName(userID) is null")) ;
		} else{
		    throw new HermesException(new Exception("ShortName(userID) is incorrect")) ;
		}
		   
	    }
	 
	    return new QueueBrowser() {
		    public void close() throws JMSException {
			// TODO Auto-generated method stub     
		    }
      
		    public Enumeration getEnumeration() throws JMSException {
			return new Enumeration() {
				int i = 0 ;
				public Object nextElement() {
				    return messages.get(i++);
	
		
				}
         
				public boolean hasMoreElements() {
				    return i <  messages.size() ;
				}
         
			    } ;
		    }
      
		    public String getMessageSelector() throws JMSException {
			// TODO Auto-generated method stub
			// return null;
			return dConfig.getSelector();
		    }
      
		    public javax.jms.Queue getQueue() throws JMSException {
			// TODO Auto-generated method stub
			return null;
		    }
      
		} ;
	} catch (AdminException e) {
	    throw new HermesException(e) ;
	} catch (ConnectException e) {
	    throw new HermesException(e);
	}
      
    }
  

   
    /* (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#close()
     */
    public void close() throws JMSException
    {
	AdminModule.disconnect();
    }

    /*
     * (non-Javadoc)
     * 
     * @see hermes.HermesAdmin#getStatistics(javax.jms.Destination)
     */
    public Map getStatistics(DestinationConfig dConfig) throws JMSException
    {
	final Map rval = new LinkedHashMap();

	if (dConfig.getDomain() == Domain.QUEUE.getId()) {
	  
	    Queue queue = (Queue) getHermes().getSession().createQueue(dConfig.getName());
	  
	    try{
		checkConnected();
		  
		rval.put("Name", queue.getName());
		rval.put("AdminName", queue.getAdminName());
		rval.put("Threshold", Integer.toString(queue.getThreshold()));
		rval.put("NbMaxMsg", Integer.toString(queue.getNbMaxMsg()));
		rval.put("getPendingMessages", Integer.toString(queue.getPendingMessages()));
		rval.put("getPendingRequests", Integer.toString(queue.getPendingRequests()));
	    }
	    catch (ConnectException e) {
		throw new HermesException(e);
	    }
	    catch (AdminException e){
		throw new HermesException(e);
	    }
	}else {

	    Topic topic = (Topic) getHermes().getSession().createTopic(dConfig.getName());
	    try{
		checkConnected();
		  
		rval.put("Name", topic.getName());
		rval.put("AdminName", topic.getAdminName());
		rval.put("number of subscription",topic.getSubscriptions());

		String[] ids = topic.getSubscriberIds();
		for(int i=0;i< ids.length;i++){
		    User u = new User("user"+i,ids[i]);
		    final Subscription[] subs = u.getSubscriptions() ;
		    for (int j = 0 ; j < subs.length ; j++) {
			rval.put("subscriber "+j,ids[i]+" : "+subs[j].getName()+"; Durable:"+subs[j].isDurable());
		
		    }
		}
	
	    }catch (ConnectException e) {
		throw new HermesException(e);
	    }catch (AdminException e){
		throw new HermesException(e);
	    }
	}
	
	return rval;
    }

    public int truncate(DestinationConfig dConfig) throws JMSException {
	try{
	    int fin =0;
	    checkConnected();
	    if (dConfig.getDomain() == Domain.QUEUE.getId()){
		Queue queue = (Queue) getHermes().getSession().createQueue(dConfig.getName());
		fin = queue.getPendingMessages();
		queue.clear();
	    }else{
		Topic topic = (Topic) getHermes().getSession().createTopic(dConfig.getName());
	
		String[] ids = topic.getSubscriberIds();
		for(int i=0;i< ids.length && fin == 0 ;i++){
		    if(dConfig.getShortName() !=null && (dConfig.getShortName()).equals(ids[i])){
			User u = new User("user"+i,ids[i]);
			final Subscription[] subs = u.getSubscriptions() ;
			for (int j = 0 ; j < subs.length && fin == 0 ; j++) {
			    if( (subs[j].getName()).equals(dConfig.getClientID()) ){
				final String[] msgids = u.getMessageIds(dConfig.getClientID()) ;
				for(int k=0;k< msgids.length;k++)
				    u.deleteMessage(dConfig.getClientID(),msgids[k]);
				fin++;
			    }
			}
		    }else if(dConfig.getShortName() ==null){
			throw new HermesException(new Exception("ShortName(userID) is null")) ;
		    } else{
			throw new HermesException(new Exception("ShortName(userID) is incorrect")) ;
		    }
		    
		}
	    }
	    return fin;
	}catch(Exception e){
	    throw new HermesException(e);
	}  

    }


    @Override
    public void delete(DestinationConfig dConfig, Collection messageIds) throws JMSException{
	try{
	    checkConnected();
	    if (dConfig.getDomain() == Domain.QUEUE.getId()){
		Queue queue = (Queue) getHermes().getSession().createQueue(dConfig.getName());
		for (Iterator iter = messageIds.iterator(); iter.hasNext();) {
		    queue.deleteMessage((String)iter.next());
		}
	    }else if ( dConfig.isDurable()){
		Topic topic = (Topic) getHermes().getSession().createTopic(dConfig.getName());
		int fin =0;
		String[] ids = topic.getSubscriberIds();
		for(int i=0;i< ids.length && fin == 0 ;i++){
		    if(dConfig.getShortName() !=null && (dConfig.getShortName()).equals(ids[i])){
			User u = new User("user"+i,ids[i]);
			final Subscription[] subs = u.getSubscriptions() ;
			for (int j = 0 ; j < subs.length && fin == 0 ; j++) {
			    if( (subs[j].getName()).equals(dConfig.getClientID()) ){
				for (Iterator iter = messageIds.iterator(); iter.hasNext();) {
				    u.deleteMessage(dConfig.getClientID(),(String)iter.next());
				}
				fin=1;
			    }
			}
		    }else{
			throw new HermesException(new Exception("Error when delete")) ;
		    }
		}
	    }else{
		throw new HermesException(new Exception("Error when delete")) ;
	    }
	}catch(Exception e){
	    throw new HermesException(e);
	}  
    }



}





