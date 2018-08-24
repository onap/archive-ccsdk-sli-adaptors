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

import java.util.Map;
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
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     * <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     * <tbody>
     * <tr><td>service_instance_id</td><td>Mandatory</td><td>The service instance ID uniquely identifying the service.</td></tr>
     * <tr><td>vf_module_id</td><td>Mandatory</td><td>The VF module ID uniquely identifying the VF.</td></tr>
     * <tr><td>prefix_id</td><td>Mandatory</td><td>The prefix from which to get next available IP.</td></tr>
     * </tbody>
     * </table>
     */
    QueryStatus assignIpAddress(Map<String, String> parameters, SvcLogicContext ctx);

    /**
     * Release the IP and update the entry in the SDNC database, table IPAM_IP_ASSIGNEMENT.
     *
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     * <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     * <tbody>
     * <tr><td>service_instance_id</td><td>Mandatory</td><td>The service instance ID uniquely identifying the service.</td></tr>
     * <tr><td>vf_module_id</td><td>Mandatory</td><td>The VF module ID uniquely identifying the VF.</td></tr>
     * <tr><td>ip_address_id</td><td>Mandatory</td><td>The IP to release.</td></tr>
     * </tbody>
     * </table>
     */
    QueryStatus unassignIpAddress(Map<String, String> parameters, SvcLogicContext ctx);
}

