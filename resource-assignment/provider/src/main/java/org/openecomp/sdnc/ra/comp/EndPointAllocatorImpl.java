/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.openecomp.sdnc.ra.comp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.openecomp.sdnc.ra.equip.data.EquipmentData;
import org.openecomp.sdnc.rm.comp.ResourceManager;
import org.openecomp.sdnc.rm.data.AllocationItem;
import org.openecomp.sdnc.rm.data.AllocationOutcome;
import org.openecomp.sdnc.rm.data.AllocationRequest;
import org.openecomp.sdnc.rm.data.AllocationStatus;
import org.openecomp.sdnc.rm.data.LimitAllocationItem;
import org.openecomp.sdnc.rm.data.LimitResource;
import org.openecomp.sdnc.rm.data.RangeAllocationItem;
import org.openecomp.sdnc.rm.data.RangeResource;
import org.openecomp.sdnc.rm.data.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndPointAllocatorImpl implements EndPointAllocator {

    private static final Logger log = LoggerFactory.getLogger(EndPointAllocatorImpl.class);

    private Map<String, List<EndPointAllocationDefinition>> endPointAllocationDefinitionMap;

    private ResourceManager resourceManager;

    @Override
    public List<EndPointData> allocateEndPoints(
            ServiceData serviceData,
            Map<String, Object> equipmentConstraints,
            boolean checkOnly,
            boolean change,
            int changeNumber) {
        List<EndPointAllocationDefinition> defList = endPointAllocationDefinitionMap.get(serviceData.serviceModel);
        if (defList == null)
            throw new NotImplementedException("Service model: " + serviceData.serviceModel + " not supported");

        List<EndPointData> epList = new ArrayList<>();
        for (EndPointAllocationDefinition def : defList) {
            if (serviceData.endPointPosition != null && !serviceData.endPointPosition.equals(def.endPointPosition))
                continue;

            log.info(
                    "Starting allocation of end point: " + def.endPointPosition + ": " + serviceData.serviceInstanceId);

            String resourceUnionId = serviceData.serviceInstanceId + '/' + def.endPointPosition;
            String resourceSetId = resourceUnionId + '/' + changeNumber;

            String equipmentId = (String) equipmentConstraints.get("equipment-id");
            if (equipmentId == null) {
                EndPointData epExisting = readEndPoint(resourceUnionId, resourceSetId);
                if (epExisting != null && epExisting.equipmentId != null) {
                    equipmentConstraints.put("equipment-id", epExisting.equipmentId);

                    log.info("Trying assignment on the current equipment: " + epExisting.equipmentId);
                }
            }

            List<EquipmentData> equipList = def.equipmentReader.readEquipment(equipmentConstraints);
            if (equipList == null || equipList.isEmpty()) {
                log.info("Equipment not found for " + def.endPointPosition);
                break;
            }

            if (def.equipmentCheckList != null) {
                for (EquipmentCheck filter : def.equipmentCheckList) {
                    List<EquipmentData> newEquipList = new ArrayList<>();
                    for (EquipmentData equipData : equipList)
                        if (filter.checkEquipment(def.endPointPosition, serviceData, equipData, equipmentConstraints))
                            newEquipList.add(equipData);
                    equipList = newEquipList;
                }
                if (equipList.isEmpty()) {
                    log.info("No equipment meets the requiremets for the service for: " + def.endPointPosition);
                    break;
                }
            }

            if (equipList.size() > 1 && def.preferenceRuleList != null && !def.preferenceRuleList.isEmpty()) {

                List<PrefEquipment> prefEquipList = new ArrayList<>();
                for (EquipmentData equipData : equipList) {
                    PrefEquipment prefEquip = new PrefEquipment();
                    prefEquip.equipData = equipData;
                    prefEquip.prefNumbers = new long[def.preferenceRuleList.size()];
                    prefEquipList.add(prefEquip);

                    int i = 0;
                    for (PreferenceRule prefRule : def.preferenceRuleList)
                        prefEquip.prefNumbers[i++] =
                                prefRule.assignOrderNumber(def.endPointPosition, serviceData, equipData);
                }

                Collections.sort(prefEquipList);

                equipList = new ArrayList<>();
                for (PrefEquipment prefEquip : prefEquipList)
                    equipList.add(prefEquip.equipData);
            }

            for (EquipmentData equipData : equipList) {
                boolean allgood = true;
                if (def.allocationRuleList != null)
                    for (AllocationRule allocationRule : def.allocationRuleList) {
                        AllocationRequest ar = allocationRule.buildAllocationRequest(resourceUnionId, resourceSetId,
                                def.endPointPosition, serviceData, equipData, checkOnly, change);
                        if (ar != null) {
                            AllocationOutcome ao = resourceManager.allocateResources(ar);
                            if (ao.status != AllocationStatus.Success) {
                                allgood = false;
                                break;
                            }
                        }
                    }
                if (allgood) {
                    EndPointData ep = readEndPoint(resourceUnionId, resourceSetId);
                    epList.add(ep);
                    break;
                }
            }
        }

        return epList;
    }

    private EndPointData readEndPoint(String resourceUnionId, String resourceSetId) {
        EndPointData ep = new EndPointData();
        ep.resourceUnionId = resourceUnionId;
        ep.resourceSetId = resourceSetId;

        int i1 = resourceUnionId.indexOf('/');
        if (i1 > 0)
            ep.endPointPosition = resourceUnionId.substring(i1 + 1);

        ep.data = new HashMap<>();

        List<Resource> rlist = resourceManager.getResourceUnion(resourceUnionId);
        for (Resource r : rlist) {
            if (r instanceof RangeResource) {
                RangeResource rr = (RangeResource) r;
                for (AllocationItem ai : r.allocationItems)
                    if (ai.resourceUnionId.equals(resourceUnionId)) {
                        RangeAllocationItem rai = (RangeAllocationItem) ai;
                        ep.data.put(ep.endPointPosition + '.' + rr.resourceKey.resourceName, rai.used.first());
                    }
            }
            if (r instanceof LimitResource) {
                LimitResource rr = (LimitResource) r;
                for (AllocationItem ai : r.allocationItems)
                    if (ai.resourceUnionId.equals(resourceUnionId)) {
                        LimitAllocationItem rai = (LimitAllocationItem) ai;
                        ep.data.put(ep.endPointPosition + '.' + rr.resourceKey.resourceName + ".allocated", rai.used);
                        ep.data.put(ep.endPointPosition + '.' + rr.resourceKey.resourceName + ".used", rr.used);
                        ep.data.put(ep.endPointPosition + '.' + rr.resourceKey.resourceName + ".assetId",
                                r.resourceKey.assetId);
                    }
            }
        }

        return ep;
    }

    private static class PrefEquipment implements Comparable<PrefEquipment> {

        public long[] prefNumbers;
        public EquipmentData equipData;

        @Override
        public int compareTo(PrefEquipment o) {
            for (int i = 0; i < prefNumbers.length; i++) {
                if (prefNumbers[i] < o.prefNumbers[i])
                    return -1;
                if (prefNumbers[i] > o.prefNumbers[i])
                    return 1;
            }
            return 0;
        }
    }

    public void setEndPointAllocationDefinitionMap(
            Map<String, List<EndPointAllocationDefinition>> endPointAllocationDefinitionMap) {
        this.endPointAllocationDefinitionMap = endPointAllocationDefinitionMap;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
