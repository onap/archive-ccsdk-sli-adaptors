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
/**
 * @author Rich Tabedzki
 *
 */
package org.onap.ccsdk.sli.adaptors.aai;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PathRequest extends AAIRequest {
	
	private final  Class<? extends AAIDatum> classType;
	
	public static final String RESOURCE_PATH = "resource-path";

	public PathRequest(Class<?> type) {
		classType = (Class<? extends AAIDatum>)type;
	}

	
	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = targetUri + "{resource-path}";
		
		String encoded_vnf = requestProperties.getProperty(RESOURCE_PATH);
		request_url = request_url.replace("{resource-path}", encoded_vnf) ;
		
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());
		
		return http_req_url;
	}
	
	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return this.getRequestUrl(method, null);
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		String json_text = null;
		try {
			json_text = mapper.writeValueAsString(classType);
		} catch (JsonProcessingException exc) {
			handleException(this, exc);
			return null;
		}
		return json_text;
	}

	@Override
	public String[] getArgsList() {
		String[] args = {RESOURCE_PATH};
		return args;
	}
	
	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return classType;
	}
}
