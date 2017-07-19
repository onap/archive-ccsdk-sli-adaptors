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

package org.openecomp.sdnc.ra.pref;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdnc.ra.comp.PreferenceRule;
import org.openecomp.sdnc.ra.comp.ServiceData;
import org.openecomp.sdnc.ra.equip.data.EquipmentData;
import org.openecomp.sdnc.rm.comp.ResourceManager;
import org.openecomp.sdnc.rm.data.LimitResource;
import org.openecomp.sdnc.rm.data.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AffinityLinkPref implements PreferenceRule {

	private static final Logger log = LoggerFactory.getLogger(AffinityLinkPref.class);

	private ResourceManager resourceManager;
	private List<String> affinityLinkIdList;

	public AffinityLinkPref() {
		// Set default values for affinity link ids (can be overridden by the spring config)
		affinityLinkIdList = new ArrayList<>();
		affinityLinkIdList.add("1");
		affinityLinkIdList.add("2");
	}

	@Override
	public int assignOrderNumber(String endPointPosition, ServiceData serviceData, EquipmentData equipData) {

		// This class does not really assign order number, but instead sets the affinity link with the lowest
		// assigned bandwidth in the equipment data

		String preferedAffinityLinkId = "1";
		long lowestAssignedBw = Long.MAX_VALUE;
		for (String affinityLinkId : affinityLinkIdList) {
			String assetId = equipData.equipmentId + "-" + affinityLinkId;
			Resource r = resourceManager.getResource("Bandwidth", assetId);
			if (r != null) {
				LimitResource ll = (LimitResource) r;
				if (ll.used < lowestAssignedBw) {
					lowestAssignedBw = ll.used;
					preferedAffinityLinkId = affinityLinkId;
				}
				log.info("Assigned bandwidth on affinity link: " + assetId + ": " + ll.used);
			}
		}

		equipData.data.put("affinity-link", preferedAffinityLinkId);

		log.info("Prefered affinity link for " + equipData.equipmentId + ": " + preferedAffinityLinkId);

		return 0;
	}

	public void setResourceManager(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	public void setAffinityLinkIdList(List<String> affinityLinkIdList) {
		this.affinityLinkIdList = affinityLinkIdList;
	}
}
