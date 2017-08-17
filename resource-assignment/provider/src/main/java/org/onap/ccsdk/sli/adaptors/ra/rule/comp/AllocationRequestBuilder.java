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

package org.onap.ccsdk.sli.adaptors.ra.rule.comp;

import org.onap.ccsdk.sli.adaptors.ra.comp.ServiceData;
import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentData;
import org.onap.ccsdk.sli.adaptors.ra.rule.data.ThresholdStatus;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationOutcome;

public interface AllocationRequestBuilder {

    AllocationRequest buildAllocationRequest(
            ServiceData serviceData,
            EquipmentData equipmentData,
            boolean checkOnly,
            boolean change);

    ThresholdStatus getThresholdStatus(
            ServiceData serviceData,
            EquipmentData equipmentData,
            LimitAllocationOutcome limitAllocationOutcome);
}
