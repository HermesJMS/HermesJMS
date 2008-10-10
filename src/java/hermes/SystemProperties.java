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

package hermes;

import hermes.selector.JAMSELMessageSelectorFactory;

import java.io.File;


/**
 * @author colincrist@hermesjms.com last changed by: $Author: colincrist $
 * @version $Id: SystemProperties.java,v 1.17 2006/07/13 07:35:34 colincrist Exp $
 */

public interface SystemProperties
{
    public static final String HERMES_XML = "hermes";
    public static final String DEFAULT_HERMES_XML = "hermes-config.xml";
    
    // true/false properties.
    
    public static final String RESTRICTED = "hermes.restricted" ;
    public static final String BASE64_ENCODE_TEXT_MESSAGE = "hermes.base64EncodeTextMessage" ;
    public static final String DISABLE_MESSAGE_STORES = "hermes.disableMessageStores" ;
    public static final String MESSAGE_STORE_JDBC_URL = "hermes.messageStore.url" ;
    
    public static final String RENDERER_CLASSES = "hermes.renderer.classes";
    public static final String DEFAULT_RENDERER_CLASSES = "hermes.renderers.DefaultMessageRenderer,hermes.renderers.DefaultMessageHeaderRenderer,hermes.renderers.ToStringMessageRenderer,hermes.renderers.HexMessageRenderer,hermes.renderers.XMLMessageRenderer,hermes.renderers.fix.FIXMessageRenderer,hermes.renderers.EBCDICMessageRenderer";
    public static final String USER_RENDERER_CLASSES = "hermes.renderer.user.classes";
    
    
    public static final String NON_COMPLIANT_PACKAGES = "hermes.nonComplientFactories.packagePrefix" ;
    public static final String DEFAULT_NON_COMPLIANT_PACKAGES = "com.tibco.tibjms" ;
    
    
    public static final String FILE_SEPARATOR = File.separator  ;
    public static final String EXT_LIBRARY_PATH = System.getProperty("hermes.libs", "../lib");
    
    public static final String SELECTOR_FACTORY = System.getProperty("hermes.selectorFactory", JAMSELMessageSelectorFactory.class.getName()) ;
    
}