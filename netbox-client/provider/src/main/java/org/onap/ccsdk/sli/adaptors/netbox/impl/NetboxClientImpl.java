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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.CompletionStage;
import org.apache.http.HttpResponse;
import org.onap.ccsdk.sli.adaptors.netbox.property.NetboxProperties;
import org.onap.ccsdk.sli.adaptors.netbox.api.NetboxClient;
import org.onap.ccsdk.sli.adaptors.netbox.model.IPAddress;
import org.onap.ccsdk.sli.adaptors.netbox.model.Prefix;
import org.onap.ccsdk.sli.adaptors.netbox.model.Status;

public class NetboxClientImpl implements NetboxClient {

    private static final String NEXT_AVAILABLE_IP_IN_PREFIX_PATH = "/api/ipam/prefixes/%s/available-ips/";
    private static final String IP_ADDRESS_PATH = "/api/ipam/ip-addresses/%s/";
    private static final String EMPTY_STRING = "";
    private static final String ID_MISSING_MSG = "Id must be set";

    private final String url;
    private final NetboxHttpClient client;
    private final String token;
    private final Gson gson;

    public NetboxClientImpl(final NetboxProperties properties, final NetboxHttpClient client) {
        this.url = properties.getHost();
        this.client = client;
        this.token = properties.getApiKey();
        final JsonSerializer<Status> vlanStatusDeserializer = (val, type, context) -> val.toJson();
        gson = new GsonBuilder()
            .registerTypeAdapter(Status.class, vlanStatusDeserializer)
            .create();
    }

    @Override
    public CompletionStage<IPAddress> nextAvailableIpInPrefix(final Prefix prefix) {
        checkArgument(prefix.getId() != null);
        return client.post(url + String.format(NEXT_AVAILABLE_IP_IN_PREFIX_PATH, prefix.getId()), EMPTY_STRING, token)
            .thenApply(this::getIpAddress);
    }

    @Override
    public CompletionStage<Void> deleteIp(final IPAddress ipAddress) {
        checkArgument(ipAddress.getId() != null);
        return client.delete(url + String.format(IP_ADDRESS_PATH, ipAddress.getId()), token)
            .thenAccept(this::checkResult);
    }

    private void checkArgument(final boolean argument) {
        if (!argument) {
            throw new IllegalArgumentException(ID_MISSING_MSG);
        }
    }

    private void checkResult(final HttpResponse response) {
        if (response.getStatusLine().getStatusCode() - 200 >= 100) {
            throw new IllegalStateException(NetboxHttpClient.getBodyAsString(response));
        }
    }

    private IPAddress getIpAddress(final HttpResponse response) {
        if (response.getStatusLine().getStatusCode() != 201) {
            throw new IllegalStateException(NetboxHttpClient.getBodyAsString(response));
        }
        try (final Reader reader = new InputStreamReader(response.getEntity().getContent())) {
            return gson.fromJson(reader, IPAddress.class);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
