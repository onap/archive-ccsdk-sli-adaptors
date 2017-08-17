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

package org.onap.ccsdk.sli.adaptors.ra.pref;

import org.onap.ccsdk.sli.adaptors.ra.comp.PreferenceRule;
import org.onap.ccsdk.sli.adaptors.ra.comp.ServiceData;
import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentData;
import org.onap.ccsdk.sli.adaptors.rm.comp.ResourceManager;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvcExistingVrfPref implements PreferenceRule {

    private static final Logger log = LoggerFactory.getLogger(EvcExistingVrfPref.class);

    private ResourceManager resourceManager;

    @Override
    public int assignOrderNumber(String endPointPosition, ServiceData serviceData, EquipmentData equipData) {
        String vrfName = (String) serviceData.data.get("vrf-name");
        if (vrfName == null)
            return 0;

        Resource r = resourceManager.getResource("VRF", equipData.equipmentId);
        if (r != null && r.allocationItems != null)
            for (AllocationItem ai : r.allocationItems)
                if (ai.resourceShareGroupList.contains(vrfName)) {
                    log.info("VRF for VPN: " + vrfName + " found on VPE: " + equipData.equipmentId);
                    return 1;
                }

        log.info("VRF for VPN: " + vrfName + " NOT found on VPE: " + equipData.equipmentId);
        return 2;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
