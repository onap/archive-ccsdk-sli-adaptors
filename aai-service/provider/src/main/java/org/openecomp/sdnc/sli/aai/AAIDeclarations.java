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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.openecomp.sdnc.sli.aai.AAIService.AAIRequestExecutor;
import org.openecomp.sdnc.sli.aai.data.AAIDatum;
import org.openecomp.sdnc.sli.aai.query.FormattedQueryResultList;
import org.openecomp.sdnc.sli.aai.query.InstanceFilter;
import org.openecomp.sdnc.sli.aai.query.InstanceFilters;
import org.openecomp.sdnc.sli.aai.query.NamedQuery;
import org.openecomp.sdnc.sli.aai.query.NamedQueryData;
import org.openecomp.sdnc.sli.aai.query.QueryParameters;
import org.openecomp.sdnc.sli.aai.query.Results;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openecomp.aai.inventory.v11.*;


public abstract class AAIDeclarations implements AAIClient {

	public static final String TRUSTSTORE_PATH    = "org.openecomp.sdnc.sli.aai.ssl.trust";
	public static final String TRUSTSTORE_PSSWD   = "org.openecomp.sdnc.sli.aai.ssl.trust.psswd";
	public static final String KEYSTORE_PATH      = "org.openecomp.sdnc.sli.aai.ssl.key";
	public static final String KEYSTORE_PSSWD     = "org.openecomp.sdnc.sli.aai.ssl.key.psswd";

	public static final String APPLICATION_ID     = "org.openecomp.sdnc.sli.aai.application";

	public static final String CLIENT_NAME		  = "org.openecomp.sdnc.sli.aai.client.name";
	public static final String CLIENT_PWWD		  = "org.openecomp.sdnc.sli.aai.client.psswd";


	public static final String CONNECTION_TIMEOUT = "connection.timeout";
	public static final String READ_TIMEOUT 	  = "read.timeout";

	public static final String TARGET_URI         = "org.openecomp.sdnc.sli.aai.uri";

	// Availability zones query
	public static final String QUERY_PATH         = "org.openecomp.sdnc.sli.aai.path.query";

	// Update
	public static final String UPDATE_PATH		  = "org.openecomp.sdnc.sli.aai.update";

	// Service instance
	public static final String SVC_INSTANCE_PATH  = "org.openecomp.sdnc.sli.aai.path.svcinst";
	public static final String SVC_INST_QRY_PATH  = "org.openecomp.sdnc.sli.aai.path.svcinst.query";

	// VServer
	public static final String NETWORK_VSERVER_PATH  = "org.openecomp.sdnc.sli.aai.path.vserver";

	public static final String VNF_IMAGE_QUERY_PATH	  = "org.openecomp.sdnc.sli.aai.path.vnf.image.query";

	public static final String PARAM_SERVICE_TYPE     = "org.openecomp.sdnc.sli.aai.param.service.type";
	public static final String CERTIFICATE_HOST_ERROR = "org.openecomp.sdnc.sli.aai.host.certificate.ignore";

	// UBB Notify
	public static final String UBB_NOTIFY_PATH        = "org.openecomp.sdnc.sli.aai.path.notify";
	public static final String SELFLINK_AVPN          = "org.openecomp.sdnc.sli.aai.notify.selflink.avpn";
	public static final String SELFLINK_FQDN          = "org.openecomp.sdnc.sli.aai.notify.selflink.fqdn";

	//Service
	public static final String SERVICE_PATH	          = "org.openecomp.sdnc.sli.aai.path.service";

	// P-Interfaces
	public static final String P_INTERFACE_PATH       = "org.openecomp.sdnc.sli.aai.path.pserver.pinterface";

	// site-pair-sets
	public static final String SITE_PAIR_SET_PATH     = "org.openecomp.sdnc.sli.aai.path.site.pair.set";

	// node query (1602)
	public static final String QUERY_NODES_PATH		  = "org.openecomp.sdnc.sli.aai.query.nodes";


	protected abstract Logger getLogger();
	public abstract AAIRequestExecutor getExecutor();


	@Override
	public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix, String orderBy, SvcLogicContext ctx)
		throws SvcLogicException {

		getLogger().debug("AAIService.query \tresource = "+resource);

		String vnfId = null;
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
		if(("vserver".equals(resource) || "vserver2".equals(resource))){
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
		Map<String,Object> attributes = new HashMap<String,Object>();

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
								ctx.setAttribute(prefix + ".error.http.response-code", "" + exc.getReturnCode());
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
						ctx.setAttribute(prefix + ".error.message", aaiexc.getMessage());
						if (aaiexc.getReturnCode() >= 300) {
							ctx.setAttribute(prefix + ".error.http.response-code", "" + aaiexc.getReturnCode());
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
						ctx.setAttribute(prefix + ".error.message", aaiexc.getMessage());
						if (aaiexc.getReturnCode() >= 300) {
							ctx.setAttribute(prefix + ".error.http.response-code", "" + aaiexc.getReturnCode());
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
						ArrayList<String> keys = new ArrayList<String>(attributes.keySet());

						int numCols = keys.size();

						for (int i = 0; i < numCols; i++) {
							String colValue = null;
							String colName = keys.get(i);
							Object object = attributes.get(colName);

							if(object != null && object instanceof String) {
								colValue = (String)object;

								if (prefix != null) {
									getLogger().debug("Setting "+prefix	+ "." + colName.replaceAll("_", "-")+" = "+ colValue);
									ctx.setAttribute(prefix	+ "." + colName.replaceAll("_", "-"), colValue);
								} else {
									getLogger().debug("Setting " + colValue.replaceAll("_", "-")+" = "+colValue);
									ctx.setAttribute(colValue.replaceAll("_", "-"), colValue);
								}
							} else if(object != null && object instanceof Map) {
								if(colName.equals(modifier) || colName.equals("relationship-list")){
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
			return (retval);

		} catch (Exception exc) {
			getLogger().warn("Failed query - returning FAILURE", exc);
			return QueryStatus.FAILURE;
		}

//		return QueryStatus.SUCCESS;
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

		if(list.size() > 0) {
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
//				params.remove("prefix");
			}
		}
		// params passed
		getLogger().debug("parms = "+ Arrays.toString(params.entrySet().toArray()));

		boolean useNewModelProcessing = true;
		// process server query by name the old way
		if(("vserver".equals(resource) || "vserver2".equals(resource))){
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
				} catch (UnsupportedEncodingException | MalformedURLException e) {
					// TODO : Fix this
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
					}
					// create a method to update relationship-list
					AAIRequest request = AAIRequest.createRequest(localResource, nameValues);
					request.setRequestObject(instance);
					request.processRequestPathValues(nameValues);

					getExecutor().post(request);
					getLogger().debug("Save relationship list - returning SUCCESS");
					return QueryStatus.SUCCESS;
//					} else {
//						getLogger().debug("Save relationship list - returning FAILURE");
//						return QueryStatus.FAILURE;
				}
			} catch (Exception exc) {
				ctx.setAttribute(prefix + ".error.message", exc.getMessage());
				if(exc instanceof AAIServiceException) {
					AAIServiceException aaiexc = (AAIServiceException)exc;
					if(aaiexc.getReturnCode() >= 300) {
						ctx.setAttribute(prefix + ".error.http.response-code", "" + aaiexc.getReturnCode());
					}

					if(aaiexc.getReturnCode() == 404) {
						return QueryStatus.NOT_FOUND;
					}
				}
				getLogger().warn("Failed save() - returning FAILURE", exc);
				return QueryStatus.FAILURE;
			}
		} else {
			String reSource = resource.toLowerCase().replace("-", "_");
				String vnfId = null;

			try {
				switch(reSource) {
					case "generic_vnf":
					case "generic-vnf":
						vnfId = nameValues.get("vnf_id");
						if(vnfId == null) {
							getLogger().debug("Save(generic-vnf) with no vnf-id specified. Returning FAILURE");
							return QueryStatus.FAILURE;
						}
						vnfId = vnfId.trim().replace("'", "").replace("$", "").replace("'", "");
						GenericVnf vnf = this.requestGenericVnfData(vnfId);
						String status = params.get("prov-status");
						boolean updated = false;
						if(status != null && !status.isEmpty()) {
							vnf.setProvStatus(status);
						}
						if(updated) {
							this.postGenericVnfData(vnfId, vnf);
						}
						break;
					case "vpe":
						return update( resource,  key, params, prefix, ctx) ;

					default:
						getLogger().debug("Save() executing default path - returning FAILURE");
						return QueryStatus.FAILURE;
				}
			} catch (Exception exc) {
				getLogger().warn("Failed save - returning FAILURE", exc);
				ctx.setAttribute(prefix + ".error.message", exc.getMessage());
				return QueryStatus.FAILURE;
			}
		}

		getLogger().debug("Save - returning SUCCESS");
		return QueryStatus.SUCCESS;
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

		if(AAIRequest.createRequest(resource, nameValues) != null) {
			if(resource.contains(":")) {
				return processDeleteRelationshipList(resource, key, ctx, nameValues);
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
			String resoourceName = resource;
			String identifier = null;

			if(resoourceName == null)
				return QueryStatus.FAILURE;

			if(resoourceName.contains(":")) {
				String[] tokens = resoourceName.split(":");
				if(tokens != null && tokens.length > 0) {
					resoourceName = tokens[0];
					identifier = tokens[1];
				}
			}
			if("relationship-list".equals(identifier) || "relationshipList".equals(identifier)) {
//				RelationshipRequest relationshipRequest = new RelationshipRequest();
				if("generic-vnf".equals(resoourceName)){
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
					List<Relationship> iterableList = new LinkedList<Relationship>(relationships);
					for(Relationship relationship : iterableList) {
						if(relationship.getRelatedTo().equals(relatedTo)) {
							relationships.remove(relationship);
							itemRemoved = true;
						}
					}

					if(!itemRemoved)
						return QueryStatus.NOT_FOUND;

//					AAIRequest masterRequest = new GenericVnfRequest();
//					masterRequest.addRequestProperty(GenericVnfRequest.VNF_ID, vnfId);
//					relationshipRequest.addMasterRequest(masterRequest);
//					Map<String, String> attributes = objectToProperties(vnf);
//					try {
//						Boolean result = getExecutor().delete(relationshipRequest, attributes.get(AAIRequest.RESOURCE_VERSION));
//					} catch (AAIServiceException e) {
//						return QueryStatus.FAILURE;
//					}

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
		// TODO Auto-generated method stub
		throw new SvcLogicException("Method AAIService.isAvailable() has not been implemented yet");
	}

	@Override
	public QueryStatus notify(String resource, String action, String key, SvcLogicContext ctx) throws SvcLogicException {
		// TODO Auto-generated method stub
		throw new SvcLogicException("Method AAIService.notify() has not been implemented yet");
	}

//	@Override
	public QueryStatus newModelQuery(String resource, boolean localOnly, String select, String key, String prefix, String orderBy, SvcLogicContext ctx) {

		Object response = null;
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

			Map<String, String> params = new HashMap<String, String>();

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
			int errorCode = aaiexc.getReturnCode();
			ctx.setAttribute(prefix + ".error.message", aaiexc.getMessage());
			if(errorCode >= 300) {
				ctx.setAttribute(prefix + ".error.http.response-code", "" + aaiexc.getReturnCode());
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

	public QueryStatus processResponseData(String rv, String resource, AAIRequest request, String prefix,  SvcLogicContext ctx, HashMap<String, String> nameValues, String modifier) throws JsonParseException, JsonMappingException, IOException, AAIServiceException
	{
		Object response = null;

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
//				writeList((ArrayList)rdList, prefix, ctx);
			}

		if("formatted-query".equals(resource) || "custom-query".equals(resource)) {
			FormattedQueryResultList rd = FormattedQueryResultList.class.cast(response);
			List<Results> iRIlist = rd.getResults();
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
			getLogger().debug("Retrofiting relationship data: " + exc.getMessage());
		}

		String preFix = null;
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
		HashMap<String, String> nameValues = new HashMap<String, String>();

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
		// TODO Auto-generated method stub
		throw new SvcLogicException("Method AAIService.release() has not been implemented yet");
	}

	@Override
	public QueryStatus reserve(String arg0, String arg1, String arg2, String arg3, SvcLogicContext arg4)
			throws SvcLogicException {
		// TODO Auto-generated method stub
		throw new SvcLogicException("Method AAIService.reserve() has not been implemented yet");
	}

	private QueryStatus newModelSave(String resource, boolean force, String key, Map<String, String> params, String prefix, SvcLogicContext ctx) {
		getLogger().debug("Executing newModelSave for resource : " + resource);
		HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);

		try {
			ArrayList<String> subResources = new ArrayList<String>();
			Set<String> set = params.keySet();
			Map<String, Method> setters = new HashMap<String, Method>();
			Map<String, Method> getters = new HashMap<String, Method>();

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
								if(type.getName().startsWith("java.lang") || "boolean".equals(type.getName()) || "long".equals(type.getName())) {
									try {
										setter.setAccessible(true);
										Object arglist[] = new Object[1];
										arglist[0] = params.get(id);

										if(arglist[0] != null) {
											if(!type.getName().equals("java.lang.String")) {
//											getLogger().debug(String.format("Processing %s with parameter %s", types[0].getName(), value));
												if("boolean".equals(type.getName())) {
													arglist[0] = valueOf(Boolean.class, params.get(id));
												} else if("long".equals(type.getName())) {
														arglist[0] = valueOf(Long.class, params.get(id));
												} else {
													arglist[0] = valueOf(type, params.get(id));
												}
											}
											Object o = setter.invoke(instance, arglist);
										}
										set.remove(id);

									} catch (Exception x) {
										Throwable cause = x.getCause();
										getLogger().warn("Failed process for " + resourceClass.getName(), x);
									}
								} else if(type.getName().equals("java.util.List")) {
									List<String> newValues = new ArrayList<String>();
									String length = id+"_length";
									if(!params.isEmpty() && params.containsKey(length)) {
										String tmp = params.get(length).toString();
										int count = Integer.valueOf(tmp);
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

							}

							Method getter = null;
							try {
								getter = resourceClass.getMethod("get"+StringUtils.capitalize(value));
								if(!type.getName().equals("java.lang.String")) {
									getters.put(id, getter);
								}
							} catch(Exception exc) {

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

			Set<String> relationshipKeys = new TreeSet<String>();
			Set<String> vlansKeys = new TreeSet<String>();
			Set<String> metadataKeys = new TreeSet<String>();

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
						method.setAccessible(true);
						Object arglist[] = new Object[0];
//						arglist[0] = value;
						Class<?>[] types = method.getParameterTypes();
						if(types.length == 0){
							Object o = method.invoke(instance, arglist);
							if(o instanceof ArrayList) {
								ArrayList<String> values = (ArrayList<String>)o;
//								getLogger().debug(String.format("Processing %s with parameter %s", types[0].getName(), value));
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
					getLogger().debug("Retrofiting relationship data: " + exc.getMessage());
				}

				if(getRelationshipListMethod != null){
					try {
						getRelationshipListMethod.setAccessible(true);
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
							setRelationshipListMethod.setAccessible(true);
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
					if(relatedLink != null) {
						relationship.setRelatedLink(relatedLink);
					} else {
//						List<RelationshipData> relData = relationship.getRelationshipData();
						Map<String, String> relParams = new HashMap<String, String>();
						
						while(true) {
							String searchRelationshipKey = "relationship-list.relationship[" + i + "].relationship-data[" + j + "].relationship-key";
							String searchRelationshipValue = "relationship-list.relationship[" + i + "].relationship-data[" + j + "].relationship-value";
							if(!params.containsKey(searchRelationshipKey))
								break;
							
//							RelationshipData relDatum = new RelationshipData();
//							relDatum.setRelationshipKey(params.get(searchRelationshipKey));
//							relDatum.setRelationshipValue(params.get(searchRelationshipValue));
//							relData.add(relDatum);
							
							relParams.put(params.get(searchRelationshipKey), params.get(searchRelationshipValue));
							j++;
						}
						AAIRequest rlRequest = AAIRequest.createRequest(relatedTo, relParams);
						for(String key1 : relParams.keySet()) {
							rlRequest.addRequestProperty(key1, relParams.get(key1));
						}
						String path = rlRequest.updatePathDataValues(null);
						relationship.setRelatedLink(path);
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
						getVLansMethod.setAccessible(true);
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
							setVlansMethod.setAccessible(true);
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
					String vlanIdInner	= params.get("vlans.vlan[" + i + "].vlan-id-inner");
					String vlanIdOute 	= params.get("vlans.vlan[" + i + "].vlan-id-outer");
					String speedValue 	= params.get("vlans.vlan[" + i + "].speed-value");
					String speedUnits 	= params.get("vlans.vlan[" + i + "].speed-units");

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
						getMetadataMethod.setAccessible(true);
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
							setMetadataMethod.setAccessible(true);
							Object arglist[] = new Object[1];
							arglist[0] = metadataList;

							obj = setMetadataMethod.invoke(instance, arglist);
						} catch (InvocationTargetException x) {
							Throwable cause = x.getCause();
						}
					}
				}

				if(metadataList.getMetadatum() == null) {
//					metadataList.setMetadatum(new ArrayList<Metadatum>());
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
//			} else {
//				boolean response = getExecutor().post(request);

		} catch(AAIServiceException exc){
			ctx.setAttribute(prefix + ".error.message", exc.getMessage());
			int returnCode = exc.getReturnCode();
			if(returnCode >= 300) {
				ctx.setAttribute(prefix + ".error.http.response-code", "" + exc.getReturnCode());
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

		Set<String> relationshipKeys = new TreeSet<String>();

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
			RelationshipList relationshipList = null;
			Object obj = null;
			Method getRelationshipListMethod = null;
			try {
				 getRelationshipListMethod = resourceClass.getMethod("getRelationshipList");
			} catch(Exception exc) {
				getLogger().debug("Retrofiting relationship data: " + exc.getMessage());
			}
			if(getRelationshipListMethod != null){
				try {
					getRelationshipListMethod.setAccessible(true);
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
						setRelationshipListMethod.setAccessible(true);
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
				relationships = new ArrayList<Relationship>();
				createdNewRelationships = true;
			}

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
					if(relatedLink != null) {
						relationship.setRelatedLink(relatedLink);
				} else  {
//					List<RelationshipData> relData = relationship.getRelationshipData();
					Map<String, String> relParams = new HashMap<String, String>();

					while(true) {
						String searchRelationshipKey = "relationship-list.relationship[" + i + "].relationship-data[" + j + "].relationship-key";
						String searchRelationshipValue = "relationship-list.relationship[" + i + "].relationship-data[" + j + "].relationship-value";
						if(!params.containsKey(searchRelationshipKey))
							break;
	
//							RelationshipData relDatum = new RelationshipData();
//							relDatum.setRelationshipKey(params.get(searchRelationshipKey));
//							relDatum.setRelationshipValue(params.get(searchRelationshipValue));
//							relData.add(relDatum);
							
						relParams.put(params.get(searchRelationshipKey), params.get(searchRelationshipValue));
						j++;
					}
					AAIRequest rlRequest = AAIRequest.createRequest(relatedTo, relParams);
					for(String key : relParams.keySet()) {
						rlRequest.addRequestProperty(key, relParams.get(key));
					}
					String path = rlRequest.updatePathDataValues(null);
					relationship.setRelatedLink(path);
				}

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

		HashMap<String, String> nameValues = new HashMap<String, String>();
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

		HashMap<String, String> nameValues = new HashMap<String, String>();
		if(AAIRequest.createRequest(resource, nameValues) != null) {

			try {
				retval = newModelBackupRequest(resource, params, "tmpRestore", ctx);
				if(retval == QueryStatus.SUCCESS) {
					String current_json = ctx.getAttribute("tmpRestore");
					ctx.  setAttribute("tmpRestore", null);

					String snapshot_json = ctx.getAttribute(prefix);
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
			AAIRequest request = AAIRequest.createRequest(resource, nameValues);
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
					getResourceVersionMethod.setAccessible(true);
					Object object = getResourceVersionMethod.invoke(instance);
					if(object != null)
						resourceVersion = object.toString();
				} catch (InvocationTargetException x) {
					Throwable cause = x.getCause();
				}
			}

			RelationshipList relationshipList = null;
			Object obj = null;
			Method getRelationshipListMethod = null;
			try {
				 getRelationshipListMethod = resourceClass.getMethod("getRelationshipList");
			} catch(Exception exc) {
				getLogger().debug("Retrofiting relationship data: " + exc.getMessage());
			}
			if(getRelationshipListMethod != null){
				try {
					getRelationshipListMethod.setAccessible(true);
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
			List<Relationship> relationshipsToDelete = new LinkedList<Relationship>();

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
				boolean response = deleteRelationshipList(deleteUrl, json_text);
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

	static final Map<String, String> ctxGetBeginsWith( SvcLogicContext ctx, String prefix ) {
		Map<String, String> tmpPrefixMap = new HashMap<String, String>();

		if(prefix == null || prefix.isEmpty()){
			return tmpPrefixMap;
		}

		for( String key : ctx.getAttributeKeySet() ) {
			if( key.startsWith(prefix) ) {
				String tmpKey = key.substring(prefix.length() + 1);
				tmpPrefixMap.put( tmpKey, ctx.getAttribute(key));
			}
		}

		Map<String, String> prefixMap = new HashMap<String, String>();
		Pattern p = Pattern.compile(".*\\[\\d\\]");

		SortedSet<String> keys = new TreeSet(tmpPrefixMap.keySet () );
		for(String key : keys) {
			Matcher m = p.matcher(key);
			if(m.matches()) {
				continue;
			} else if(key.endsWith("_length")) {
				String listKey = key.substring(0, key.indexOf("_length"));
				int max = Integer.parseInt(tmpPrefixMap.get(key));

				ArrayList<String> data = new ArrayList<String>();
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
						}
					}
				}
			}
		}

		return data;
	}

	public abstract <T> T getResource(String key, Class<T> type) throws AAIServiceException ;
	protected abstract boolean deleteRelationshipList(URL url, String caller) throws AAIServiceException;
}
