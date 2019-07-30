/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * Modifications Copyright (C) 2018 IBM.
 * Modifications Copyright (C) 2019 IBM.
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.onap.ccsdk.sli.adaptors.aai.query.FormattedQueryRequestData;
import org.onap.ccsdk.sli.adaptors.aai.query.FormattedQueryResultList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class CustomQueryRequest extends AAIRequest {

	public static final String GENERIC_SEARCH_PATH_CONST			= "org.onap.ccsdk.sli.adaptors.aai.query.generic";

	private final String generic_search_path;

	public static final String FORMAT = "format";


	public CustomQueryRequest() {
		String tmpGenericSearchPath = configProperties.getProperty(GENERIC_SEARCH_PATH_CONST);
		tmpGenericSearchPath = tmpGenericSearchPath.split("search")[0];
		generic_search_path = tmpGenericSearchPath +"query";
	}


	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String requestUrl = targetUri+generic_search_path;

		requestUrl = processPathData(requestUrl, requestProperties);

		String formatQuery = requestProperties.getProperty(FORMAT);

		if(formatQuery != null) {
			requestUrl = requestUrl +"?format="+formatQuery;
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
		FormattedQueryRequestData tenant = (FormattedQueryRequestData)requestDatum;
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
		String[] args = {FORMAT};
		return args;
	}


	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return FormattedQueryRequestData.class;
	}


	public static String processPathData(String requestUrl, Properties requestProperties) throws UnsupportedEncodingException {

		String key = FORMAT;

		String encodedVnf = encodeQuery(requestProperties.getProperty(key));
		requestUrl = requestUrl.replace("{identifier}", encodedVnf) ;
		aaiService.LOGwriteDateTrace("identifier", requestProperties.getProperty(key));

		return requestUrl;
	}
	
	@Override
	public AAIDatum jsonStringToObject(String jsonData) throws IOException {
		if(jsonData == null) {
			return null;
		}

		AAIDatum response = null;
		ObjectMapper mapper = getObjectMapper();
		response = mapper.readValue(jsonData, FormattedQueryResultList.class);
		return response;
	}

	protected boolean expectsDataFromPUTRequest() {
		return true;
	}
}
