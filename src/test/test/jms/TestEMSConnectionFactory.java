package test.jms;

import hermes.util.ReflectUtils;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import com.tibco.tibjms.TibjmsConnectionFactory;


public class TestEMSConnectionFactory {

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
		Connection c = cf.createConnection() ;
		c.close() ;
	}
}
