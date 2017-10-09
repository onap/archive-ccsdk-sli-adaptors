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

package org.onap.ccsdk.sli.adaptors.aai;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateRequest extends AAIRequest {

	private AAIRequest request;

	private Map<String, String> params;

	/**
	 * @param request
	 * @param parms
	 */
	public UpdateRequest(AAIRequest request, Map<String, String> parms) {
		this.request = request;
		this.params = parms;
	}

	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.AAIRequest#getRequestUrl(java.lang.String, java.lang.String)
	 */
	@Override
	public URL getRequestUrl(String method, String resourceVersion)
			throws UnsupportedEncodingException, MalformedURLException {
		return request.getRequestUrl(method, resourceVersion);
	}

	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return request.getRequestQueryUrl(method);
	}

	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.AAIRequest#toJSONString()
	 */
	@Override
	public String toJSONString() {
		ObjectMapper mapper = AAIService.getObjectMapper();
		String json = null;

		try {
			json = mapper.writeValueAsString(params);
		} catch (JsonProcessingException e) {
			LOG.error("Could not convert parameters of " + request.getRequestObject().getClass().getName(), e);
		}

		return json;
	}

	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.AAIRequest#getArgsList()
	 */
	@Override
	public String[] getArgsList() {
		return request.getArgsList();
	}

	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.AAIRequest#getModelClass()
	 */
	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return request.getModelClass();
	}

	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.AAIRequest#addRequestProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public void addRequestProperty(String key, String value) {
		request.requestProperties.put(key, value);
	}

	/**
	 * @param request_url
	 * @param requestProperties
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String processPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {
		return request_url;
	}

	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.AAIRequest#processRequestPathValues(java.util.Map)
	 */
	public void processRequestPathValues(Map<String, String> nameValues) {
		request.processRequestPathValues(nameValues);
	}

}
