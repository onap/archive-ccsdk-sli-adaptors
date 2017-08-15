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
    "vmId",
    "vmName",
    "vmName2",
    "host",
    "image",
    "flavor",
    "ipAddresses",
    "vserverLink",
    "relationshipList"
})
public class VServer {

    @JsonProperty("vmId")
    private String vmId;
    @JsonProperty("vmName")
    private String vmName;
    @JsonProperty("vmName2")
    private String vmName2;
    @JsonProperty("host")
    private Host host;
    @JsonProperty("image")
    private Image image;
    @JsonProperty("flavor")
    private Flavor flavor;
    @JsonProperty("ipAddresses")
    private List<IpAddress> ipAddresses = new ArrayList<IpAddress>();
    @JsonProperty("vserverLink")
    private String vserverLink;
    @JsonProperty("relationshipList")
    private RelationshipList relationshipList;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The vmId
     */
    @JsonProperty("vmId")
    public String getVmId() {
        return vmId;
    }

    /**
     * 
     * @param vmId
     *     The vmId
     */
    @JsonProperty("vmId")
    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    /**
     * 
     * @return
     *     The vmName
     */
    @JsonProperty("vmName")
    public String getVmName() {
        return vmName;
    }

    /**
     * 
     * @param vmName
     *     The vmName
     */
    @JsonProperty("vmName")
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    /**
     * 
     * @return
     *     The vmName2
     */
    @JsonProperty("vmName2")
    public String getVmName2() {
        return vmName2;
    }

    /**
     * 
     * @param vmName2
     *     The vmName2
     */
    @JsonProperty("vmName2")
    public void setVmName2(String vmName2) {
        this.vmName2 = vmName2;
    }

    /**
     * 
     * @return
     *     The host
     */
    @JsonProperty("host")
    public Host getHost() {
        return host;
    }

    /**
     * 
     * @param host
     *     The host
     */
    @JsonProperty("host")
    public void setHost(Host host) {
        this.host = host;
    }

    /**
     * 
     * @return
     *     The image
     */
    @JsonProperty("image")
    public Image getImage() {
        return image;
    }

    /**
     * 
     * @param image
     *     The image
     */
    @JsonProperty("image")
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * 
     * @return
     *     The flavor
     */
    @JsonProperty("flavor")
    public Flavor getFlavor() {
        return flavor;
    }

    /**
     * 
     * @param flavor
     *     The flavor
     */
    @JsonProperty("flavor")
    public void setFlavor(Flavor flavor) {
        this.flavor = flavor;
    }

    /**
     * 
     * @return
     *     The ipAddresses
     */
    @JsonProperty("ipAddresses")
    public List<IpAddress> getIpAddresses() {
        return ipAddresses;
    }

    /**
     * 
     * @param ipAddresses
     *     The ipAddresses
     */
    @JsonProperty("ipAddresses")
    public void setIpAddresses(List<IpAddress> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    /**
     * 
     * @return
     *     The vserverLink
     */
    @JsonProperty("vserverLink")
    public String getVserverLink() {
        return vserverLink;
    }

    /**
     * 
     * @param vserverLink
     *     The vserverLink
     */
    @JsonProperty("vserverLink")
    public void setVserverLink(String vserverLink) {
        this.vserverLink = vserverLink;
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
