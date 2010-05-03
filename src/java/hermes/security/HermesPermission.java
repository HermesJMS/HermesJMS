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

package hermes.security;

import java.security.Permission;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: HermesPermission.java,v 1.1 2005/06/20 15:28:35 colincrist Exp $
 */

public class HermesPermission extends Permission
{

   /**
	 * 
	 */
	private static final long serialVersionUID = -7080239568822100792L;

public HermesPermission(String name)
   {
      super(name);
      // TODO Auto-generated constructor stub
   }

   @Override
   public boolean implies(Permission permission)
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public boolean equals(Object obj)
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public int hashCode()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public String getActions()
   {
      // TODO Auto-generated method stub
      return null;
   }

}
