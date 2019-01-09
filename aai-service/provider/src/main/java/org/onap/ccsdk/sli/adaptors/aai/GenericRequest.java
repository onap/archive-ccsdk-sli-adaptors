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
 
package org.onap.ccsdk.sli.adaptors.aai;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.onap.aai.inventory.v14.L3Network;
import org.onap.aai.inventory.v14.L3Networks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

public class GenericRequest extends AAIRequest {


    protected Class<? extends AAIDatum> model;

    public GenericRequest(Class<? extends AAIDatum> clazz) {
        model = clazz;
    }

    @Override
    public String updatePathDataValues(Object resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

        String originalPath = null;
        String pathSubstitute = null;
        String resourceName = null;

        Annotation[] annotations = model.getAnnotations();
        for(Annotation annotation : annotations) {
            Class<? extends Annotation> anotationType = annotation.annotationType();
            String annotationName = anotationType.getName();
            if("javax.xml.bind.annotation.XmlRootElement".equals(annotationName)){
                XmlRootElement order = (XmlRootElement)annotation;
                resourceName = order.name();
            }
        }

        String request_url = getRequestPath(resourceName);

        Map<String, String> queryParams = new HashMap<> ();
        if(resourceVersion != null) {
            queryParams.put("resource-version", resourceVersion.toString());
        }

        Set<String> uniqueResources = extractUniqueResourceSetFromKeys(requestProperties.stringPropertyNames());

        String[] keys = requestProperties.keySet().toArray(new String[0]);
        for(String key : keys) {
            switch(key) {
            case "cloud-region.cloud-region-id":
            case "entitlement.resource-uuid":
            case "license.resource-uuid":
            case "route-target.route-target-role":
            case "service-capability.vnf-type":
            case "ctag-pool.availability-zone-name":
                continue;
            }

            String value = requestProperties.getProperty(key);
            if(key.contains(".")) {
                String[] splitKey = key.split("\\.");
                if("cloud-region".equals(splitKey[0])){
                    String cloudRegionId =  requestProperties.getProperty("cloud-region.cloud-region-id");
                    aaiService.LOGwriteDateTrace("cloud-region-id", cloudRegionId);
                    String token = String.format("%s/{%s}/{cloud-region-id}", splitKey[0], splitKey[1] );
                    String encoded_owner = encodeQuery(value);
                    String encoded_region = encodeQuery(cloudRegionId);
                    request_url = request_url.replace(token, String.format("%s/%s/%s", splitKey[0], encoded_owner, encoded_region));
                } else     if("entitlement".equals(splitKey[0])){
                    String cloudRegionId =  requestProperties.getProperty("entitlement.resource-uuid");
                    aaiService.LOGwriteDateTrace("resource-uuid", cloudRegionId);
                    String token = String.format("%s/{%s}/{resource-uuid}", splitKey[0], splitKey[1] );
                    String encoded_owner = encodeQuery(value);
                    String encoded_region = encodeQuery(cloudRegionId);
                    request_url = request_url.replace(token, String.format("%s/%s/%s", splitKey[0], encoded_owner, encoded_region));
                } else     if("license".equals(splitKey[0])){
                    String cloudRegionId =  requestProperties.getProperty("license.resource-uuid");
                    aaiService.LOGwriteDateTrace("resource-uuid", cloudRegionId);
                    String token = String.format("%s/{%s}/{resource-uuid}", splitKey[0], splitKey[1] );
                    String encoded_owner = encodeQuery(value);
                    String encoded_region = encodeQuery(cloudRegionId);
                    request_url = request_url.replace(token, String.format("%s/%s/%s", splitKey[0], encoded_owner, encoded_region));
                } else     if("route-target".equals(splitKey[0])){
                    String cloudRegionId =  requestProperties.getProperty("route-target.route-target-role");
                    aaiService.LOGwriteDateTrace("route-target-role", cloudRegionId);
                    String token = String.format("%s/{%s}/{route-target-role}", splitKey[0], splitKey[1] );
                    String encoded_owner = encodeQuery(value);
                    String encoded_region = encodeQuery(cloudRegionId);
                    request_url = request_url.replace(token, String.format("%s/%s/%s", splitKey[0], encoded_owner, encoded_region));
                } else     if("service-capability".equals(splitKey[0])){
                    String vnfType =  requestProperties.getProperty("service-capability.vnf-type");
                    aaiService.LOGwriteDateTrace("vnf-type", vnfType);
                    String token = String.format("%s/{%s}/{vnf-type}", splitKey[0], splitKey[1] );
                    String encoded_service_type = encodeQuery(value);
                    String encoded_vnf_type = encodeQuery(vnfType);
                    if("service-capability".equals(resourceName)) {
                        request_url = request_url.replace(token, String.format("%s/%s/%s", splitKey[0], encoded_service_type, encoded_vnf_type));
                    } else if("service-capabilities".equals(resourceName)) {
                        queryParams.put("service-type", encoded_service_type);
                        queryParams.put("vnf-type", encoded_vnf_type);
                    }
                } else     if("ctag-pool".equals(splitKey[0])){
                    String cloudRegionId =  requestProperties.getProperty("ctag-pool.availability-zone-name");
                    aaiService.LOGwriteDateTrace("availability-zone-name", cloudRegionId);
                    String token = String.format("%s/{%s}/{availability-zone-name}", splitKey[0], splitKey[1] );
                    String encoded_owner = encodeQuery(value);
                    String encoded_region = encodeQuery(cloudRegionId);
                    request_url = request_url.replace(token, String.format("%s/%s/%s", splitKey[0], encoded_owner, encoded_region));
                } else {
                    Class<? extends AAIDatum> clazz = getClassFromResource(splitKey[0]);

                    if(clazz != null) {
                        if(clazz == this.model) {
                            Field[] fields = this.model.getDeclaredFields();
                            Field field = fields[0];
                            String fieldName = field.getName();
                            XmlElement annotation = field.getAnnotation(XmlElement.class);
                            String primaryId = null;
                            if(annotation != null) {
                                primaryId = annotation.name();
                            if("##default".equals(primaryId)) {
                                primaryId = fieldName;
                            }
                            } else {
                                primaryId = fieldName;
                            }

                            String token = String.format("%s/{%s}", splitKey[0], primaryId);

                            if(splitKey[1].equals(primaryId)) {
                                String encoded_vnf = encodeQuery(value);
                                request_url = request_url.replace(token, String.format("%s/%s", splitKey[0], encoded_vnf));
                            } else {
                                queryParams.put(splitKey[1], encodeQuery(value));
                                originalPath = token;
                                pathSubstitute = String.format("%s", splitKey[0]);
                            }
                        } else if(L3Networks.class == this.model) {
                            Field[] fields = L3Network.class.getDeclaredFields();
                            Field field = fields[0];
                            String fieldName = field.getName();
                            XmlElement annotation = field.getAnnotation(XmlElement.class);
                            String primaryId = annotation.name();
                            if("##default".equals(primaryId)) {
                                primaryId = fieldName;
                            }

                            String token = String.format("%s/{%s}", splitKey[0], primaryId);
                            originalPath = token;
                            pathSubstitute = String.format("");

                            queryParams.put(splitKey[1], encodeQuery(value));
                        } else {
                            String token = String.format("%s/{%s}", splitKey[0], splitKey[1]);
                            String encoded_vnf = encodeQuery(value);
                            request_url = request_url.replace(token, String.format("%s/%s", splitKey[0], encoded_vnf));
                        }
                    }

                }
                aaiService.LOGwriteDateTrace(splitKey[1], value);
            }
        }

        if(originalPath != null && pathSubstitute != null)
            request_url = request_url.replace(originalPath, pathSubstitute);

        if(!queryParams.isEmpty()) {
            Joiner.MapJoiner mapJoiner = Joiner.on("&").withKeyValueSeparator("=");
            String queryString = mapJoiner.join(queryParams);
            request_url = String.format("%s?%s", request_url, queryString);
        }

        return request_url;
    }

    @Override
    public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        return this.getRequestUrl(method, null);
    }


    @Override
    public String toJSONString() {
        ObjectMapper mapper = getObjectMapper();

        String json_text = null;
        try {
            json_text = mapper.writeValueAsString(requestDatum);
        } catch (JsonProcessingException exc) {
            handleException(this, exc);
            return null;
        }
        return json_text;
    }

    @Override
    public String[] getArgsList() {
        String[] args = {};
        return args;
    }

    @Override
    public Class<? extends AAIDatum> getModelClass() {
        return model;
    }

    public void processRequestPathValues(Map<String, String> nameValues) {
        // identify unique resources
        Set<String> uniqueResources = AAIRequest.extractUniqueResourceSetFromKeys(nameValues.keySet());

        String[] arguments = nameValues.keySet().toArray(new String[0]);
        for(String name : arguments) {
            String tmpName = name.replaceAll("-", "_");
            String value = nameValues.get(tmpName);
            if(value != null && !value.isEmpty()) {
                value = value.trim().replace("'", "").replace("$", "").replace("'", "");
                tmpName = name.replaceAll("_", "-");
                this.addRequestProperty(tmpName, value);
            }
        }
    }
}
