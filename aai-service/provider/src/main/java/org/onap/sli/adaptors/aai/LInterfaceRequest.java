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

import org.openecomp.aai.inventory.v10.LInterface;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LInterfaceRequest extends AAIRequest {

	// tenant (1602)
	public static final String LAGINTERFACE_LINTERFACE_PATH			= "org.onap.ccsdk.sli.adaptors.aai.path.lag.interface.l.interface";
	public static final String LAGINTERFACE_LINTERFACE_QUERY_PATH	= "org.onap.ccsdk.sli.adaptors.aai.path.lag.interface.l.interface.query";

	public static final String P_INTERFACE_LINTERFACE_PATH			= "org.onap.ccsdk.sli.adaptors.aai.path.p.interface.l.interface";
	public static final String P_INTERFACE_LINTERFACE_QUERY_PATH	= "org.onap.ccsdk.sli.adaptors.aai.path.p.interface.l.interface.query";

	public static final String LAGINTERFACE_LINTERFACE_PNF_PATH		= "org.onap.ccsdk.sli.adaptors.aai.path.lag.interface.l.interface.pnf";
	public static final String P_INTERFACE_LINTERFACE_PNF_PATH		= "org.onap.ccsdk.sli.adaptors.aai.path.p.interface.l.interface.pnf";

	private final String laginterface_linterface_path;
	private final String laginterface_linterface_query_path;
	private final String p_interface_linterface_path;
	private final String p_interface_linterface_query_path;

	private final String laginterface_linterface_pnf_path;
	private final String p_interface_linterface_pnf_path;

	public static final String INTERFACE_NAME = "interface-name";
	public static final String LINTERFACE_INTERFACE_NAME 	= "l-interface.interface-name";
	public static final String LAG_INTERFACE_INTERFACE_NAME = "lag-interface.interface-name";
	public static final String P_INTERFACE_INTERFACE_NAME 	= "p-interface.interface-name";
	public static final String PNF_PNF_NAME	= "pnf.pnf-name";

	public static final String ROUTER_NAME = "router-name";
	public static final String HOSTNAME = "hostname";


	public static enum TYPE { L2_BRIDGE_BGF, L2_BRIDGE_SBG};

	private final TYPE type;

	public LInterfaceRequest(TYPE type) {
		this.type = type;

		laginterface_linterface_path = configProperties.getProperty(LAGINTERFACE_LINTERFACE_PATH);
		laginterface_linterface_query_path = configProperties.getProperty(LAGINTERFACE_LINTERFACE_QUERY_PATH);

		p_interface_linterface_path = configProperties.getProperty(P_INTERFACE_LINTERFACE_PATH);
		p_interface_linterface_query_path = configProperties.getProperty(P_INTERFACE_LINTERFACE_QUERY_PATH);

		laginterface_linterface_pnf_path = configProperties.getProperty(LAGINTERFACE_LINTERFACE_PNF_PATH);
		p_interface_linterface_pnf_path = configProperties.getProperty(P_INTERFACE_LINTERFACE_PNF_PATH);
	}


	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = null;
		String encoded_vnf = null;
		String hostname = null;
		String pnfname = null;
		String interfaceName = null;

		if(type == TYPE.L2_BRIDGE_SBG) {
			if(requestProperties.containsKey(PNF_PNF_NAME)) {
				request_url = target_uri + laginterface_linterface_pnf_path;
			} else {
				request_url = target_uri + laginterface_linterface_path;
			}

			if(requestProperties.containsKey(ROUTER_NAME)) {
				hostname = requestProperties.getProperty(ROUTER_NAME);
				encoded_vnf = encodeQuery(hostname);
				request_url = request_url.replace("{hostname}", encoded_vnf);
			}

			if(requestProperties.containsKey(HOSTNAME)) {
				hostname = requestProperties.getProperty(HOSTNAME);
				encoded_vnf = encodeQuery(hostname);
				request_url = request_url.replace("{hostname}", encoded_vnf);
			}

			if(requestProperties.containsKey(PNF_PNF_NAME)) {
				pnfname = requestProperties.getProperty(PNF_PNF_NAME);
				encoded_vnf = encodeQuery(pnfname);
				request_url = request_url.replace("{pnf-name}", encoded_vnf);
			}

			encoded_vnf = encodeQuery(requestProperties.getProperty(LAG_INTERFACE_INTERFACE_NAME));
			request_url = request_url.replace("{lag-interface.interface-name}", encoded_vnf) ;


			interfaceName = requestProperties.getProperty(INTERFACE_NAME);
			if(interfaceName == null || interfaceName.isEmpty()) {
				interfaceName = requestProperties.getProperty(LINTERFACE_INTERFACE_NAME);
			}
			encoded_vnf = encodeQuery(interfaceName);
			request_url = request_url.replace("{interface-name}", encoded_vnf) ;

		}
		if(type == TYPE.L2_BRIDGE_BGF) {
			if(requestProperties.containsKey(PNF_PNF_NAME)) {
				request_url = target_uri + p_interface_linterface_pnf_path;
			} else {
				request_url = target_uri + p_interface_linterface_path;
			}


			if(requestProperties.containsKey(ROUTER_NAME)) {
				hostname = requestProperties.getProperty(ROUTER_NAME);
				encoded_vnf = encodeQuery(hostname);
				request_url = request_url.replace("{hostname}", encoded_vnf);
			}

			if(requestProperties.containsKey(HOSTNAME)) {
				hostname = requestProperties.getProperty(HOSTNAME);
				encoded_vnf = encodeQuery(hostname);
				request_url = request_url.replace("{hostname}", encoded_vnf);
			}

			if(requestProperties.containsKey(PNF_PNF_NAME)) {
				pnfname = requestProperties.getProperty(PNF_PNF_NAME);
				encoded_vnf = encodeQuery(pnfname);
				request_url = request_url.replace("{pnf-name}", encoded_vnf);
			}

			encoded_vnf = encodeQuery(requestProperties.getProperty(P_INTERFACE_INTERFACE_NAME));
			request_url = request_url.replace("{p-interface.interface-name}", encoded_vnf) ;


			interfaceName = requestProperties.getProperty(INTERFACE_NAME);
			if(interfaceName == null || interfaceName.isEmpty()) {
				interfaceName = requestProperties.getProperty(LINTERFACE_INTERFACE_NAME);
			}
			encoded_vnf = encodeQuery(interfaceName);
			request_url = request_url.replace("{interface-name}", encoded_vnf) ;
		}


		if(resourceVersion != null) {
			request_url = request_url +"?resource-version="+resourceVersion;
		}
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());
		if(hostname != null)
			aaiService.LOGwriteDateTrace("hostname", hostname);
		if(pnfname != null)
			aaiService.LOGwriteDateTrace("pnf-name", pnfname);
		if(requestProperties.containsKey(LAG_INTERFACE_INTERFACE_NAME)) {
			aaiService.LOGwriteDateTrace("lag-interface.interface-name", requestProperties.getProperty(LAG_INTERFACE_INTERFACE_NAME));
		}
		if(requestProperties.containsKey(P_INTERFACE_INTERFACE_NAME)) {
			aaiService.LOGwriteDateTrace("p-interface.interface-name", requestProperties.getProperty(P_INTERFACE_INTERFACE_NAME));
		}
		aaiService.LOGwriteDateTrace("interface-name", interfaceName);

		return http_req_url;
	}

	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return this.getRequestUrl(method, null);
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		LInterface vnfc = (LInterface)requestDatum;
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
		String[] args = {};
		if(type == TYPE.L2_BRIDGE_SBG) {
			String[] tmpArray = {INTERFACE_NAME, LINTERFACE_INTERFACE_NAME, LAG_INTERFACE_INTERFACE_NAME, HOSTNAME, ROUTER_NAME, PNF_PNF_NAME};
			args = tmpArray;
		}
		if(type == TYPE.L2_BRIDGE_BGF) {
			String[] tmpArray = {INTERFACE_NAME, LINTERFACE_INTERFACE_NAME, P_INTERFACE_INTERFACE_NAME, HOSTNAME, ROUTER_NAME, PNF_PNF_NAME};
			args = tmpArray;
		}

		return args;
	}

	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return LInterface.class;
	}

	@Override
	public String getPrimaryResourceName(String resource) {
		return "l-interface";
	}

	public static final String processPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {
		String interfaceName = requestProperties.getProperty(INTERFACE_NAME);
		if(interfaceName == null || interfaceName.isEmpty()) {
			interfaceName = requestProperties.getProperty(LINTERFACE_INTERFACE_NAME);
		}

		request_url = request_url.replace("{interface-name}", encodeQuery(interfaceName)) ;
		return request_url;
	}
}
