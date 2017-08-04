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

import org.openecomp.aai.inventory.v10.PInterface;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PInterfaceRequest extends AAIRequest {

	// tenant (1602)
	public static final String PINTERFACE_PATH			= "org.onap.ccsdk.sli.adaptors.aai.path.pserver.pinterface";
	public static final String PINTERFACE_QUERY_PATH	= "org.onap.ccsdk.sli.adaptors.aai.path.pserver.pinterface.query";
	
	private final String pinterface_path;
	private final String pinterface_query_path;
	
	public static final String HOSTNAME = "hostname";
	public static final String PSERVER_HOSTNAME = "pserver.hostname";
	public static final String INTERFACE_NAME = "interface-name";
	public static final String PINTERFACE_INTERFACE_NAME = "p-interface.interface-name";


	public PInterfaceRequest() {
		pinterface_path = configProperties.getProperty(PINTERFACE_PATH);
		pinterface_query_path = configProperties.getProperty(PINTERFACE_QUERY_PATH);
		LoggerFactory.getLogger(PInterfaceRequest.class).debug("org.onap.ccsdk.sli.adaptors.aai.path.pserver.pinterface=\t" + pinterface_path);
		LoggerFactory.getLogger(PInterfaceRequest.class).debug("org.onap.ccsdk.sli.adaptors.aai.path.pserver.pinterface.query=\t" + pinterface_query_path);
		if(pinterface_path == null) {
			LoggerFactory.getLogger(PInterfaceRequest.class).warn("org.onap.ccsdk.sli.adaptors.aai.path.pserver.pinterface PATH not found in aaiclient.properties");
		}
	}

	
	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = target_uri + pinterface_path;
		String encoded_vnf = null;
		
		String hostname = null;
		String interfaceName = null;

		if(requestProperties.containsKey(HOSTNAME)) {
			hostname = requestProperties.getProperty(HOSTNAME);
		}
		if(requestProperties.containsKey(PSERVER_HOSTNAME)) {
			hostname = requestProperties.getProperty(PSERVER_HOSTNAME);
		}
		
		if(requestProperties.containsKey(INTERFACE_NAME)) {
			interfaceName = requestProperties.getProperty(INTERFACE_NAME);
		}
		if(requestProperties.containsKey(PINTERFACE_INTERFACE_NAME)) {
			interfaceName = requestProperties.getProperty(PINTERFACE_INTERFACE_NAME);
		}

		encoded_vnf = encodeQuery(hostname);
		request_url = request_url.replace("{hostname}", encoded_vnf) ;

		encoded_vnf = encodeQuery(interfaceName);
		request_url = request_url.replace("{interface-name}", encoded_vnf) ;

		if(resourceVersion != null) {
			request_url = request_url +"?resource-version="+resourceVersion;
		}
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());
		aaiService.LOGwriteDateTrace("hostname", hostname);
		aaiService.LOGwriteDateTrace("interface-name", hostname);
		
		return http_req_url;
	}
	
	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return this.getRequestUrl(method, null);
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		PInterface vnfc = (PInterface)requestDatum;
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
		String[] args = {HOSTNAME, PSERVER_HOSTNAME, INTERFACE_NAME, PINTERFACE_INTERFACE_NAME};
		return args;
	}
	
	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return PInterface.class;
	}
}
