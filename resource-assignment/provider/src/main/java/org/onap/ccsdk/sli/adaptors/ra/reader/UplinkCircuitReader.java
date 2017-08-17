/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
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

package org.onap.ccsdk.sli.adaptors.ra.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onap.ccsdk.sli.adaptors.ra.equip.comp.EquipmentReader;
import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentData;
import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentLevel;

public class UplinkCircuitReader implements EquipmentReader {

    @SuppressWarnings("unchecked")
    @Override
    public List<EquipmentData> readEquipment(Map<String, Object> equipmentConstraints) {
        List<EquipmentData> equipList = new ArrayList<>();

        List<Map<String, Object>> uplinkCircuitList =
                (List<Map<String, Object>>) equipmentConstraints.get("uplink-circuit-list");
        if (uplinkCircuitList == null || uplinkCircuitList.isEmpty())
            return equipList;

        for (Map<String, Object> uplinkCircuit : uplinkCircuitList) {
            EquipmentData equipData = new EquipmentData();
            equipData.equipmentLevel = EquipmentLevel.Device;
            equipData.equipmentId = (String) uplinkCircuit.get("uplink-circuit-id");
            equipData.data = uplinkCircuit;
            equipList.add(equipData);
        }

        return equipList;
    }
}
