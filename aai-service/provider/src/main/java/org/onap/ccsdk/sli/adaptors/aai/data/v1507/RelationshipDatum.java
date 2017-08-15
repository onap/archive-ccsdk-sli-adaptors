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

package org.onap.ccsdk.sli.adaptors.aai.data.v1507;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    "relationshipKey",
    "relationshipValue",
    "any"
})
public class RelationshipDatum {

    @JsonProperty("relationshipKey")
    private String relationshipKey;
    @JsonProperty("relationshipValue")
    private String relationshipValue;
    @JsonProperty("any")
    private List<Object> any = new ArrayList<Object>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The relationshipKey
     */
    @JsonProperty("relationshipKey")
    public String getRelationshipKey() {
        return relationshipKey;
    }

    /**
     * 
     * @param relationshipKey
     *     The relationshipKey
     */
    @JsonProperty("relationshipKey")
    public void setRelationshipKey(String relationshipKey) {
        this.relationshipKey = relationshipKey;
    }

    /**
     * 
     * @return
     *     The relationshipValue
     */
    @JsonProperty("relationshipValue")
    public String getRelationshipValue() {
        return relationshipValue;
    }

    /**
     * 
     * @param relationshipValue
     *     The relationshipValue
     */
    @JsonProperty("relationshipValue")
    public void setRelationshipValue(String relationshipValue) {
        this.relationshipValue = relationshipValue;
    }

    /**
     * 
     * @return
     *     The any
     */
    @JsonProperty("any")
    public List<Object> getAny() {
        return any;
    }

    /**
     * 
     * @param any
     *     The any
     */
    @JsonProperty("any")
    public void setAny(List<Object> any) {
        this.any = any;
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
