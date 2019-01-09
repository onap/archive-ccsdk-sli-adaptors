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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.onap.ccsdk.sli.adaptors.aai.query.FormattedQueryResultList;
import org.onap.ccsdk.sli.adaptors.aai.query.InstanceFilter;
import org.onap.ccsdk.sli.adaptors.aai.query.InstanceFilters;
import org.onap.ccsdk.sli.adaptors.aai.query.NamedQuery;
import org.onap.ccsdk.sli.adaptors.aai.query.NamedQueryData;
import org.onap.ccsdk.sli.adaptors.aai.query.QueryParameters;
import org.onap.ccsdk.sli.adaptors.aai.query.Result;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.aai.inventory.v14.GenericVnf;
import org.onap.aai.inventory.v14.Image;
import org.onap.aai.inventory.v14.InventoryResponseItem;
import org.onap.aai.inventory.v14.InventoryResponseItems;
import org.onap.aai.inventory.v14.L3Network;
import org.onap.aai.inventory.v14.LogicalLink;
import org.onap.aai.inventory.v14.Metadata;
import org.onap.aai.inventory.v14.Metadatum;
import org.onap.aai.inventory.v14.Pnf;
import org.onap.aai.inventory.v14.RelatedToProperty;
import org.onap.aai.inventory.v14.Relationship;
import org.onap.aai.inventory.v14.RelationshipData;
import org.onap.aai.inventory.v14.RelationshipList;
import org.onap.aai.inventory.v14.ResultData;
import org.onap.aai.inventory.v14.SearchResults;
import org.onap.aai.inventory.v14.ServiceInstance;
import org.onap.aai.inventory.v14.Vlan;
import org.onap.aai.inventory.v14.Vlans;
import org.onap.aai.inventory.v14.Vserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public abstract class AAIDeclarations implements AAIClient {

    public static final String TRUSTSTORE_PATH    = "org.onap.ccsdk.sli.adaptors.aai.ssl.trust";
    public static final String TRUSTSTORE_PSSWD   = "org.onap.ccsdk.sli.adaptors.aai.ssl.trust.psswd";
    public static final String KEYSTORE_PATH      = "org.onap.ccsdk.sli.adaptors.aai.ssl.key";
    public static final String KEYSTORE_PSSWD     = "org.onap.ccsdk.sli.adaptors.aai.ssl.key.psswd";

    public static final String APPLICATION_ID     = "org.onap.ccsdk.sli.adaptors.aai.application";

    public static final String CLIENT_NAME          = "org.onap.ccsdk.sli.adaptors.aai.client.name";
    public static final String CLIENT_PWWD          = "org.onap.ccsdk.sli.adaptors.aai.client.psswd";


    public static final String CONNECTION_TIMEOUT = "connection.timeout";
    public static final String READ_TIMEOUT       = "read.timeout";

    public static final String TARGET_URI         = "org.onap.ccsdk.sli.adaptors.aai.uri";

    public static final String AAI_VERSION          = "org.onap.ccsdk.sli.adaptors.aai.version";

    // Availability zones query
    public static final String QUERY_PATH         = "org.onap.ccsdk.sli.adaptors.aai.path.query";

    // Update
    public static final String UPDATE_PATH          = "org.onap.ccsdk.sli.adaptors.aai.update";

    // Service instance
    public static final String SVC_INSTANCE_PATH  = "org.onap.ccsdk.sli.adaptors.aai.path.svcinst";
    public static final String SVC_INST_QRY_PATH  = "org.onap.ccsdk.sli.adaptors.aai.path.svcinst.query";

    // VServer
    public static final String NETWORK_VSERVER_PATH  = "org.onap.ccsdk.sli.adaptors.aai.path.vserver";

    public static final String VNF_IMAGE_QUERY_PATH      = "org.onap.ccsdk.sli.adaptors.aai.path.vnf.image.query";

    public static final String PARAM_SERVICE_TYPE     = "org.onap.ccsdk.sli.adaptors.aai.param.service.type";
    public static final String CERTIFICATE_HOST_ERROR = "org.onap.ccsdk.sli.adaptors.aai.host.certificate.ignore";

    // UBB Notify
    public static final String UBB_NOTIFY_PATH        = "org.onap.ccsdk.sli.adaptors.aai.path.notify";
    public static final String SELFLINK_AVPN          = "org.onap.ccsdk.sli.adaptors.aai.notify.selflink.avpn";
    public static final String SELFLINK_FQDN          = "org.onap.ccsdk.sli.adaptors.aai.notify.selflink.fqdn";

    //Service
    public static final String SERVICE_PATH              = "org.onap.ccsdk.sli.adaptors.aai.path.service";

    // P-Interfaces
    public static final String P_INTERFACE_PATH       = "org.onap.ccsdk.sli.adaptors.aai.path.pserver.pinterface";

    // site-pair-sets
    public static final String SITE_PAIR_SET_PATH     = "org.onap.ccsdk.sli.adaptors.aai.path.site.pair.set";

    // node query (1602)
    public static final String QUERY_NODES_PATH          = "org.onap.ccsdk.sli.adaptors.aai.query.nodes";

    private static final String VERSION_PATTERN = "/v$/";
 
    private static final String AAI_SERVICE_EXCEPTION = "AAI Service Exception";

    protected abstract Logger getLogger();
    public abstract AAIExecutorInterface getExecutor();
    
    private static final String RELATIONSHIP_DATA="Retrofiting relationship data: ";


    @Override
    public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix, String orderBy, SvcLogicContext ctx)
            throws SvcLogicException {

        getLogger().debug("AAIService.query \tresource = "+resource);

        String vnfId;
        String vnfName = null;
        HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);
        getLogger().debug("key = "+ nameValues.toString());

        if(!AAIServiceUtils.isValidFormat(resource, nameValues)) {
            ctx.setAttribute(String.format("%s.error.message", prefix), String.format("Resource %s is not supported. Key string contains invaid identifiers", resource));
            return QueryStatus.FAILURE;
        }

        if(resource == null || resource.isEmpty() || AAIRequest.createRequest(resource, nameValues) == null) {
            ctx.setAttribute(String.format("%s.error.message", prefix), String.format("Resource %s is not supported", resource));
            return QueryStatus.FAILURE;
        }

        // process data using new model
        boolean useNewModelProcessing = true;
        // process server query by name the old way
        if("vserver".equals(resource) || "vserver2".equals(resource)){
            if(nameValues.containsKey("vserver_name") || nameValues.containsKey("vserver-name") || nameValues.containsKey("vserver.vserver_name") || nameValues.containsKey("vserver.vserver-name"))
                useNewModelProcessing = false;
        }
        if("generic-vnf".equals(resource)){
            if(nameValues.containsKey("vnf_name") || nameValues.containsKey("vnf-name") || nameValues.containsKey("generic_vnf.vnf_name") || nameValues.containsKey("generic-vnf.vnf-name"))
                useNewModelProcessing = false;
        }

        // process data using new model
        if(useNewModelProcessing && AAIRequest.createRequest(resource, nameValues) != null) {

            try {
                return newModelQuery(resource, localOnly, select, key, prefix, orderBy, ctx);
            } catch (Exception exc) {
                getLogger().warn("Failed query - returning FAILURE", exc);
                return QueryStatus.FAILURE;
            }
        }

        ObjectMapper mapper = AAIService.getObjectMapper();
        Map<String,Object> attributes = new HashMap<>();

        String modifier = null;

        if(resource.contains(":")) {
            String[] tokens = resource.split(":");
            resource = tokens[0];
            if(tokens.length > 1) {
                modifier = tokens[1];
            }
        }

        resource = resource.toLowerCase().replace("-", "_");

        try {

            switch(resource) {
                case "generic_vnf":
                    vnfId = nameValues.get("vnf_id");
                    if(nameValues.containsKey("vnf_id"))
                        vnfId = nameValues.get("vnf_id");
                    else if(nameValues.containsKey("generic_vnf.vnf_name"))
                        vnfId = nameValues.get("generic_vnf.vserver_name");

                    if(nameValues.containsKey("vnf_name"))
                        vnfName = nameValues.get("vnf_name");
                    else if(nameValues.containsKey("generic_vnf.vnf_name"))
                        vnfName = nameValues.get("generic_vnf.vnf_name");

                    if(vnfId != null && !vnfId.isEmpty()) {
                        // at this point of the project this part should not be executed
                        vnfId = vnfId.trim().replace("'", "").replace("$", "").replace("'", "");
                        GenericVnf vnf = this.requestGenericVnfData(vnfId);
                        if(vnf == null) {
                            return QueryStatus.NOT_FOUND;
                        }

                        attributes = mapper.convertValue(vnf, attributes.getClass());
                    } else if(vnfName != null && !vnfName.isEmpty()) {
                        try {
                            vnfName = vnfName.trim().replace("'", "").replace("$", "").replace("'", "");
                            GenericVnf vnf = this.requestGenericVnfeNodeQuery(vnfName);
                            if(vnf == null) {
                                return QueryStatus.NOT_FOUND;
                            }
                            vnfId=vnf.getVnfId();
                            nameValues.put("vnf_id", vnfId);
                            attributes = mapper.convertValue(vnf, attributes.getClass());
                        } catch (AAIServiceException exc) {
                            int errorCode = exc.getReturnCode();
                            switch(errorCode) {
                                case 400:
                                case 404:
                                case 412:
                                    break;
                                default:
                                    getLogger().warn("Caught exception trying to refresh generic VNF", exc);
                            }
                            ctx.setAttribute(prefix + ".error.message", exc.getMessage());
                            if(errorCode >= 300) {
                                ctx.setAttribute(prefix + ".error.http.response-code",
                                        Integer.toString(exc.getReturnCode()));
                            }
                            return QueryStatus.FAILURE;
                        }
                    } else {
                        getLogger().warn("No arguments are available to process generic VNF");
                        return QueryStatus.FAILURE;
                    }
                    break;
                case "vserver":
                case "vserver2":
                    String vserverName = null;
                    if(nameValues.containsKey("vserver_name"))
                        vserverName = nameValues.get("vserver_name");
                    else if(nameValues.containsKey("vserver.vserver_name"))
                        vserverName = nameValues.get("vserver.vserver_name");

                    String vserverId = null;
                    if(nameValues.containsKey("vserver_id"))
                        vserverId = nameValues.get("vserver_id");
                    if(nameValues.containsKey("vserver.vserver_id"))
                        vserverId = nameValues.get("vserver.vserver_id");
                    String tenantId = nameValues.get("teannt_id");

                    if(vserverName != null) vserverName = vserverName.trim().replace("'", "").replace("$", "").replace("'", "");
                    if(vserverId != null) vserverId = vserverId.trim().replace("'", "").replace("$", "").replace("'", "");
                    if(tenantId != null) tenantId = tenantId.trim().replace("'", "").replace("$", "").replace("'", "");

                    if (vserverName != null) {
                        URL vserverUrl = null;
                        try {
                            vserverUrl = this.requestVserverURLNodeQuery(vserverName);
                        } catch (AAIServiceException aaiexc) {
                            getLogger().warn(AAI_SERVICE_EXCEPTION, aaiexc);
                            ctx.setAttribute(prefix + ".error.message", aaiexc.getMessage());
                            if (aaiexc.getReturnCode() >= 300) {
                                ctx.setAttribute(prefix + ".error.http" + "" + ".response-code", Integer.toString(aaiexc.getReturnCode()));
                            }

                            if (aaiexc.getReturnCode() == 404)
                                return QueryStatus.NOT_FOUND;
                            else
                                return QueryStatus.FAILURE;
                        }
                        if (vserverUrl == null) {
                            return QueryStatus.NOT_FOUND;
                        }

                        tenantId = getTenantIdFromVserverUrl(vserverUrl);
                        String cloudOwner = getCloudOwnerFromVserverUrl(vserverUrl);
                        String cloudRegionId = getCloudRegionFromVserverUrl(vserverUrl);

                        Vserver vserver = null;
                        try {
                            vserver = this.requestVServerDataByURL(vserverUrl);
                        } catch (AAIServiceException aaiexc) {
                            getLogger().warn(AAI_SERVICE_EXCEPTION, aaiexc);
                            ctx.setAttribute(prefix + ".error.message", aaiexc.getMessage());
                            if (aaiexc.getReturnCode() >= 300) {
                                ctx.setAttribute(prefix + ".error.http" + ".response-code", Integer.toString(aaiexc.getReturnCode()));
                            }

                            if (aaiexc.getReturnCode() == 404)
                                return QueryStatus.NOT_FOUND;
                            else
                                return QueryStatus.FAILURE;
                        }
                        if (vserver == null) {
                            return QueryStatus.NOT_FOUND;
                        }
                        attributes = mapper.convertValue(vserver, attributes.getClass());
                        if (!attributes.containsKey("tenant-id") && tenantId != null) {
                            attributes.put("tenant-id", tenantId);
                        }
                        if (!attributes.containsKey("cloud-owner") && cloudOwner != null) {
                            attributes.put("cloud-owner", cloudOwner);
                        }
                        if (!attributes.containsKey("cloud-region-id") && cloudRegionId != null) {
                            attributes.put("cloud-region-id", cloudRegionId);
                        }
                    } else if (vserverId != null && tenantId != null) {
                        Vserver vserver = this.requestVServerData(tenantId, vserverId, "att-aic", "AAIAIC25");
                        if(vserver == null) {
                            return QueryStatus.NOT_FOUND;
                        }
                        attributes = mapper.convertValue(vserver, attributes.getClass());
                        if(!attributes.containsKey("tenant-id") && tenantId != null){
                            attributes.put("tenant-id", tenantId);
                        }
                    } else {
                        return QueryStatus.FAILURE;
                    }
                    break;

                default:
                    return QueryStatus.FAILURE;
            }

            QueryStatus retval = QueryStatus.SUCCESS;

            if (attributes == null || attributes.isEmpty()) {
                retval = QueryStatus.NOT_FOUND;
                getLogger().debug("No data found");
            } else {
                if (ctx != null) {
                    if (prefix != null) {
                        ArrayList<String> keys = new ArrayList<>(attributes.keySet());

                        int numCols = keys.size();

                        for (int i = 0; i < numCols; i++) {
                            String colValue;
                            String colName = keys.get(i);
                            Object object = attributes.get(colName);

                            if(object != null && object instanceof String) {
                                colValue = (String)object;

                                if (prefix != null) {
                                    getLogger().debug("Setting "+prefix    + "." + colName.replaceAll("_", "-")+" = "+ colValue);
                                    ctx.setAttribute(prefix    + "." + colName.replaceAll("_", "-"), colValue);
                                } else {
                                    getLogger().debug("Setting " + colValue.replaceAll("_", "-")+" = "+colValue);
                                    ctx.setAttribute(colValue.replaceAll("_", "-"), colValue);
                                }
                            } else if(object != null && object instanceof Map) {
                                if(colName.equals(modifier) || "relationship-list".equals(colName)){
                                    String localNodifier = modifier;
                                    if(localNodifier == null)
                                        localNodifier = "relationship-list";
                                    Map<String, Object> properties = (Map<String, Object>)object;
                                    writeMap(properties, prefix+"."+localNodifier,  ctx);
                                }
                            }
                        }
                    }
                }
            }
            getLogger().debug("Query - returning " + retval);
            return retval;

        } catch (Exception exc) {
            getLogger().warn("Failed query - returning FAILURE", exc);
            return QueryStatus.FAILURE;
        }
    }


    public void writeMap(Map<String, Object> properties, String prefix, SvcLogicContext ctx) {
        Set<String> mapKeys = properties.keySet();

        for(String mapKey : mapKeys) {
            Object entity = properties.get(mapKey);
            if(entity instanceof ArrayList) {
                writeList((ArrayList<?>)entity, prefix + "." + mapKey, ctx);
            } else
            if(entity instanceof String ||  entity instanceof Long || entity instanceof Integer || entity instanceof Boolean) {
                ctx.setAttribute(prefix + "." + mapKey, entity.toString());
                getLogger().debug(prefix + "." + mapKey + " : " + entity.toString());
            } else if(entity instanceof Map) {
                String localPrefix = prefix;
                if(mapKey != null) {
                    localPrefix = String.format("%s.%s", prefix, mapKey);
                }
                writeMap( (Map<String, Object>)entity,  localPrefix,  ctx);
            }
        }
    }

    private void writeList(ArrayList<?> list, String prefix, SvcLogicContext ctx) {
        for(int i = 0; i < list.size(); i++ ) {
            Object entity = list.get(i);
            if(entity instanceof Map) {
                writeMap( (Map<String, Object>)entity,  prefix + "[" + i + "]",  ctx);
            } else
            if(entity instanceof String ||  entity instanceof Long || entity instanceof Integer || entity instanceof Boolean) {
                ctx.setAttribute(prefix, entity.toString());
                getLogger().debug(prefix  + " : " + entity.toString());
            }
        }

        if(!list.isEmpty()) {
            ctx.setAttribute(prefix + "_length", Integer.toString(list.size()));
            getLogger().debug(prefix + "_length"  + " : " + Integer.toString(list.size()));
        }
    }

    @Override
    public QueryStatus save(String resource, boolean force, boolean localOnly, String key, Map<String, String> params, String prefix, SvcLogicContext ctx)
            throws SvcLogicException {

        getLogger().debug("AAIService.save\tresource="+resource);
        HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);

        if(!AAIServiceUtils.isValidFormat(resource, nameValues)) {
            ctx.setAttribute(String.format("%s.error.message", prefix), String.format("Resource %s is not supported. Key string contains invaid identifiers", resource));
            return QueryStatus.FAILURE;
        }

        if(resource == null || resource.isEmpty() || AAIRequest.createRequest(resource, nameValues) == null) {
            getLogger().warn("AAIService.save has unspecified resource");
            ctx.setAttribute(String.format("%s.error.message", prefix), String.format("Resource %s is not supported", resource));
            return QueryStatus.FAILURE;
        }
        // keys passed
        getLogger().debug("key = "+ Arrays.toString(nameValues.entrySet().toArray()));

        // process params
        if(params.containsKey("prefix")) {
            Map<String, String> tmpParams = ctxGetBeginsWith(ctx, params.get("prefix"));
            if(!tmpParams.isEmpty()) {
                params.putAll(tmpParams);
//                params.remove("prefix");
            }
        }
        // params passed
        getLogger().debug("parms = "+ Arrays.toString(params.entrySet().toArray()));

        boolean useNewModelProcessing = true;
        // process server query by name the old way
        if("vserver".equals(resource) || "vserver2".equals(resource)){
            if(nameValues.containsKey("vserver-name")) {
                useNewModelProcessing = false;
            }

            if(!params.containsKey("vserver-selflink")) {

                AAIRequest request = AAIRequest.createRequest(resource, nameValues);
                URL path = null;
                try {
                    request.processRequestPathValues(nameValues);
                    path = request.getRequestUrl("GET", null);
                    params.put("vserver-selflink", path.toString());
                } catch (UnsupportedEncodingException | MalformedURLException | URISyntaxException e) {
                    getLogger().warn("URL error Exception", e);
                    params.put("vserver-selflink", "/vserver");
                }
            }
        }

        // process data using new model
        if(useNewModelProcessing && AAIRequest.createRequest(resource, nameValues) != null) {

            try {
                if(!resource.contains(":")){
                    return newModelSave(resource, force, key, params, prefix, ctx);
                } else {
                    String[] tokens = resource.split(":");
                    String localResource = tokens[0];
                    String dependency = tokens[1];

                    AAIDatum instance = newModelObjectRequest( localResource, nameValues, prefix, ctx);
                    if(instance == null) {
                        return QueryStatus.NOT_FOUND;
                    }

                    switch(dependency){
                        case "relationship-list":
                            newModelProcessRelationshipList(instance, params, prefix, ctx);
                            break;
                        case "metadata":
                            newModelProcessMetadata(instance, params, prefix, ctx);
                            break;
                    }
                    // create a method to update relationship-list
                    AAIRequest request = AAIRequest.createRequest(localResource, nameValues);
                    request.setRequestObject(instance);
                    request.processRequestPathValues(nameValues);

                    getExecutor().post(request);
                    getLogger().debug("Save relationship list - returning SUCCESS");
                    return QueryStatus.SUCCESS;
                }
            } catch (Exception exc) {
                ctx.setAttribute(prefix + ".error.message", exc.getMessage());
                if(exc instanceof AAIServiceException) {
                    AAIServiceException aaiexc = (AAIServiceException)exc;
                    if(aaiexc.getReturnCode() >= 300) {
                        ctx.setAttribute(prefix + ".error.http" + ".response-code", Integer.toString(aaiexc.getReturnCode()));
                    }

                    if(aaiexc.getReturnCode() == 404) {
                        return QueryStatus.NOT_FOUND;
                    }
                }
                getLogger().warn("Failed save() - returning FAILURE", exc);
                return QueryStatus.FAILURE;
            }
        } else {
            getLogger().debug("Save() request for {} is not supported- returning FAILURE", resource);
            return QueryStatus.FAILURE;
        }
    }

    @Override
    public QueryStatus update(String resource, String key, Map<String, String> params, String prefix, SvcLogicContext ctx) throws SvcLogicException {

        resource = resource.toLowerCase();
        HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);
        getLogger().debug("key = "+ Arrays.toString(nameValues.entrySet().toArray()));
        if(!AAIServiceUtils.isValidFormat(resource, nameValues)) {
            ctx.setAttribute(String.format("%s.error.message", prefix), String.format("Resource %s is not supported. Key string contains invaid identifiers", resource));
            return QueryStatus.FAILURE;
        }

        if(resource == null || resource.isEmpty() || AAIRequest.createRequest(resource, nameValues) == null) {
            ctx.setAttribute(String.format("%s.error.message", prefix), String.format("Resource %s is not supported", resource));
            return QueryStatus.FAILURE;
        }

        // check if request is for groups
        if(!AAIServiceUtils.containsResource(resource, nameValues)) {
            ctx.setAttribute(String.format("%s.error.message", prefix), String.format("Resource %s is not permitted in 'update' operation", resource));
            return QueryStatus.FAILURE;
        }

        getLogger().debug("parms = "+ Arrays.toString(params.entrySet().toArray()));

        AAIRequest request = AAIRequest.createRequest(resource, nameValues);
        request = new UpdateRequest(request, params);

        String[] arguments = request.getArgsList();
        for(String name : arguments) {
            String modifiedKey = name.replaceAll("-", "_");
            if(nameValues.containsKey(modifiedKey)) {
                String argValue = nameValues.get(modifiedKey);
                if(argValue != null) argValue = argValue.trim().replace("'", "").replace("$", "").replace("'", "");
                request.addRequestProperty(name, argValue);
            }
        }

        try {
            QueryStatus retval = QueryStatus.SUCCESS;

            retval = newModelQuery(resource, false, null, key, "tmpDelete", null,  ctx);

            if(retval == null || retval != QueryStatus.SUCCESS) {
                return retval;
            }

            String resourceVersion = ctx.getAttribute("tmpDelete.resource-version");
            if(resourceVersion == null) {
                return QueryStatus.NOT_FOUND;
            }
            params.put("resource-version", resourceVersion);

            request.processRequestPathValues(nameValues);
            getExecutor().patch(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            getLogger().warn(AAI_SERVICE_EXCEPTION, aaiexc);
            if(aaiexc.getReturnCode() == 404)
                return QueryStatus.NOT_FOUND;
            else
                return QueryStatus.FAILURE;
        } catch (Exception exc) {
            getLogger().warn("Failed update - returning FAILURE", exc);
            return QueryStatus.FAILURE;
        }

        getLogger().debug("Update - returning SUCCESS");
        return QueryStatus.SUCCESS;
    }

    @Override
    public QueryStatus delete(String resource, String key, SvcLogicContext ctx) throws SvcLogicException {
        getLogger().debug("AAIService.delete\tresource="+resource);
        HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);
        getLogger().debug("key = "+ Arrays.toString(nameValues.entrySet().toArray()));

        if(!AAIServiceUtils.isValidFormat(resource, nameValues)) {
            ctx.setAttribute(String.format("%s.error.message", "aaiData"), String.format("Resource %s is not supported. Key string contains invaid identifiers", resource));
            return QueryStatus.FAILURE;
        }

        if(resource == null || resource.isEmpty() || AAIRequest.createRequest(resource, nameValues) == null) {
            ctx.setAttribute(String.format("%s.error.message", "tmpDelete"), String.format("Resource %s is not supported", resource));
            return QueryStatus.FAILURE;
        }

        // check if request is for groups
        if(!AAIServiceUtils.containsResource(resource, nameValues)) {
            ctx.setAttribute(String.format("%s.error.message", "tmpDelete"), String.format("Resource %s is not permitted in 'delete' operation", resource));
            return QueryStatus.FAILURE;
        }

        if(AAIRequest.createRequest(resource, nameValues) != null) {
            if(resource.contains(":")) {
                switch (resource.split(":")[1]){
                    case "relationship-list":
                        return processDeleteRelationshipList(resource, key, ctx, nameValues);
                    case "metadata":
                        return processDeleteMetadata(resource, key, ctx, nameValues);
                }
            }


            try {
                QueryStatus retval = QueryStatus.SUCCESS;

                retval = newModelQuery(resource, false, null, key, "tmpDelete", null,  ctx);

                if(retval == null || retval != QueryStatus.SUCCESS) {
                    return retval;
                }

                String resourceVersion = ctx.getAttribute("tmpDelete.resource-version");
                if(resourceVersion == null) {
                    return QueryStatus.NOT_FOUND;
                }

                try {
                    AAIRequest request = AAIRequest.createRequest(resource, nameValues);
                    if(request == null) {
                        return QueryStatus.FAILURE;
                    }

                    request.processRequestPathValues(nameValues);

                    if(getExecutor().delete(request, resourceVersion)) {
                        return QueryStatus.SUCCESS;
                    }
                } catch(AAIServiceException aaiexc) {
                    getLogger().warn(AAI_SERVICE_EXCEPTION, aaiexc);
                    if(aaiexc.getReturnCode() == 404)
                        return QueryStatus.NOT_FOUND;
                    else
                        return QueryStatus.FAILURE;

                } catch (Exception exc) {
                    getLogger().warn("requestGenericVnfData", exc);
                    return QueryStatus.FAILURE;
                }

            } catch (Exception exc) {
                getLogger().warn("Failed delete - returning FAILURE", exc);
                return QueryStatus.FAILURE;
            }
        } else {
            String resourceName = resource;
            String identifier = null;

            if(resourceName.contains(":")) {
                String[] tokens = resourceName.split(":");
                if(tokens != null && tokens.length > 0) {
                    resourceName = tokens[0];
                    identifier = tokens[1];
                }
            }
            if("relationship-list".equals(identifier) || "relationshipList".equals(identifier)) {
//                RelationshipRequest relationshipRequest = new RelationshipRequest();
                if("generic-vnf".equals(resourceName)){
                    String vnfId = nameValues.get("vnf_id");
                    String relatedTo  = nameValues.get("related_to");
                    vnfId = vnfId.trim().replace("'", "").replace("$", "").replace("'", "");
                    relatedTo = relatedTo.trim().replace("'", "").replace("$", "").replace("'", "");

                    GenericVnf vnf;
                    try {
                        vnf = this.requestGenericVnfData(vnfId);
                        if(vnf == null)
                            return QueryStatus.NOT_FOUND;
                    } catch (AAIServiceException exc) {
                        getLogger().warn("Failed delete - returning NOT_FOUND", exc);
                        return QueryStatus.NOT_FOUND;
                    }
                    boolean itemRemoved = false;
                    RelationshipList relationshipList = vnf.getRelationshipList();
                    List<Relationship> relationships = relationshipList.getRelationship();
                    List<Relationship> iterableList = new LinkedList<>(relationships);
                    for(Relationship relationship : iterableList) {
                        if(relationship.getRelatedTo().equals(relatedTo)) {
                            relationships.remove(relationship);
                            itemRemoved = true;
                        }
                    }

                    if(!itemRemoved)
                        return QueryStatus.NOT_FOUND;
                    try {
                        this.postGenericVnfData(vnf.getVnfId(), vnf);
                    } catch (AAIServiceException exc) {
                        if(exc.getReturnCode() == 404){
                            return QueryStatus.NOT_FOUND;
                        } else {
                            getLogger().warn("Failed delete - returning FAILURE", exc);
                            return QueryStatus.FAILURE;
                        }
                    }
                    return QueryStatus.SUCCESS;
                }
            }
        }
        return QueryStatus.FAILURE;
    }

    @Override
    public QueryStatus exists(String resource, String key, String prefix, SvcLogicContext ctx) throws SvcLogicException {
        return query(resource, false, null, key, prefix, null, ctx);
    }

    @Override
    public QueryStatus isAvailable(String arg0, String arg1, String arg2, SvcLogicContext arg3)
            throws SvcLogicException {
        throw new SvcLogicException("Method AAIService.isAvailable() has not been implemented yet");
    }

    @Override
    public QueryStatus notify(String resource, String action, String key, SvcLogicContext ctx) throws SvcLogicException {
        throw new SvcLogicException("Method AAIService.notify() has not been implemented yet");
    }

    //    @Override
    public QueryStatus newModelQuery(String resource, boolean localOnly, String select, String key, String prefix, String orderBy, SvcLogicContext ctx) {

        QueryStatus retval = QueryStatus.SUCCESS;
        String modifier = null;

        HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);
        if(resource.contains(":")) {
            modifier = resource.split(":")[1];
        }

        try {
            AAIRequest request = AAIRequest.createRequest(resource, nameValues);
            if(request == null) {
                return QueryStatus.FAILURE;
            }

            Map<String, String> params = new HashMap<>();

            request.processRequestPathValues(nameValues);
            if(nameValues.containsKey("prefix")){
                Map<String, String> tmpParams = ctxGetBeginsWith(ctx, nameValues.get("prefix"));
                if(!tmpParams.isEmpty()) {
                    params.putAll(tmpParams);
                }
                if("named-query".equals(resource))
                    request.setRequestObject(extractNamedQueryDataFromQueryPrefix(nameValues, params));
            }
            String rv = getExecutor().get(request);

            retval = processResponseData(rv, resource, request, prefix,  ctx, nameValues, modifier);

        } catch(AAIServiceException aaiexc) {
            getLogger().warn(AAI_SERVICE_EXCEPTION, aaiexc);
            int errorCode = aaiexc.getReturnCode();
            ctx.setAttribute(prefix + ".error.message", aaiexc.getMessage());
            if(errorCode >= 300) {
                ctx.setAttribute(prefix + ".error.http.response-code",
                        Integer.toString(aaiexc.getReturnCode()));
            }

            if(aaiexc.getReturnCode() == 404)
                return QueryStatus.NOT_FOUND;

            return QueryStatus.FAILURE;
        } catch (Exception exc) {
            getLogger().warn("requestGenericVnfData", exc);
            ctx.setAttribute(prefix + ".error.message", exc.getMessage());
            return QueryStatus.FAILURE;
        }

        return retval;
    }

    public QueryStatus processResponseData(String rv, String resource, AAIRequest request, String prefix,  SvcLogicContext ctx, Map<String, String> nameValues, String modifier) throws JsonParseException, JsonMappingException, IOException, AAIServiceException
    {
        Object response;

        if(rv == null) {
            return QueryStatus.NOT_FOUND;
        }

        response = request.jsonStringToObject(rv);
        if(response == null) {
            return QueryStatus.NOT_FOUND;
        }

        if("generic-query".equals(resource)) {
            SearchResults rd = SearchResults.class.cast(response);
            List<ResultData> rdList = rd.getResultData();
            if(rdList == null || rdList.isEmpty()) {
                return QueryStatus.NOT_FOUND;
            }
            ResultData rDatum = rdList.get(0);
            nameValues.put("selflink", rDatum.getResourceLink());
            AAIRequest req2 = AAIRequest.createRequest(rDatum.getResourceType(), nameValues);
            req2.processRequestPathValues(nameValues);
            rv = getExecutor().get(req2);
            if(rv == null) {
                return QueryStatus.NOT_FOUND;
            }

            response = req2.jsonStringToObject(rv);
            if(response == null) {
                return QueryStatus.NOT_FOUND;
            }
        }

        if("named-query".equals(resource)) {
            InventoryResponseItems rd = InventoryResponseItems.class.cast(response);
            List<InventoryResponseItem> iRIlist = rd.getInventoryResponseItem();
            if(iRIlist == null || iRIlist.isEmpty()) {
                return QueryStatus.NOT_FOUND;
            }
        }

        if("nodes-query".equals(resource)) {
            SearchResults rd = SearchResults.class.cast(response);
            List<ResultData> rdList = rd.getResultData();
            if(rdList == null || rdList.isEmpty()) {
                return QueryStatus.NOT_FOUND;
            }
            ResultData rDatum = rdList.get(0);
            response = rDatum;
        }

        if("formatted-query".equals(resource) || "custom-query".equals(resource)) {
            FormattedQueryResultList rd = FormattedQueryResultList.class.cast(response);
            List<Result> iRIlist = rd.getResults();
            if(iRIlist == null || iRIlist.isEmpty()) {
                return QueryStatus.NOT_FOUND;
            }
        }

        // process relationship list
        // this is a temporary soluton to address the realationship handling changes added in Release 17.07
        try {
            Class<?> clazz = response.getClass();
            Method getter = clazz.getMethod("getRelationshipList");
            Object obj = getter.invoke(response);
            if(obj != null && obj instanceof RelationshipList) {
                RelationshipList list = RelationshipList.class.cast(obj);
                AAIServiceUtils.populateRelationshipDataFromPath(list);
            }
        } catch(Exception exc) {
            getLogger().debug(RELATIONSHIP_DATA + exc.getMessage());
        }

        String preFix;
        if(prefix == null || prefix.isEmpty()) {
            preFix = "";
        } else {
            preFix = prefix + ".";
        }

        Map<String,Object> props = objectToProperties(response);
        Set<String> keys = props.keySet();
        for(String theKey: keys) {
            if(getLogger().isTraceEnabled())
                getLogger().trace(theKey);

            Object value = props.get(theKey);
            if(value == null)
                continue;
            Object type = value.getClass();
            if(value instanceof String) {
                ctx.setAttribute(preFix + theKey, value.toString());
                continue;
            }
            if(value instanceof Boolean) {
                ctx.setAttribute(preFix + theKey, value.toString());
                continue;
            }
            if(value instanceof Integer) {
                ctx.setAttribute(preFix + theKey, value.toString());
                continue;
            }
            if(value instanceof Long) {
                ctx.setAttribute(preFix + theKey, value.toString());
                continue;
            }

            if(value instanceof ArrayList) {
                ArrayList<?> array = ArrayList.class.cast(value);
                for(int i = 0; i < array.size(); i++) {
                    writeList(array, String.format("%s.%s", prefix, theKey), ctx);
                }
                continue;
            }

            if("relationship-list".equals(theKey)){
                Map<String, Object> relationshipList = (Map<String, Object>)value;
                // we are interested in seeing just the selected relationship
                if(theKey.equals(modifier)) {
                    List<?> relationships = (List<?>)relationshipList.get("relationship");
                    if(relationships != null && !relationships.isEmpty()) {

                        List newRelationships = new LinkedList();
                        newRelationships.addAll(relationships);

                        for(Object obj : newRelationships){
                            if(obj instanceof Map<?, ?>) {
                                Map<?, ?> relProperties = (Map<?, ?>)obj;
                                if(relProperties.containsKey("related-to")) {
                                    Object relPropsRelatedTo = relProperties.get("related-to");

                                    String relatedTo = nameValues.get("related_to");
                                    if(relatedTo != null) {
                                        relatedTo = relatedTo.trim().replace("'", "").replace("$", "").replace("'", "");
                                        if(!relatedTo.equals(relPropsRelatedTo)) {
                                            relationships.remove(relProperties);
                                        }
                                        continue;
                                    } else {
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
                writeMap(relationshipList, String.format("%s.%s", prefix, theKey), ctx);
                continue;
            }

            if(value instanceof Map) {
                Map<String, Object> subnetsList = (Map<String, Object>)value;
                writeMap(subnetsList, String.format("%s.%s", prefix, theKey), ctx);
                continue;
            }

        }
        return QueryStatus.SUCCESS;
    }


    public QueryStatus newModelBackupRequest(String resource,  Map<String, String> params,  String prefix,  SvcLogicContext ctx) {

        QueryStatus retval = QueryStatus.SUCCESS;
        HashMap<String, String> nameValues = new HashMap<>();

        try {
            AAIRequest request = AAIRequest.createRequest(resource, nameValues);
            if(request == null) {
                return QueryStatus.FAILURE;
            }

            boolean argsFound = false;
            String[] arguments = request.getArgsList();
            for(String name : arguments) {
                String tmpName = name.replaceAll("-", "_");
                String value = params.get(tmpName);
                if(value != null && !value.isEmpty()) {
                    value = value.trim().replace("'", "").replace("$", "").replace("'", "");
                    request.addRequestProperty(name, value);
                    argsFound = true;
                }
            }
            if(!argsFound) {
                getLogger().warn("No arguments were found. Terminating backup request.");
                return QueryStatus.FAILURE;
            }

            String rv = getExecutor().get(request);
            ctx.setAttribute(prefix, rv);
        } catch(AAIServiceException aaiexc) {
            getLogger().warn(AAI_SERVICE_EXCEPTION, aaiexc);
            if(aaiexc.getReturnCode() == 404)
                return QueryStatus.NOT_FOUND;

            return QueryStatus.FAILURE;
        } catch (Exception exc) {
            getLogger().warn("newModelBackupRequest", exc);
            return QueryStatus.FAILURE;
        }

        return retval;
    }

    public AAIDatum newModelObjectRequest(String resource,  Map<String, String> params,  String prefix,  SvcLogicContext ctx)
            throws AAIServiceException {

        AAIDatum response = null;

        try {
            AAIRequest request = AAIRequest.createRequest(resource, params);
            if(request == null) {
                return null;
            }

            request.processRequestPathValues(params);
            String rv = getExecutor().get(request);
            response = request.jsonStringToObject(rv);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            getLogger().warn("newModelBackupRequest", exc);
            throw new AAIServiceException(exc);
        }

        return response;
    }


    @Override
    public QueryStatus release(String arg0, String arg1, SvcLogicContext arg2) throws SvcLogicException {
        throw new SvcLogicException("Method AAIService.release() has not been implemented yet");
    }

    @Override
    public QueryStatus reserve(String arg0, String arg1, String arg2, String arg3, SvcLogicContext arg4)
            throws SvcLogicException {
        throw new SvcLogicException("Method AAIService.reserve() has not been implemented yet");
    }

    private QueryStatus newModelSave(String resource, boolean force, String key, Map<String, String> params, String prefix, SvcLogicContext ctx) {
        getLogger().debug("Executing newModelSave for resource : " + resource);
        HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);

        try {
            ArrayList<String> subResources = new ArrayList<>();
            Set<String> set = params.keySet();
            Map<String, Method> setters = new HashMap<>();
            Map<String, Method> getters = new HashMap<>();

            // 1. find class
            AAIRequest request = AAIRequest.createRequest(resource, nameValues);
            Class<? extends AAIDatum> resourceClass = request.getModelClass();
            getLogger().debug(resourceClass.getName());
            AAIDatum instance = resourceClass.newInstance();

            {
                Annotation[] annotations = resourceClass.getAnnotations();
                for(Annotation annotation : annotations) {
                    Class<? extends Annotation> anotationType = annotation.annotationType();
                    String annotationName = anotationType.getName();

                    // 2. find string property setters and getters for the lists
                    if("javax.xml.bind.annotation.XmlType".equals(annotationName)){
                        XmlType order = (XmlType)annotation;
                        String[]  values = order.propOrder();
                        for(String value : values) {
                            String id = AAIServiceUtils.camelCaseToDashedString(value);
                            Field field = resourceClass.getDeclaredField(value);
                            Class<?> type = field.getType();
                            Method setter = null;
                            try {
                                setter = resourceClass.getMethod("set"+StringUtils.capitalize(value), type);
                                if(type.getName().startsWith("java.lang") || "boolean".equals(type.getName()) || "long".equals(type.getName()) || "int".equals(type.getName())) {
                                    try {
                                        Object arglist[] = new Object[1];
                                        arglist[0] = params.get(id);

                                        if(arglist[0] != null) {
                                            if(!type.getName().equals("java.lang.String")) {
//                                            getLogger().debug(String.format("Processing %s with parameter %s", types[0].getName(), value));
                                                if("java.lang.Long".equals(type.getName()) || "java.lang.Integer".equals(type.getName())) {
                                                    String fv = params.get(id);
                                                    if(fv == null || fv.isEmpty()) {
                                                        arglist[0] = null;
                                                    } else {
                                                        arglist[0] = valueOf(type, params.get(id));
                                                    }
                                                } else if("boolean".equals(type.getName())) {
                                                    arglist[0] = valueOf(Boolean.class, params.get(id));
                                                } else if("int".equals(type.getName())) {
                                                    arglist[0] = valueOf(Integer.class, params.get(id));
                                                } else if("long".equals(type.getName())) {
                                                    String fv = params.get(id);
                                                    if(fv == null || fv.isEmpty()) {
                                                        arglist[0] = null;
                                                    } else {
                                                        arglist[0] = valueOf(Long.class, params.get(id));
                                                    }
                                                } else {
                                                    arglist[0] = valueOf(type, params.get(id));
                                                }
                                            }
                                            Object obj = setter.invoke(instance, arglist);
                                        }
                                        set.remove(id);

                                    } catch (Exception x) {
                                        Throwable cause = x.getCause();
                                        getLogger().warn("Failed process for " + resourceClass.getName(), x);
                                    }
                                } else if("java.util.List".equals(type.getName())) {
                                    List<String> newValues = new ArrayList<>();
                                    String length = id+"_length";
                                    if(!params.isEmpty() && params.containsKey(length)) {
                                        String tmp = params.get(length);
                                        int count = Integer.parseInt(tmp);
                                        for(int i=0; i<count; i++) {
                                            String tmpValue = params.get(String.format("%s[%d]", id, i));
                                            newValues.add(tmpValue);
                                        }
                                        if(!newValues.isEmpty()) {
                                            Object o = setter.invoke(instance, newValues);
                                        }
                                    }
                                    set.remove(id);
                                } else {
                                    setters.put(id, setter);
                                }
                            } catch(Exception exc) {
                                getLogger().warn(AAI_SERVICE_EXCEPTION, exc);
                            }

                            Method getter;
                            try {
                                getter = resourceClass.getMethod("get"+StringUtils.capitalize(value));
                                if(!type.getName().equals("java.lang.String")) {
                                    getters.put(id, getter);
                                }
                            } catch(Exception exc) {
                                getLogger().warn(AAI_SERVICE_EXCEPTION, exc);
                            }

                        }
                        subResources.addAll(Arrays.asList(values));
                    }
                }
            }

            // remove getters that have matching setter
            for(String setKey : setters.keySet()) {
                if(getters.containsKey(setKey)) {
                    getters.remove(setKey);
                }
            }

            Set<String> relationshipKeys = new TreeSet<>();
            Set<String> vlansKeys = new TreeSet<>();
            Set<String> metadataKeys = new TreeSet<>();

            for(String attribute : set) {
                String value = params.get(attribute);
                if(attribute.startsWith("relationship-list")) {
                    relationshipKeys.add(attribute);
                } else if(attribute.startsWith("vlans")) {
                    vlansKeys.add(attribute);
                } else if(attribute.startsWith("metadata")) {
                    metadataKeys.add(attribute);
                }
            }
            // 3. find list property getters
            for(String attribute : set) {
                String value = params.get(attribute);
                Method method = getters.get(attribute);
                if(method != null) {
                    try {
                        Object arglist[] = new Object[0];
//                        arglist[0] = value;
                        Class<?>[] types = method.getParameterTypes();
                        if(types.length == 0){
                            Object o = method.invoke(instance, arglist);
                            if(o instanceof ArrayList) {
                                ArrayList<String> values = (ArrayList<String>)o;
                                value = value.replace("[", "").replace("]", "");
                                List<String> items = Arrays.asList(value.split("\\s*,\\s*"));
                                for(String s : items) {
                                    values.add(s.trim());
                                }
                            }
                        }
                    } catch (Exception x) {
                        Throwable cause = x.getCause();
                        getLogger().warn("Failed process for " + resourceClass.getName(), x);
                    }
                }
            }
            // 4. Process Relationships
            // add relationship list
            if( (subResources.contains("relationship-list") || subResources.contains("relationshipList")) &&  !relationshipKeys.isEmpty()) {
                RelationshipList relationshipList = null;
                Object obj = null;
                Method getRelationshipListMethod = null;
                try {
                    getRelationshipListMethod = resourceClass.getMethod("getRelationshipList");
                } catch(Exception exc) {
                    getLogger().debug(RELATIONSHIP_DATA + exc.getMessage());
                }

                if(getRelationshipListMethod != null){
                    try {
                        obj = getRelationshipListMethod.invoke(instance);
                    } catch (InvocationTargetException x) {
                        Throwable cause = x.getCause();
                    }
                }
                if(obj != null && obj instanceof RelationshipList){
                    relationshipList = (RelationshipList)obj;
                } else {
                    relationshipList = new RelationshipList();
                    Method setRelationshipListMethod = resourceClass.getMethod("setRelationshipList", RelationshipList.class);
                    if(setRelationshipListMethod != null){
                        try {
                            Object arglist[] = new Object[1];
                            arglist[0] = relationshipList;

                            obj = setRelationshipListMethod.invoke(instance, arglist);
                        } catch (InvocationTargetException x) {
                            Throwable cause = x.getCause();
                        }
                    }
                }

                List<Relationship> relationships = relationshipList.getRelationship();

                int i = 0;
                while(true){
                    String searchKey = "relationship-list.relationship[" + i + "].related-to";
                    if(!params.containsKey(searchKey))
                        break;
                    int j = 0;
                    String relatedTo = params.get(searchKey);
                    String relatedLinkKey = "relationship-list.relationship[" + i + "].related-link";
                    String relatedLink = null;
                    if(params.containsKey(relatedLinkKey)) {
                        relatedLink = params.get(relatedLinkKey);
                    }
                    Relationship relationship = new Relationship();
                    relationships.add(relationship);
                    relationship.setRelatedTo(relatedTo);
                    String relationshipLabel = "relationship-list.relationship[" + i + "].relationship-label";
                    if(params.containsKey(searchKey)) {
                        relationship.setRelationshipLabel(params.get(relationshipLabel));
                    }
                    getLogger().debug("About to process related link of {}", relatedLink);
                    if(relatedLink != null) {
                        if(relatedLink.contains("v$"))
                            relatedLink = relatedLink.replace(VERSION_PATTERN, "/v14/");
                        relationship.setRelatedLink(relatedLink);
                    } else {
                        Map<String, String> relParams = new HashMap<>();

                        while(true) {
                            String searchRelationshipKey = "relationship-list.relationship[" + i + "].relationship-data[" + j + "].relationship-key";
                            String searchRelationshipValue = "relationship-list.relationship[" + i + "].relationship-data[" + j + "].relationship-value";
                            if(!params.containsKey(searchRelationshipKey))
                                break;

                            relParams.put(params.get(searchRelationshipKey), params.get(searchRelationshipValue));
                            j++;
                        }
                        AAIRequest rlRequest = AAIRequest.createRequest(relatedTo, relParams);
                        for(Map.Entry<String,String> entry : relParams.entrySet()) {
                            rlRequest.addRequestProperty(entry.getKey(), entry.getValue());
                        }
                        String path = rlRequest.updatePathDataValues(null);
                        relationship.setRelatedLink(path);
                    }
                    {
                        int k = 0;
                        // process related to properties
                        Map<String, String> relParams = new HashMap<String, String>();

                        while(true) {
                            String searchRelatedToKey = "relationship-list.relationship[" + i + "].related-to-property[" + k + "].property-key";
                            String searchRelatedToValue = "relationship-list.relationship[" + i + "].related-to-property[" + k + "].property-value";
                            if(!params.containsKey(searchRelatedToKey))
                                break;

                            RelatedToProperty relDatum = new RelatedToProperty();
                            relDatum.setPropertyKey(params.get(searchRelatedToKey));
                            relDatum.setPropertyValue(params.get(searchRelatedToValue));
                            relationship.getRelatedToProperty().add(relDatum);

                            relParams.put(params.get(searchRelatedToKey), params.get(searchRelatedToValue));
                            k++;
                        }
                    }
                    i++;
                }
            }

            // 4. vlans
            if(subResources.contains("vlans") &&  !vlansKeys.isEmpty()) {
                Object obj = null;
                Vlans vlanList = null;
                Method getVLansMethod = resourceClass.getMethod("getVlans");
                if(getVLansMethod != null){
                    try {
                        obj = getVLansMethod.invoke(instance);
                    } catch (InvocationTargetException x) {
                        Throwable cause = x.getCause();
                    }
                }
                if(obj != null && obj instanceof Vlans){
                    vlanList = (Vlans)obj;
                } else {
                    vlanList = new Vlans();
                    Method setVlansMethod = resourceClass.getMethod("setVlans", Vlans.class);
                    if(setVlansMethod != null){
                        try {
                            Object arglist[] = new Object[1];
                            arglist[0] = vlanList;

                            obj = setVlansMethod.invoke(instance, arglist);
                        } catch (InvocationTargetException x) {
                            Throwable cause = x.getCause();
                        }
                    }
                }

                int i = 0;
                while(true){
                    String searchKey = "vlans.vlan[" + i + "].vlan-interface";
                    if(!params.containsKey(searchKey))
                        break;

                    String vlanInterface = params.get("vlans.vlan[" + i + "].vlan-interface");
                    String vlanIdInner    = params.get("vlans.vlan[" + i + "].vlan-id-inner");
                    String vlanIdOute     = params.get("vlans.vlan[" + i + "].vlan-id-outer");
                    String speedValue     = params.get("vlans.vlan[" + i + "].speed-value");
                    String speedUnits     = params.get("vlans.vlan[" + i + "].speed-units");

                    Vlan vlan = new Vlan();
                    vlan.setVlanInterface(vlanInterface);

                    if(vlanIdInner != null) {
                        Long iVlanIdInner = Long.parseLong(vlanIdInner);
                        vlan.setVlanIdInner(iVlanIdInner);
                    }

                    if(vlanIdOute != null) {
                        Long iVlanIdOuter = Long.parseLong(vlanIdOute);
                        vlan.setVlanIdOuter(iVlanIdOuter);
                    }

                    if(speedValue != null) {
                        vlan.setSpeedValue(speedValue);
                        vlan.setSpeedUnits(speedUnits);
                    }

                    vlanList.getVlan().add(vlan);
                    i++;
                }
            }

            // 5. metadata
            if(subResources.contains("metadata") &&  !metadataKeys.isEmpty()) {
                Object obj = null;
                Metadata metadataList = null;
                Method getMetadataMethod = resourceClass.getMethod("getMetadata");
                if(getMetadataMethod != null){
                    try {
                        obj = getMetadataMethod.invoke(instance);
                    } catch (InvocationTargetException x) {
                        Throwable cause = x.getCause();
                    }
                }
                if(obj != null && obj instanceof Metadata){
                    metadataList = (Metadata)obj;
                } else {
                    metadataList = new Metadata();
                    Method setMetadataMethod = resourceClass.getMethod("setMetadata", Metadata.class);
                    if(setMetadataMethod != null){
                        try {
                            Object arglist[] = new Object[1];
                            arglist[0] = metadataList;

                            obj = setMetadataMethod.invoke(instance, arglist);
                        } catch (InvocationTargetException x) {
                            Throwable cause = x.getCause();
                        }
                    }
                }

                // process data
                int i = 0;
                while(true){
                    String metaKey = "metadata.metadatum[" + i + "].meta-key";
                    if(!params.containsKey(metaKey))
                        break;

                    String metaValue = params.get("metadata.metadatum[" + i + "].meta-value");

                    Metadatum vlan = new Metadatum();
                    vlan.setMetaname(metaKey);
                    vlan.setMetaval(metaValue);

                    metadataList.getMetadatum().add(vlan);
                    i++;
                }

            }


            // 6. Prepare AAI request
            String[] args = request.getArgsList();
            for(String arg : args) {
                String modifiedKey = arg.replaceAll("-", "_");
                if(nameValues.containsKey(modifiedKey)) {
                    String argValue = nameValues.get(modifiedKey);
                    if(argValue != null) argValue = argValue.trim().replace("'", "").replace("$", "").replace("'", "");
                    request.addRequestProperty(arg, argValue);
                }
            }

            request.processRequestPathValues(nameValues);
            request.setRequestObject(instance);
            Object response = getExecutor().post(request);
            if(request.expectsDataFromPUTRequest()){
                if(response != null && response instanceof String) {
                    String rv = response.toString();
                    QueryStatus retval = processResponseData(rv, resource, request, prefix,  ctx, nameValues, null);
                    getLogger().debug("newModelSave - returning " + retval.toString());
                    return retval;
                }
            }

        } catch(AAIServiceException exc){
            ctx.setAttribute(prefix + ".error.message", exc.getMessage());
            int returnCode = exc.getReturnCode();
            if(returnCode >= 300) {
                ctx.setAttribute(prefix + ".error.http.response-code",
                        Integer.toString(exc.getReturnCode()));
            }

            if(returnCode == 400 || returnCode == 412)
                return QueryStatus.FAILURE;
            else if(returnCode == 404)
                return QueryStatus.NOT_FOUND;
            else {
                getLogger().warn("Failed newModelSave - returning FAILURE", exc);
                return QueryStatus.FAILURE;
            }
        } catch(Exception exc){
            getLogger().warn("Failed newModelSave - returning FAILURE", exc);
            ctx.setAttribute(prefix + ".error.message", exc.getMessage());
            return QueryStatus.FAILURE;
        }

        getLogger().debug("newModelSave - returning SUCCESS");
        return QueryStatus.SUCCESS;
    }

    private QueryStatus newModelProcessRelationshipList(Object instance, Map<String, String> params, String prefix, SvcLogicContext ctx) throws Exception {

        Class resourceClass = instance.getClass();

        Set<String> relationshipKeys = new TreeSet<>();

        Set<String> set = params.keySet();

        for(String attribute : set) {
            String value = params.get(attribute);

            if(attribute.startsWith("relationship-list")) {
                relationshipKeys.add(attribute);
            }
        }

        // 3. Process Relationships
        // add relationship list
        if(!relationshipKeys.isEmpty()) {
            RelationshipList relationshipList;
            Object obj = null;
            Method getRelationshipListMethod = null;
            try {
                getRelationshipListMethod = resourceClass.getMethod("getRelationshipList");
            } catch(Exception exc) {
                getLogger().debug(RELATIONSHIP_DATA + exc.getMessage());
            }
            if(getRelationshipListMethod != null){
                try {
                    obj = getRelationshipListMethod.invoke(instance);
                } catch (InvocationTargetException x) {
                    Throwable cause = x.getCause();
                }
            }
            if(obj != null && obj instanceof RelationshipList){
                relationshipList = (RelationshipList)obj;
            } else {
                relationshipList = new RelationshipList();
                Method setRelationshipListMethod = resourceClass.getMethod("setRelationshipList", RelationshipList.class);
                if(setRelationshipListMethod != null){
                    try {
                        Object arglist[] = new Object[1];
                        arglist[0] = relationshipList;

                        obj = setRelationshipListMethod.invoke(instance, arglist);
                    } catch (InvocationTargetException x) {
                        Throwable cause = x.getCause();
                    }
                }
            }

            boolean createdNewRelationships = false;
            List<Relationship> relationships = relationshipList.getRelationship();
            if(relationships == null) {
                relationships = new ArrayList<>();
                createdNewRelationships = true;
            }

            int i = 0;
            while(true){
                String searchKey = "relationship-list.relationship[" + i + "].related-to";
                if(!params.containsKey(searchKey))
                    break;

                String relatedTo = params.get(searchKey);
                String relatedLinkKey = "relationship-list.relationship[" + i + "].related-link";
                String relatedLink = null;
                if(params.containsKey(relatedLinkKey)) {
                    relatedLink = params.get(relatedLinkKey);
                }

                Relationship relationship = new Relationship();
                relationships.add(relationship);
                relationship.setRelatedTo(relatedTo);

                String relationshipLabel = "relationship-list.relationship[" + i + "].relationship-label";
                if(params.containsKey(searchKey)) {
                    relationship.setRelationshipLabel(params.get(relationshipLabel));
                }

                if (relatedLink != null) {
                    if(relatedLink.contains("v$"))
                        relatedLink = relatedLink.replace(VERSION_PATTERN,  AAIRequest.getSupportedAAIVersion());
                    relationship.setRelatedLink(relatedLink);
                } else {
                    Map<String, String> relParams = new HashMap<>();
                    int j = 0;

                    while (true) {
                        String searchRelationshipKey = "relationship-list.relationship[" + i + "].relationship-data["
                                + j + "].relationship-key";
                        String searchRelationshipValue = "relationship-list.relationship[" + i + "].relationship-data["
                                + j + "].relationship-value";
                        if (!params.containsKey(searchRelationshipKey))
                            break;

                        RelationshipData relDatum = new RelationshipData();
                        relDatum.setRelationshipKey(params.get(searchRelationshipKey));
                        relDatum.setRelationshipValue(params.get(searchRelationshipValue));
                        relationship.getRelationshipData().add(relDatum);

                        relParams.put(params.get(searchRelationshipKey), params.get(searchRelationshipValue));
                        j++;
                    }
                    AAIRequest rlRequest = AAIRequest.createRequest(relatedTo, relParams);
                    for (Map.Entry<String, String> entry : relParams.entrySet()) {
                        rlRequest.addRequestProperty(entry.getKey(), entry.getValue());
                    }
                    String path = rlRequest.updatePathDataValues(null);
                    relationship.setRelatedLink(path);
                }
                {
                    int k = 0;
                    // process related to properties
                    Map<String, String> relParams = new HashMap<String, String>();

                    while(true) {
                        String searchRelatedToKey = "relationship-list.relationship[" + i + "].related-to-property[" + k + "].property-key";
                        String searchRelatedToValue = "relationship-list.relationship[" + i + "].related-to-property[" + k + "].property-value";
                        if(!params.containsKey(searchRelatedToKey))
                            break;

                        RelatedToProperty relDatum = new RelatedToProperty();
                        relDatum.setPropertyKey(params.get(searchRelatedToKey));
                        relDatum.setPropertyValue(params.get(searchRelatedToValue));
                        relationship.getRelatedToProperty().add(relDatum);

                        relParams.put(params.get(searchRelatedToKey), params.get(searchRelatedToValue));
                        k++;
                    }
                }

                i++;
            }
        }

        return QueryStatus.SUCCESS;
    }

    private QueryStatus newModelProcessMetadata(Object instance, Map<String, String> params, String prefix, SvcLogicContext ctx) throws Exception {

        if (!(instance instanceof ServiceInstance) && !(instance instanceof Image)) {
            throw new IllegalArgumentException("request is not applicable for selected request");
        }

        Class resourceClass = instance.getClass();
        Set<String> metadataKeys = new TreeSet<String>();
        Set<String> set = params.keySet();
        for(String attribute : set) {
            if(attribute.startsWith("metadata")) {
                metadataKeys.add(attribute);
            }
        }

        // 3. Process Metadata
        // add metadata
        if(!metadataKeys.isEmpty()) {
            Metadata metadata = null;
            Object obj = null;
            Method getMetadataMethod = resourceClass.getMethod("getMetadata");
            if(getMetadataMethod != null){
                try {
                    obj = getMetadataMethod.invoke(instance);
                } catch (InvocationTargetException x) {
                    Throwable cause = x.getCause();
                }
            }
            if(obj != null && obj instanceof Metadata){
                metadata = (Metadata)obj;
            } else {
                metadata = new Metadata();
                Method setMetadataMethod = resourceClass.getMethod("setMetadata", Metadata.class);
                if(setMetadataMethod != null){
                    try {
                        setMetadataMethod.invoke(instance, metadata);
                    } catch (InvocationTargetException x) {
                    }
                }
            }

            List<Metadatum> metadatumList = metadata.getMetadatum();
            int i = 0;
            while(true){
                String metaNameKey = "metadata.metadatum[" + i + "].metaname";
                String metaValueKey = "metadata.metadatum[" + i + "].metaval";
                if(!params.containsKey(metaNameKey) || !params.containsKey(metaValueKey))
                    break;

                Metadatum metadatum = new Metadatum();
                metadatum.setMetaname(params.get(metaNameKey));
                metadatum.setMetaval(params.get(metaValueKey));
                metadatumList.add(metadatum);

                i++;
            }
        }

        return QueryStatus.SUCCESS;
    }

    private Relationship findRelationship(List<Relationship> relationships, String relatedTo) {
        if(relatedTo == null)
            return null;

        for(Relationship relationship : relationships) {
            if(relationship.getRelatedTo().equals(relatedTo)){
                return relationship;
            }
        }
        return null;
    }


    public QueryStatus backup(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        String resource = params.get("resource").toLowerCase();
        String prefix = params.get("data-key");

        HashMap<String, String> nameValues = new HashMap<>();
        if(AAIRequest.createRequest(resource, nameValues) != null) {

            try {
                return newModelBackupRequest(resource, params, prefix, ctx);
            } catch (Exception exc) {
                getLogger().warn("Failed backup - returning FAILURE", exc);
                return QueryStatus.FAILURE;
            }
        }

        return QueryStatus.NOT_FOUND;
    }

    @Override
    public QueryStatus restore(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        QueryStatus retval = QueryStatus.SUCCESS;
        String resource = params.get("resource").toLowerCase();
        String prefix = params.get("data-key");

        HashMap<String, String> nameValues = new HashMap<>();
        if(AAIRequest.createRequest(resource, nameValues) != null) {

            try {
                retval = newModelBackupRequest(resource, params, "tmpRestore", ctx);
                if(retval == QueryStatus.SUCCESS) {
                    ctx.setAttribute("tmpRestore", null);
                }
            } catch (Exception exc) {
                getLogger().warn("Failed restore - returning FAILURE", exc);
                return QueryStatus.FAILURE;
            }
        }

        return QueryStatus.NOT_FOUND;
    }

    protected Map<String, Object> objectToProperties(Object object) {
        ObjectMapper mapper = AAIService.getObjectMapper();
        return mapper.convertValue(object, Map.class);
    }

    static <T> T valueOf(Class<T> klazz, String arg) {
        Exception cause = null;
        T ret = null;
        try {
            ret = klazz.cast(klazz.getDeclaredMethod("valueOf", String.class).invoke(null, arg));
        } catch (NoSuchMethodException exc) {
            LoggerFactory.getLogger(AAIService.class).warn("Wrong data type", exc);
            ret = klazz.cast(arg);
        } catch (IllegalAccessException e) {
            cause = e;
        } catch (InvocationTargetException e) {
            cause = e;
        }
        if (cause == null) {
            return ret;
        } else {
            throw new IllegalArgumentException(cause);
        }
    }

    private QueryStatus processDeleteRelationshipList(String resource, String key, SvcLogicContext ctx, HashMap<String, String> nameValues) {
        try {
            AAIRequest request = AAIRequest.createRequest(resource.split(":")[0], nameValues);
            if(request == null) {
                return QueryStatus.FAILURE;
            }

            request.processRequestPathValues(nameValues);
            URL url = request.getRequestUrl("GET", null);

            Class resourceClass = request.getModelClass();
            Object instance = getResource(url.toString(), resourceClass);
            if(instance == null)
                return QueryStatus.NOT_FOUND;

            // get resource version
            String resourceVersion = null;
            Method getResourceVersionMethod = resourceClass.getMethod("getResourceVersion");
            if(getResourceVersionMethod != null){
                try {
                    Object object = getResourceVersionMethod.invoke(instance);
                    if(object != null)
                        resourceVersion = object.toString();
                } catch (InvocationTargetException exc) {
                    getLogger().warn("Retrieving resource version", exc);
                }
            }

            RelationshipList relationshipList = null;
            Object obj = null;
            Method getRelationshipListMethod = null;
            try {
                getRelationshipListMethod = resourceClass.getMethod("getRelationshipList");
            } catch(Exception exc) {
                getLogger().debug(RELATIONSHIP_DATA + exc.getMessage());
            }
            if(getRelationshipListMethod != null){
                try {
                    obj = getRelationshipListMethod.invoke(instance);
                } catch (InvocationTargetException x) {
                    Throwable cause = x.getCause();
                }
            }
            if(obj != null && obj instanceof RelationshipList){
                relationshipList = (RelationshipList)obj;
            } else {
                getLogger().debug("No relationships found to process.");
                return QueryStatus.NOT_FOUND;
            }

            if(relationshipList.getRelationship() == null || relationshipList.getRelationship().isEmpty()) {
                return QueryStatus.NOT_FOUND;
            }
            String relatedTo = nameValues.get("related_to");
            if(relatedTo == null) {
                return QueryStatus.FAILURE;
            }

            relatedTo = relatedTo.replaceAll("_", "-");

            String relatedLink = nameValues.get("relationship.related_link");
            if(relatedLink != null) {
                relatedLink = URLDecoder.decode(relatedLink, "UTF-8");
            }

            List<Relationship> relationships = relationshipList.getRelationship();
            List<Relationship> relationshipsToDelete = new LinkedList<>();

            for(Relationship relationship : relationships) {
                if(relatedTo.equals(relationship.getRelatedTo())) {
                    if(relatedLink != null) {
                        if(relationship.getRelatedLink() != null ) {
                            String localRelatedLink = relationship.getRelatedLink();
                            localRelatedLink = URLDecoder.decode(localRelatedLink, "UTF-8");
                            if(localRelatedLink.endsWith(relatedLink)) {
                                getLogger().debug(String.format("Found relationship of '%s' to keyword '%s'", relationship.getRelatedTo(),  relatedTo));
                                relationshipsToDelete.add(relationship);
                            }
                        }
                    } else {
                        getLogger().debug(String.format("Found relationship of '%s' to keyword '%s'", relationship.getRelatedTo(),  relatedTo));
                        relationshipsToDelete.add(relationship);
                    }
                }
            }
            if(relationshipsToDelete == null || relationshipsToDelete.isEmpty()) {
                getLogger().info(String.format("Relationship has not been found for %s", key));
                return QueryStatus.NOT_FOUND;
            }

            String path = url.toString();
            path = path + "/relationship-list/relationship";
            URL deleteUrl = new URL(path);

            ObjectMapper mapper = AAIService.getObjectMapper();

            boolean cumulativeResponse = true;

            for(Relationship targetRelationship : relationshipsToDelete) {
                String json_text = mapper.writeValueAsString(targetRelationship);
                boolean response = deleteList(deleteUrl, json_text);
                if(!response)
                    cumulativeResponse = response;

            }

            if(!cumulativeResponse)
                return QueryStatus.FAILURE;

            return QueryStatus.SUCCESS;

        } catch(Exception exc) {
            getLogger().warn("processDelete", exc);
            return QueryStatus.FAILURE;
        }
    }

    private QueryStatus processDeleteMetadata(String resource, String key, SvcLogicContext ctx, HashMap<String, String> nameValues) {
        try {
            AAIRequest request = AAIRequest.createRequest(resource, nameValues);
            if(request == null) {
                return QueryStatus.FAILURE;
            }

            request.processRequestPathValues(nameValues);
            URL url = request.getRequestUrl("GET", null);

            Class<?> resourceClass = request.getModelClass();
            Object instance = getResource(url.toString(), resourceClass);

            // get resource version
            String resourceVersion = null;
            Method getResourceVersionMethod = resourceClass.getMethod("getResourceVersion");
            if(getResourceVersionMethod != null){
                try {
                    resourceVersion = (String) getResourceVersionMethod.invoke(instance);
                } catch (InvocationTargetException x) {
                }
            }

            Metadata metadata = null;
            Object obj = null;
            Method getMetadataMethod = resourceClass.getMethod("getMetadata");
            if(getMetadataMethod != null){
                try {
                    obj = getMetadataMethod.invoke(instance);
                } catch (InvocationTargetException x) {
                    Throwable cause = x.getCause();
                }
            }
            if(obj != null && obj instanceof Metadata){
                metadata = (Metadata)obj;
            } else {
                getLogger().debug("No metadata found to process.");
                return QueryStatus.NOT_FOUND;
            }

            if(metadata.getMetadatum() == null || metadata.getMetadatum().isEmpty()) {
                return QueryStatus.NOT_FOUND;
            }

            List<Metadatum> metadatumList = metadata.getMetadatum();
            Metadatum metadatumToDelete = null;

            final String metaname = nameValues.get("metaname");

            for(Metadatum metadatum : metadatumList) {
                getLogger().debug(String.format("Comparing existing metadatum of '%s' to keyword '%s'", metadatum.getMetaname(),  metaname));
                if(metaname.equals(metadatum.getMetaname())) {
                    metadatumToDelete = metadatum;
                    break;
                }
            }
            if(metadatumToDelete == null) {
                getLogger().info(String.format("Metadatum has not been found for %s", key));
                return QueryStatus.NOT_FOUND;
            }

            String path = url.toString();
            path = path + "/metadata/metadatum/" + encodeQuery( metadatumToDelete.getMetaname() ) +
                    "?resource-version=" + metadatumToDelete.getResourceVersion();
            URL deleteUrl = new URL(path);
            boolean response = deleteList(deleteUrl, null);

            if(!response)
                return QueryStatus.FAILURE;

            return QueryStatus.SUCCESS;

        } catch(Exception exc) {
            getLogger().warn("processDelete", exc);
            return QueryStatus.FAILURE;
        }
    }

    protected String encodeQuery(String param) throws UnsupportedEncodingException {
        return URLEncoder.encode(param, "UTF-8").replace("+", "%20");
    }

    static final Map<String, String> ctxGetBeginsWith( SvcLogicContext ctx, String prefix ) {
        Map<String, String> tmpPrefixMap = new HashMap<>();

        if(prefix == null || prefix.isEmpty()){
            return tmpPrefixMap;
        }

        for( String key : ctx.getAttributeKeySet() ) {
            if( key.startsWith(prefix) ) {
                String tmpKey = key.substring(prefix.length() + 1);
                tmpPrefixMap.put( tmpKey, ctx.getAttribute(key));
            }
        }

        Map<String, String> prefixMap = new HashMap<>();
        Pattern p = Pattern.compile(".*\\[\\d\\]");

        SortedSet<String> keys = new TreeSet<String>(tmpPrefixMap.keySet () );
        for(String key : keys) {
            Matcher m = p.matcher(key);
            if(m.matches()) {
                continue;
            } else if(key.endsWith("_length")) {
                String listKey = key.substring(0, key.indexOf("_length"));
                int max = Integer.parseInt(tmpPrefixMap.get(key));

                ArrayList<String> data = new ArrayList<>();
                for(int x = 0; x < max; x++){
                    String tmpKey = String.format("%s[%d]", listKey, x);
                    String tmpValue = tmpPrefixMap.get(tmpKey);
                    if(tmpValue != null && !tmpValue.isEmpty()) {
                        data.add(tmpValue);
                    }
                }
                if(!data.isEmpty()) {
                    prefixMap.put(listKey, data.toString());
                } else {
                    prefixMap.put(key, tmpPrefixMap.get(key));
                }
            } else {
                prefixMap.put(key, tmpPrefixMap.get(key));
            }
        }

        return prefixMap;
    }

    /**
     */
    protected NamedQueryData extractNamedQueryDataFromQueryPrefix(HashMap<String, String> nameValues, Map<String, String> parms) {
        if(parms.isEmpty()) {
            return null;
        }

        NamedQueryData data = new NamedQueryData();

        // query parameters
        if(data.getQueryParameters() == null) {
            data.setQueryParameters(new QueryParameters());
        }
        String namedQueryUuid = nameValues.get("named-query-uuid".replaceAll("-", "_"));
        if(namedQueryUuid == null) {
            namedQueryUuid = parms.get("query-parameters.named-query.named-query-uuid");
        }
        NamedQuery namedQuery = new NamedQuery();
        namedQuery.setNamedQueryUuid(namedQueryUuid);
        data.getQueryParameters().setNamedQuery(namedQuery);

        // instance filters
        if(data.getInstanceFilters() == null) {
            data.setInstanceFilters(new InstanceFilters());
        }


        String quantity = parms.get("instance-filters.instance-filter_length");
        if(quantity != null && StringUtils.isNumeric(quantity)) {
            int max = Integer.parseInt(quantity);
            for(int i = 0; i < max; i++) {
                String keyPattern = String.format("instance-filters.instance-filter[%d].", i);
                Set<String> keys = parms.keySet();
                for(String key: keys) {
                    if(key.startsWith(keyPattern)){
                        String value = parms.get(key);
                        String remainder = key.substring(keyPattern.length());
                        String[] split = remainder.split("\\.");
                        getLogger().debug(String.format("%s", remainder));
                        if("logical-link".equals(split[0])) {
                            InstanceFilter insf = null;
                            if(data.getInstanceFilters().getInstanceFilter().isEmpty()) {
                                insf = new InstanceFilter();
                                data.getInstanceFilters().getInstanceFilter().add(insf);
                            } else {
                                insf = data.getInstanceFilters().getInstanceFilter().get(0);
                            }
                            LogicalLink logicalLink = insf.getLogicalLink();
                            if(logicalLink == null) {
                                logicalLink = new LogicalLink();
                                insf.setLogicalLink(logicalLink);
                            }

                            switch(split[1]) {
                                case "link-name":
                                    logicalLink.setLinkName(value);
                                    break;
                                case "link-type":
                                    logicalLink.setLinkType(value);
                                    break;
                                case "operational-state":
                                    logicalLink.setOperationalStatus(value);
                                    break;
                            }

                        } else if("pnf".equals(split[0])) {
                            Pnf pnf = new Pnf();
                            pnf.setPnfName(value);

                            InstanceFilter insf = new InstanceFilter();
                            insf.setPnf(pnf);
                            data.getInstanceFilters().getInstanceFilter().add(insf);

                        } else if("service-instance".equals(split[0])) {
                            ServiceInstance serviceInstance = new ServiceInstance();
                            serviceInstance.setServiceInstanceId(value);

                            InstanceFilter insf = new InstanceFilter();
                            insf.setServiceInstance(serviceInstance);
                            data.getInstanceFilters().getInstanceFilter().add(insf);

                        } else if("l3-network".equals(split[0])) {
                            L3Network l3Network = new L3Network();
                            if("network-role".equals(split[1])) {
                                l3Network.setNetworkRole(value);
                            }

                            InstanceFilter insf = new InstanceFilter();
                            insf.setL3Network(l3Network);
                            data.getInstanceFilters().getInstanceFilter().add(insf);
                        } else if("generic-vnf".equals(split[0])) {
                            GenericVnf vnf = new GenericVnf();
                            if("vnf-id".equals(split[1])) {
                                vnf.setVnfId(value);
                            }

                            InstanceFilter insf = new InstanceFilter();
                            insf.setGenericVnf(vnf);
                            data.getInstanceFilters().getInstanceFilter().add(insf);
                        }
                    }
                }
            }
        }

        return data;
    }

    public abstract <T> T getResource(String key, Class<T> type) throws AAIServiceException ;
    protected abstract boolean deleteList(URL url, String caller) throws AAIServiceException;
}
