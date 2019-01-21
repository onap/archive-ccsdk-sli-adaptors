/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
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

package org.onap.ccsdk.sli.adaptors.ra.comp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.sli.adaptors.rm.comp.ResourceManager;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationStatus;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitResource;
import org.onap.ccsdk.sli.adaptors.rm.data.MultiResourceAllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.RangeAllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.RangeAllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.RangeResource;
import org.onap.ccsdk.sli.adaptors.rm.data.ReleaseRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.Resource;
import org.onap.ccsdk.sli.adaptors.util.str.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndPointAllocatorImpl implements EndPointAllocator {

    private static final Logger log = LoggerFactory.getLogger(EndPointAllocatorImpl.class);

    private ResourceManager resourceManager;

    private Map<String, List<AllocationRule>> allocationRuleMap;

    @Override
    public List<ResourceData> allocateResources(String serviceModel, ResourceEntity resourceEntity,
            ResourceTarget resourceTarget, ResourceRequest resourceRequest, boolean checkOnly, boolean change) {

        List<ResourceData> resourceList = new ArrayList<>();

        if (allocationRuleMap != null) {
            List<AllocationRule> allocationRuleList = allocationRuleMap.get(serviceModel);
            if (allocationRuleList == null) {
                allocationRuleList = allocationRuleMap.get("DEFAULT");
            }

            if (allocationRuleList != null) {
                boolean allgood = true;
                for (AllocationRule allocationRule : allocationRuleList) {
                    AllocationRequest ar = allocationRule.buildAllocationRequest(serviceModel, resourceEntity,
                            resourceTarget, resourceRequest, checkOnly, change);
                    if (ar != null) {
                        AllocationOutcome ao = resourceManager.allocateResources(ar);
                        List<ResourceData> rr = getResourceData(ao);
                        resourceList.addAll(rr);

                        if (ao.status != AllocationStatus.Success) {
                            allgood = false;
                        }
                    }
                }

                if (!allgood) {
                    String resourceSetId = resourceEntity.resourceEntityType + "::" + resourceEntity.resourceEntityId
                            + "::" + resourceEntity.resourceEntityVersion;
                    resourceManager.releaseResources(ReleaseRequest.resourceSet(resourceSetId));
                }
            }
        }

        return resourceList;
    }

    private List<ResourceData> getResourceData(AllocationOutcome ao) {
        if (ao instanceof MultiResourceAllocationOutcome) {
            List<ResourceData> rr = new ArrayList<>();
            for (AllocationOutcome ao1 : ((MultiResourceAllocationOutcome) ao).allocationOutcomeList) {
                rr.addAll(getResourceData(ao1));
            }
            return rr;
        }

        ResourceData rd = new ResourceData();
        rd.data = new HashMap<>();

        AllocationRequest ar = ao.request;
        rd.resourceName = ar.resourceName;
        rd.endPointPosition = ar.endPointPosition;
        int i1 = ar.assetId.indexOf("::");
        if (i1 > 0) {
            rd.resourceTargetType = ar.assetId.substring(0, i1);
            rd.resourceTargetId = ar.assetId.substring(i1 + 2);
        } else {
            rd.resourceTargetType = "";
            rd.resourceTargetId = ar.assetId;
        }
        rd.status = ao.status.toString();

        if (ao instanceof LimitAllocationOutcome) {
            LimitAllocationOutcome lao = (LimitAllocationOutcome) ao;
            rd.data.put("allocated", String.valueOf(lao.allocatedCount));
            rd.data.put("used", String.valueOf(lao.used));
            rd.data.put("limit", String.valueOf(lao.limit));
            rd.data.put("available", String.valueOf(lao.limit - lao.used));
        } else if (ao instanceof RangeAllocationOutcome) {
            RangeAllocationOutcome rao = (RangeAllocationOutcome) ao;
            rd.data.put("allocated", String.valueOf(StrUtil.listInt(rao.allocated)));
            rd.data.put("used", String.valueOf(StrUtil.listInt(rao.used)));
        }

        return Collections.singletonList(rd);
    }

    @Override
    public List<ResourceData> getResourcesForEntity(String resourceEntityType, String resourceEntityId,
            String resourceEntityVersion) {
        List<ResourceData> rdlist = new ArrayList<>();

        String resourceUnionId = resourceEntityType + "::" + resourceEntityId;
        List<Resource> rlist = resourceManager.getResourceUnion(resourceUnionId);

        for (Resource r : rlist) {

            // Find the needed allocation item: if resourceEntityVersion is specified, use that,
            // otherwise, find the latest allocation item
            AllocationItem ai = null;
            if (resourceEntityVersion != null) {
                String resourceSetId = resourceUnionId + "::" + resourceEntityVersion;
                for (AllocationItem ai1 : r.allocationItems) {
                    if (ai1.resourceSetId.equals(resourceSetId)) {
                        ai = ai1;
                        break;
                    }
                }
            } else {
                Date aitime = null;
                for (AllocationItem ai1 : r.allocationItems) {
                    if (ai1.resourceUnionId.equals(resourceUnionId)) {
                        if (aitime == null || ai1.allocationTime.after(aitime)) {
                            ai = ai1;
                            aitime = ai1.allocationTime;
                        }
                    }
                }
            }

            if (ai != null) {
                ResourceData rd = new ResourceData();
                rdlist.add(rd);

                rd.resourceName = r.resourceKey.resourceName;
                int i1 = r.resourceKey.assetId.indexOf("::");
                if (i1 > 0) {
                    rd.resourceTargetType = r.resourceKey.assetId.substring(0, i1);
                    rd.resourceTargetId = r.resourceKey.assetId.substring(i1 + 2);

                    int i2 = r.resourceKey.assetId.lastIndexOf("::");
                    if (i2 > i1) {
                        rd.resourceTargetValue = r.resourceKey.assetId.substring(i2 + 2);
                    }
                } else {
                    rd.resourceTargetType = "";
                    rd.resourceTargetId = r.resourceKey.assetId;
                }

                rd.data = new HashMap<>();

                if (ai instanceof RangeAllocationItem) {
                    RangeAllocationItem rai = (RangeAllocationItem) ai;

                    String ss = String.valueOf(rai.used);
                    ss = ss.substring(1, ss.length() - 1);
                    rd.data.put("allocated", ss);

                } else if (ai instanceof LimitAllocationItem) {
                    LimitAllocationItem lai = (LimitAllocationItem) ai;

                    rd.data.put("allocated", String.valueOf(lai.used));
                }
            }
        }

        return rdlist;
    }

    @Override
    public List<ResourceData> getResourcesForTarget(String resourceTargetTypeFilter, String resourceTargetIdFilter,
            String resourceName) {
        List<ResourceData> rdlist = new ArrayList<>();

        String assetIdFilter = null;
        if (resourceTargetTypeFilter != null && resourceTargetIdFilter != null) {
            assetIdFilter = resourceTargetTypeFilter + "::" + resourceTargetIdFilter;
        } else if (resourceTargetTypeFilter != null) {
            assetIdFilter = resourceTargetTypeFilter;
        } else if (resourceTargetIdFilter != null) {
            assetIdFilter = resourceTargetIdFilter;
        }

        List<Resource> rlist = resourceManager.queryResources(resourceName, assetIdFilter);

        for (Resource r : rlist) {

            log.info("ResourceName:" + r.resourceKey.resourceName + " assetId:" + r.resourceKey.assetId);

            ResourceData rd = new ResourceData();
            rdlist.add(rd);

            rd.resourceName = r.resourceKey.resourceName;
            int i1 = r.resourceKey.assetId.indexOf("::");
            if (i1 > 0) {
                rd.resourceTargetType = r.resourceKey.assetId.substring(0, i1);
                rd.resourceTargetId = r.resourceKey.assetId.substring(i1 + 2);

                int i2 = r.resourceKey.assetId.lastIndexOf("::");
                if (i2 > i1) {
                    rd.resourceTargetValue = r.resourceKey.assetId.substring(i2 + 2);
                }
            } else {
                rd.resourceTargetType = "";
                rd.resourceTargetId = r.resourceKey.assetId;
            }

            rd.data = new HashMap<>();

            if (r instanceof RangeResource) {
                RangeResource rr = (RangeResource) r;

                log.info("rr.used: " + rr.used);
                String ss = String.valueOf(rr.used);
                ss = ss.substring(1, ss.length() - 1);
                rd.data.put("allocated", ss);

            } else if (r instanceof LimitResource) {
                LimitResource lr = (LimitResource) r;

                log.info("lr.used: " + lr.used);
                rd.data.put("allocated", String.valueOf(lr.used));
            }
        }

        return rdlist;
    }

    @Override
    public ResourceData getResource(String resourceTargetType, String resourceTargetId, String resourceName,
            String resourceEntityTypeFilter, String resourceEntityIdFilter, String resourceShareGroupFilter) {
        ResourceData rd = new ResourceData();
        String assetId = resourceTargetType + "::" + resourceTargetId;

        String resourceUnionFilter = null;
        if (resourceEntityTypeFilter != null && resourceEntityIdFilter != null) {
            resourceUnionFilter = resourceEntityTypeFilter + "::" + resourceEntityIdFilter;
        } else if (resourceEntityTypeFilter != null) {
            resourceUnionFilter = resourceEntityTypeFilter;
        } else if (resourceEntityIdFilter != null) {
            resourceUnionFilter = resourceEntityIdFilter;
        }

        Resource r = null;
        if (resourceUnionFilter != null || resourceShareGroupFilter != null) {
            r = resourceManager.queryResource(resourceName, assetId, resourceUnionFilter, resourceShareGroupFilter);
        } else {
            r = resourceManager.getResource(resourceName, assetId);
        }

        if (r != null) {
            log.info("ResourceName:" + r.resourceKey.resourceName + " assetId:" + r.resourceKey.assetId);

            rd.resourceName = r.resourceKey.resourceName;
            int i1 = r.resourceKey.assetId.indexOf("::");
            if (i1 > 0) {
                rd.resourceTargetType = r.resourceKey.assetId.substring(0, i1);
                rd.resourceTargetId = r.resourceKey.assetId.substring(i1 + 2);

                int i2 = r.resourceKey.assetId.lastIndexOf("::");
                if (i2 > i1) {
                    rd.resourceTargetValue = r.resourceKey.assetId.substring(i2 + 2);
                }
            } else {
                rd.resourceTargetType = "";
                rd.resourceTargetId = r.resourceKey.assetId;
            }

            rd.data = new HashMap<>();

            if (r instanceof RangeResource) {
                RangeResource rr = (RangeResource) r;

                log.info("rr.used: " + rr.used);
                String ss = String.valueOf(rr.used);
                ss = ss.substring(1, ss.length() - 1);
                rd.data.put("allocated", ss);

            } else if (r instanceof LimitResource) {
                LimitResource lr = (LimitResource) r;

                log.info("lr.used: " + lr.used);
                rd.data.put("allocated", String.valueOf(lr.used));
            }
        }

        return rd;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void setAllocationRuleMap(Map<String, List<AllocationRule>> allocationRuleMap) {
        this.allocationRuleMap = allocationRuleMap;
    }
}
