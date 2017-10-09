/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.aai.update;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "key-name",
    "key-value"
})
public class UpdateNodeKey {

    @JsonProperty("key-name")
    private String keyName;
    @JsonProperty("key-value")
    private String keyValue;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * 
     * @return
     *     The keyName
     */
    @JsonProperty("key-name")
    public String getKeyName() {
        return keyName;
    }

    /**
     * 
     * @param keyName
     *     The key-name
     */
    @JsonProperty("key-name")
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * 
     * @return
     *     The keyValue
     */
    @JsonProperty("key-value")
    public String getKeyValue() {
        return keyValue;
    }

    /**
     * 
     * @param keyValue
     *     The key-value
     */
    @JsonProperty("key-value")
    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
