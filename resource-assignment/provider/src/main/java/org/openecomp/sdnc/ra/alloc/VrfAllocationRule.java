/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights
 * reserved.
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

package org.openecomp.sdnc.ra.alloc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.openecomp.sdnc.ra.comp.AllocationRule;
import org.openecomp.sdnc.ra.comp.ServiceData;
import org.openecomp.sdnc.ra.equip.data.EquipmentData;
import org.openecomp.sdnc.rm.data.AllocationAction;
import org.openecomp.sdnc.rm.data.AllocationRequest;
import org.openecomp.sdnc.rm.data.LimitAllocationRequest;
import org.openecomp.sdnc.rm.data.MultiResourceAllocationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VrfAllocationRule implements AllocationRule {

    private static final Logger log = LoggerFactory.getLogger(VrfAllocationRule.class);

    @Override
    public AllocationRequest buildAllocationRequest(
            String resourceUnionId,
            String resourceSetId,
            String endPointPosition,
            ServiceData serviceData,
            EquipmentData equipmentData,
            boolean checkOnly,
            boolean change) {
        String vrfName = (String) serviceData.data.get("vrf-name");
        if (vrfName == null)
            return null;

        log.info("vrfName: " + vrfName);

        Set<String> resourceShareGroupList = new HashSet<>();
        resourceShareGroupList.add(vrfName);

        LimitAllocationRequest ar = new LimitAllocationRequest();
        ar.resourceSetId = resourceSetId;
        ar.resourceUnionId = resourceUnionId;
        ar.resourceShareGroupList = resourceShareGroupList;
        ar.resourceName = "VRF";
        ar.assetId = equipmentData.equipmentId;
        ar.missingResourceAction = AllocationAction.Succeed_Allocate;
        ar.expiredResourceAction = AllocationAction.Succeed_Allocate;
        ar.replace = true;
        ar.strict = false;
        ar.checkLimit = 999999999;
        ar.checkCount = 1;
        ar.allocateCount = 1;

        String v4MulticastStr = (String) serviceData.data.get("v4-multicast");
        String v6MulticastStr = (String) serviceData.data.get("v6-multicast");
        boolean v4Multicast = v4MulticastStr != null &&
                (v4MulticastStr.equalsIgnoreCase("Y") || v4MulticastStr.equalsIgnoreCase("true"));
        boolean v6Multicast = v6MulticastStr != null &&
                (v6MulticastStr.equalsIgnoreCase("Y") || v6MulticastStr.equalsIgnoreCase("true"));
        if (v4Multicast || v6Multicast) {
            LimitAllocationRequest ar2 = new LimitAllocationRequest();
            ar2.resourceSetId = resourceSetId;
            ar2.resourceUnionId = resourceUnionId;
            ar2.resourceShareGroupList = resourceShareGroupList;
            ar2.resourceName = "MVRF";
            ar2.assetId = equipmentData.equipmentId;
            ar2.missingResourceAction = AllocationAction.Succeed_Allocate;
            ar2.expiredResourceAction = AllocationAction.Succeed_Allocate;
            ar2.replace = true;
            ar2.strict = false;
            ar2.checkLimit = 999999999;
            ar2.checkCount = 1;
            ar2.allocateCount = 1;

            MultiResourceAllocationRequest mar = new MultiResourceAllocationRequest();
            mar.resourceSetId = resourceSetId;
            mar.resourceUnionId = resourceUnionId;
            mar.resourceShareGroupList = resourceShareGroupList;
            mar.assetId = equipmentData.equipmentId;
            mar.missingResourceAction = AllocationAction.Succeed_Allocate;
            mar.expiredResourceAction = AllocationAction.Succeed_Allocate;
            mar.allocationRequestList = new ArrayList<>();
            mar.allocationRequestList.add(ar);
            mar.allocationRequestList.add(ar2);

            return mar;
        }

        return ar;
    }
}
