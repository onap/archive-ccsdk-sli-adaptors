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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProvStatusCheck implements EquipmentCheck {

    private static final Logger log = LoggerFactory.getLogger(ProvStatusCheck.class);

    @Override
    public boolean checkEquipment(
            String endPointPosition,
            ServiceData serviceData,
            EquipmentData equipData,
            Map<String, Object> equipmentConstraints) {
        String provStatus = (String) equipData.data.get("provisioning-status");
        if (provStatus == null || !provStatus.equals("PROV")) {
            log.info("Skipping VPE " + equipData.equipmentId + ": Not in PROV status.");
            return false;
        }
        return true;
    }
}
