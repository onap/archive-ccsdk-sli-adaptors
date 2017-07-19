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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openecomp.aai.inventory.v10.CloudRegion;
import org.openecomp.sdnc.sli.aai.data.AAIDatum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

public class CloudRegionRequest extends AAIRequest {

	public static final String CLOUD_REGION_PATH	= "org.openecomp.sdnc.sli.aai.path.cloud.region";
	
	private final String cloud_region_path;
	
	public static final String CLOUD_REGION_CLOUD_OWNER	= "cloud-region.cloud-owner";
	public static final String CLOUD_REGION_CLOUD_REGION_ID	= "cloud-region.cloud-region-id";


	public CloudRegionRequest() {
		cloud_region_path = configProperties.getProperty(CLOUD_REGION_PATH);
	}

	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return this.getRequestUrl(method, null);
	}
	
	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = target_uri+cloud_region_path;

		request_url = processPathData(request_url, requestProperties);

		Map<String, String> query = new HashMap<String, String>();
		if(requestProperties.containsKey(DEPTH)) {
			query.put(DEPTH, requestProperties.getProperty(DEPTH));
		}

		if(resourceVersion != null) {
//			request_url = request_url +"?resource-version="+resourceVersion;
			query.put(RESOURCE_VERSION, resourceVersion);
		}
		
		if(!query.isEmpty()) {
			Joiner.MapJoiner mapJoiner = Joiner.on("&").withKeyValueSeparator("=");
			String queryString = mapJoiner.join(query);
			request_url = String.format("%s?%s", request_url, queryString);
		}
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());
		
		return http_req_url;
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		CloudRegion vnfc = (CloudRegion)requestDatum;
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
		String[] args =  
			{
				CLOUD_REGION_CLOUD_OWNER,
				CLOUD_REGION_CLOUD_REGION_ID,
				DEPTH
			};

		return args;
	}

	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return CloudRegion.class;
	}
	
	public static String processPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {

		if(!requestProperties.containsKey(CLOUD_REGION_CLOUD_OWNER) || !requestProperties.containsKey(CLOUD_REGION_CLOUD_REGION_ID)) {
			aaiService.logKeyError(String.format("%s,%s", CLOUD_REGION_CLOUD_OWNER, CLOUD_REGION_CLOUD_REGION_ID));
		}
		
		String encoded_vnf = encodeQuery(requestProperties.getProperty(CLOUD_REGION_CLOUD_OWNER));
		request_url = request_url.replace("{cloud-owner}", encoded_vnf) ;

		encoded_vnf = encodeQuery(requestProperties.getProperty(CLOUD_REGION_CLOUD_REGION_ID));
		request_url = request_url.replace("{cloud-region-id}", encoded_vnf) ;

		aaiService.LOGwriteDateTrace("cloud-owner", requestProperties.getProperty(CLOUD_REGION_CLOUD_OWNER));
		aaiService.LOGwriteDateTrace("cloud-region-id", requestProperties.getProperty(CLOUD_REGION_CLOUD_REGION_ID));
		
		return request_url;
	}
}
