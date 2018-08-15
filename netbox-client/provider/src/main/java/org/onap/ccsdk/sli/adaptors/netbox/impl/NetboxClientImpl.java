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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.CompletionException;
import org.apache.http.HttpResponse;
import org.onap.ccsdk.sli.adaptors.netbox.api.IpamException;
import org.onap.ccsdk.sli.adaptors.netbox.api.NetboxClient;
import org.onap.ccsdk.sli.adaptors.netbox.model.IPAddress;
import org.onap.ccsdk.sli.adaptors.netbox.model.Prefix;
import org.onap.ccsdk.sli.adaptors.netbox.model.Status;

public class NetboxClientImpl implements NetboxClient {

    private static final String NEXT_AVAILABLE_IP_IN_PREFIX_PATH = "/api/ipam/prefixes/%s/available-ips/";
    private static final String IP_ADDRESS_PATH = "/api/ipam/ip-addresses/%s/";
    private static final String EMPTY_STRING = "";
    private static final String ID_MISSING_MSG = "Id must be set";

    private final NetboxHttpClient client;
    private final Gson gson;

    public NetboxClientImpl(final NetboxHttpClient client) {
        this.client = client;
        final JsonSerializer<Status> vlanStatusDeserializer = (val, type, context) -> val.toJson();
        gson = new GsonBuilder()
            .registerTypeAdapter(Status.class, vlanStatusDeserializer)
            .create();
    }

    @Override
    public IPAddress assign(final Prefix prefix) throws IpamException {
        checkArgument(prefix.getId() != null);
        try {
            return client.post(String.format(NEXT_AVAILABLE_IP_IN_PREFIX_PATH, prefix.getId()), EMPTY_STRING)
                .thenApply(this::getIpAddress)
                .toCompletableFuture()
                .join();
        } catch (CompletionException e) {
            // Unwrap the ComplettionException and wrap in IpamException
            throw new IpamException("Fail to assign IP for Prefix(id= " + prefix.getId() + "). " + e.getMessage(),
                e.getCause());
        }
    }

    @Override
    public void unassign(final IPAddress ipAddress) throws IpamException {
        checkArgument(ipAddress.getId() != null);
        try {
            client.delete(String.format(IP_ADDRESS_PATH, ipAddress.getId()))
                .thenAccept(this::checkResult)
                .toCompletableFuture()
                .join();
        } catch (CompletionException e) {
            // Unwrap the ComplettionException and wrap in IpamException
            throw new IpamException("Fail to unassign IP for IPAddress(id= " + ipAddress.getId() + "). " + e.getMessage(),
                e.getCause());
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

    private static void checkArgument(final boolean argument) throws IpamException {
        if (!argument) {
            throw new IpamException(ID_MISSING_MSG);
        }
    }

    private void checkResult(final HttpResponse response) {
        if (response.getStatusLine().getStatusCode() - 200 >= 100) {
            throw new IllegalStateException(
                "Netbox request failed with status: " + NetboxHttpClient.getBodyAsString(response));
        }
    }
}
