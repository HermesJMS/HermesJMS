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


import java.util.Map;
import java.util.Set;

import quickfix.DataDictionary;
import quickfix.Field;

/**
 * A facade onto a QuickFIX Message.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: FIXMessage.java,v 1.8 2007/02/28 10:47:28 colincrist Exp $
 */

public interface FIXMessage 
{
   public quickfix.Message getMessage() ;
   
   public DataDictionary getDictionary() ;
   
   public abstract String getMsgType();

   public abstract Set<Integer> getFieldOrder();

   public abstract boolean fieldExists(int tag);

   public abstract Map<Integer, Field> getAllFields();

   public abstract void reset();

   public abstract Object getObject(Field field) ;

   public abstract Object getObject(int tag) throws NoSuchFieldException;;

   public abstract String getString(int field) ;
   
   public byte[] getBytes() ;
   

}