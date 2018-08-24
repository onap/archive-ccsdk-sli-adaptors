/*
 * Copyright (C) 2018 Bell Canada.
 *
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
 */
package org.onap.ccsdk.sli.adaptors.netbox.api;

import org.onap.ccsdk.sli.adaptors.netbox.model.IPAddress;
import org.onap.ccsdk.sli.adaptors.netbox.model.Prefix;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

/**
 * This client is meant to interact both with the IPAM system, and the SDNC DB, in order to provide, at any time,
 * an up to date status of the assigned resources.
 */
public interface NetboxClient extends SvcLogicJavaPlugin {

    /**
     * Assign next available IP in prefix and store it in the SDNC database, table IPAM_IP_ASSIGNEMENT.
     *
     * @param prefix The prefix from which to get next available IP.
     * @param serviceInstanceId The service instance ID uniquely identifying the service.
     * @param vfModuleId The VF module ID uniquely identifying the VF.
     */
    QueryStatus assignIpAddress(Prefix prefix, String serviceInstanceId, String vfModuleId, SvcLogicContext ctx);

    /**
     * Release the IP and update the entry in the SDNC database, table IPAM_IP_ASSIGNEMENT.
     *
     * @param ip The IP to release.
     * @param serviceInstanceId The service instance ID uniquely identifying the service.
     * @param vfModuleId The VF module ID uniquely identifying the VF.
     */
    QueryStatus unassignIpAddress(IPAddress ip, String serviceInstanceId, String vfModuleId, SvcLogicContext ctx);
}

