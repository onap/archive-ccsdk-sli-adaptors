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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.openecomp.sdnc.sli.aai.data.AAIDatum;
import org.openecomp.sdnc.sli.aai.query.FormattedQueryRequestData;
import org.openecomp.sdnc.sli.aai.query.FormattedQueryResultList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class CustomQueryRequest extends AAIRequest {

	public static final String GENERIC_SEARCH_PATH			= "org.openecomp.sdnc.sli.aai.query.generic";

	private final String generic_search_path;

	public static final String FORMAT = "format";


	public CustomQueryRequest() {
		String tmp_generic_search_path = configProperties.getProperty(GENERIC_SEARCH_PATH);
		tmp_generic_search_path = tmp_generic_search_path.split("search")[0];
		generic_search_path = tmp_generic_search_path +"query";
	}


	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = target_uri+generic_search_path;

		request_url = processPathData(request_url, requestProperties);

		String formatQuery = requestProperties.getProperty(FORMAT);

		if(formatQuery != null) {
			request_url = request_url +"?format="+formatQuery;
		}
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());

		return http_req_url;
	}

	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return getRequestUrl(method, null);
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		FormattedQueryRequestData tenant = (FormattedQueryRequestData)requestDatum;
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
		String[] args = {FORMAT};
		return args;
	}


	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return FormattedQueryRequestData.class;
	}


	public static String processPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {

		String key = FORMAT;

		String encoded_vnf = encodeQuery(requestProperties.getProperty(key));
		request_url = request_url.replace("{identifier}", encoded_vnf) ;
		aaiService.LOGwriteDateTrace("identifier", requestProperties.getProperty(key));

		return request_url;
	}

	public AAIDatum jsonStringToObject(String jsonData) throws JsonParseException, JsonMappingException, IOException {
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
