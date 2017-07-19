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

package org.openecomp.sdnc.ra.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdnc.ra.equip.comp.EquipmentReader;
import org.openecomp.sdnc.ra.equip.data.EquipmentData;
import org.openecomp.sdnc.ra.equip.data.EquipmentLevel;

public class AicSiteReader implements EquipmentReader {

	@Override
	public List<EquipmentData> readEquipment(Map<String, Object> equipmentConstraints) {
		String aicSiteId = (String) equipmentConstraints.get("aic-site-id");

		EquipmentData equipData = new EquipmentData();
		equipData.equipmentLevel = EquipmentLevel.Site;
		equipData.equipmentId = aicSiteId;
		equipData.data = new HashMap<String, Object>();

		List<EquipmentData> equipList = new ArrayList<>();
		equipList.add(equipData);

		return equipList;
	}
}
