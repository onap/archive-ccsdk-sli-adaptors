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

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.onap.ccsdk.sli.adaptors.netbox.api.IpamException;
import org.onap.ccsdk.sli.adaptors.netbox.property.NetboxProperties;

public class NetboxHttpClient implements AutoCloseable {

    private static final String APPLICATION_JSON = "application/json";

    private final CloseableHttpAsyncClient client;
    private final String url;
    private final String token;

    // Used by the blueprint container
    public NetboxHttpClient(NetboxProperties properties) {
        this(properties.getHost(), properties.getApiKey());
    }

    NetboxHttpClient(final String url, final String token) {
        this.url = url;
        this.token = token;

        final TrustStrategy acceptingTrustStrategy = (certificate, authType) -> true;
        final SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (final NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new IllegalStateException("Can't create http client", e);
        }
        client = HttpAsyncClientBuilder.create()
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setSSLContext(sslContext)
            .build();
    }

    // Has to be public for blueprint container to access it
    public void init() {
        client.start();
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    CompletionStage<HttpResponse> post(final String uri, final String requestBody) {
        return sendRequest(uri, requestBody, HttpPost::new);
    }

    CompletionStage<HttpResponse> delete(final String uri) {
        return sendRequest(uri, HttpDelete::new);
    }

    static String getBodyAsString(final HttpResponse response) {
        final String body;
        if (response.getEntity() != null) {
            try (final Scanner s = new java.util.Scanner(response.getEntity().getContent()).useDelimiter("\\A")) {
                body = s.hasNext() ? s.next() : "";
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            body = "";
        }
        return response.toString() + "\n" + body;
    }

    private <T extends HttpUriRequest> CompletionStage<HttpResponse> sendRequest(final String uri,
        final Function<String, T> supplier) {
        final T request = supplier.apply(url + uri);
        request.addHeader(ACCEPT, APPLICATION_JSON);
        request.addHeader(CONTENT_TYPE, APPLICATION_JSON);
        request.addHeader(AUTHORIZATION, "Token " + token);
        return sendRequest(request);
    }

    private <T extends HttpEntityEnclosingRequest & HttpUriRequest>
    CompletionStage<HttpResponse> sendRequest(final String uri, final String body,
        final Function<String, T> supplier) {
        final T request = supplier.apply(url + uri);
        request.addHeader(ACCEPT, APPLICATION_JSON);
        request.addHeader(CONTENT_TYPE, APPLICATION_JSON);
        request.addHeader(AUTHORIZATION, "Token " + token);
        request.setEntity(new StringEntity(body, Charset.forName("UTF-8")));
        return sendRequest(request);
    }

    private CompletionStage<HttpResponse> sendRequest(final HttpUriRequest request) {
        final CompletableFuture<HttpResponse> future = new CompletableFuture<>();
        client.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(final HttpResponse httpResponse) {
                future.complete(httpResponse);
            }

            @Override
            public void failed(final Exception e) {
                future.completeExceptionally(new IpamException("Netbox request failed", e));
            }

            @Override
            public void cancelled() {
                future.cancel(false);
            }
        });
        return future;
    }
}