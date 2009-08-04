/* 
 * Copyright 2003,2004,2005 Colin Crist
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

package hermes.fix;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: SessionKey.java,v 1.2 2006/05/26 10:08:19 colincrist Exp $
 */

public class SessionKey
{
   private String key;
   private String senderCompID;
   private String targetCompID;
   private String senderSubID ;
   private String targetSubID ;
   private long numMessages ;
   private SessionRole role ;

   public SessionKey(String senderCompID, String targetCompID, String senderSubID, String targetSubID, SessionRole role)
   {
      this(senderCompID, targetCompID, role) ;
      
      this.senderSubID = senderSubID ;
      this.targetSubID = targetSubID ;
   }
   
   public SessionKey(String senderCompID, String targetCompID, SessionRole role)
   {
      super();
      
      this.senderCompID = senderCompID ;
      this.targetCompID = targetCompID ;      
      this.key = senderCompID + "-" + targetCompID;
      this.role = role ;
   }
   
   public SessionKey(String senderCompID, String targetCompID)
   {
      this(senderCompID, targetCompID, SessionRole.UNKNOWN);
     
   }

   @Override
   public boolean equals(Object obj)
   {
       if (obj != null && obj instanceof SessionKey)
       {
          return key.equals(obj.toString()) ;
       }
       
       return false ;
   }
   
   public void setSessionRole(SessionRole role)
   {
      this.role = role ;
   }
   
   public SessionRole getSessionRole()
   {
      return role ;
   }

   public String getSenderCompID()
   {
      return senderCompID;
   }

   public String getTargetCompID()
   {
      return targetCompID;
   }

   @Override
   public int hashCode()
   {
      return key.hashCode();
   }

   @Override
   public String toString()
   {
      return key;
   }
   
   public long getNumMessages()
   {
      return numMessages ;
   }
   
   public void setNumMessages(long numMessages)
   {
      this.numMessages = numMessages ;
   }

   public String getSenderSubID()
   {
      return senderSubID;
   }

   public String getTargetSubID()
   {
      return targetSubID;
   }
}
