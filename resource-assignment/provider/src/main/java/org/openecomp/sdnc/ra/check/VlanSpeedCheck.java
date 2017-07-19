/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights
 * 						reserved.
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

package org.openecomp.sdnc.ra.check;

import java.util.Map;

import org.openecomp.sdnc.ra.comp.EquipmentCheck;
import org.openecomp.sdnc.ra.comp.ServiceData;
import org.openecomp.sdnc.ra.equip.data.EquipmentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VlanSpeedCheck implements EquipmentCheck {

	private static final Logger log = LoggerFactory.getLogger(VlanSpeedCheck.class);

	@Override
	public boolean checkEquipment(
	        String endPointPosition,
	        ServiceData serviceData,
	        EquipmentData equipData,
	        Map<String, Object> equipmentConstraints) {
		String vpeName = (String) equipData.data.get("vpe-id");
		Long serviceSpeed = (Long) serviceData.data.get("service-speed-kbps");
		if (serviceSpeed != null && serviceSpeed > 0 && serviceSpeed < 1000) {
			log.info("Skipping VPE " + vpeName + ": Service speed < 1Mbps is not supported.");
			return false;
		}
		return true;
	}
}
