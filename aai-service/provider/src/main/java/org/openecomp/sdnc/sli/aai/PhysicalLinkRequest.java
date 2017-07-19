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

import org.openecomp.aai.inventory.v10.PhysicalLink;
import org.openecomp.sdnc.sli.aai.data.AAIDatum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PhysicalLinkRequest extends AAIRequest {

	// physical link
	public static final String PHYSICAL_LINK_PATH		= "org.openecomp.sdnc.sli.aai.path.physical.link";
	public static final String PHYSICAL_LINK_QUERY_PATH	= "org.openecomp.sdnc.sli.aai.path.physical.link.query";
	
	private final String physical_link_path;
	private final String physical_link_query_path;
	
	public static final String LINK_NAME			= "link-name";
	public static final String PHYSICAL_LINK_NAME	= "physical-link.link-name";


	public PhysicalLinkRequest() {
		physical_link_path = configProperties.getProperty(PHYSICAL_LINK_PATH);
		physical_link_query_path = configProperties.getProperty(PHYSICAL_LINK_QUERY_PATH);
	}

	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return this.getRequestUrl(method, null);
	}
	
	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = target_uri+physical_link_path;
	
		String linkName = null;
		if(requestProperties.containsKey(LINK_NAME)) {
			linkName = requestProperties.getProperty(LINK_NAME);
		}

		if(requestProperties.containsKey(PHYSICAL_LINK_NAME)) {
			linkName = requestProperties.getProperty(PHYSICAL_LINK_NAME);
		}

		
		String encoded_vnf = encodeQuery(linkName);
		request_url = request_url.replace("{link-name}", encoded_vnf) ;

		if(resourceVersion != null) {
			request_url = request_url +"?resource-version="+resourceVersion;
		}
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());
		aaiService.LOGwriteDateTrace("link-name", linkName);
		
		return http_req_url;
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		PhysicalLink vpe = (PhysicalLink)requestDatum;
		String json_text = null;
		try {
			json_text = mapper.writeValueAsString(vpe);
		} catch (JsonProcessingException exc) {
			handleException(this, exc);
			return null;
		}
		return json_text;
	}

	@Override
	public String[] getArgsList() {
		String[] args =  {LINK_NAME, PHYSICAL_LINK_NAME};

		return args;
	}

	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return PhysicalLink.class;
	}
}
