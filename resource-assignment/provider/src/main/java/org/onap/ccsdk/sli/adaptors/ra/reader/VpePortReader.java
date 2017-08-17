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
import org.onap.ccsdk.sli.adaptors.ra.equip.dao.VpePortDao;
import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentData;
import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentLevel;

public class VpePortReader implements EquipmentReader {

    private VpePortDao vpePortDao;

    @Override
    public List<EquipmentData> readEquipment(Map<String, Object> equipmentConstraints) {
        String clli = (String) equipmentConstraints.get("clli");
        String vpeName = (String) equipmentConstraints.get("vpe-name");
        if (vpeName == null) {
            String equipmentId = (String) equipmentConstraints.get("equipment-id");
            if (equipmentId != null) {
                int i1 = equipmentId.indexOf('/');
                if (i1 > 0)
                    equipmentId = equipmentId.substring(0, i1);
                vpeName = equipmentId;
            }
        }

        List<Map<String, Object>> vpeDataList = vpePortDao.getVpePortData(clli, vpeName);

        List<EquipmentData> equipList = new ArrayList<>();
        for (Map<String, Object> vpeData : vpeDataList) {
            EquipmentData equipData = new EquipmentData();
            equipData.equipmentLevel = EquipmentLevel.Port;
            equipData.equipmentId =
                    (String) vpeData.get("vpe-id") + '/' + (String) vpeData.get("physical-interface-name");
            equipData.data = vpeData;

            equipList.add(equipData);
        }

        return equipList;
    }

    public void setVpePortDao(VpePortDao vpePortDao) {
        this.vpePortDao = vpePortDao;
    }
}
