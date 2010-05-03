/* 
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

package hermes.ext.ems;

import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesAdminFactory;
import hermes.HermesException;
import hermes.HermesRuntimeException;
import hermes.JNDIConnectionFactory;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.tibco.tibjms.TibjmsConnectionFactory;
import com.tibco.tibjms.TibjmsSSL;
import com.tibco.tibjms.admin.TibjmsAdmin;

/**
 * Administration plugin for Tibco EMS.
 * 
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: TibcoEMSAdminFactory.java,v 1.2 2005/04/08 15:54:36 colincrist
 *          Exp $
 */
public class TibcoEMSAdminFactory implements HermesAdminFactory
{
   private final static Logger log = Logger.getLogger(TibcoEMSAdminFactory.class);

   private String serverURL;
   private String username;
   private String password;

   public TibcoEMSAdminFactory()
   {
      super();
   }

   /*
    * (non-Javadoc)
    * 
    * @see hermes.HermesAdminFactory#createSession(hermes.Hermes,
    *      javax.jms.ConnectionFactory)
    */
   public HermesAdmin createSession(Hermes hermes, ConnectionFactory connectionFactory) throws JMSException, NamingException
   {
      TibjmsAdmin admin = null;

      return new TibcoEMSAdmin(hermes, this);
   }

   TibjmsAdmin createAdmin(ConnectionFactory connectionFactory) throws JMSException
   {
      TibjmsAdmin admin = null;

      try
      {
         if (connectionFactory instanceof JNDIConnectionFactory)
         {
            final JNDIConnectionFactory jndiCF = (JNDIConnectionFactory) connectionFactory;

            if (username == null && jndiCF.getSecurityPrincipal() != null)
            {
               username = jndiCF.getSecurityCredentials();

               if (password == null && jndiCF.getSecurityCredentials() != null)
               {
                  password = jndiCF.getSecurityCredentials();
               }
            }

            admin = new TibjmsAdmin(serverURL == null ? jndiCF.getProviderURL() : serverURL, username, password);
         }
         else if (connectionFactory instanceof TibjmsConnectionFactory)
         {
            final TibjmsConnectionFactory tibCF = (TibjmsConnectionFactory) connectionFactory;
            final Map ssl = getSSLParameters(tibCF);

            serverURL = serverURL == null ? (String) BeanUtils.getProperty(tibCF, "serverUrl") : serverURL;
            username = username == null ? (String) BeanUtils.getProperty(tibCF, "userName") : username;
            password = password == null ? (String) BeanUtils.getProperty(tibCF, "userPassword") : password;

            if (ssl.size() == 0)
            {
               admin = new TibjmsAdmin(serverURL, username, password);
            }
            else
            {
               admin = new TibjmsAdmin(serverURL, username, password, ssl);
            }

         }

         if (admin == null)
         {
            throw new HermesException("Provider is not TibcoEMS");
         }
      }
      catch (Exception e)
      {
         log.error(e.getMessage(), e);

         throw new HermesException(e);
      }

      return admin;
   }

   private Map getSSLParameters(TibjmsConnectionFactory tibCF)
   {
      try
      {
         Map rval = new HashMap();

         if (BeanUtils.getProperty(tibCF, "SSLIdentity") != null)
         {
            rval.put(TibjmsSSL.IDENTITY, BeanUtils.getProperty(tibCF, "SSLIdentity"));
            rval.put(TibjmsSSL.AUTH_ONLY, BeanUtils.getProperty(tibCF, "SSLAuthOnly")) ;
            rval.put(TibjmsSSL.CIPHER_SUITES, BeanUtils.getProperty(tibCF, "SSLCipherSuites")) ;
            rval.put(TibjmsSSL.DEBUG_TRACE, BeanUtils.getProperty(tibCF, "SSLDebugTrace")) ;
            rval.put(TibjmsSSL.ENABLE_VERIFY_HOST, BeanUtils.getProperty(tibCF, "SSLEnableVerifyHost")) ;            
            rval.put(TibjmsSSL.ENABLE_VERIFY_HOST_NAME, BeanUtils.getProperty(tibCF, "SSLEnableVerifyHostName")) ;
            rval.put(TibjmsSSL.EXPECTED_HOST_NAME, BeanUtils.getProperty(tibCF, "SSLExpectedHostName")) ;          
            rval.put(TibjmsSSL.IDENTITY_ENCODING, BeanUtils.getProperty(tibCF, "SSLIdentityEncoding")) ;
            rval.put(TibjmsSSL.ISSUER_CERTIFICATES, BeanUtils.getProperty(tibCF, "SSLIssuerCertificate")) ;
            rval.put(TibjmsSSL.PASSWORD, BeanUtils.getProperty(tibCF, "SSLPassword")) ;
            rval.put(TibjmsSSL.PRIVATE_KEY, BeanUtils.getProperty(tibCF, "SSLPrivateKey")) ;
            rval.put(TibjmsSSL.PRIVATE_KEY_ENCODING, BeanUtils.getProperty(tibCF, "SSLPrivateKeyEncoding")) ;
            rval.put(TibjmsSSL.TRACE, BeanUtils.getProperty(tibCF, "SSLTrace")) ;
            rval.put(TibjmsSSL.TRUSTED_CERTIFICATES, BeanUtils.getProperty(tibCF, "SSLTrustedCertificate")) ;
            rval.put(TibjmsSSL.VENDOR, BeanUtils.getProperty(tibCF, "SSLVendor")) ;            
         }

         return rval;
      }
      catch (Exception ex)
      {
         throw new HermesRuntimeException(ex);
      }
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      if (password.equals(""))
      {
         this.password = null;
      }
      else
      {
         this.password = password;
      }
   }

   public String getServerURL()
   {
      return serverURL;
   }

   public void setServerURL(String serverURL)
   {
      if (serverURL.equals(""))
      {
         this.serverURL = null;
      }
      else
      {
         this.serverURL = serverURL;
      }
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      if (username.equals(""))
      {
         this.username = null;
      }
      else
      {
         this.username = username;
      }
   }
}
