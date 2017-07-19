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
import org.openecomp.sdnc.ra.rule.dao.VpeLockDao;
import org.openecomp.sdnc.rm.comp.ResourceManager;
import org.openecomp.sdnc.rm.data.AllocationItem;
import org.openecomp.sdnc.rm.data.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VpeLockCheck implements EquipmentCheck {

	private static final Logger log = LoggerFactory.getLogger(VpeLockCheck.class);

	private VpeLockDao vpeLockDao;
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

		String vpeName = (String) equipData.data.get("vpe-id");
		String vpeLock = vpeLockDao.getVpeLock(vpeName);
		if (vpeLock == null)
			return true;

		if (vpeLock.equals("vpe-total-lock")) {
			log.info("Skipping VPE " + vpeName + ": There is a " + vpeLock + " on it.");
			return false;
		}

		if (vpeLock.equals("vpe-vrf-lock") && requiresNewVrf(equipData.equipmentId, vrfName)) {
			log.info("Skipping VPE " + vpeName + ": There is a " + vpeLock +
			        " on it and it requires a new VRF for VPN: " + vrfName + ".");
			return false;
		}

		if (vpeLock.equals("vpe-mvrf-lock") && requiresNewMVrf(equipData.equipmentId, vrfName)) {
			log.info("Skipping VPE " + vpeName + ": There is a " + vpeLock +
			        " on it and it requires a new multicast VRF for VPN: " + vrfName + ".");
			return false;
		}

		return true;
	}

	boolean requiresNewVrf(String equipmentId, String vrfName) {
		Resource r = resourceManager.getResource("VRF", equipmentId);
		if (r == null || r.allocationItems == null)
			return true;

		for (AllocationItem ai : r.allocationItems) {
			if (ai.resourceShareGroupList.contains(vrfName))
				return false;
		}

		return true;
	}

	boolean requiresNewMVrf(String equipmentId, String vrfName) {
		Resource r = resourceManager.getResource("MVRF", equipmentId);
		if (r == null || r.allocationItems == null)
			return true;

		for (AllocationItem ai : r.allocationItems) {
			if (ai.resourceShareGroupList.contains(vrfName))
				return false;
		}

		return true;
	}

	public void setVpeLockDao(VpeLockDao vpeLockDao) {
		this.vpeLockDao = vpeLockDao;
	}

	public void setResourceManager(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}
}
