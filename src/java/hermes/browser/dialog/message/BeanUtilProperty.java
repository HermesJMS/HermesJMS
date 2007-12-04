/* 
 * Copyright 2007 Colin Crist
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

package hermes.browser.dialog.message;

import hermes.HermesRuntimeException;

import org.apache.commons.beanutils.BeanUtils;

import com.jidesoft.grid.Property;

/**
 * @author colincrist@hermesjms.com
 * @version $Id$
 */

public class BeanUtilProperty extends Property
{
   private Object bean ;
   private String property ;
   
   public BeanUtilProperty(Object bean, String property, Class clazz, String section) 
   {
      super(property, property, clazz, section) ;
      this.bean = bean ;
      this.property = property ;
   }
   
   @Override
   public Object getValue()
   {
      try
      {
         return BeanUtils.getProperty(bean, property) ;
      }
      catch (Exception ex)
      {
         throw new HermesRuntimeException(ex) ;
      }
   }

   @Override
   public boolean hasValue()
   {
      return getValue() != null ;
   }

   @Override
   public void setValue(Object value)
   {
      try
      {
         BeanUtils.setProperty(bean, property, value) ;
      }
      catch (Exception ex)
      {
         throw new HermesRuntimeException(ex) ;
      }

   }

}
