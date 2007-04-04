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

package hermes.taglib;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: TagValueBean.java,v 1.3 2004/09/16 20:30:50 colincrist Exp $
 */

public class TagValueBean
{
    private String tag;
    private String value;

    /**
     *  
     */
    public TagValueBean(String tag, String value)
    {
        super();

        this.tag = tag;
        this.value = value;
    }

    /**
     * @return Returns the tag.
     */
    public String getTag()
    {
        return tag;
    }

    /**
     * @param tag
     *            The tag to set.
     */
    public void setTag(String tag)
    {
        this.tag = tag;
    }

    /**
     * @return Returns the value.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(String value)
    {
        this.value = value;
    }

}