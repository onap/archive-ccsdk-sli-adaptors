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

package org.onap.ccsdk.sli.adaptors.util.vrf;

public class VrfUtil {

    public static String createVrfInstanceName(
            String serviceInstanceId,
            String vpnId,
            String siteType,
            String routeGroup) {
        if (vpnId == null || vpnId.trim().length() == 0)
            return null;

        String ss = "VPN-" + vpnId;
        if (siteType != null && siteType.equalsIgnoreCase("hub"))
            ss += "-HUB";
        if (siteType != null && siteType.equalsIgnoreCase("spoke"))
            ss += "-SP-" + serviceInstanceId;
        if (routeGroup != null && routeGroup.trim().length() > 0)
            ss += "-RG-" + routeGroup;

        return ss;
    }

    public static VpnParam parseVrfInstanceName(String vrfInstanceName) {
        VpnParam vpnParam = new VpnParam();

        int i1 = vrfInstanceName.indexOf("-HUB");
        if (i1 > 0)
            vpnParam.siteType = "HUB";

        int i2 = vrfInstanceName.indexOf("-SP-");
        if (i2 > 0)
            vpnParam.siteType = "SPOKE";

        int i3 = vrfInstanceName.indexOf("-RG-");
        if (i3 > 0)
            vpnParam.routeGroupName = vrfInstanceName.substring(i3 + 4);

        int i4 = vrfInstanceName.length();
        if (i1 > 0)
            i4 = i1;
        else if (i2 > 0)
            i4 = i2;
        else if (i3 > 0)
            i4 = i3;
        vpnParam.vpnId = vrfInstanceName.substring(4, i4);

        if (i2 > 0 && i3 < 0)
            vpnParam.spokeServiceInstanceId = vrfInstanceName.substring(i2 + 4);
        if (i2 > 0 && i3 > 0)
            vpnParam.spokeServiceInstanceId = vrfInstanceName.substring(i2 + 4, i3);

        return vpnParam;
    }
}
