/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All
 *                         rights
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

package org.onap.ccsdk.sli.adaptors.ra.check;

import java.util.Map;

import org.onap.ccsdk.sli.adaptors.ra.comp.EquipmentCheck;
import org.onap.ccsdk.sli.adaptors.ra.comp.ServiceData;
import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentData;
import org.onap.ccsdk.sli.adaptors.rm.comp.ResourceManager;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnyVrfPresentCheck implements EquipmentCheck {

    private static final Logger log = LoggerFactory.getLogger(AnyVrfPresentCheck.class);

    private ResourceManager resourceManager;

    @Override
    public boolean checkEquipment(
            String endPointPosition,
            ServiceData serviceData,
            EquipmentData equipData,
            Map<String, Object> equipmentConstraints) {
        String vrfNameListStr = (String) serviceData.data.get("vrf-name-list");
        if (vrfNameListStr == null)
            vrfNameListStr = (String) serviceData.data.get("vrf-name");
        if (vrfNameListStr == null)
            return true;

        String vrfRequiredStr = (String) equipmentConstraints.get("vrf-required");
        if (vrfRequiredStr == null || !vrfRequiredStr.equalsIgnoreCase("true"))
            return true;

        String[] vrfNameList = vrfNameListStr.split(",");

        Resource r = resourceManager.getResource("VRF", equipData.equipmentId);
        if (r != null && r.allocationItems != null)
            for (AllocationItem ai : r.allocationItems)
                for (String vrfName : vrfNameList)
                    if (ai.resourceShareGroupList.contains(vrfName))
                        return true;

        log.info("Skipping VPE " + equipData.equipmentId +
                ": Existing VRF is required, but there is no existing VRF on the VPE for any of the requested VPNs.");
        return false;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
