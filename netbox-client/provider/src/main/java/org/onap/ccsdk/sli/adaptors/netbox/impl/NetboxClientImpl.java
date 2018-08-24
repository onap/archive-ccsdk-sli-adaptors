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
package org.onap.ccsdk.sli.adaptors.netbox.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.onap.ccsdk.sli.adaptors.netbox.api.NetboxClient;
import org.onap.ccsdk.sli.adaptors.netbox.model.IPAddress;
import org.onap.ccsdk.sli.adaptors.netbox.model.IPStatus;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetboxClientImpl implements NetboxClient {

    private static final Logger LOG = LoggerFactory.getLogger(NetboxClientImpl.class);

    private static final String NEXT_AVAILABLE_IP_IN_PREFIX_PATH = "/api/ipam/prefixes/%s/available-ips/";
    private static final String IP_ADDRESS_PATH = "/api/ipam/ip-addresses/%s/";
    private static final String EMPTY_STRING = "";
    private static final String SERVICE_INSTANCE_ID_PROP = "service_instance_id";
    private static final String VF_MODULE_ID_PROP = "vf_module_id";

    private static final String ASSIGN_IP_SQL_STATEMENT =
        "INSERT INTO IPAM_IP_ASSIGNEMENT (service_instance_id, vf_module_id, prefix_id, ip_address_id, ip_address, ip_status, ip_response_json) \n"
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UNASSIGN_IP_SQL_STATEMENT =
        "UPDATE IPAM_IP_ASSIGNEMENT SET ip_status = ? WHERE service_instance_id = ? AND vf_module_id = ? AND ip_address_id = ?";

    private final NetboxHttpClient client;

    private final DbLibService dbLibService;

    public NetboxClientImpl(final NetboxHttpClient client, final DbLibService dbLibService) {
        this.client = client;
        this.dbLibService = dbLibService;
    }

    @Override
    public QueryStatus assignIpAddress(final Map<String, String> parameters, final SvcLogicContext ctx) {

        try {
            SliPluginUtils
                .checkParameters(parameters, new String[]{SERVICE_INSTANCE_ID_PROP, VF_MODULE_ID_PROP, "prefix_id"},
                    LOG);
        } catch (SvcLogicException e) {
            return QueryStatus.FAILURE;
        }

        final String serviceInstanceId = parameters.get(SERVICE_INSTANCE_ID_PROP);
        LOG.trace("assignIpAddress: service_instance_id = {}", serviceInstanceId);
        final String vfModuleId = parameters.get(VF_MODULE_ID_PROP);
        LOG.trace("assignIpAddress: vf_module_id = {}", vfModuleId);
        final String prefixId = parameters.get("prefix_id");
        LOG.trace("assignIpAddress: prefix_id = {}", prefixId);

        HttpResponse httpResp;
        try {
            httpResp = client
                .post(String.format(NEXT_AVAILABLE_IP_IN_PREFIX_PATH, prefixId), EMPTY_STRING);
        } catch (IOException e) {
            LOG.error("Fail to assign IP for Prefix(id={}). {}", prefixId, e.getMessage(), e.getCause());
            return QueryStatus.FAILURE;
        }

        String ipamRespJson;
        try {
            ipamRespJson = EntityUtils.toString(httpResp.getEntity(), "UTF-8");
        } catch (IOException e) {
            LOG.error("Fail to parse IPAM response for assign in Prefix(id={}). Response={}", prefixId,
                httpResp.getEntity(), e);
            return QueryStatus.FAILURE;
        }

        if (httpResp.getStatusLine().getStatusCode() != 201) {
            LOG.error("Fail to assign IP for Prefix(id={}). HTTP code 201!={}. Response={}", prefixId,
                httpResp.getStatusLine().getStatusCode(), ipamRespJson);
            return QueryStatus.FAILURE;
        }

        IPAddress ipAddress;
        try {
            ipAddress = IPAddress.fromJson(ipamRespJson);
        } catch (JsonSyntaxException e) {
            LOG.error("Fail to parse IPAM JSON reponse to IPAddress POJO. IPAM JSON Response={}", ipamRespJson, e);
            return QueryStatus.FAILURE;
        }

        ArrayList<String> args = Lists.newArrayList(serviceInstanceId,
            vfModuleId,
            String.valueOf(prefixId),
            String.valueOf(ipAddress.getId()),
            ipAddress.getAddress(),
            IPStatus.ASSIGNED.name(),
            ipamRespJson);

        try {
            dbLibService.writeData(ASSIGN_IP_SQL_STATEMENT, args, null);
        } catch (SQLException e) {
            LOG.error("Caught SQL exception", e);
            return QueryStatus.FAILURE;
        }

        ctx.setAttribute("self_serve_ra_netbox_ip_address", ipAddress.getAddress());

        return QueryStatus.SUCCESS;
    }

    @Override
    public QueryStatus unassignIpAddress(final Map<String, String> parameters, final SvcLogicContext ctx) {
        try {
            SliPluginUtils
                .checkParameters(parameters, new String[]{SERVICE_INSTANCE_ID_PROP, VF_MODULE_ID_PROP, "ip_address_id"},
                    LOG);
        } catch (SvcLogicException e) {
            return QueryStatus.FAILURE;
        }

        final String serviceInstanceId = parameters.get(SERVICE_INSTANCE_ID_PROP);
        LOG.trace("assignIpAddress: service_instance_id = {}", serviceInstanceId);
        final String vfModuleId = parameters.get(VF_MODULE_ID_PROP);
        LOG.trace("assignIpAddress: vf_module_id = {}", vfModuleId);
        final String ipAddressId = parameters.get("ip_address_id");
        LOG.trace("assignIpAddress: ip_address_id = {}", ipAddressId);
        HttpResponse httpResp;
        try {
            httpResp = client.delete(String.format(IP_ADDRESS_PATH, ipAddressId));
        } catch (IOException e) {
            LOG.error("Fail to unassign IP for IPAddress(id= " + ipAddressId + "). " + e.getMessage(),
                e.getCause());
            return QueryStatus.FAILURE;
        }

        if (httpResp.getStatusLine().getStatusCode() - 200 >= 100) {
            LOG.error("Fail to unassign IP for IPAddress(id={}). HTTP code={}.", ipAddressId,
                httpResp.getStatusLine().getStatusCode());
            return QueryStatus.FAILURE;
        }

        ArrayList<String> args = Lists.newArrayList(
            IPStatus.UNASSIGNED.name(),
            serviceInstanceId,
            vfModuleId,
            String.valueOf(ipAddressId));
        try {
            dbLibService.writeData(UNASSIGN_IP_SQL_STATEMENT, args, null);
        } catch (SQLException e) {
            LOG.error("Caught SQL exception", e);
            return QueryStatus.FAILURE;
        }

        return QueryStatus.SUCCESS;
    }
}
