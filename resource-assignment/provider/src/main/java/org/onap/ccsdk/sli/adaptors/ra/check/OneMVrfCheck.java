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
import org.onap.ccsdk.sli.adaptors.util.vrf.VpnParam;
import org.onap.ccsdk.sli.adaptors.util.vrf.VrfUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneMVrfCheck implements EquipmentCheck {

    private static final Logger log = LoggerFactory.getLogger(OneMVrfCheck.class);

    private ResourceManager resourceManager;

    @Override
    public boolean checkEquipment(
            String endPointPosition,
            ServiceData serviceData,
            EquipmentData equipData,
            Map<String, Object> equipmentConstraints) {
        String vrfName = (String) serviceData.data.get("vrf-name");
        if (vrfName == null)
            return true;

        String v4MulticastStr = (String) serviceData.data.get("v4-multicast");
        String v6MulticastStr = (String) serviceData.data.get("v6-multicast");
        boolean v4Multicast = v4MulticastStr != null &&
                (v4MulticastStr.equalsIgnoreCase("Y") || v4MulticastStr.equalsIgnoreCase("true"));
        boolean v6Multicast = v6MulticastStr != null &&
                (v6MulticastStr.equalsIgnoreCase("Y") || v6MulticastStr.equalsIgnoreCase("true"));
        if (!v4Multicast && !v6Multicast)
            return true;

        // First check if a new VRF would be required. If not, we are good
        Resource r = resourceManager.getResource("VRF", equipData.equipmentId);
        if (r != null && r.allocationItems != null)
            for (AllocationItem ai : r.allocationItems)
                if (ai.resourceShareGroupList.contains(vrfName))
                    return true;

        String resourceUnionId = serviceData.serviceInstanceId + '/' + serviceData.endPointPosition;

        // Check if there is already another multicast VRF for the same VPN
        VpnParam vpnp = VrfUtil.parseVrfInstanceName(vrfName);
        r = resourceManager.getResource("MVRF", equipData.equipmentId);
        if (r != null && r.allocationItems != null) {
            for (AllocationItem ai : r.allocationItems) {

                // Skip the allocation item for the current service instance, if there, in case it is a change order
                if (ai.resourceUnionId.equals(resourceUnionId))
                    continue;

                if (ai.resourceShareGroupList != null && ai.resourceShareGroupList.size() > 0) {
                    String vrfName2 = ai.resourceShareGroupList.iterator().next();
                    VpnParam vpnp2 = VrfUtil.parseVrfInstanceName(vrfName2);
                    if (vpnp.vpnId.equals(vpnp2.vpnId)) {
                        log.info("Skipping VPE " + equipData.equipmentId +
                                ": This request requires new multicast VRF, " +
                                "but there is already another multicast VRF for the same VPN: " + vrfName2 + ".");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }
}
