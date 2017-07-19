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

package org.openecomp.sdnc.sli.aai.data.v1507;

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
    "relatedTo",
    "relatedLink",
    "relationshipData",
    "any"
})
public class Relationship {

    @JsonProperty("relatedTo")
    private String relatedTo;
    @JsonProperty("relatedLink")
    private String relatedLink;
    @JsonProperty("relationshipData")
    private List<RelationshipDatum> relationshipData = new ArrayList<RelationshipDatum>();
    @JsonProperty("any")
    private List<Object> any = new ArrayList<Object>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The relatedTo
     */
    @JsonProperty("relatedTo")
    public String getRelatedTo() {
        return relatedTo;
    }

    /**
     * 
     * @param relatedTo
     *     The relatedTo
     */
    @JsonProperty("relatedTo")
    public void setRelatedTo(String relatedTo) {
        this.relatedTo = relatedTo;
    }

    /**
     * 
     * @return
     *     The relatedLink
     */
    @JsonProperty("relatedLink")
    public String getRelatedLink() {
        return relatedLink;
    }

    /**
     * 
     * @param relatedLink
     *     The relatedLink
     */
    @JsonProperty("relatedLink")
    public void setRelatedLink(String relatedLink) {
        this.relatedLink = relatedLink;
    }

    /**
     * 
     * @return
     *     The relationshipData
     */
    @JsonProperty("relationshipData")
    public List<RelationshipDatum> getRelationshipData() {
        return relationshipData;
    }

    /**
     * 
     * @param relationshipData
     *     The relationshipData
     */
    @JsonProperty("relationshipData")
    public void setRelationshipData(List<RelationshipDatum> relationshipData) {
        this.relationshipData = relationshipData;
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
