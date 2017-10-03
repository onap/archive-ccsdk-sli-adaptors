/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *             reserved.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.openecomp.aai.inventory.v11.InventoryResponseItems;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

public class NamedQueryRequest extends AAIRequest {

    public static final String NAMED_SEARCH_PATH            = "org.onap.ccsdk.sli.adaptors.aai.query.named";

    private final String named_search_path;

    public static final String NAMED_QUERY_UUID = "named-query-uuid";
    public static final String PREFIX = "prefix";


    public NamedQueryRequest() {
        named_search_path = configProperties.getProperty(NAMED_SEARCH_PATH);
    }

    @Override
    public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

        String request_url = targetUri+named_search_path;

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
        ObjectMapper mapper = AAIService.getObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        mapper.setSerializationInclusion(Include.NON_DEFAULT);

        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospector(AnnotationIntrospector.pair(introspector, secondary));
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        AAIDatum tenant = (AAIDatum)requestDatum;
        String json_text = null;
        try {
            ObjectNode node = mapper.valueToTree(tenant);
            Iterator<JsonNode> it = node.elements();
            while(it.hasNext()){
                JsonNode jn = it.next();
                JsonNode child = jn.get("instance-filter");
                if(child != null) {
                    child = child.get(0);
                    if(child.has("l3-network")) {
                        JsonNode innerChild =  child.get("l3-network");
                        if(innerChild != null) {
                            if(innerChild instanceof ObjectNode) {
                                ObjectNode on = ObjectNode.class.cast(innerChild);
                                List<String> namesToDelete = new ArrayList<String>();
                                Iterator<String> names = on.fieldNames();
                                while(names.hasNext()) {
                                    String name = names.next();
                                    if(name != null && name.startsWith("is-")) {
                                        namesToDelete.add(name);
                                    }
                                }
                                for(String nameToDelete : namesToDelete) {
                                    on.remove(nameToDelete);
                                }
                            }
                        }
                    } else if(child.has("pnf")) {
                        JsonNode innerChild =  child.get("pnf");
                        if(innerChild != null) {
                            if(innerChild instanceof ObjectNode) {
                                ObjectNode on = ObjectNode.class.cast(innerChild);
                                List<String> namesToDelete = new ArrayList<String>();
                                Iterator<String> names = on.fieldNames();
                                while(names.hasNext()) {
                                    String name = names.next();
                                    if(name != null && name.startsWith("in-maint")) {
                                        namesToDelete.add(name);
                                    }
                                }
                                for(String nameToDelete : namesToDelete) {
                                    on.remove(nameToDelete);
                                }
                            }
                        }
                    } else     if(child.has("generic-vnf")) {
                        JsonNode innerChild =  child.get("generic-vnf");
                        if(innerChild != null) {
                            if(innerChild instanceof ObjectNode) {
                                ObjectNode on = ObjectNode.class.cast(innerChild);
                                List<String> namesToDelete = new ArrayList<String>();
                                Iterator<String> names = on.fieldNames();
                                while(names.hasNext()) {
                                    String name = names.next();
                                    if(name != null && name.startsWith("is-")) {
                                        namesToDelete.add(name);
                                    } else if(name != null && name.startsWith("in-maint")) {
                                        namesToDelete.add(name);
                                    }
                                }
                                for(String nameToDelete : namesToDelete) {
                                    on.remove(nameToDelete);
                                }
                            }
                        }
                    }
                }
            }
            json_text = node.toString();
            if(json_text == null)
            json_text = mapper.writeValueAsString(tenant);
        } catch (JsonProcessingException exc) {
            handleException(this, exc);
            return null;
        }
        return json_text;
    }


    @Override
    public String[] getArgsList() {
        String[] args = {NAMED_QUERY_UUID, PREFIX};
        return args;
    }


    @Override
    public Class<? extends AAIDatum> getModelClass() {
        return InventoryResponseItems.class;
    }


    public static String processPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {


        String encoded_vnf ;
        String key = NAMED_QUERY_UUID;

        if(requestProperties.containsKey(key)) {
            encoded_vnf = encodeQuery(requestProperties.getProperty(key));
            request_url = request_url.replace("{named-query-uuid}", encoded_vnf) ;
            aaiService.LOGwriteDateTrace("named-query-uuid", requestProperties.getProperty(key));
        }

        key = PREFIX;

        if(requestProperties.containsKey(key)) {
            encoded_vnf = encodeQuery(requestProperties.getProperty(key));
            request_url = request_url.replace("{prefix}", encoded_vnf) ;
            aaiService.LOGwriteDateTrace("prefix", requestProperties.getProperty(key));
        }

        return request_url;
    }
}
