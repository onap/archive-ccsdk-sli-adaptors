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

package org.onap.ccsdk.sli.adaptors.aai.data;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.openecomp.aai.inventory.v11.RelationshipList;
import org.openecomp.aai.inventory.v11.Vlans;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "interface-name",
    "interface-role",
    "resource-version",
    "vlans",
    "relationshipList"
})
public class SubInterface implements AAIDatum {

    @JsonProperty("interface-name")
    private String interfaceName;
    @JsonProperty("interface-role")
    private String interfaceRole;
    @JsonProperty("resource-version")
    private String resourceVersion;
    @JsonProperty("vlans")
    private Vlans vlans;
    @JsonProperty("relationshipList")
    private RelationshipList relationshipList;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The interfaceName
     */
    @JsonProperty("interface-name")
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * 
     * @param interfaceName
     *     The interface-name
     */
    @JsonProperty("interface-name")
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * 
     * @return
     *     The interfaceRole
     */
    @JsonProperty("interface-role")
    public String getInterfaceRole() {
        return interfaceRole;
    }

    /**
     * 
     * @param interfaceRole
     *     The interface-role
     */
    @JsonProperty("interface-role")
    public void setInterfaceRole(String interfaceRole) {
        this.interfaceRole = interfaceRole;
    }

    /**
     * 
     * @return
     *     The resourceVersion
     */
    @JsonProperty("resource-version")
    public String getResourceVersion() {
        return resourceVersion;
    }

    /**
     * 
     * @param resourceVersion
     *     The resource-version
     */
    @JsonProperty("resource-version")
    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    /**
     * 
     * @return
     *     The vlans
     */
    @JsonProperty("vlans")
    public Vlans getVlans() {
        return vlans;
    }

    /**
     * 
     * @param vlans
     *     The vlans
     */
    @JsonProperty("vlans")
    public void setVlans(Vlans vlans) {
        this.vlans = vlans;
    }

    /**
     * 
     * @return
     *     The relationshipList
     */
    @JsonProperty("relationshipList")
    public RelationshipList getRelationshipList() {
        return relationshipList;
    }

    /**
     * 
     * @param relationshipList
     *     The relationshipList
     */
    @JsonProperty("relationshipList")
    public void setRelationshipList(RelationshipList relationshipList) {
        this.relationshipList = relationshipList;
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
