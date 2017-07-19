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

package org.openecomp.sdnc.util.speed;

public class SpeedUtil {

	private long unitFactor = 1000;

	public long convertToKbps(long maxSpeed, String unit) {
		if (unit.equalsIgnoreCase("kbps"))
			return maxSpeed;
		if (unit.equalsIgnoreCase("Mbps"))
			return maxSpeed * unitFactor;
		if (unit.equalsIgnoreCase("Gbps"))
			return maxSpeed * unitFactor * unitFactor;
		return 0;
	}

	public void setUnitFactor(long unitFactor) {
		this.unitFactor = unitFactor;
	}
}
