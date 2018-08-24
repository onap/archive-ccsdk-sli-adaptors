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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CompletionException;
import org.apache.http.HttpResponse;
import org.jline.utils.Log;
import org.onap.ccsdk.sli.adaptors.netbox.api.NetboxClient;
import org.onap.ccsdk.sli.adaptors.netbox.model.IPAddress;
import org.onap.ccsdk.sli.adaptors.netbox.model.Prefix;
import org.onap.ccsdk.sli.adaptors.netbox.model.Status;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetboxClientImpl implements NetboxClient {

    private static final Logger LOG = LoggerFactory.getLogger(NetboxClientImpl.class);

    private static final String NEXT_AVAILABLE_IP_IN_PREFIX_PATH = "/api/ipam/prefixes/%s/available-ips/";
    private static final String IP_ADDRESS_PATH = "/api/ipam/ip-addresses/%s/";
    private static final String EMPTY_STRING = "";
    private static final String ID_MISSING_MSG = "Id must be set";

    private static final String ASSIGN_IP_SQL_STATEMENT =
        "INSERT INTO IPAM_IP_ASSIGNEMENT (service_instance_id, vf_module_id, prefix_id, ip_address_id, ip_address, ip_status) \n"
            + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UNASSIGN_IP_SQL_STATEMENT =
        "DELETE FROM IPAM_IP_ASSIGNEMENT WHERE service_instance_id = ? AND vf_module_id = ? AND ip_address_id = ?";

    private final NetboxHttpClient client;
    private final Gson gson;
    private final DbLibService dbLibService;

    public NetboxClientImpl(final NetboxHttpClient client, final DbLibService dbLibService) {
        this.client = client;
        this.dbLibService = dbLibService;
        final JsonSerializer<Status> vlanStatusDeserializer = (val, type, context) -> val.toJson();
        gson = new GsonBuilder()
            .registerTypeAdapter(Status.class, vlanStatusDeserializer)
            .create();
    }

    @Override
    public QueryStatus assignIpAddress(final Prefix prefix, final String serviceInstanceId, final String vfModuleId,
        final SvcLogicContext ctx) {

        if (prefix.getId() == null) {
            Log.error(ID_MISSING_MSG);
        }

        try {
            IPAddress ipAddress = client
                .post(String.format(NEXT_AVAILABLE_IP_IN_PREFIX_PATH, prefix.getId()), EMPTY_STRING)
                .thenApply(this::getIpAddress)
                .toCompletableFuture()
                .join();

            ArrayList<String> args = Lists.newArrayList(serviceInstanceId,
                vfModuleId,
                String.valueOf(prefix.getId()),
                String.valueOf(ipAddress.getId()),
                ipAddress.getAddress(),
                ipAddress.getStatus().getLabel());
            dbLibService.writeData(ASSIGN_IP_SQL_STATEMENT, args, null);

            return QueryStatus.SUCCESS;
        } catch (CompletionException e) {
            // Unwrap the CompletionException and wrap in IpamException
            LOG.error("Fail to assign IP for Prefix(id= " + prefix.getId() + "). " + e.getMessage(), e.getCause());
            return QueryStatus.FAILURE;
        } catch (SQLException e) {
            LOG.error("Caught SQL exception", e);
            return QueryStatus.FAILURE;
        }
    }

    @Override
    public QueryStatus unassignIpAddress(final IPAddress ipAddress, final String serviceInstanceId,
        final String vfModuleId, final SvcLogicContext ctx) {

        if (ipAddress.getId() == null) {
            Log.error(ID_MISSING_MSG);
        }

        try {
            client.delete(String.format(IP_ADDRESS_PATH, ipAddress.getId()))
                .thenAccept(this::checkResult)
                .toCompletableFuture()
                .join();

            ArrayList<String> args = Lists.newArrayList(
                serviceInstanceId,
                vfModuleId,
                String.valueOf(ipAddress.getId()));
            dbLibService.writeData(UNASSIGN_IP_SQL_STATEMENT, args, null);

            return QueryStatus.SUCCESS;
        } catch (CompletionException e) {
            // Unwrap the CompletionException and wrap in IpamException
            LOG.error("Fail to unassign IP for IPAddress(id= " + ipAddress.getId() + "). " + e.getMessage(),
                e.getCause());
            return QueryStatus.FAILURE;
        } catch (SQLException e) {
            LOG.error("Caught SQL exception", e);
            return QueryStatus.FAILURE;
        }
    }

    @VisibleForTesting
    IPAddress getIpAddress(final HttpResponse response) {
        if (response.getStatusLine().getStatusCode() != 201) {
            throw new IllegalStateException(NetboxHttpClient.getBodyAsString(response));
        }
        try (final Reader reader = new InputStreamReader(response.getEntity().getContent())) {
            return gson.fromJson(reader, IPAddress.class);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String checkResult(final HttpResponse response) {
        if (response.getStatusLine().getStatusCode() - 200 >= 100) {
            return "Netbox request failed with status: " + NetboxHttpClient.getBodyAsString(response);
        }
        return null;
    }
}
