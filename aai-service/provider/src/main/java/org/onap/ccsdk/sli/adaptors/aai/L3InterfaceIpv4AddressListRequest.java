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

package org.onap.ccsdk.sli.adaptors.aai;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.openecomp.aai.inventory.v11.L3InterfaceIpv4AddressList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class L3InterfaceIpv4AddressListRequest extends AAIRequest {

	/*
	 * Note: there are 3 possible paths:
	 * /network/generic-vnfs/generic-vnf/{vnf-id}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /network/generic-vnfs/generic-vnf/{vnf-id}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /network/generic-vnfs/generic-vnf/{vnf-id}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /network/generic-vnfs/generic-vnf/{vnf-id}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/newvces/newvce/{vnf-id2}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region-id}/tenants/tenant/{tenant-id}/vservers/vserver/{vserver-id}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/cloud-infrastructure/cloud-regions/cloud-region/{cloud-owner}/{cloud-region-id}/tenants/tenant/{tenant-id}/vservers/vserver/{vserver-id}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/cloud-infrastructure/cloud-regions/pservers/pserver/{hostname}/p-interfaces/p-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/cloud-infrastructure/cloud-regions/pservers/pserver/{hostname}/p-interfaces/p-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/cloud-infrastructure/cloud-regions/pservers/pserver/{hostname}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/cloud-infrastructure/cloud-regions/pservers/pserver/{hostname}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/newvces/newvce/{vnf-id2}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/vpes/vpe/{vnf-id}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/vpes/vpe/{vnf-id}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/vpes/vpe/{vnf-id}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/vpes/vpe/{vnf-id}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/vpls-pes/vpls-pe/{equipment-name}/p-interfaces/p-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/vpls-pes/vpls-pe/{equipment-name}/p-interfaces/p-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/vpls-pes/vpls-pe/{equipment-name}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/vpls-pes/vpls-pe/{equipment-name}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/pnfs/pnf/{pnf-name}/p-interfaces/p-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 * /inventory/network/pnfs/pnf/{pnf-name}/p-interfaces/p-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}
	 */

	public static final String L3_INTERFACE_IPV4_ADDRESS_LIST_PATH	=     "org.onap.ccsdk.sli.adaptors.aai.path.l3.interface.ipv4.address.list";
	public static final String VLAN_L3_INTERFACE_IPV4_ADDRESS_LIST_PATH	= "org.onap.ccsdk.sli.adaptors.aai.path.vlan.l3.interface.ipv4.address.list";

	private final String l3_interface_ipv4_address_list_path;
	private final String vlan_l3_interface_ipv4_address_list_path;

	public static final String L3_INTERFACE_IPV4_ADDRESS	= "l3-interface-ipv4-address";
	public static final String LIST_L3_INTERFACE_IPV4_ADDRESS	= "l3-interface-ipv4-address-list.l3-interface-ipv4-address";
	public static final String VLAN_INTERFACE	= "vlan-interface";
	public static final String VLAN_VLAN_INTERFACE	= "vlan.vlan-interface";

	public L3InterfaceIpv4AddressListRequest() {
		l3_interface_ipv4_address_list_path = configProperties.getProperty(L3_INTERFACE_IPV4_ADDRESS_LIST_PATH);
		vlan_l3_interface_ipv4_address_list_path = configProperties.getProperty(VLAN_L3_INTERFACE_IPV4_ADDRESS_LIST_PATH);
	}

	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return this.getRequestUrl(method, null);
	}

	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = target_uri+l3_interface_ipv4_address_list_path;

		if(requestProperties.containsKey(VLAN_INTERFACE) || requestProperties.containsKey(VLAN_VLAN_INTERFACE)){
			request_url = target_uri+vlan_l3_interface_ipv4_address_list_path;
			request_url = processVLanRequestPathData(request_url, requestProperties);
		}

		request_url = processPathData(request_url, requestProperties);
		request_url = LInterfaceRequest.processPathData(request_url, requestProperties);
//		request_url = GenericVnfRequest.processPathData(request_url, requestProperties);

		if(resourceVersion != null) {
			request_url = request_url +"?resource-version="+resourceVersion;
		}
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());

		return http_req_url;
	}

	public static String processVLanRequestPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {
		String key = null;

		if(requestProperties.containsKey(VLAN_VLAN_INTERFACE)) {
			key = VLAN_VLAN_INTERFACE;
		} else if(requestProperties.containsKey(VLAN_INTERFACE)) {
			key = VLAN_INTERFACE;
		} else {
			aaiService.logKeyError(String.format("%s,%s", VLAN_INTERFACE, VLAN_VLAN_INTERFACE));
		}

		String encoded_vnf = encodeQuery(requestProperties.getProperty(key));

		request_url = request_url.replace("{vlan-interface}", encoded_vnf) ;
		aaiService.LOGwriteDateTrace("vlan-interface", requestProperties.getProperty(key));
		return request_url;
	}

	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		L3InterfaceIpv4AddressList vnfc = (L3InterfaceIpv4AddressList)requestDatum;
		String json_text = null;
		try {
			json_text = mapper.writeValueAsString(vnfc);
		} catch (JsonProcessingException exc) {
			handleException(this, exc);
			return null;
		}
		return json_text;
	}

	@Override
	public String[] getArgsList() {
		String[] args =
			{
				L3_INTERFACE_IPV4_ADDRESS,
				LIST_L3_INTERFACE_IPV4_ADDRESS,
				VLAN_INTERFACE,
				VLAN_VLAN_INTERFACE,
				LInterfaceRequest.INTERFACE_NAME,
				LInterfaceRequest.LINTERFACE_INTERFACE_NAME,
				"generic-vnf.vnf-id"
			};

		return args;
	}

	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return L3InterfaceIpv4AddressList.class;
	}

	public static String processPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {
		String key = L3_INTERFACE_IPV4_ADDRESS;
		if(requestProperties.containsKey(LIST_L3_INTERFACE_IPV4_ADDRESS)) {
			key = LIST_L3_INTERFACE_IPV4_ADDRESS;
		}

		if(!requestProperties.containsKey(key)) {
			aaiService.logKeyError(String.format("%s,%s", L3_INTERFACE_IPV4_ADDRESS, LIST_L3_INTERFACE_IPV4_ADDRESS));
		}

		String encoded_vnf = encodeQuery(requestProperties.getProperty(key));

		request_url = request_url.replace("{l3-interface-ipv4-address}", encoded_vnf) ;

		aaiService.LOGwriteDateTrace("l3-interface-ipv4-address", requestProperties.getProperty(key));
		return request_url;
	}

	/*
	 * (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.PathConfigurator#configurePath(java.lang.String)
	 */
//	@Override
//	public void processRequestPathValues(Map<String, String> nameValues) {
//		String[] paths =
//		{
//		 "/network/generic-vnfs/generic-vnf/{vnf-id}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}",
//		 "/network/generic-vnfs/generic-vnf/{vnf-id}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}",
//		 "/network/generic-vnfs/generic-vnf/{vnf-id}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}",
//		 "/network/generic-vnfs/generic-vnf/{vnf-id}/lag-interfaces/lag-interface/{interface-name}/l-interfaces/l-interface/{interface-name}/vlans/vlan/{vlan-interface}/l3-interface-ipv4-address-list/{l3-interface-ipv4-address}"
//		};
//
//		List<String>keys = new LinkedList<String>();
//		keys.addAll(nameValues.keySet());
//		for(String key : keys) {
//			if(key.contains(".")) {
//				key = key.replaceAll("_", "-");
//				if(!LIST_L3_INTERFACE_IPV4_ADDRESS.equals(key)) {
//					int index = key.indexOf(".");
//					String resource = key.substring(0, index);
//					AAIRequest rx = createRequest(resource, nameValues);
//					if(rx != null) {
//						pathElements.add(rx);
//					}
//				}
//			}
//		}
//		super.processRequestPathValues(nameValues);
//	}
//	List<AAIRequest> pathElements = new LinkedList<AAIRequest>();

}
