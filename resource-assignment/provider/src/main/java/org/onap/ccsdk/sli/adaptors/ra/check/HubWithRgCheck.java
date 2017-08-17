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

public class HubWithRgCheck implements EquipmentCheck {

    private static final Logger log = LoggerFactory.getLogger(HubWithRgCheck.class);

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

        // Check if this is HUB. If not, this check is not applicable
        VpnParam vpnp = VrfUtil.parseVrfInstanceName(vrfName);
        if (vpnp.siteType == null || !vpnp.siteType.equals("HUB"))
            return true;

        boolean rgPresent = vpnp.routeGroupName != null;

        // First check if a new VRF would be required. If not, we are good
        Resource r = resourceManager.getResource("VRF", equipData.equipmentId);
        if (r != null && r.allocationItems != null) {
            for (AllocationItem ai : r.allocationItems)
                if (ai.resourceShareGroupList.contains(vrfName))
                    return true;

            String resourceUnionId = serviceData.serviceInstanceId + '/' + serviceData.endPointPosition;

            // Check if there is already another HUB VRF with RG presence that does not match the requested
            for (AllocationItem ai : r.allocationItems) {

                // Skip the allocation item for the current service instance, if there, in case it is a change order
                if (ai.resourceUnionId.equals(resourceUnionId))
                    continue;

                if (ai.resourceShareGroupList != null && ai.resourceShareGroupList.size() > 0) {
                    String vrfName2 = ai.resourceShareGroupList.iterator().next();
                    VpnParam vpnp2 = VrfUtil.parseVrfInstanceName(vrfName2);

                    if (vpnp2.siteType == null || !vpnp2.siteType.equals("HUB"))
                        continue;

                    boolean rgPresent2 = vpnp2.routeGroupName != null;

                    if (rgPresent && !rgPresent2) {
                        log.info("Skipping VPE " + equipData.equipmentId +
                                ": This request requires new HUB with RG VRF, " +
                                "but there is already another HUB VRF with no RG: " + vrfName2 + ".");
                        return false;
                    }
                    if (!rgPresent && rgPresent2) {
                        log.info("Skipping VPE " + equipData.equipmentId +
                                ": This request requires new HUB VRF with no RG, " +
                                "but there is already another HUB with RG VRF: " + vrfName2 + ".");
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
