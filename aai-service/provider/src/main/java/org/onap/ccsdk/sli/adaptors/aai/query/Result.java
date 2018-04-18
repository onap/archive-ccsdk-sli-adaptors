/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
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

package org.onap.ccsdk.sli.adaptors.aai.query;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.openecomp.aai.inventory.v11.CloudRegion;
import org.openecomp.aai.inventory.v11.Complex;
import org.openecomp.aai.inventory.v11.Configuration;
import org.openecomp.aai.inventory.v11.GenericVnf;
import org.openecomp.aai.inventory.v11.L3InterfaceIpv4AddressList;
import org.openecomp.aai.inventory.v11.L3InterfaceIpv6AddressList;
import org.openecomp.aai.inventory.v11.L3Network;
import org.openecomp.aai.inventory.v11.LInterface;
//import org.openecomp.aai.inventory.v11.OwningEntity;
import org.openecomp.aai.inventory.v11.Pserver;
import org.openecomp.aai.inventory.v11.ServiceInstance;
import org.openecomp.aai.inventory.v11.Vnfc;
import org.openecomp.aai.inventory.v11.Vserver;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
	"resource-type",
	"resource-link",
	"url",
    "cloud-region",
    "complex",
	"configuration",
    "generic-vnf",
    "l3-interface-ipv4-address-list",
    "l3-interface-ipv6-address-list",
    "l3-network",
    "l-interface",
    "owning-entity",
    "pserver",
    "service-instance",
    "vnfc",
    "vserver"
})
@XmlRootElement(name = "result")
public class Result {

    @XmlElement(name = "resource-type")
    private String resourceType;
    @XmlElement(name = "resource-link")
    private String resourceLink;
    @XmlElement(name = "url")
    private String url;
    @XmlElement(name = "cloud-region")
    private CloudRegion cloudRegion;
    @XmlElement(name = "complex")
    private Complex complex;
	@XmlElement(name = "configuration")
	private Configuration configuration;
    @XmlElement(name = "generic-vnf")
    private GenericVnf genericVnf;
    @XmlElement(name = "l3-interface-ipv4-address-list")
    private L3InterfaceIpv4AddressList l3InterfaceIpv4AddressList;
    @XmlElement(name = "l3-interface-ipv6-address-list")
    private L3InterfaceIpv6AddressList l3InterfaceIpv6AddressList;
    @XmlElement(name = "l3-network")
    private L3Network l3Network;
    @XmlElement(name = "l-interface")
    private LInterface lInterface;
//	@XmlElement(name = "owning-entity")
//	private OwningEntity owningEntity;
    @XmlElement(name = "pserver")
    private Pserver pserver;
    @XmlElement(name = "service-instance")
    private ServiceInstance serviceInstance;
    @XmlElement(name = "vnfc")
    private Vnfc vnfc;
    @XmlElement(name = "vserver")
    private Vserver vserver;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @XmlElement(name = "cloud-region")
    public CloudRegion getCloudRegion() {
        return cloudRegion;
    }

    @XmlElement(name = "cloud-region")
    public void setCloudRegion(CloudRegion cloudRegion) {
        this.cloudRegion = cloudRegion;
    }

    @XmlElement(name = "complex")
    public Complex getComplex() {
        return complex;
    }

    @XmlElement(name = "complex")
    public void setComplex(Complex complex) {
        this.complex = complex;
    }

	@XmlElement(name = "configuration")
	public Configuration getConfiguration() {
		return configuration;
	}

	@XmlElement(name = "configuration")
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

    @XmlElement(name = "generic-vnf")
    public GenericVnf getGenericVnf ()
    {
        return genericVnf;
    }

    @XmlElement(name = "generic-vnf")
    public void setGenericVnf (GenericVnf genericVnf)
    {
        this.genericVnf = genericVnf;
    }

    @JsonProperty("l3-interface-ipv4-address-list")
    public L3InterfaceIpv4AddressList getL3InterfaceIpv4AddressList() {
        return l3InterfaceIpv4AddressList;
    }

    @JsonProperty("l3-interface-ipv4-address-list")
    public void setL3InterfaceIpv4AddressList(L3InterfaceIpv4AddressList l3InterfaceIpv4AddressList) {
        this.l3InterfaceIpv4AddressList = l3InterfaceIpv4AddressList;
    }

    @JsonProperty("l3-interface-ipv6-address-list")
    public L3InterfaceIpv6AddressList getL3InterfaceIpv6AddressList() {
        return l3InterfaceIpv6AddressList;
    }

    @JsonProperty("l3-interface-ipv6-address-list")
    public void setL3InterfaceIpv6AddressList(L3InterfaceIpv6AddressList l3InterfaceIpv6AddressList) {
        this.l3InterfaceIpv6AddressList = l3InterfaceIpv6AddressList;
    }

    @XmlElement(name = "l3-network")
    public L3Network getL3Network() {
        return l3Network;
    }

    @XmlElement(name = "l3-network")
    public void setL3Network(L3Network l3Network) {
        this.l3Network = l3Network;
    }

    @XmlElement(name = "l-interface")
    public LInterface getLInterface() {
        return lInterface;
    }
    @XmlElement(name = "l-interface")
    public void setLInterface(LInterface linterface) {
        this.lInterface = linterface;
    }

//	@XmlElement(name = "owning-entity")
//	public OwningEntity getOwningEntity() {
//		return owningEntity;
//	}

//	@XmlElement(name = "owning-entity")
//	public void setOwningEntity(OwningEntity owningEntity) {
//		this.owningEntity = owningEntity;
//	}

    @XmlElement(name = "pserver")
    public Pserver getPserver() {
        return pserver;
    }
    @XmlElement(name = "pserver")
    public void setPserver(Pserver pserver) {
        this.pserver = pserver;
    }

    @XmlElement(name = "service-instance")
    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    @XmlElement(name = "service-instance")
    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    @XmlElement(name = "vnfc")
    public Vnfc getVnfc() {
        return vnfc;
    }

    @XmlElement(name = "vnfc")
    public void setVnfc(Vnfc vnfc) {
        this.vnfc = vnfc;
    }

    @XmlElement(name = "vserver")
    public Vserver getVserver() {
        return vserver;
    }

    @XmlElement(name = "vserver")
    public void setVserver(Vserver vserver) {
        this.vserver = vserver;
    }

    @Override
    public String toString()
    {
        return " [generic-vnf = "+genericVnf+"]";
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
    @XmlElement(name = "resource-type")
	public String getResourceType() {
		return resourceType;
	}
    @XmlElement(name = "resource-type")
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
    @XmlElement(name = "resource-link")
	public String getResourceLink() {
		return resourceLink;
	}
    @XmlElement(name = "resource-link")
	public void setResourceLink(String resourceLink) {
		this.resourceLink = resourceLink;
	}
    @XmlElement(name = "url")
	public String getUrl() {
		return url;
	}
    @XmlElement(name = "url")
	public void setUrl(String url) {
		this.url = url;
	}
}
