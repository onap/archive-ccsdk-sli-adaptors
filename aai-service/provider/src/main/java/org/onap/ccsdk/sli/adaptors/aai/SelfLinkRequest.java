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
import java.net.URI;
import java.net.URL;

import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

public class SelfLinkRequest extends AAIRequest {

	private final  Class<? extends AAIDatum> classType;

	public static final String SELFLINK = "selflink";

	public SelfLinkRequest(Class<?> type) {
		classType = (Class<? extends AAIDatum>)type;
	}


	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.AAIRequest#getRequestUrl(java.lang.String, java.lang.String)
	 */
	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = null;

		request_url = requestProperties.getProperty(SELFLINK);
		try {
			URI uri = new URI(request_url);
			if(uri.getHost() == null) {
				request_url = targetUri + request_url;
			}
		} catch(Exception exc) {
			LOG.error("SelfLinkRequest.getRequestUrl", exc);
		}
		String query = null;

		if(request_url.contains("?")) {
			query = request_url.substring(request_url.indexOf("?"));
			Joiner.MapJoiner mapJoiner = Joiner.on(",").withKeyValueSeparator("=");
//			String queryString = mapJoiner.join(query);
		} else {
			request_url = request_url + "?depth=1";
		}

		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());

		return http_req_url;
	}

	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return this.getRequestUrl(method, null);
	}


	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.AAIRequest#toJSONString()
	 */
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

	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.AAIRequest#getArgsList()
	 */
	@Override
	public String[] getArgsList() {
		String[] args = {SELFLINK};
		return args;
	}

	/* (non-Javadoc)
	 * @see org.onap.ccsdk.sli.adaptors.aai.AAIRequest#getModelClass()
	 */
	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return classType;
	}
}
