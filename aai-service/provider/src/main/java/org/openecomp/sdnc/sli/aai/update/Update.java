/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights
 * 						reserved.
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

package org.openecomp.sdnc.sli.aai.update;

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
    "update-node-type",
    "update-node-key",
    "action"
})
public class Update {

    @JsonProperty("update-node-type")
    private String updateNodeType;
    @JsonProperty("action")
    private List<Action> action = new ArrayList<Action>();
    @JsonProperty("update-node-key")
    private List<UpdateNodeKey> updateNodeKey = new ArrayList<UpdateNodeKey>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The updateNodeType
     */
    @JsonProperty("update-node-type")
    public String getUpdateNodeType() {
        return updateNodeType;
    }

    /**
     * 
     * @param updateNodeType
     *     The update-node-type
     */
    @JsonProperty("update-node-type")
    public void setUpdateNodeType(String updateNodeType) {
        this.updateNodeType = updateNodeType;
    }

    /**
     * 
     * @return
     *     The action
     */
    @JsonProperty("action")
    public List<Action> getAction() {
        return action;
    }

    /**
     * 
     * @param action
     *     The action
     */
    @JsonProperty("action")
    public void setAction(List<Action> action) {
        this.action = action;
    }

    /**
     * 
     * @return
     *     The updateNodeKey
     */
    @JsonProperty("update-node-key")
    public List<UpdateNodeKey> getUpdateNodeKey() {
        return updateNodeKey;
    }

    /**
     * 
     * @param updateNodeKey
     *     The update-node-key
     */
    @JsonProperty("update-node-key")
    public void setUpdateNodeKey(List<UpdateNodeKey> updateNodeKey) {
        this.updateNodeKey = updateNodeKey;
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
