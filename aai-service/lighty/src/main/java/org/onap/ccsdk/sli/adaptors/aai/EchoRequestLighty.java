/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * Modifications Copyright (C) 2018 IBM.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.onap.ccsdk.sli.adaptors.aai.data.EchoResponse;

/**
 * THIS CLASS IS A COPY OF {@link EchoRequest} WITH REMOVED OSGi DEPENDENCIES
 */
public class EchoRequestLighty extends AAIRequestLighty {



	private final String echoPath;

	public EchoRequestLighty() {
		echoPath = "/aai/util/echo";
	}


	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String requestUrl = targetUri+echoPath;

		if(resourceVersion != null) {
			requestUrl = requestUrl +"?resource-version="+resourceVersion;
		}
		URL httpReqUrl =	new URL(requestUrl);

		aaiService.LOGwriteFirstTrace(method, httpReqUrl.toString());

		return httpReqUrl;
	}

	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return getRequestUrl(method, null);
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		EchoResponse tenant = (EchoResponse)requestDatum;
		String jsonText = null;
		try {
			jsonText = mapper.writeValueAsString(tenant);
		} catch (JsonProcessingException exc) {
			handleException(this, exc);
			return null;
		}
		return jsonText;
	}


	@Override
	public String[] getArgsList() {
		String[] args = {};
		return args;
	}


	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return EchoResponse.class;
	}

}
