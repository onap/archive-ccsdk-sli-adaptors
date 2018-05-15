/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 							reserved.
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

package org.onap.ccsdk.sli.adaptors.ra;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onap.ccsdk.sli.adaptors.ra.comp.EndPointAllocator;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceData;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceEntity;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceRequest;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceResponse;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceTarget;
import org.onap.ccsdk.sli.adaptors.rm.comp.ResourceManager;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationStatus;
import org.onap.ccsdk.sli.adaptors.util.speed.SpeedUtil;
import org.onap.ccsdk.sli.adaptors.util.str.StrUtil;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceAllocator implements SvcLogicResource {

    private static final Logger log = LoggerFactory.getLogger(ResourceAllocator.class);

    private static final String[] INPUT_PREFIX = {"ra-input.", "tmp.resource-allocator."};

    private ResourceManager resourceManager;
    private EndPointAllocator endPointAllocator;
    private SpeedUtil speedUtil;

    public ResourceAllocator() {
        log.info("ResourceAllocator created.");
    }

    @Override
    public QueryStatus notify(String resource, String action, String key, SvcLogicContext ctx)
            throws SvcLogicException {
        return (QueryStatus.SUCCESS);
    }

    @Override
    public QueryStatus update(String resource, String key, Map<String, String> parms, String prefix,
            SvcLogicContext ctx) throws SvcLogicException {

        return QueryStatus.SUCCESS;
    }

    @Override
    public QueryStatus exists(String resource, String key, String prefix, SvcLogicContext ctx)
            throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    @Override
    public QueryStatus delete(String arg0, String arg1, SvcLogicContext arg2) throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    @Override
    public QueryStatus save(String arg0, boolean arg1, boolean arg2, String arg3, Map<String, String> arg4, String arg5,
            SvcLogicContext arg6) throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    @Override
    public QueryStatus isAvailable(String resource, String key, String prefix, SvcLogicContext ctx)
            throws SvcLogicException {
        return allocateResources(ctx, true, prefix);
    }

    @Override
    public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix,
            String orderBy, SvcLogicContext ctx) throws SvcLogicException {

        String resourceEntityId = getParam(ctx,
                new String[] {"service-instance-id", "reservation-entity-id", "resource-entity-id"}, false, null);
        String resourceEntityType =
                getParam(ctx, new String[] {"reservation-entity-type", "resource-entity-type"}, false, null);
        String resourceEntityVersion =
                getParam(ctx, new String[] {"reservation-entity-version", "resource-entity-version"}, false, "1");

        String resourceTargetId =
                getParam(ctx, new String[] {"reservation-target-id", "resource-target-id"}, false, null);
        String resourceTargetType =
                getParam(ctx, new String[] {"reservation-target-type", "resource-target-type"}, false, null);
        String resourceName = getParam(ctx, "resource-name", false, null);

        if (resourceEntityId != null && resourceEntityType != null) {
            List<ResourceData> rdlist = endPointAllocator.getResourcesForEntity(resourceEntityType, resourceEntityId,
                    resourceEntityVersion);
            setResourceDataInContext(ctx, prefix, rdlist);
        } else if (resourceTargetId != null && resourceTargetType != null && resourceName != null) {
            ResourceData rd = endPointAllocator.getResource(resourceTargetType, resourceTargetId, resourceName);
            setResourceDataInContext(ctx, prefix, Collections.singletonList(rd));
        }

        return QueryStatus.SUCCESS;
    }

    public AllocationStatus query(ResourceEntity sd, ResourceTarget rt, ResourceRequest rr,
            List<ResourceResponse> rsList) throws Exception {

        if (sd.resourceEntityId != null && sd.resourceEntityType != null) {
            List<ResourceData> rdlist = endPointAllocator.getResourcesForEntity(sd.resourceEntityType,
                    sd.resourceEntityId, sd.resourceEntityVersion);
            setResourceDataInResponse(rdlist, rsList);
        } else if (rt.resourceTargetId != null && rt.resourceTargetType != null && rr.resourceName != null) {
            ResourceData rd =
                    endPointAllocator.getResource(rt.resourceTargetType, rt.resourceTargetId, rr.resourceName);
            setResourceDataInResponse(Collections.singletonList(rd), rsList);
        }

        return AllocationStatus.Success;
    }

    private void setResourceDataInContext(SvcLogicContext ctx, String prefix, List<ResourceData> rdlist) {
        prefix = prefix == null ? "" : prefix + '.';

        setAttr(ctx, prefix + "resource-list_length", String.valueOf(rdlist.size()));

        for (int i = 0; i < rdlist.size(); i++) {
            ResourceData rd = rdlist.get(i);

            String pp = prefix + "resource-list[" + i + "].";

            setAttr(ctx, pp + "resource-name", rd.resourceName);
            setAttr(ctx, pp + "endpoint-position", rd.endPointPosition);
            setAttr(ctx, pp + "resource-target-type", rd.resourceTargetType);
            setAttr(ctx, pp + "resource-target-id", rd.resourceTargetId);
            // SDNGC-7687
            setAttr(ctx, pp + "resource-target-value", rd.resourceTargetValue);
            setAttr(ctx, pp + "status", rd.status);

            if (rd.data != null && !rd.data.isEmpty()) {
                for (String kk : rd.data.keySet()) {
                    String value = String.valueOf(rd.data.get(kk));
                    setAttr(ctx, pp + kk, value);
                }
            }
        }
    }

    @Override
    public QueryStatus reserve(String resource, String select, String key, String prefix, SvcLogicContext ctx)
            throws SvcLogicException {
        return allocateResources(ctx, false, prefix);
    }

    public AllocationStatus reserve(ResourceEntity sd, ResourceTarget rt, ResourceRequest rr,
            List<ResourceResponse> rsList) throws Exception {
        return allocateResources(sd, rt, rr, rsList);
    }

    @Override
    public QueryStatus release(String resource, String key, SvcLogicContext ctx) throws SvcLogicException {
        String resourceEntityId = getParam(ctx,
                new String[] {"service-instance-id", "reservation-entity-id", "resource-entity-id"}, true, null);
        String resourceEntityType =
                getParam(ctx, new String[] {"reservation-entity-type", "resource-entity-type"}, true, null);
        String resourceEntityVersion =
                getParam(ctx, new String[] {"reservation-entity-version", "resource-entity-version"}, false, null);

        ResourceEntity sd = new ResourceEntity();
        sd.resourceEntityId = resourceEntityId;
        sd.resourceEntityType = resourceEntityType;
        sd.resourceEntityVersion = resourceEntityVersion;

        try {
            this.release(sd);
        } catch (Exception e) {
            throw new SvcLogicException(e.getMessage());
        }
        return QueryStatus.SUCCESS;
    }

    public AllocationStatus release(ResourceEntity sd) throws Exception {

        if (sd.resourceEntityVersion != null) {
            String resourceSet = sd.resourceEntityType + "::" + sd.resourceEntityId + "::" + sd.resourceEntityVersion;
            log.info("Starting release for: " + resourceSet);

            resourceManager.releaseResourceSet(resourceSet);
        } else {
            String resourceUnion = sd.resourceEntityType + "::" + sd.resourceEntityId;
            log.info("Starting release for: " + resourceUnion);

            resourceManager.releaseResourceUnion(resourceUnion);
        }

        return AllocationStatus.Success;

    }

    private QueryStatus allocateResources(SvcLogicContext ctx, boolean checkOnly, String prefix)
            throws SvcLogicException {
        String serviceModel = getParam(ctx, "service-model", true, null);
        String requestType = getParam(ctx, "request-type", false, "New");

        ResourceEntity sd = getResourceEntityData(ctx);
        ResourceTarget rt = getResourceTargetData(ctx);
        ResourceRequest rr = getResourceRequest(ctx);

        log.info("Starting reserve: " + requestType + ", service-model: " + serviceModel);
        StrUtil.info(log, sd);
        StrUtil.info(log, rt);
        StrUtil.info(log, rr);

        boolean change = requestType.equalsIgnoreCase("change");

        List<ResourceData> rlist = endPointAllocator.allocateResources(serviceModel, sd, rt, rr, checkOnly, change);

        if (rlist != null && !rlist.isEmpty()) {
            setResourceDataInContext(ctx, prefix, rlist);

            for (ResourceData rd : rlist) {
                if (!rd.status.equals("Success")) {
                    log.info("Capacity not found for: " + sd.resourceEntityType + "::" + sd.resourceEntityId);
                    return QueryStatus.NOT_FOUND;
                }
            }
        }
        return QueryStatus.SUCCESS;
    }

    private AllocationStatus allocateResources(ResourceEntity sd, ResourceTarget rt, ResourceRequest rr,
            List<ResourceResponse> rsList) throws Exception {

        String serviceModel = rr.serviceModel;
        String requestType = rr.requestType == null ? "New" : rr.requestType;

        log.info("Starting reserve: " + requestType + ", service-model: " + serviceModel);
        StrUtil.info(log, sd);
        StrUtil.info(log, rt);
        StrUtil.info(log, rr);

        boolean change = requestType.equalsIgnoreCase("change");

        List<ResourceData> rlist = endPointAllocator.allocateResources(serviceModel, sd, rt, rr, rr.checkOnly, change);

        if (rlist != null && !rlist.isEmpty()) {
            setResourceDataInResponse(rlist, rsList);

            for (ResourceData rd : rlist) {
                if (!rd.status.equals("Success")) {
                    log.info("Capacity not found for: " + sd.resourceEntityType + "::" + sd.resourceEntityId);
                    return AllocationStatus.ResourceNotFound;
                }
            }
        }

        return AllocationStatus.Success;
    }

    private void setResourceDataInResponse(List<ResourceData> rlist, List<ResourceResponse> rsList) {
        for (ResourceData rd : emptyIfNull(rlist)) {
            ResourceResponse res = new ResourceResponse();
            res.resourceName = rd.resourceName;
            res.endPointPosition = rd.endPointPosition;
            res.resourceTargetId = rd.resourceTargetId;
            res.resourceTargetType = rd.resourceTargetType;
            res.status = rd.status;
            if (rd.data != null && !rd.data.isEmpty()) {
                for (String kk : rd.data.keySet()) {
                    if (kk.equalsIgnoreCase("allocated")) {
                        res.resourceAllocated = String.valueOf(rd.data.get(kk));
                    }

                    if (kk.equalsIgnoreCase("used")) {
                        res.resourceUsed = String.valueOf(rd.data.get(kk));
                    }

                    if (kk.equalsIgnoreCase("available")) {
                        res.resourceAvailable = String.valueOf(rd.data.get(kk));
                    }

                    if (kk.equalsIgnoreCase("limit")) {
                        res.resourceLimit = String.valueOf(rd.data.get(kk));
                    }

                }
            }
            rsList.add(res);
        }

    }

    public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.<T>emptyList() : iterable;
    }

    private void setAttr(SvcLogicContext ctx, String name, String value) {
        ctx.setAttribute(name, value);
        log.info("Added context attr: " + name + ": " + value);
    }

    private ResourceEntity getResourceEntityData(SvcLogicContext ctx) throws SvcLogicException {
        ResourceEntity sd = new ResourceEntity();
        sd.resourceEntityId = getParam(ctx,
                new String[] {"service-instance-id", "reservation-entity-id", "resource-entity-id"}, true, null);
        sd.resourceEntityType =
                getParam(ctx, new String[] {"reservation-entity-type", "resource-entity-type"}, true, null);
        sd.resourceEntityVersion =
                getParam(ctx, new String[] {"reservation-entity-version", "resource-entity-version"}, false, "1");
        sd.data = getDataParam(ctx, "reservation-entity-data", "resource-entity-data", "service-data");
        return sd;
    }

    private ResourceTarget getResourceTargetData(SvcLogicContext ctx) throws SvcLogicException {
        ResourceTarget sd = new ResourceTarget();
        sd.resourceTargetId = getParam(ctx, new String[] {"reservation-target-id", "resource-target-id"}, true, null);
        sd.resourceTargetType =
                getParam(ctx, new String[] {"reservation-target-type", "resource-target-type"}, true, null);
        sd.data = getDataParam(ctx, "reservation-target-data", "resource-target-data", "equipment-data");
        return sd;
    }

    private ResourceRequest getResourceRequest(SvcLogicContext ctx) throws SvcLogicException {
        ResourceRequest rr = new ResourceRequest();
        rr.resourceName = getParam(ctx, "resource-name", false, null);
        rr.resourceShareGroup = getParam(ctx, "resource-share-group", false, null);
        rr.rangeRequestedNumbers = getParam(ctx, "range-requested-numbers", false, null);
        rr.rangeExcludeNumbers = getParam(ctx, "range-exclude-numbers", false, null);
        String rangeReverseOrderStr = getParam(ctx, "range-reverse-order", false, "false");
        rr.rangeReverseOrder = Boolean.parseBoolean(rangeReverseOrderStr);
        String rangeMinOverrideStr = getParam(ctx, "range-min-override", false, "-1");
        rr.rangeMinOverride = Integer.parseInt(rangeMinOverrideStr);
        String rangeMaxOverrideStr = getParam(ctx, "range-max-override", false, "-1");
        rr.rangeMaxOverride = Integer.parseInt(rangeMaxOverrideStr);
        String replaceStr = getParam(ctx, "replace", false, "true");
        rr.replace = Boolean.parseBoolean(replaceStr);
        rr.applicationId = getParam(ctx, "application-id", false, "SDNC");
        rr.endPointPosition = getParam(ctx, "endpoint-position", false, null);
        return rr;
    }

    private String getParam(SvcLogicContext ctx, String name, boolean required, String def) throws SvcLogicException {
        String v = null;
        for (String p : INPUT_PREFIX) {
            v = ctx.getAttribute(p + name);
            if (v != null && v.trim().length() > 0) {
                log.info("Param: " + name + ": " + v.trim());
                return v.trim();
            }
        }
        if (required) {
            throw new SvcLogicException("The following variable is required in DG context: " + name);
        }

        log.info("Param: " + name + " not supplied. Using default: " + def);
        return def;
    }

    private String getParam(SvcLogicContext ctx, String[] names, boolean required, String def)
            throws SvcLogicException {
        String v = null;
        for (String name : names) {
            v = getParam(ctx, name, false, def);
            if (v != null) {
                return v;
            }
        }
        if (required) {
            throw new SvcLogicException(
                    "One of the following variable is required in DG context: " + Arrays.deepToString(names));
        }

        log.info("Param: " + names + " not supplied. Using default: " + def);
        return def;
    }

    private Map<String, String> getDataParam(SvcLogicContext ctx, String... names) {
        Map<String, String> data = new HashMap<>();
        Set<String> ctxNames = ctx.getAttributeKeySet();
        for (String n : ctxNames) {
            for (String p : INPUT_PREFIX) {
                for (String name : names) {
                    String pp = p + name + '.';
                    if (n.startsWith(pp)) {
                        String nn = n.substring(pp.length());
                        String vv = ctx.getAttribute(n);
                        data.put(nn, vv);

                        log.info("Data param: " + nn + ": " + vv);

                        if (ctxNames.contains(n + "-unit")) {
                            try {
                                long v = Long.parseLong(vv);
                                String unit = ctx.getAttribute(n + "-unit");
                                long kbps = speedUtil.convertToKbps(v, unit);
                                long mbps = speedUtil.convertToMbps(v, unit);
                                data.put(nn + "-kbps", String.valueOf(kbps));
                                data.put(nn + "-mbps", String.valueOf(mbps));

                                log.info("Data param: " + nn + "-kbps: " + kbps);
                                log.info("Data param: " + nn + "-mbps: " + mbps);

                            } catch (Exception e) {
                                log.warn("Invalid number for " + n + ": " + vv);
                            }
                        }
                    }
                }
            }
        }
        return data;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void setEndPointAllocator(EndPointAllocator endPointAllocator) {
        this.endPointAllocator = endPointAllocator;
    }

    public void setSpeedUtil(SpeedUtil speedUtil) {
        this.speedUtil = speedUtil;
    }
}
