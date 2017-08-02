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

import org.openecomp.sdnc.ra.comp.AllocationRule;
import org.openecomp.sdnc.ra.comp.ServiceData;
import org.openecomp.sdnc.ra.equip.data.EquipmentData;
import org.openecomp.sdnc.rm.data.AllocationAction;
import org.openecomp.sdnc.rm.data.AllocationRequest;
import org.openecomp.sdnc.rm.data.LimitAllocationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AffinityAllocationRule implements AllocationRule {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AffinityAllocationRule.class);

    @Override
    public AllocationRequest buildAllocationRequest(
            String resourceUnionId,
            String resourceSetId,
            String endPointPosition,
            ServiceData serviceData,
            EquipmentData equipmentData,
            boolean checkOnly,
            boolean change) {
        String affinityLink = (String) equipmentData.data.get("affinity-link");
        if (affinityLink == null)
            affinityLink = "1";

        long serviceSpeed = (Long) serviceData.data.get("service-speed-kbps");

        LimitAllocationRequest ar = new LimitAllocationRequest();
        ar.resourceSetId = resourceSetId;
        ar.resourceUnionId = resourceUnionId;
        ar.resourceShareGroupList = null;
        ar.resourceName = "Bandwidth";
        ar.assetId = equipmentData.equipmentId + "-" + affinityLink;
        ar.missingResourceAction = AllocationAction.Succeed_Allocate;
        ar.expiredResourceAction = AllocationAction.Succeed_Allocate;
        ar.replace = true;
        ar.strict = false;
        ar.checkLimit = Long.MAX_VALUE;
        ar.checkCount = 0;
        ar.allocateCount = serviceSpeed;
        return ar;
    }
}
