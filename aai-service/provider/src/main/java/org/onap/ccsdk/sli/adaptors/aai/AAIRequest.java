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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.openecomp.aai.inventory.v11.GenericVnf;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AAIRequest {
    protected static final Logger LOG = LoggerFactory.getLogger(AAIRequest.class);

    protected static final String TARGET_URI = "org.onap.ccsdk.sli.adaptors.aai.uri";

    protected static final String MASTER_REQUEST = "master-request";

    public static final String RESOURCE_VERSION = "resource-version";

    public static final String DEPTH = "depth";

    protected static Properties configProperties;
    protected final String targetUri;
    protected static AAIService aaiService;

    protected AAIDatum requestDatum;

    protected final Properties requestProperties = new Properties();


    public static AAIRequest createRequest(String resoourceName, Map<String, String> nameValues){

        String resoource = resoourceName;
		String masterResource = null;

        if(resoource == null)
            return null;

        if(resoource.contains(":")) {
            String[] tokens = resoource.split(":");
			if(tokens != null && tokens.length == 2) {
				resoource = tokens[1];
				masterResource = tokens[0];
				//
				Class<? extends AAIDatum> clazz = null;
				try {
					clazz = getClassFromResource(resoource) ;
				} catch (ClassNotFoundException e) {
					LOG.warn("AAIRequest does not support class: " + e.getMessage());
					return null;
				}

				if(clazz == null) {
					return null;
				}
            }
        }

        if(nameValues.containsKey("selflink")){
            Class<? extends AAIDatum> clazz = null;
            try {
                clazz = getClassFromResource(resoource) ;
            } catch (ClassNotFoundException e) {
                LOG.warn("AAIRequest does not support class: " + e.getMessage());
                return null;
            }

            if(clazz != null)
                return new SelfLinkRequest(clazz);
            else
                return null;
        }

        switch(resoource){
        case "generic-query":
            return new GenericQueryRequest();
        case "named-query":
            return new NamedQueryRequest();
        case "nodes-query":
            return new NodesQueryRequest();
        case "custom-query":
        case "formatted-query":
            return new CustomQueryRequest();
        case "echo":
        case "test":
            return new EchoRequest();

        case "linterface":
        case "l2-bridge-sbg":
        case "l2-bridge-bgf":
            {
            	resoource = "l-interface";
                return getRequestFromResource("l-interface");
            }
		case "relationship-list":
			 return new RelationshipListRequest(AAIRequest.createRequest(masterResource, nameValues));
		case "relationship":
			 return new RelationshipRequest(AAIRequest.createRequest(masterResource, nameValues));
        default:
                return getRequestFromResource(resoource);
        }
    }


    /**
     * Map containing resource tag to its bit position in bitset mapping
     */
    private static Map<String, String> tagValues = new LinkedHashMap<>();
    /**
     * Map containing bitset value of the path to its path mapping
     */
    private static Map<BitSet, String> bitsetPaths = new LinkedHashMap<>();


    public static Set<String> getResourceNames() {
        return tagValues.keySet();
    }


    public static void setProperties(Properties props, AAIService aaiService) {
        AAIRequest.configProperties = props;
        AAIRequest.aaiService = aaiService;

        try
        {
            URL url = null;
            Bundle bundle = FrameworkUtil.getBundle(AAIServiceActivator.class);
            if(bundle != null) {
                BundleContext ctx = bundle.getBundleContext();
                if(ctx == null)
                    return;

                url = ctx.getBundle().getResource(AAIService.PATH_PROPERTIES);
            } else {
                url = aaiService.getClass().getResource("/aai-path.properties");
            }

            InputStream in = url.openStream();
            Reader reader = new InputStreamReader(in, "UTF-8");

            Properties properties = new Properties();
            properties.load(reader);
            LOG.info("loaded " + properties.size());

            Set<String> keys = properties.stringPropertyNames();

            int index = 0;
            Set<String> resourceNames = new TreeSet<>();

            for(String key : keys) {
                String[] tags = key.split("\\|");
                for(String tag : tags) {
                    if(!resourceNames.contains(tag)) {
                        resourceNames.add(tag);
                        tagValues.put(tag, Integer.toString(++index));
                    }
                }
                BitSet bs = new BitSet(256);
                for(String tag : tags) {
                    String value = tagValues.get(tag);
                    Integer bitIndex = Integer.parseInt(value) ;
                    bs.set(bitIndex);
                }
                String path = properties.getProperty(key);
                LOG.info(String.format("bitset %s\t\t%s", bs.toString(), path));
                bitsetPaths.put(bs, path);
            }
            LOG.info("loaded " + resourceNames.toString());
        }
        catch (Exception e)
        {
            LOG.error("Caught exception", e);
        }
    }

    public AAIRequest() {
        targetUri    = configProperties.getProperty(TARGET_URI);
    }

    public void addRequestProperty(String key, String value) {
        requestProperties.put(key, value);
    }

    public final void setRequestObject(AAIDatum value) {
        requestDatum = value;
    }

    public final AAIDatum getRequestObject() {
        return requestDatum;
    }

    public final void addMasterRequest(AAIRequest masterRequest) {
        requestProperties.put(MASTER_REQUEST, masterRequest);
    }

    protected static String encodeQuery(String param) throws UnsupportedEncodingException {
        return URLEncoder.encode(param, "UTF-8").replace("+", "%20");
    }

    protected void handleException(AAIRequest lInterfaceRequest, JsonProcessingException exc) {
        aaiService.getLogger().warn("Could not deserialize object of type " + lInterfaceRequest.getClass().getSimpleName(), exc) ;
    }

	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {

        String request_url = null;

        request_url = targetUri + updatePathDataValues(resourceVersion);

        URL http_req_url =    new URL(request_url);

        aaiService.LOGwriteFirstTrace(method, http_req_url.toString());

        return http_req_url;
    }

    public String updatePathDataValues(Object resourceVersion) throws UnsupportedEncodingException, MalformedURLException {
        String request_url = getRequestPath();

        Set<String> uniqueResources = extractUniqueResourceSetFromKeys(requestProperties.stringPropertyNames());

        for(String resoourceName:uniqueResources) {
            AAIRequest locRequest = AAIRequest.createRequest(resoourceName, new HashMap<String, String>());
            if(locRequest != null) {
                Class<?> clazz = locRequest.getClass();
                Method function = null;
                try {
                    function = clazz.getMethod("processPathData", request_url.getClass(), requestProperties.getClass());
                    request_url = (String) function.invoke(null, request_url,  requestProperties);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(resourceVersion != null) {
            request_url = request_url +"?resource-version="+resourceVersion;
        }

        return request_url;
    }

    protected String getRequestPath() throws MalformedURLException {
        return getRequestPath(null);
    }

    protected String getRequestPath(String resource) throws MalformedURLException {
		if(requestProperties.containsKey("resource-path")) {
			return requestProperties.getProperty("resource-path");
		}

        Set<String> uniqueResources = extractUniqueResourceSetFromKeys(requestProperties.stringPropertyNames());
        if(resource != null) {
            // for group search add itself, but remove singular version of itself
            if(!uniqueResources.contains(resource)) {
                boolean replaced =  false;
                Set<String> tmpUniqueResources = new HashSet<String>();
                tmpUniqueResources.addAll(uniqueResources);
                for(String item : tmpUniqueResources){
                    String plural = item +"s";
                    if(item.endsWith("y")){
                        plural = item.substring(0, item.length()-1)+ "ies";
                    }
                    if(plural.equals(resource)) {
                        uniqueResources.remove(item);
                        uniqueResources.add(resource);
                        replaced = true;
                        break;
                    }
                }
                if(!replaced){
                    if(!uniqueResources.contains(resource)) {
                        uniqueResources.add(resource);
                    }
                }
            }
        }
        BitSet bitset = new BitSet();
        for(String key : uniqueResources) {
            if(tagValues.containsKey(key)) {
                Object tmpValue = tagValues.get(key);
                if(tmpValue != null) {
                    String value = tmpValue.toString();
                    int bitIndex = Integer.parseInt(value);
                    bitset.set(bitIndex);
                }
            }
        }

        String path = bitsetPaths.get(bitset);
        if(path == null) {
            throw new MalformedURLException("PATH not found for key string containing valies :" +requestProperties.toString());
        }
        return path;
    }

	public abstract URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException;

    public abstract String toJSONString();

    public abstract String[] getArgsList();

    public abstract Class<? extends AAIDatum> getModelClass() ;

    public String getPrimaryResourceName(String resource) {
        return resource;
    }

    public String formatKey(String argument) {
        return argument;
    }

    public AAIDatum jsonStringToObject(String jsonData) throws JsonParseException, JsonMappingException, IOException {
        if(jsonData == null) {
            return null;
        }

        AAIDatum response = null;
        ObjectMapper mapper = getObjectMapper();
        response = mapper.readValue(jsonData, getModelClass());
        return response;
    }

    protected static Set<String> extractUniqueResourceSetFromKeys(Set<String> keySet) {
        Set<String> uniqueResources = new TreeSet<>();
        List<String> keys = new ArrayList<>(keySet);
        for(String resource : keys) {
            if(resource.contains(".")) {
                String [] split = resource.split("\\.");
                uniqueResources.add(split[0].replaceAll("_", "-"));
            }
        }
        return uniqueResources;
    }

    public void processRequestPathValues(Map<String, String> nameValues) {
        Set<String> uniqueResources = extractUniqueResourceSetFromKeys(nameValues.keySet());

        Set<String> tokens = new TreeSet<>();
        tokens.add(DEPTH);
        tokens.addAll(Arrays.asList(this.getArgsList()));

        for(String resoourceName:uniqueResources) {
            AAIRequest locRequest = AAIRequest.createRequest(resoourceName, nameValues);
            if(locRequest != null)
                tokens.addAll(Arrays.asList(locRequest.getArgsList()));
        }

        String[] arguments = tokens.toArray(new String[0]);
        for(String name : arguments) {
            String tmpName = name.replaceAll("-", "_");
            String value = nameValues.get(tmpName);
            if(value != null && !value.isEmpty()) {
                value = value.trim().replace("'", "").replace("$", "").replace("'", "");
                this.addRequestProperty(name, value);
            }
        }
    }

    public static String processPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {
        return request_url;
    }

    public boolean isDeleteDataRequired() {
        return false;
    }

    ObjectMapper getObjectMapper() {
        return AAIService.getObjectMapper();
    }

    public static Class<? extends AAIDatum> getClassFromResource(String resoourceName) throws ClassNotFoundException {
        String className = GenericVnf.class.getName();
        String[] split = resoourceName.split("-");
        for(int i = 0; i < split.length; i++) {
            split[i] = StringUtils.capitalize(split[i]);
        }

        String caps = StringUtils.join(split);
        className = className.replace("GenericVnf", caps);
        Class<? extends AAIDatum> clazz = null;
        try {
            clazz = (Class<? extends AAIDatum>)Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOG.warn("AAIRequest does not support class: " + e.getMessage());
            return null;
        }

        return clazz;
    }

    protected static AAIRequest getRequestFromResource(String resoourceName) {

        Class<? extends AAIDatum> clazz = null;
        try {
            clazz = getClassFromResource(resoourceName);
        } catch (ClassNotFoundException e) {
            LOG.warn("AAIRequest does not support class: " + e.getMessage());
            return null;
        }
        if(clazz == null) {
            return null;
        }
        GenericRequest request = new GenericRequest(clazz);
        return request;
    }

    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();

        if(query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        }
        return query_pairs;
    }

    public static Map<String, String> splitPath(String path) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();

        if(path != null && !path.isEmpty()) {
            String[] pairs = path.split("/");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        }
        return query_pairs;
    }

    protected boolean expectsDataFromPUTRequest() {
        return false;
    }


    public String getTargetUri() {
        return targetUri;
    }
}
