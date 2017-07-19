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

import org.openecomp.aai.inventory.v10.GenericVnf;
import org.openecomp.sdnc.sli.aai.data.AAIDatum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenericVnfRequest extends AAIRequest {

	// tenant (1602)
	public static final String GENERIC_VNF_PATH			= "org.openecomp.sdnc.sli.aai.path.generic.vnf";
	public static final String GENERIC_VNF_QUERY_PATH	= "org.openecomp.sdnc.sli.aai.path.generic.vnf.query";
	
	private final String generic_vnf_path;
	private final String generic_vnf_query_path;
	
	public static final String GENERIC_VNF_ID = "generic_vnf.vnf_id";
	public static final String VNF_ID = "vnf_id";


	public GenericVnfRequest() {
		generic_vnf_path = configProperties.getProperty(GENERIC_VNF_PATH);
		generic_vnf_query_path = configProperties.getProperty(GENERIC_VNF_QUERY_PATH);
	}

	
	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = target_uri+generic_vnf_path;
		String key = VNF_ID;
		if(requestProperties.containsKey(GENERIC_VNF_ID)) {
			key = GENERIC_VNF_ID;
		}
		
		if(!requestProperties.containsKey(key)) {
			aaiService.logKeyError(String.format("%s,%s", VNF_ID, GENERIC_VNF_ID));
		}
		
		String encoded_vnf = encodeQuery(requestProperties.getProperty(key));
		request_url = request_url.replace("{vnf-id}", encoded_vnf) ;
		if(resourceVersion != null) {
			request_url = request_url +"?resource-version="+resourceVersion;
		}
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());
		aaiService.LOGwriteDateTrace("vnf-id", requestProperties.getProperty(key));
		
		return http_req_url;
	}
	
	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = target_uri+generic_vnf_path;
		
		String key = VNF_ID;
		if(requestProperties.containsKey(GENERIC_VNF_ID)) {
			key = GENERIC_VNF_ID;
		}
		
		if(!requestProperties.containsKey(key)) {
			aaiService.logKeyError(String.format("%s,%s", VNF_ID, GENERIC_VNF_ID));
		}
		
		String encoded_vnf = encodeQuery(requestProperties.getProperty(key));
		request_url = request_url.replace("{vnf-id}", encoded_vnf) ;
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());
		aaiService.LOGwriteDateTrace("vnf-id", requestProperties.getProperty(key));
		
		return http_req_url;
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		GenericVnf tenant = (GenericVnf)requestDatum;
		String json_text = null;
		try {
			json_text = mapper.writeValueAsString(tenant);
		} catch (JsonProcessingException exc) {
			handleException(this, exc);
			return null;
		}
		return json_text;
	}


	@Override
	public String[] getArgsList() {
		String[] args = {VNF_ID, GENERIC_VNF_ID};
		return args;
	}


	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return GenericVnf.class;
	}


	public static String processPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {

		String key = VNF_ID;
		if(requestProperties.containsKey(GENERIC_VNF_ID)) {
			key = GENERIC_VNF_ID;
		}
		String encoded_vnf = encodeQuery(requestProperties.getProperty(key));
		request_url = request_url.replace("{vnf-id}", encoded_vnf) ;

		return request_url;
	}
}
