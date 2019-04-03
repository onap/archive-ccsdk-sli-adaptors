/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *             reserved.
 * ================================================================================
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
import java.util.Properties;
import org.onap.aai.inventory.v16.SearchResults;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;

public class NodesQueryRequestLighty extends AAIRequestLighty {

    public static final String NODES_SEARCH_PATH            = "org.onap.ccsdk.sli.adaptors.aai.query.nodes";

    private final String nodes_search_path;

    public static final String NODE_TYPE = "node-type";
    public static final String ENTITY_IDENTIFIER = "entity-identifier";
    public static final String ENTITY_VALUE = "entity-value";


    public NodesQueryRequestLighty() {
        nodes_search_path = configProperties.getProperty(NODES_SEARCH_PATH);
    }


    @Override
    public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

        String request_url = targetUri+nodes_search_path;

        request_url = processPathData(request_url, requestProperties);

        if(resourceVersion != null) {
            request_url = request_url +"?resource-version="+resourceVersion;
        }
        URL http_req_url =    new URL(request_url);

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
        SearchResults tenant = (SearchResults)requestDatum;
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
        String[] args = {NODE_TYPE, ENTITY_IDENTIFIER, ENTITY_VALUE};
        return args;
    }


    @Override
    public Class<? extends AAIDatum> getModelClass() {
        return SearchResults.class;
    }


    public static String processPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {

        String key = ENTITY_IDENTIFIER;

        String encoded_vnf = encodeQuery(requestProperties.getProperty(key));
        request_url = request_url.replace("{entity-identifier}", encoded_vnf) ;
        aaiService.LOGwriteDateTrace(ENTITY_IDENTIFIER, requestProperties.getProperty(key));

        key = ENTITY_VALUE;

        encoded_vnf = encodeQuery(requestProperties.getProperty(key));
        request_url = request_url.replace("{entity-name}", encoded_vnf) ;
        aaiService.LOGwriteDateTrace("entity-name", requestProperties.getProperty(key));

        key = NODE_TYPE;

        encoded_vnf = encodeQuery(requestProperties.getProperty(key));
        request_url = request_url.replace("{node-type}", encoded_vnf) ;
        aaiService.LOGwriteDateTrace(NODE_TYPE, requestProperties.getProperty(key));

        return request_url;
    }
}
