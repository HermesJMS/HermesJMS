package test.jms;

import hermes.util.ReflectUtils;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.tibco.tibjms.TibjmsConnectionFactory;


public class TestEMSConnectionFactory {

	private static final Logger log = Logger.getLogger(TestEMSConnectionFactory.class) ;
	  
	public static class DebugCF extends TibjmsConnectionFactory {
		@Override
		public void setServerUrl(String arg0) throws JMSException {
			log.info("in setServerUrl") ;
			super.setServerUrl(arg0);
		}		
		
		public String toString() {
			return "DebugCF: " + super.toString();
		}
	}
	@Test
	public void testConnect() throws JMSException {
		TibjmsConnectionFactory cf = new TibjmsConnectionFactory() ;
		cf.setServerUrl("tcp://ldnmwaredev01.eur.ad.tullib.com:7222") ;
		Connection c = cf.createConnection() ;
		c.close() ;
	}
	
	@Test
	public void testConnectWithProxy() throws Exception {
		ConnectionFactory cf = ReflectUtils.createConnectionFactory(TibjmsConnectionFactory.class) ;
		BeanUtils.setProperty(cf, "serverUrl", "tcp://ldnmwaredev01.eur.ad.tullib.com:7222") ;
		Assert.assertEquals("tcp://ldnmwaredev01.eur.ad.tullib.com:7222", BeanUtils.getProperty(cf, "serverUrl")) ;
		
		//log.info(BeanUtils.describe(cf)) ;
		Connection c = cf.createConnection("admin", "MessageBus") ;
		c.close() ;
	}
}
