/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *             reserved.
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
 * The UpdateRequest class provides processing related to update transaction.
 * @author  richtabedzki
 */

package org.onap.ccsdk.sli.adaptors.aai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;

/**
 * THIS CLASS IS A COPY OF {@link UpdateRequest} WITH REMOVED OSGi DEPENDENCIES
 */
public class UpdateRequestLighty extends AAIRequestLighty {

    private AAIRequestLighty request;
    private Map<String, String> params;

    public UpdateRequestLighty(AAIRequestLighty request, Map<String, String> parms) {
        this.request = request;
        this.params = parms;
    }

    @Override
    public URL getRequestUrl(String method, String resourceVersion)
            throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        return request.getRequestUrl(method, resourceVersion);
    }

    @Override
    public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
        return request.getRequestQueryUrl(method);
    }

    @Override
    public String toJSONString() {
        updateArrayEntries(params);
        ObjectMapper mapper = AAIService.getObjectMapper();
        String json = null;
      
        try {
            json = mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            LOG.error("Could not convert parameters of " + request.getRequestObject().getClass().getName(), e);
        }
      
        return json;
    }

    /**
    *
    * Update array entries.
    * The method converts indexed data entries to an array of values
    *
    * @param data Map containing String:String values representing input data
    */
    private void updateArrayEntries( Map<String, String> data) {
        Set<String> set = data.keySet()
                .stream()
                .filter(s -> s.endsWith("_length"))
                .collect(Collectors.toSet());
          
        for(String lenghtKey : set) {
            String key = lenghtKey.replace("_length", "");
//            String index = data.get(lenghtKey);
            List<String> array = new ArrayList<>();
          
            Set<String> subset = data.keySet()
                    .stream()
                    .filter(s -> s.startsWith(String.format("%s[",key)))
                    .collect(Collectors.toSet());
            for(String subKey : subset) {
                String subValue = data.get(subKey);
                array.add(subValue);
                LOG.trace("{} : {} ", subKey, subValue);
            }
            data.put(key, array.toString());
            data.remove(lenghtKey);
            for(String subKey : subset) {
                data.remove(subKey);
            }
        }
    }

    @Override
    public String[] getArgsList() {
        return request.getArgsList();
    }

    @Override
    public Class<? extends AAIDatum> getModelClass() {
        return request.getModelClass();
    }
  
    @Override
    public void addRequestProperty(String key, String value) {
        request.requestProperties.put(key, value);
    }

    public static String processPathData(String requestUrl, Properties requestProperties) {
      
//        if(request != null) {
//            Class<?> clazz = request.getClass();
//            Method function = null;
//            try {
//                function = clazz.getMethod("processPathData", request_url.getClass(), requestProperties.getClass());
//                request_url = (String) function.invoke(null, request_url,  requestProperties);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
      
//        request.processPathData(request_url, requestProperties);
        return requestUrl;
    }
  
    public void processRequestPathValues(Map<String, String> nameValues) {
        request.processRequestPathValues(nameValues);
    }

}
