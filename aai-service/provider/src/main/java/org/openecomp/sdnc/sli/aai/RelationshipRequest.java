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

import org.openecomp.sdnc.sli.aai.data.AAIDatum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openecomp.aai.inventory.v11.RelationshipList;

public class RelationshipRequest extends AAIRequest {

	// tenant (1602)
	public static final String RELATIONSHIP_LIST_PATH		= "org.openecomp.sdnc.sli.aai.path.relationship.list";
	public static final String RELATIONSHIP_LIST_QUERY_PATH	= "org.openecomp.sdnc.sli.aai.path.relationship.list.query";
	
	private final String relationship_path;
	private final String relationship_query_path;
	
	public static final String RELATED_TO = "related-to";
	public static final String RELATIONSHIP_KEY = "relationship-key";

	public RelationshipRequest() {
		relationship_path = configProperties.getProperty(RELATIONSHIP_LIST_PATH, "/relationship-list/relationship");
		relationship_query_path = configProperties.getProperty(RELATIONSHIP_LIST_QUERY_PATH);
	}

	
	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		AAIRequest masterRequest = (AAIRequest)requestProperties.get(MASTER_REQUEST);
		URL masterURL = masterRequest.getRequestUrl(method, null);
		
		String request_url = masterURL.toString();
		request_url = request_url + relationship_path;
		
		if(request_url.contains("//")) {
			request_url = request_url.replaceAll("//", "/");
		}

		if(requestProperties.containsKey(RELATED_TO)) {
			String encoded_vnf = encodeQuery(requestProperties.getProperty(RELATED_TO));
			request_url = request_url.replace("{related-to}", encoded_vnf) ;			
		}

//		if(resourceVersion != null) {
//			request_url = request_url +"?resource-version="+resourceVersion;
//		}
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());
		aaiService.LOGwriteDateTrace("related-to", requestProperties.getProperty(RELATED_TO));
		
		return http_req_url;
	}
	
	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = target_uri+relationship_query_path;
		String encoded_vnf = encodeQuery(requestProperties.getProperty(RELATIONSHIP_KEY));
		request_url = request_url.replace("{tenant-name}", encoded_vnf) ;
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());
		aaiService.LOGwriteDateTrace("tenant_name", requestProperties.getProperty(RELATIONSHIP_KEY));
		
		return http_req_url;
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		Object tenant = requestDatum;
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
		String[] args = {RELATED_TO, RELATIONSHIP_KEY};
		return args;
	}


	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return RelationshipList.class;
	}
	
	public boolean isDeleteDataRequired() {
		return true;
	}
}
