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

package org.onap.ccsdk.sli.adaptors.ra.comp;

import java.util.List;

import org.onap.ccsdk.sli.adaptors.ra.equip.comp.EquipmentReader;

public class EndPointAllocationDefinition {

    public String serviceModel;
    public String endPointPosition;
    public EquipmentReader equipmentReader;
    public List<EquipmentCheck> equipmentCheckList;
    public List<PreferenceRule> preferenceRuleList;
    public List<AllocationRule> allocationRuleList;

    public void setServiceModel(String serviceModel) {
        this.serviceModel = serviceModel;
    }

    public void setEndPointPosition(String endPointPosition) {
        this.endPointPosition = endPointPosition;
    }

    public void setEquipmentReader(EquipmentReader equipmentReader) {
        this.equipmentReader = equipmentReader;
    }

    public void setEquipmentCheckList(List<EquipmentCheck> equipmentCheckList) {
        this.equipmentCheckList = equipmentCheckList;
    }

    public void setPreferenceRuleList(List<PreferenceRule> preferenceRuleList) {
        this.preferenceRuleList = preferenceRuleList;
    }

    public void setAllocationRuleList(List<AllocationRule> allocationRuleList) {
        this.allocationRuleList = allocationRuleList;
    }
}
