/**
 * Copyright (c) 2011 CJSC Investment Company "Troika Dialog", http://troika.ru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. * 
 */
package hermes.ext.qpid;


/**
 * QMF types enum.
 *
 * Represents qpid managed objec types.
 * @author Barys Ilyushonak
 */
public enum QmfTypes {

    QUEUE("queue"), BINDING("binding"), EXCHANGE("exchange"), CONNECTION("connection")
    , SESSION("session"), SUBSCRIPTION("subscription"); 

    private String value;

    private QmfTypes(String value) {
        this.value = value;
    }

    /**
     * Return string value.
     *
     * @return value
     */
    public String getValue() {

        return this.value;
    }
}