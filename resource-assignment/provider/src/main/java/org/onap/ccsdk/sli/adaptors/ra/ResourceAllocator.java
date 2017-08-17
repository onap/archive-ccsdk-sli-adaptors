/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.adaptors.ra.comp.EndPointAllocator;
import org.onap.ccsdk.sli.adaptors.ra.comp.EndPointData;
import org.onap.ccsdk.sli.adaptors.ra.comp.ServiceData;
import org.onap.ccsdk.sli.adaptors.ra.equip.dao.ServerDao;
import org.onap.ccsdk.sli.adaptors.ra.equip.dao.VpePortDao;
import org.onap.ccsdk.sli.adaptors.ra.equip.dao.VplspePortDao;
import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentData;
import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentLevel;
import org.onap.ccsdk.sli.adaptors.ra.rule.comp.AllocationRequestBuilder;
import org.onap.ccsdk.sli.adaptors.ra.rule.dao.MaxPortSpeedDao;
import org.onap.ccsdk.sli.adaptors.ra.rule.dao.MaxServerSpeedDao;
import org.onap.ccsdk.sli.adaptors.ra.rule.dao.ParameterDao;
import org.onap.ccsdk.sli.adaptors.ra.rule.data.ThresholdStatus;
import org.onap.ccsdk.sli.adaptors.ra.service.dao.ServiceResourceDao;
import org.onap.ccsdk.sli.adaptors.ra.service.data.ServiceResource;
import org.onap.ccsdk.sli.adaptors.ra.service.data.ServiceStatus;
import org.onap.ccsdk.sli.adaptors.rm.comp.ResourceManager;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationAction;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationStatus;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitResource;
import org.onap.ccsdk.sli.adaptors.rm.data.MultiResourceAllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.RangeAllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.RangeResource;
import org.onap.ccsdk.sli.adaptors.rm.data.Resource;
import org.onap.ccsdk.sli.adaptors.util.speed.SpeedUtil;
import org.onap.ccsdk.sli.adaptors.util.str.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceAllocator implements SvcLogicResource {

    private static final Logger log = LoggerFactory.getLogger(ResourceAllocator.class);

    public ResourceAllocator() {
        log.info("ResourceAllocator created.");
    }

    private ServerDao serverDao;
    private VpePortDao vpePortDao;
    private VplspePortDao vplspePortDao;
    private MaxPortSpeedDao maxPortSpeedDao;
    private MaxServerSpeedDao maxServerSpeedDao;
    private ServiceResourceDao serviceResourceDao;
    private ParameterDao parameterDao;

    private AllocationRequestBuilder allocationRequestBuilder;
    private ResourceManager resourceManager;
    private SpeedUtil speedUtil;

    private EndPointAllocator endPointAllocator;

    @Override
    public QueryStatus notify(String resource, String action, String key, SvcLogicContext ctx)
            throws SvcLogicException {
        return (QueryStatus.SUCCESS);
    }

    @Override
    public QueryStatus update(
            String resource,
            String key,
            Map<String, String> parms,
            String prefix,
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
    public QueryStatus save(
            String arg0,
            boolean arg1,
            boolean arg2,
            String arg3,
            Map<String, String> arg4,
            String arg5,
            SvcLogicContext arg6) throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }

    @Override
    public QueryStatus isAvailable(String resource, String key, String prefix, SvcLogicContext ctx)
            throws SvcLogicException {
        String serviceModel = ctx.getAttribute("tmp.resource-allocator.service-model");
        if (serviceModel != null && serviceModel.trim().length() > 0)
            return allocateResources(serviceModel, ctx, true, prefix);
        return allocateResourcesL3SDN(ctx, true, prefix);
    }

    @Override
    public QueryStatus query(
            String resource,
            boolean localOnly,
            String select,
            String key,
            String prefix,
            String orderBy,
            SvcLogicContext ctx) throws SvcLogicException {

        prefix = prefix == null ? "" : prefix + '.';

        if (!resource.equals("NetworkCapacity")) {
            log.info("resource: " + resource);
            log.info("key: " + key);

            Resource r = resourceManager.getResource(resource, key);
            if (r == null)
                return QueryStatus.NOT_FOUND;

            if (r instanceof LimitResource) {
                ctx.setAttribute(prefix + "used", String.valueOf(((LimitResource) r).used));

                log.info("Added context attr: " + prefix + "used: " + String.valueOf(((LimitResource) r).used));
            }

            return QueryStatus.SUCCESS;
        }

        log.info("key: " + key);
        log.info("prefix: " + prefix);

        if (key == null)
            return QueryStatus.SUCCESS;

        if (key.startsWith("'") && key.endsWith("'"))
            key = key.substring(1, key.length() - 1);

        String endPointPosition = "VPE-Cust";

        String resourceUnionId = key + '/' + endPointPosition;
        List<Resource> rlist = resourceManager.getResourceUnion(resourceUnionId);

        log.info("Resources found for " + resourceUnionId + ": " + rlist.size());

        String assetId = null;
        for (Resource r : rlist) {
            log.info("Resource: " + r.resourceKey.resourceName);

            if (r instanceof RangeResource) {
                RangeResource rr = (RangeResource) r;
                for (AllocationItem ai : r.allocationItems)
                    if (ai.resourceUnionId.equals(resourceUnionId)) {
                        RangeAllocationItem rai = (RangeAllocationItem) ai;
                        ctx.setAttribute(prefix + r.resourceKey.resourceName, String.valueOf(rai.used.first()));

                        log.info("Added context attr: " + prefix + r.resourceKey.resourceName + ": " +
                                String.valueOf(rr.used.first()));

                        assetId = r.resourceKey.assetId;
                        String vpeName = assetId;
                        int i1 = assetId.indexOf('/');
                        if (i1 > 0)
                            vpeName = assetId.substring(0, i1);
                        ctx.setAttribute(prefix + "vpe-name", vpeName);

                        log.info("Added context attr: " + prefix + "vpe-name: " + vpeName);
                    }
            }
        }

        String affinityLink = "1";
        if (assetId != null) {
            for (Resource r : rlist) {
                if (r instanceof LimitResource) {
                    LimitResource ll = (LimitResource) r;
                    if (ll.resourceKey.assetId.startsWith(assetId + '-')) {
                        int i1 = ll.resourceKey.assetId.lastIndexOf('-');
                        affinityLink = ll.resourceKey.assetId.substring(i1 + 1);
                        break;
                    }
                }
            }
        }

        ctx.setAttribute(prefix + "affinity-link", affinityLink);

        log.info("Added context attr: " + prefix + "affinity-link: " + affinityLink);

        return QueryStatus.SUCCESS;
    }

    @Override
    public QueryStatus reserve(String resource, String select, String key, String prefix, SvcLogicContext ctx)
            throws SvcLogicException {
        String serviceModel = ctx.getAttribute("tmp.resource-allocator.service-model");
        if (serviceModel != null && serviceModel.trim().length() > 0)
            return allocateResources(serviceModel, ctx, false, prefix);
        return allocateResourcesL3SDN(ctx, false, prefix);
    }

    @Override
    public QueryStatus release(String resource, String key, SvcLogicContext ctx) throws SvcLogicException {
        String serviceInstanceId = ctx.getAttribute("tmp.resource-allocator.service-instance-id");
        if (serviceInstanceId == null)
            throw new SvcLogicException("tmp.resource-allocator.service-instance-id is required in ResourceAllocator");

        String requestTypeStr = ctx.getAttribute("tmp.resource-allocator.request-type");
        if (requestTypeStr == null)
            throw new SvcLogicException("tmp.resource-allocator.request-type is required in ResourceAllocator");

        ReleaseRequestType requestType = null;
        try {
            requestType = ReleaseRequestType.convert(requestTypeStr);
        } catch (IllegalArgumentException e) {
            throw new SvcLogicException("Invalid tmp.resource-allocator.request-type: " + requestTypeStr +
                    ". Supported values are Cancel, Activate, Disconnect.");
        }

        log.info("Starting release: " + requestType + " for: " + serviceInstanceId);

        ServiceResource activeServiceResource =
                serviceResourceDao.getServiceResource(serviceInstanceId, ServiceStatus.Active);
        ServiceResource pendingServiceResource =
                serviceResourceDao.getServiceResource(serviceInstanceId, ServiceStatus.Pending);

        log.info("Active ServiceResource: ");
        StrUtil.info(log, activeServiceResource);
        log.info("Pending ServiceResource: ");
        StrUtil.info(log, pendingServiceResource);

        if (requestType == ReleaseRequestType.Cancel) {
            if (pendingServiceResource != null) {
                log.info("Releasing pending resources: " + pendingServiceResource.resourceSetId);

                resourceManager.releaseResourceSet(pendingServiceResource.resourceSetId);
                serviceResourceDao.deleteServiceResource(serviceInstanceId, ServiceStatus.Pending);
            } else {
                log.info("Pending record not found for service instance: " + serviceInstanceId + ". Nothing to do.");
            }

        } else if (requestType == ReleaseRequestType.Activate) {
            if (pendingServiceResource != null) {
                if (activeServiceResource != null) {
                    log.info("Releasing active resources: " + activeServiceResource.resourceSetId);

                    resourceManager.releaseResourceSet(activeServiceResource.resourceSetId);
                    serviceResourceDao.deleteServiceResource(serviceInstanceId, ServiceStatus.Active);
                }

                log.info("Updating the status of the pending record to active.");

                serviceResourceDao.updateServiceStatus(serviceInstanceId, ServiceStatus.Pending, ServiceStatus.Active);
            } else {
                log.info("Pending record not found for service instance: " + serviceInstanceId + ". Nothing to do.");
            }

        } else if (requestType == ReleaseRequestType.Disconnect) {
            if (pendingServiceResource != null) {
                log.info("Releasing pending resources: " + pendingServiceResource.resourceSetId);

                resourceManager.releaseResourceSet(pendingServiceResource.resourceSetId);
                serviceResourceDao.deleteServiceResource(serviceInstanceId, ServiceStatus.Pending);
            }
            if (activeServiceResource != null) {
                log.info("Releasing active resources: " + activeServiceResource.resourceSetId);

                resourceManager.releaseResourceSet(activeServiceResource.resourceSetId);
                serviceResourceDao.deleteServiceResource(serviceInstanceId, ServiceStatus.Active);
            }
        }

        return QueryStatus.SUCCESS;
    }

    private QueryStatus allocateResourcesL3SDN(SvcLogicContext ctx, boolean checkOnly, String prefix)
            throws SvcLogicException {
        prefix = prefix == null ? "" : prefix + '.';

        String aicSiteId = getAicSiteId(ctx);
        Map<String, Object> service = getServiceData(ctx);

        String requestTypeStr = ctx.getAttribute("tmp.resource-allocator.request-type");
        if (requestTypeStr == null)
            requestTypeStr = "New";

        ReserveRequestType requestType = null;
        try {
            requestType = ReserveRequestType.convert(requestTypeStr);
        } catch (IllegalArgumentException e) {
            throw new SvcLogicException("Invalid tmp.resource-allocator.request-type: " + requestTypeStr +
                    ". Supported values are New, Change.");
        }

        String serviceInstanceId = String.valueOf(service.get("service-instance-id"));

        ServiceResource activeServiceResource =
                serviceResourceDao.getServiceResource(serviceInstanceId, ServiceStatus.Active);
        ServiceResource pendingServiceResource =
                serviceResourceDao.getServiceResource(serviceInstanceId, ServiceStatus.Pending);

        log.info("Active ServiceResource: ");
        StrUtil.info(log, activeServiceResource);
        log.info("Pending ServiceResource: ");
        StrUtil.info(log, pendingServiceResource);

        ServiceResource sr = new ServiceResource();
        sr.serviceInstanceId = serviceInstanceId;
        sr.serviceStatus = ServiceStatus.Pending;
        sr.serviceChangeNumber = 1;
        if (pendingServiceResource != null)
            sr.serviceChangeNumber = pendingServiceResource.serviceChangeNumber + 1;
        else if (activeServiceResource != null)
            sr.serviceChangeNumber = activeServiceResource.serviceChangeNumber + 1;
        sr.resourceSetId = serviceInstanceId + "/" + sr.serviceChangeNumber;
        sr.resourceUnionId = serviceInstanceId;

        log.info("New ServiceResource: ");
        StrUtil.info(log, sr);

        List<Map<String, Object>> vpePortData = vpePortDao.getVpePortData(aicSiteId);
        List<Map<String, Object>> vplspePortData = vplspePortDao.getVplspePortData(aicSiteId);
        List<Map<String, Object>> serverData = serverDao.getServerData(aicSiteId);

        vpePortData = orderVpe(vpePortData);

        long maxAvailableSpeedVpePort = 0;
        boolean vpePortFound = false;

        for (Map<String, Object> vpe : vpePortData) {
            String vpeId = String.valueOf(vpe.get("vpe-id"));
            String interfaceName = String.valueOf(vpe.get("physical-interface-name"));
            String portId = vpeId + "/" + interfaceName;

            log.info("Checking VPE port: " + portId);

            String provStatus = String.valueOf(vpe.get("provisioning-status"));
            if (!provStatus.equals("PROV")) {
                log.info("Skipping port " + portId + ": Provisioning status is not PROV.");
                continue;
            }

            String imageFile = String.valueOf(vpe.get("image-file-name"));
            String endPointPosition = "VPE-Cust";
            long maxPortSpeed = maxPortSpeedDao.getMaxPortSpeed(imageFile, endPointPosition, interfaceName);
            vpe.put("max-port-speed", maxPortSpeed);

            EquipmentData ed = new EquipmentData();
            ed.data = vpe;
            ed.equipmentId = portId;
            ed.equipmentLevel = EquipmentLevel.Port;

            ServiceData sd = new ServiceData();
            sd.data = service;
            sd.serviceModel = "L3SDN";
            sd.endPointPosition = endPointPosition;
            sd.resourceUnionId = sr.resourceUnionId;
            sd.resourceSetId = sr.resourceSetId;

            StrUtil.info(log, ed);
            StrUtil.info(log, sd);

            AllocationRequest ar = allocationRequestBuilder.buildAllocationRequest(sd, ed, checkOnly,
                    requestType == ReserveRequestType.Change);
            AllocationOutcome ao = resourceManager.allocateResources(ar);

            if (ao.status == AllocationStatus.Success) {

                // Assign affinity link
                if (!checkOnly) {
                    List<String> affinityLinkIdList = new ArrayList<>();
                    affinityLinkIdList.add("0");
                    affinityLinkIdList.add("1");
                    affinityLinkIdList.add("2");
                    affinityLinkIdList.add("3");

                    String preferedAffinityLinkId = "0";
                    long lowestAssignedBw = Long.MAX_VALUE;
                    for (String affinityLinkId : affinityLinkIdList) {
                        long used = 0;
                        String assetId = ed.equipmentId + "-" + affinityLinkId;
                        Resource r = resourceManager.getResource("Bandwidth", assetId);
                        if (r != null) {
                            LimitResource ll = (LimitResource) r;
                            used = ll.used;
                        }
                        if (used < lowestAssignedBw) {
                            lowestAssignedBw = used;
                            preferedAffinityLinkId = affinityLinkId;
                        }
                        log.info("Assigned bandwidth on affinity link: " + assetId + ": " + used);
                    }

                    log.info("Prefered affinity link for " + ed.equipmentId + ": " + preferedAffinityLinkId);

                    ctx.setAttribute(prefix + "affinity-link", preferedAffinityLinkId);

                    LimitAllocationRequest ar1 = new LimitAllocationRequest();
                    ar1.resourceSetId = sd.resourceSetId;
                    ar1.resourceUnionId = sd.resourceUnionId;
                    ar1.resourceShareGroupList = null;
                    ar1.resourceName = "Bandwidth";
                    ar1.assetId = ed.equipmentId + "-" + preferedAffinityLinkId;
                    ar1.missingResourceAction = AllocationAction.Succeed_Allocate;
                    ar1.expiredResourceAction = AllocationAction.Succeed_Allocate;
                    ar1.replace = true;
                    ar1.strict = false;
                    ar1.checkLimit = Long.MAX_VALUE;
                    ar1.checkCount = 0;
                    ar1.allocateCount = (Long) sd.data.get("service-speed-kbps");

                    resourceManager.allocateResources(ar1);
                }

                ctx.setAttribute(prefix + "vpe-name", vpeId);

                vpePortFound = true;
                break;
            }

            if (ao instanceof LimitAllocationOutcome) {
                LimitAllocationOutcome lao = (LimitAllocationOutcome) ao;
                long available = lao.limit - lao.used;
                if (available > maxAvailableSpeedVpePort)
                    maxAvailableSpeedVpePort = available;
            }
        }

        long maxAvailableSpeedVplspePort = 0;
        boolean vplspePortFound = false;

        for (Map<String, Object> vplspe : vplspePortData) {
            String vplspeId = String.valueOf(vplspe.get("vplspe-id"));
            String interfaceName = String.valueOf(vplspe.get("physical-interface-name"));
            String portId = vplspeId + "/" + interfaceName;

            log.info("Checking VPLSPE port: " + portId);

            String provStatus = String.valueOf(vplspe.get("provisioning-status"));
            if (!provStatus.equals("PROV")) {
                log.info("Skipping port " + portId + ": Provisioning status is not PROV.");
                continue;
            }

            long physicalSpeed = (Long) vplspe.get("physical-interface-speed");
            String physicalSpeedUnit = String.valueOf(vplspe.get("physical-interface-speed-unit"));
            long maxPortSpeed = speedUtil.convertToKbps(physicalSpeed, physicalSpeedUnit);
            vplspe.put("max-port-speed", maxPortSpeed);

            EquipmentData ed = new EquipmentData();
            ed.data = vplspe;
            ed.equipmentId = portId;
            ed.equipmentLevel = EquipmentLevel.Port;

            ServiceData sd = new ServiceData();
            sd.data = service;
            sd.serviceModel = "L3SDN";
            sd.endPointPosition = "IPAG-TOA";
            sd.resourceUnionId = sr.resourceUnionId;
            sd.resourceSetId = sr.resourceSetId;

            StrUtil.info(log, ed);
            StrUtil.info(log, sd);

            AllocationRequest ar = allocationRequestBuilder.buildAllocationRequest(sd, ed, checkOnly,
                    requestType == ReserveRequestType.Change);
            AllocationOutcome ao = resourceManager.allocateResources(ar);

            if (ao.status == AllocationStatus.Success) {
                vplspePortFound = true;
                break;
            }

            if (ao instanceof LimitAllocationOutcome) {
                LimitAllocationOutcome lao = (LimitAllocationOutcome) ao;
                long available = lao.limit - lao.used;
                if (available > maxAvailableSpeedVplspePort)
                    maxAvailableSpeedVplspePort = available;
            }
        }

        long maxAvailableSpeedServer = 0;
        boolean serverFound = false;

        for (Map<String, Object> server : serverData) {
            String serverId = String.valueOf(server.get("server-id"));
            String serverModel = String.valueOf(server.get("server-model"));

            log.info("Checking Server: " + serverId);

            String endPointPosition = "VCE-Cust";

            int serverCount = (Integer) server.get("server-count");
            if (serverCount == 0)
                serverCount = 1;
            String ratioString = parameterDao.getParameter("homing.pserver.sparing.ratio");
            if (ratioString == null || ratioString.length() == 0)
                ratioString = "1:1";
            int primaryServerCount = calculatePrimaryServerCount(serverCount, ratioString);
            server.put("number-primary-servers", primaryServerCount);

            int evcCount = getEvcCountOnServer(serverId);
            int evcCountPerServer = (evcCount + primaryServerCount - 1) / primaryServerCount;
            long maxServerSpeed = maxServerSpeedDao.getMaxServerSpeed(serverModel, evcCountPerServer);
            server.put("max-server-speed", maxServerSpeed);
            server.put("evc-count", evcCount);
            server.put("evc-count-per-server", evcCountPerServer);

            EquipmentData ed = new EquipmentData();
            ed.data = server;
            ed.equipmentId = serverId;
            ed.equipmentLevel = EquipmentLevel.Server;

            ServiceData sd = new ServiceData();
            sd.data = service;
            sd.serviceModel = "L3SDN";
            sd.endPointPosition = endPointPosition;
            sd.resourceUnionId = sr.resourceUnionId;
            sd.resourceSetId = sr.resourceSetId;

            StrUtil.info(log, ed);
            StrUtil.info(log, sd);

            AllocationRequest ar = allocationRequestBuilder.buildAllocationRequest(sd, ed, checkOnly,
                    requestType == ReserveRequestType.Change);
            AllocationOutcome ao = resourceManager.allocateResources(ar);

            if (ao.status == AllocationStatus.Success) {
                serverFound = true;

                if (ao instanceof MultiResourceAllocationOutcome) {
                    MultiResourceAllocationOutcome mrao = (MultiResourceAllocationOutcome) ao;
                    for (AllocationOutcome ao1 : mrao.allocationOutcomeList) {
                        if (ao1 instanceof LimitAllocationOutcome) {
                            LimitAllocationOutcome lao = (LimitAllocationOutcome) ao1;
                            if (lao.request.resourceName.equals("Bandwidth")) {
                                ThresholdStatus th = allocationRequestBuilder.getThresholdStatus(sd, ed, lao);
                                setThresholdData(ctx, th, sd, ed);
                            }
                        }
                    }
                }

                break;
            }

            if (ao instanceof MultiResourceAllocationOutcome) {
                MultiResourceAllocationOutcome mrao = (MultiResourceAllocationOutcome) ao;
                for (AllocationOutcome ao1 : mrao.allocationOutcomeList) {
                    if (ao1 instanceof LimitAllocationOutcome) {
                        LimitAllocationOutcome lao = (LimitAllocationOutcome) ao1;
                        if (lao.status == AllocationStatus.Failure && lao.request.resourceName.equals("Bandwidth")) {
                            long available = lao.limit - lao.used;
                            if (available > maxAvailableSpeedServer)
                                maxAvailableSpeedServer = available;
                        }
                        if (lao.status == AllocationStatus.Failure && lao.request.resourceName.equals("Connection")) {
                            maxAvailableSpeedServer = 0;
                            break;
                        }

                        ThresholdStatus th = allocationRequestBuilder.getThresholdStatus(sd, ed, lao);
                        setThresholdData(ctx, th, sd, ed);
                    }
                }
            }
        }

        if (vpePortFound && vplspePortFound && serverFound) {
            if (!checkOnly) {
                if (pendingServiceResource == null) {
                    log.info("Adding the pending service resource record to DB.");
                    serviceResourceDao.addServiceResource(sr);
                } else {
                    log.info("Releasing previously allocated resources for resource set id: " +
                            pendingServiceResource.resourceSetId);
                    resourceManager.releaseResourceSet(pendingServiceResource.resourceSetId);

                    log.info("Updating the pending service resource record in DB with service change number: " +
                            sr.serviceChangeNumber);
                    serviceResourceDao.updateServiceResource(sr);
                }
            }

            return QueryStatus.SUCCESS;
        }

        log.info("Releasing allocated resources (if any) for resource set id: " + sr.resourceSetId);
        resourceManager.releaseResourceSet(sr.resourceSetId);

        long maxAvailableSpeed = Long.MAX_VALUE;
        if (!vpePortFound && maxAvailableSpeedVpePort < maxAvailableSpeed)
            maxAvailableSpeed = maxAvailableSpeedVpePort;
        if (!vplspePortFound && maxAvailableSpeedVplspePort < maxAvailableSpeed)
            maxAvailableSpeed = maxAvailableSpeedVplspePort;
        if (!serverFound && maxAvailableSpeedServer < maxAvailableSpeed)
            maxAvailableSpeed = maxAvailableSpeedServer;

        setOutputContext(ctx, maxAvailableSpeed, "kbps");
        return QueryStatus.NOT_FOUND;
    }

    private List<Map<String, Object>> orderVpe(List<Map<String, Object>> vpePortData) {
        for (Map<String, Object> vpe : vpePortData) {
            String vpeId = String.valueOf(vpe.get("vpe-id"));
            String interfaceName = String.valueOf(vpe.get("physical-interface-name"));
            String portId = vpeId + "/" + interfaceName;
            Resource r = resourceManager.getResource("Bandwidth", portId);
            long used = 0;
            if (r != null) {
                LimitResource ll = (LimitResource) r;
                used = ll.used;
            }
            vpe.put("used-bandwidth", used);

            log.info("Used bandwidth on VPE: " + vpeId + ": " + used);
        }

        Collections.sort(vpePortData, new Comparator<Map<String, Object>>() {

            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                long used1 = (Long) o1.get("used-bandwidth");
                long used2 = (Long) o2.get("used-bandwidth");
                if (used1 < used2)
                    return -1;
                if (used1 > used2)
                    return 1;
                return 0;
            }
        });

        return vpePortData;
    }

    private void setThresholdData(SvcLogicContext ctx, ThresholdStatus th, ServiceData sd, EquipmentData ed) {
        if (th == null)
            return;

        String pp = "tmp.resource-allocator-output.threshold-notification-list.threshold-hotification[0].";
        ctx.setAttribute("tmp.resource-allocator-output.threshold-notification-list.threshold-hotification_length",
                "1");
        ctx.setAttribute(pp + "message", th.resourceThreshold.message);
        ctx.setAttribute(pp + "resource-rule.service-model", th.resourceRule.serviceModel);
        ctx.setAttribute(pp + "resource-rule.endpoint-position", th.resourceRule.endPointPosition);
        ctx.setAttribute(pp + "resource-rule.resource-name", th.resourceRule.resourceName);
        ctx.setAttribute(pp + "resource-rule.equipment-level", th.resourceRule.equipmentLevel);
        ctx.setAttribute(pp + "resource-rule.soft-limit-expression", th.resourceRule.softLimitExpression);
        ctx.setAttribute(pp + "resource-rule.hard-limit-expression", th.resourceRule.hardLimitExpression);
        ctx.setAttribute(pp + "resource-state.used", String.valueOf(th.used));
        ctx.setAttribute(pp + "resource-state.limit-value", String.valueOf(th.limitValue));
        ctx.setAttribute(pp + "resource-state.threshold-value", String.valueOf(th.thresholdValue));
        ctx.setAttribute(pp + "resource-state.last-added", String.valueOf(th.lastAdded));
        ctx.setAttribute(pp + "equipment-data.equipment-id", ed.equipmentId);
        for (String edKey : ed.data.keySet())
            ctx.setAttribute(pp + "equipment-data." + edKey, String.valueOf(ed.data.get(edKey)));
    }

    private QueryStatus allocateResources(String serviceModel, SvcLogicContext ctx, boolean checkOnly, String prefix)
            throws SvcLogicException {
        prefix = prefix == null ? "" : prefix + '.';

        Map<String, Object> service = getServiceData(ctx);
        Map<String, Object> ec = getEquipConstraints(ctx);

        String requestTypeStr = ctx.getAttribute("tmp.resource-allocator.request-type");
        if (requestTypeStr == null)
            requestTypeStr = "New";

        ReserveRequestType requestType = null;
        try {
            requestType = ReserveRequestType.convert(requestTypeStr);
        } catch (IllegalArgumentException e) {
            throw new SvcLogicException("Invalid tmp.resource-allocator.request-type: " + requestTypeStr +
                    ". Supported values are New, Change.");
        }

        String serviceInstanceId = String.valueOf(service.get("service-instance-id"));

        log.info("Starting reserve: " + requestType + ", service-instance-id: " + serviceInstanceId);

        ServiceResource activeServiceResource =
                serviceResourceDao.getServiceResource(serviceInstanceId, ServiceStatus.Active);
        ServiceResource pendingServiceResource =
                serviceResourceDao.getServiceResource(serviceInstanceId, ServiceStatus.Pending);

        log.info("Active ServiceResource: ");
        StrUtil.info(log, activeServiceResource);
        log.info("Pending ServiceResource: ");
        StrUtil.info(log, pendingServiceResource);

        int changeNumber = 1;
        if (pendingServiceResource != null)
            changeNumber = pendingServiceResource.serviceChangeNumber + 1;
        else if (activeServiceResource != null)
            changeNumber = activeServiceResource.serviceChangeNumber + 1;

        ServiceData sd = new ServiceData();
        sd.data = service;
        sd.serviceModel = serviceModel;
        sd.endPointPosition = (String) service.get("end-point-position");
        sd.resourceShareGroup = (String) service.get("resource-share-group");
        sd.resourceName = (String) service.get("resource-name");
        sd.serviceInstanceId = serviceInstanceId;

        StrUtil.info(log, sd);

        List<EndPointData> epList = endPointAllocator.allocateEndPoints(sd, ec, checkOnly,
                requestType == ReserveRequestType.Change, changeNumber);

        if (epList != null && !epList.isEmpty()) {
            if (!checkOnly) {
                EndPointData ep = epList.get(0);

                if (sd.resourceName == null) {
                ServiceResource sr = new ServiceResource();
                sr.serviceInstanceId = serviceInstanceId;
                sr.serviceStatus = ServiceStatus.Pending;
                sr.serviceChangeNumber = changeNumber;
                sr.resourceSetId = ep.resourceSetId;
                sr.resourceUnionId = ep.resourceUnionId;

                log.info("New ServiceResource: ");
                StrUtil.info(log, sr);

                if (pendingServiceResource == null) {
                    log.info("Adding the pending service resource record to DB.");
                    serviceResourceDao.addServiceResource(sr);
                } else {
                    log.info("Releasing previously allocated resources for resource set id: " +
                            pendingServiceResource.resourceSetId);
                    resourceManager.releaseResourceSet(pendingServiceResource.resourceSetId);

                    log.info("Updating the pending service resource record in DB with service change number: " +
                            sr.serviceChangeNumber);
                    serviceResourceDao.updateServiceResource(sr);
                }
            }

                for (EndPointData ep1 : epList)
                    if (ep1.data != null && !ep1.data.isEmpty())
                        for (String key : ep1.data.keySet()) {
                            String value = String.valueOf(ep1.data.get(key));
                            ctx.setAttribute(prefix + key, value);

                            log.info("Added context attr: " + prefix + key + ": " + value);
                        }
            }

            return QueryStatus.SUCCESS;
        }

        log.info("Capacity not found for EVC: " + serviceInstanceId);

        return QueryStatus.NOT_FOUND;
    }

    private int getEvcCountOnServer(String serverId) {
        LimitResource l = (LimitResource) resourceManager.getResource("Connection", serverId);
        if (l != null)
            return (int) l.used;
        return 0;
    }

    private String getAicSiteId(SvcLogicContext ctx) throws SvcLogicException {
        String aicSiteId = ctx.getAttribute("tmp.resource-allocator.aic-site-id");
        if (aicSiteId == null)
            throw new SvcLogicException("tmp.resource-allocator.aic-site-id is required in ResourceAllocator");
        return aicSiteId;
    }

    private Map<String, Object> getServiceData(SvcLogicContext ctx) throws SvcLogicException {
        Map<String, Object> sd = new HashMap<String, Object>();

        String endPointPosition = ctx.getAttribute("tmp.resource-allocator.end-point-position");
        if (endPointPosition != null && endPointPosition.trim().length() > 0)
            sd.put("end-point-position", endPointPosition.trim());

        String resourceName = ctx.getAttribute("tmp.resource-allocator.resource-name");
        if (resourceName != null && resourceName.trim().length() > 0)
            sd.put("resource-name", resourceName.trim());

        String resourceShareGroup = ctx.getAttribute("tmp.resource-allocator.resource-share-group");
        if (resourceShareGroup != null && resourceShareGroup.trim().length() > 0)
            sd.put("resource-share-group", resourceShareGroup.trim());

        String serviceInstanceId = ctx.getAttribute("tmp.resource-allocator.service-instance-id");
        if (serviceInstanceId == null)
            serviceInstanceId = "checkServiceInstance";
        sd.put("service-instance-id", serviceInstanceId);

        String speedStr = ctx.getAttribute("tmp.resource-allocator.speed");
        if (speedStr != null && speedStr.trim().length() > 0) {
        long speed = 0;
        try {
            speed = Long.parseLong(speedStr);
        } catch (NumberFormatException e) {
            throw new SvcLogicException("Invalid tmp.resource-allocator.speed. Must be a number.");
        }
        String unit = ctx.getAttribute("tmp.resource-allocator.speed-unit");
        if (unit == null || unit.trim().length() == 0)
            throw new SvcLogicException("tmp.resource-allocator.speed-unit is required in ResourceAllocator");
        long serviceSpeedKbps = speedUtil.convertToKbps(speed, unit);

        sd.put("service-speed-kbps", serviceSpeedKbps);
        }

        String vpnId = ctx.getAttribute("tmp.resource-allocator.vpn-id");
        if (vpnId != null && vpnId.trim().length() > 0)
            sd.put("vpn-id", vpnId.trim());

        String vpnIdList = ctx.getAttribute("tmp.resource-allocator.vpn-id-list");
        if (vpnIdList != null && vpnIdList.trim().length() > 0)
            sd.put("vpn-id-list", vpnIdList.trim());

        String vrfName = ctx.getAttribute("tmp.resource-allocator.vrf-name");
        if (vrfName != null && vrfName.trim().length() > 0)
            sd.put("vrf-name", vrfName.trim());

        String vrfNameList = ctx.getAttribute("tmp.resource-allocator.vrf-name-list");
        if (vrfNameList != null && vrfNameList.trim().length() > 0)
            sd.put("vrf-name-list", vrfNameList.trim());

        String v4multicast = ctx.getAttribute("tmp.resource-allocator.v4-multicast");
        if (v4multicast != null && v4multicast.trim().length() > 0)
            sd.put("v4-multicast", v4multicast.trim());

        String v6multicast = ctx.getAttribute("tmp.resource-allocator.v6-multicast");
        if (v6multicast != null && v6multicast.trim().length() > 0)
            sd.put("v6-multicast", v6multicast.trim());

        String v4ServingSite = ctx.getAttribute("tmp.resource-allocator.v4-serving-site");
        if (v4ServingSite != null && v4ServingSite.trim().length() > 0)
            sd.put("v4-serving-site", v4ServingSite.trim());

        String v6ServingSite = ctx.getAttribute("tmp.resource-allocator.v6-serving-site");
        if (v6ServingSite != null && v6ServingSite.trim().length() > 0)
            sd.put("v6-serving-site", v6ServingSite.trim());

        return sd;
    }

    private Map<String, Object> getEquipConstraints(SvcLogicContext ctx) throws SvcLogicException {
        Map<String, Object> mm = new HashMap<String, Object>();

        String vrfRequired = ctx.getAttribute("tmp.resource-allocator.vrf-required");
        if (vrfRequired != null && vrfRequired.trim().length() > 0)
            mm.put("vrf-required", vrfRequired.trim());

        String clli = ctx.getAttribute("tmp.resource-allocator.clli");
        if (clli == null || clli.trim().length() == 0)
            clli = ctx.getAttribute("tmp.resource-allocator.aic-site-id");
        if (clli != null) {
        mm.put("clli", clli.trim());
            mm.put("aic-site-id", clli.trim());
        }

        String vpeName = ctx.getAttribute("tmp.resource-allocator.vpe-name");
        if (vpeName != null && vpeName.trim().length() > 0)
            mm.put("vpe-name", vpeName.trim());

        String vnfName = ctx.getAttribute("tmp.resource-allocator.device-name");
        if (vnfName != null && vnfName.trim().length() > 0)
            mm.put("vnf-name", vnfName.trim());

        String excludeVpeList = ctx.getAttribute("tmp.resource-allocator.exclude-vpe-list");
        if (excludeVpeList != null && excludeVpeList.trim().length() > 0)
            mm.put("exclude-vpe-list", excludeVpeList.trim());

        String uplinkCircuitCountStr =
                ctx.getAttribute("tmp.resource-allocator.uplink-circuit-list.uplink-circuit_length");
        if (uplinkCircuitCountStr != null) {
            long uplinkCircuitCount = 0;
            try {
                uplinkCircuitCount = Long.parseLong(uplinkCircuitCountStr);
            } catch (NumberFormatException e) {
                throw new SvcLogicException(
                        "Invalid tmp.resource-allocator.uplink-circuit-list.uplink-circuit_length. Must be a number.");
            }
            List<Map<String, Object>> uplinkCircuitList = new ArrayList<>();
            for (int i = 0; i < uplinkCircuitCount; i++) {
                String uplinkCircuitId = ctx.getAttribute(
                        "tmp.resource-allocator.uplink-circuit-list.uplink-circuit[" + i + "].uplink-circuit-id");
                String uplinkCircuitBandwidthStr =
                        ctx.getAttribute("tmp.resource-allocator.uplink-circuit-list.uplink-circuit[" + i +
                                "].uplink-circuit-bandwidth");
                String uplinkCircuitBandwidthUnit =
                        ctx.getAttribute("tmp.resource-allocator.uplink-circuit-list.uplink-circuit[" + i +
                                "].uplink-circuit-bandwidth-unit");

                long uplinkCircuitBandwidth = 0;
                try {
                    uplinkCircuitBandwidth = Long.parseLong(uplinkCircuitBandwidthStr);
                } catch (NumberFormatException e) {
                    throw new SvcLogicException("Invalid tmp.resource-allocator.uplink-circuit-list.uplink-circuit[" +
                            i + "].uplink-circuit-id. Must be a number.");
                }

                long uplinkCircuitBandwidthKbps =
                        speedUtil.convertToKbps(uplinkCircuitBandwidth, uplinkCircuitBandwidthUnit);

                Map<String, Object> uplinkCircuit = new HashMap<String, Object>();
                uplinkCircuit.put("uplink-circuit-id", uplinkCircuitId);
                uplinkCircuit.put("uplink-circuit-bandwidth", uplinkCircuitBandwidthKbps);
                uplinkCircuitList.add(uplinkCircuit);
            }
            mm.put("uplink-circuit-list", uplinkCircuitList);
        }

        return mm;
    }

    private void setOutputContext(SvcLogicContext ctx, long maxAvailableSpeed, String unit) {
        ctx.setAttribute("tmp.resource-allocator-output.max-available-speed", String.valueOf(maxAvailableSpeed));
        ctx.setAttribute("tmp.resource-allocator-output.speed-unit", unit);
    }

    private int calculatePrimaryServerCount(int serverCount, String ratioString) throws SvcLogicException {
        String[] ss = ratioString.split(":");
        if (ss.length != 2)
            throw new SvcLogicException("Invalid value for homing.pserver.sparing.ratio: " + ratioString);

        int n = 1, m = 1;
        try {
            n = Integer.parseInt(ss[0]);
            m = Integer.parseInt(ss[1]);
        } catch (Exception e) {
            throw new SvcLogicException("Invalid value for homing.pserver.sparing.ratio: " + ratioString);
        }

        return (serverCount - 1) * n / (n + m) + 1;
    }

    public void setServerDao(ServerDao serverDao) {
        this.serverDao = serverDao;
    }

    public void setVpePortDao(VpePortDao vpePortDao) {
        this.vpePortDao = vpePortDao;
    }

    public void setVplspePortDao(VplspePortDao vplspePortDao) {
        this.vplspePortDao = vplspePortDao;
    }

    public void setMaxPortSpeedDao(MaxPortSpeedDao maxPortSpeedDao) {
        this.maxPortSpeedDao = maxPortSpeedDao;
    }

    public void setMaxServerSpeedDao(MaxServerSpeedDao maxServerSpeedDao) {
        this.maxServerSpeedDao = maxServerSpeedDao;
    }

    public void setAllocationRequestBuilder(AllocationRequestBuilder allocationRequestBuilder) {
        this.allocationRequestBuilder = allocationRequestBuilder;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void setSpeedUtil(SpeedUtil speedUtil) {
        this.speedUtil = speedUtil;
    }

    public void setServiceResourceDao(ServiceResourceDao serviceResourceDao) {
        this.serviceResourceDao = serviceResourceDao;
    }

    public void setEndPointAllocator(EndPointAllocator endPointAllocator) {
        this.endPointAllocator = endPointAllocator;
    }

    public void setParameterDao(ParameterDao parameterDao) {
        this.parameterDao = parameterDao;
    }
}
