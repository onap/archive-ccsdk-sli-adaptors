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

package org.openecomp.sdnc.ra.alloc;

import org.openecomp.sdnc.ra.comp.AllocationRule;
import org.openecomp.sdnc.ra.comp.ServiceData;
import org.openecomp.sdnc.ra.equip.data.EquipmentData;
import org.openecomp.sdnc.rm.data.AllocationAction;
import org.openecomp.sdnc.rm.data.AllocationRequest;
import org.openecomp.sdnc.rm.data.LimitAllocationRequest;
import org.openecomp.sdnc.util.vrf.VpnParam;
import org.openecomp.sdnc.util.vrf.VrfUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServingSiteAllocationRule implements AllocationRule {

    private static final Logger log = LoggerFactory.getLogger(ServingSiteAllocationRule.class);

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

        String v4ServingSiteStr = (String) serviceData.data.get("v4-serving-site");
        String v6ServingSiteStr = (String) serviceData.data.get("v6-serving-site");
        boolean v4ServingSite = v4ServingSiteStr != null &&
                (v4ServingSiteStr.equalsIgnoreCase("Y") || v4ServingSiteStr.equalsIgnoreCase("true"));
        boolean v6ServingSite = v6ServingSiteStr != null &&
                (v6ServingSiteStr.equalsIgnoreCase("Y") || v6ServingSiteStr.equalsIgnoreCase("true"));
        if (!v4ServingSite && !v6ServingSite)
            return null;

        VpnParam vpnp = VrfUtil.parseVrfInstanceName(vrfName);

        LimitAllocationRequest ar = new LimitAllocationRequest();
        ar.resourceSetId = resourceSetId;
        ar.resourceUnionId = resourceUnionId;
        ar.resourceName = "ServingSite";
        ar.assetId = equipmentData.equipmentId + "-" + vpnp.vpnId;
        ar.missingResourceAction = AllocationAction.Succeed_Allocate;
        ar.expiredResourceAction = AllocationAction.Succeed_Allocate;
        ar.replace = true;
        ar.strict = false;
        ar.checkLimit = 1;
        ar.checkCount = 1;
        ar.allocateCount = 1;

        return ar;
    }
}
