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

package org.openecomp.sdnc.sli.aai.query;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.openecomp.aai.inventory.v11.L3Network;
import org.openecomp.aai.inventory.v11.Pnf;
import org.openecomp.aai.inventory.v11.LogicalLink;
import org.openecomp.aai.inventory.v11.ServiceInstance;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "logical-link",
    "pnf",
    "l3-network",
    "service-instance"
})
public class InstanceFilter {

    @JsonProperty("logical-link")
    private LogicalLink logicalLink;
    @JsonProperty("pnf")
    private Pnf pnf;
	@JsonProperty("l3-network")
    private L3Network l3Network;
	@JsonProperty("service-instance")
    private ServiceInstance serviceInstance;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The logicalLink
     */
    @JsonProperty("logical-link")
    public LogicalLink getLogicalLink() {
        return logicalLink;
    }

    /**
     * 
     * @param logicalLink
     *     The logical-link
     */
    @JsonProperty("logical-link")
    public void setLogicalLink(LogicalLink logicalLink) {
        this.logicalLink = logicalLink;
    }

    /**
     * 
     * @return
     *     The pnf
     */
    @JsonProperty("pnf")
    public Pnf getPnf() {
        return pnf;
    }

    /**
     * 
     * @param pnf
     *     The pnf
     */
    @JsonProperty("pnf")
    public void setPnf(Pnf pnf) {
        this.pnf = pnf;
    }

    @JsonProperty("l3-network")
    public L3Network getL3Network() {
		return l3Network;
	}

    @JsonProperty("l3-network")
    public void setL3Network(L3Network l3Network) {
		this.l3Network = l3Network;
	}

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonProperty("service-instance")
	public ServiceInstance getServiceInstance() {
		return serviceInstance;
	}

    @JsonProperty("service-instance")
	public void setServiceInstance(ServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

}
