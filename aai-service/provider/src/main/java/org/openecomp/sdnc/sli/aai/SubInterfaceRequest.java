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

package org.openecomp.sdnc.sli.aai;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.openecomp.aai.inventory.v10.LInterface;
import org.openecomp.sdnc.sli.aai.data.AAIDatum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SubInterfaceRequest extends AAIRequest {

	// tenant (1602)
	public static final String PNF_LAGINTERFACE_SUBINTERFACE_PATH		= "org.openecomp.sdnc.sli.aai.path.pnf.lag.interface.subinterface";
	public static final String PNF_LAGINTERFACE_SUBINTERFACE_QUERY_PATH	= "org.openecomp.sdnc.sli.aai.path.pnf.lag.interface.subinterface.query";

	public static final String PNF_P_INTERFACE_SUBINTERFACE_PATH		= "org.openecomp.sdnc.sli.aai.path.pnf.p.interface.l.interface";
	public static final String PNF_P_INTERFACE_SUBINTERFACE_QUERY_PATH	= "org.openecomp.sdnc.sli.aai.path.pnf.p.interface.l.interface.query";

	private final String pnf_laginterface_subinterface_path;
	private final String pnf_laginterface_subinterface_query_path;
	private final String pnf_p_interface_subinterface_path;
	private final String pnf_p_interface_subinterface_query_path;

	public static final String SUBINTERFACE_INTERFACE_NAME 	= "l-interface.interface-name";
	public static final String LAG_INTERFACE_INTERFACE_NAME = "lag-interface.interface-name";
	public static final String PNF_PNF_NAME	= "pnf.pnf-name";
	public static enum TYPE { L2_BRIDGE_BGF, L2_BRIDGE_SBG};

	private final TYPE type;

	public SubInterfaceRequest(TYPE type) {
		this.type = type;

		pnf_laginterface_subinterface_path = configProperties.getProperty(PNF_LAGINTERFACE_SUBINTERFACE_PATH);
		pnf_laginterface_subinterface_query_path = configProperties.getProperty(PNF_LAGINTERFACE_SUBINTERFACE_QUERY_PATH);

		pnf_p_interface_subinterface_path = configProperties.getProperty(PNF_P_INTERFACE_SUBINTERFACE_PATH);
		pnf_p_interface_subinterface_query_path = configProperties.getProperty(PNF_P_INTERFACE_SUBINTERFACE_QUERY_PATH);
	}


	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = null;
		String encoded_vnf = null;

		String pnfname = null;
		String interfaceName = null;

		if(type == TYPE.L2_BRIDGE_SBG) {
			request_url = target_uri + pnf_laginterface_subinterface_path;

			if(requestProperties.containsKey(PNF_PNF_NAME)) {
				pnfname = requestProperties.getProperty(PNF_PNF_NAME);
				encoded_vnf = encodeQuery(pnfname);
				request_url = request_url.replace("{pnf-name}", encoded_vnf);
			}

			encoded_vnf = encodeQuery(requestProperties.getProperty(LAG_INTERFACE_INTERFACE_NAME));
			request_url = request_url.replace("{lag-interface.interface-name}", encoded_vnf) ;


			interfaceName = requestProperties.getProperty(SUBINTERFACE_INTERFACE_NAME);
			encoded_vnf = encodeQuery(interfaceName);
			request_url = request_url.replace("{interface-name}", encoded_vnf) ;

		}
		if(type == TYPE.L2_BRIDGE_BGF) {
			request_url = target_uri + pnf_p_interface_subinterface_path;

			if(requestProperties.containsKey(PNF_PNF_NAME)) {
				pnfname = requestProperties.getProperty(PNF_PNF_NAME);
				encoded_vnf = encodeQuery(pnfname);
				request_url = request_url.replace("{pnf-name}", encoded_vnf);
			}

			encoded_vnf = encodeQuery(requestProperties.getProperty(PInterfaceRequest.PINTERFACE_INTERFACE_NAME));
			request_url = request_url.replace("{p-interface.interface-name}", encoded_vnf) ;


			interfaceName = requestProperties.getProperty(SUBINTERFACE_INTERFACE_NAME);
			encoded_vnf = encodeQuery(interfaceName);
			request_url = request_url.replace("{interface-name}", encoded_vnf) ;
		}


		if(resourceVersion != null) {
			request_url = request_url +"?resource-version="+resourceVersion;
		}
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());

		if(pnfname != null)
			aaiService.LOGwriteDateTrace("pnf-name", pnfname);
		if(requestProperties.containsKey(LAG_INTERFACE_INTERFACE_NAME)) {
			aaiService.LOGwriteDateTrace("lag-interface.interface-name", requestProperties.getProperty(LAG_INTERFACE_INTERFACE_NAME));
		}
		if(requestProperties.containsKey(PInterfaceRequest.PINTERFACE_INTERFACE_NAME)) {
			aaiService.LOGwriteDateTrace("p-interface.interface-name", requestProperties.getProperty(PInterfaceRequest.PINTERFACE_INTERFACE_NAME));
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
			String[] tmpArray = {SUBINTERFACE_INTERFACE_NAME, LAG_INTERFACE_INTERFACE_NAME, PNF_PNF_NAME};
			args = tmpArray;
		}
		if(type == TYPE.L2_BRIDGE_BGF) {
			String[] tmpArray = {SUBINTERFACE_INTERFACE_NAME, PInterfaceRequest.PINTERFACE_INTERFACE_NAME, PNF_PNF_NAME};
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
		return "sub-interface";
	}

	public static final String processPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {
		String interfaceName = requestProperties.getProperty(SUBINTERFACE_INTERFACE_NAME);
		request_url = request_url.replace("{interface-name}", encodeQuery(interfaceName)) ;
		return request_url;
	}
}
